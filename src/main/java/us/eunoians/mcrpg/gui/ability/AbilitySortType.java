package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.ChainComparator;
import com.diamonddagger590.mccore.util.LinkedNode;
import com.diamonddagger590.mccore.util.PlayerContextFilter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.util.filter.ability.AbilityUpgradeFilter;
import us.eunoians.mcrpg.util.filter.ability.ActiveAbilityFilter;
import us.eunoians.mcrpg.util.filter.ability.DefaultAbilityFilter;
import us.eunoians.mcrpg.util.filter.ability.PassiveAbilityFilter;
import us.eunoians.mcrpg.util.filter.ability.UnlockableAbilityFilter;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * This enum contains all the different ways {@link Ability Abilities} can be sorted, allowing an easily accessible way
 * to add a sorting {@link Slot} to a given {@link com.diamonddagger590.mccore.gui.Gui} using {@link #getSlot()}.
 * <p>
 * This enum also provides a {@link LinkedNode}, where each node is linked to each other in a closed loop. The first node in the list can be
 * {@link #getFirstSortType()}.
 */
public enum AbilitySortType {

    ALPHABETICAL(Material.BAMBOO_HANGING_SIGN, "Alphabetical Sort", null, Comparator.comparing(Ability::getDisplayName)),
    DEFAULT_ABILITIES(Material.REDSTONE, "Default Abilities Sort", new DefaultAbilityFilter(), new ChainComparator<>(
            Comparator.comparing(ability -> ability instanceof UnlockableAbility), ALPHABETICAL.getAbilityComparator())),
    SKILL(Material.DIAMOND_SWORD, "Sort by Skill", null, new ChainComparator<>(//ALPHABETICAL.getAbilityComparator(),
            Comparator.comparing(ability -> ability.getSkill().isPresent()),
            // After we've sorted it so abilities with skills are put in front of abilities without skills, sort the skills by name
            (ability, ability1) -> {
                SkillRegistry skillRegistry = McRPG.getInstance().getSkillRegistry();
                Optional<Skill> skillOptional = Optional.ofNullable(ability.getSkill().isPresent() ? skillRegistry.getRegisteredSkill(ability.getSkill().get()) : null);
                Optional<Skill> skillOptional1 = Optional.ofNullable(ability1.getSkill().isPresent() ? skillRegistry.getRegisteredSkill(ability1.getSkill().get()) : null);
                // If one of these has a skill but the other doesn't, then we want to prioritize the one that has the skill first
                if (skillOptional.isPresent() != skillOptional1.isPresent()) {
                    return skillOptional.isEmpty() ? -1 : 1;
                }
                // Sort by skill display names if both have skills
                if (skillOptional.isPresent() && skillOptional1.isPresent()) {
                    Skill skill = skillOptional.get();
                    Skill skill1 = skillOptional1.get();
                    return skill.getDisplayName().compareTo(skill1.getDisplayName());
                }
                // Otherwise, they both don't have skills then say they're equal
                return 0;
            },
            DEFAULT_ABILITIES.getAbilityComparator())),
    UNLOCKED_ABILITIES(Material.DIAMOND, "Sort by Unlock Level", new UnlockableAbilityFilter(), new ChainComparator<>(
            Comparator.comparing(ability -> !(ability instanceof UnlockableAbility)),
            Comparator.comparing(ability -> ability.getSkill().isPresent()),
            // After we've sorted it so abilities with skills are put in front of abilities without skills, sort the skills by name
            (ability, ability1) -> {
                SkillRegistry skillRegistry = McRPG.getInstance().getSkillRegistry();
                Optional<Skill> skillOptional = Optional.ofNullable(ability.getSkill().isPresent() ? skillRegistry.getRegisteredSkill(ability.getSkill().get()) : null);
                Optional<Skill> skillOptional1 = Optional.ofNullable(ability1.getSkill().isPresent() ? skillRegistry.getRegisteredSkill(ability1.getSkill().get()) : null);
                // If one of these has a skill but the other doesn't, then we want to prioritize the one that has the skill first
                if (skillOptional.isPresent() != skillOptional1.isPresent()) {
                    return skillOptional.isEmpty() ? -1 : 1;
                }
                // Sort by skill display names if both have skills
                if (skillOptional.isPresent() && skillOptional1.isPresent()) {
                    Skill skill = skillOptional.get();
                    Skill skill1 = skillOptional1.get();
                    return skill.getDisplayName().compareTo(skill1.getDisplayName());
                }
                // Otherwise, they both don't have skills then say they're equal
                return 0;
            },
            (ability, ability1) -> {
                if (ability instanceof UnlockableAbility != ability1 instanceof UnlockableAbility) {
                    return ability instanceof UnlockableAbility ? 1 : -1;
                }

                if (ability instanceof UnlockableAbility unlockableAbility && ability1 instanceof UnlockableAbility unlockableAbility1) {
                    return Integer.compare(unlockableAbility.getUnlockLevel(), unlockableAbility1.getUnlockLevel());
                }
                return 0;
            })),
    UPGRADEABLE_ABILITIES(Material.GOLD_INGOT, "Sort by abilities you can upgrade", new AbilityUpgradeFilter(),
            SKILL.getAbilityComparator()),
    PASSIVE_ABILITIES(Material.REDSTONE, "Sort by passive abilities", new PassiveAbilityFilter(), SKILL.getAbilityComparator()),
    ACTIVE_ABILITIES(Material.STONE_SWORD, "Sort by active abilities", new ActiveAbilityFilter(), SKILL.getAbilityComparator());

    private final static LinkedNode<AbilitySortType> FIRST_SORT_TYPE = new LinkedNode<>(AbilitySortType.SKILL);

    static {
        LinkedNode<AbilitySortType> prev = FIRST_SORT_TYPE;
        // Using definition order as the link order
        for (AbilitySortType type : values()) {
            // Skip our first node type
            if (type != FIRST_SORT_TYPE.getNodeValue()) {
                LinkedNode<AbilitySortType> next = new LinkedNode<>(type);
                prev.setNext(next);
                prev = next;
            }
        }
        // Set the tail of these linked nodes to start back at the head
        prev.setNext(FIRST_SORT_TYPE);
    }

    private final Material displayMaterial;
    private final String displayName;
    private final PlayerContextFilter<Ability> filter;
    private final Comparator<Ability> abilityComparator;

    AbilitySortType(@NotNull Material displayMaterial, @NotNull String displayName, @Nullable PlayerContextFilter<Ability> filter, @NotNull Comparator<Ability> abilityComparator) {
        this.displayMaterial = displayMaterial;
        this.displayName = displayName;
        this.filter = filter;
        this.abilityComparator = abilityComparator;
    }

    /**
     * Gets the {@link Material} used to display this sort.
     *
     * @return The {@link Material} used to display this sort.
     */
    @NotNull
    public Material getDisplayMaterial() {
        return this.displayMaterial;
    }

    /**
     * Gets the name to use when displaying this sort
     *
     * @return The name used to display this sort
     */
    @NotNull
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Gets the {@link Comparator} used to sort {@link Ability Abilities}.
     *
     * @return The {@link Comparator} used to sort {@link Ability Abilities}
     */
    @NotNull
    public Comparator<Ability> getAbilityComparator() {
        return this.abilityComparator;
    }

    /**
     * Gets the filtered collection to display based on the filter for this sort type, or
     * just the original list if there is no filter present.
     *
     * @param abilities The list of {@link Ability Abilities} to filter.
     * @return A filtered {@link List} of {@link Ability Abilities}
     */
    @NotNull
    public List<Ability> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull List<Ability> abilities) {
        return filter == null ? abilities : List.copyOf(filter.filter(mcRPGPlayer, abilities));
    }

    /**
     * Gets a {@link Slot} that will progress to the next sort type and refresh the gui it is hosted in.
     *
     * This slot can only be added to {@link SortableAbilityGui}'s, and if it is added to a {@link PaginatedGui} as well
     * (see {@link PaginatedSortedAbilityGui}), then it will set the page to 1 before refreshing the gui.
     * @return A {@link Slot} that when clicked will progress to the next sort type and refresh the gui it is
     * hosted in.¬
     */
    @NotNull
    public Slot getSlot() {
        return new Slot() {
            @Override
            public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
                var guiOptional = CorePlugin.getInstance().getGuiTracker().getOpenedGui(corePlayer);
                guiOptional.ifPresent(gui -> {
                    // If it's a sortable gui, progress node
                    if (gui instanceof SortableAbilityGui sortableAbilityGui) {
                        corePlayer.getAsBukkitPlayer().ifPresent(player -> {
                            sortableAbilityGui.progressToNextSortNode();
                            // If it's a paginated gui, reset page
                            if (gui instanceof PaginatedGui paginatedGui) {
                                paginatedGui.setPage(1);
                            }
                            // Refresh gui
                            gui.refreshGUI();
                        });
                    }
                });
                return true;
            }

            @NotNull
            @Override
            public ItemStack getItem() {
                ItemStack itemStack = new ItemStack(displayMaterial);
                ItemMeta itemMeta = itemStack.getItemMeta();
                MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                itemMeta.displayName(miniMessage.deserialize("<red>" + displayName + "</red>"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to change how abilities are sorted.")));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
        };
    }

    /**
     * Gets the first {@link LinkedNode} in the chain of sort types.
     *
     * @return The first {@link LinkedNode} in the chain of sort types.
     */
    @NotNull
    public static LinkedNode<AbilitySortType> getFirstSortType() {
        return FIRST_SORT_TYPE;
    }
}
