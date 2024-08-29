package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.ChainComparator;
import com.diamonddagger590.mccore.util.LinkedNode;
import com.diamonddagger590.mccore.util.PlayerContextFilter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.AbilitySlot;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.util.filter.AbilityUpgradeFilter;
import us.eunoians.mcrpg.util.filter.DefaultAbilityFilter;
import us.eunoians.mcrpg.util.filter.UnlockableAbilityFilter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This gui is the main gui for players to view all their abilities from.
 */
public class AbilityGui extends PaginatedGui {

    private static final Slot FILLER_GLASS_SLOT;
    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;

    // Create static slots
    static {
        // Create filler glass
        ItemStack fillerGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerGlassMeta = fillerGlass.getItemMeta();
        fillerGlassMeta.setDisplayName(" ");
        fillerGlass.setItemMeta(fillerGlassMeta);
        FILLER_GLASS_SLOT = new Slot() {

            @Override
            public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
                return true;
            }

            @NotNull
            @Override
            public ItemStack getItem() {
                return fillerGlass;
            }
        };
    }

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;
    private final Map<AbilityGuiSortType, List<Ability>> cachedSorts;
    private LinkedNode<AbilityGuiSortType> sortTypeNode;

    public AbilityGui(@NotNull McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.cachedSorts = new HashMap<>();
        this.sortTypeNode = AbilityGuiSortType.getFirstSortType();
    }

    /**
     * Get the {@link McRPGPlayer} who is viewing their abilities.
     *
     * @return The {@link McRPGPlayer} who is viewing their abilities.
     */
    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int i) {
        return Bukkit.createInventory(player, 54, McRPG.getInstance().getMiniMessage().deserialize("<gold>Skills Menu"));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintNavigationBar(page);
        paintAbilities(page);
    }

    @Override
    public int getMaximumPage() {
        return (int) Math.max(1, Math.ceil((double) mcRPGPlayer.asSkillHolder().getAvailableAbilities().size() / 45));
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    /**
     * Paints the navigation bar for a given page.
     *
     * @param page The page to paint the navigation bar for.
     */
    private void paintNavigationBar(int page) {
        // Paint the nav bar with filler glass
        for (int i = 0; i < 9; i++) {
            setSlot(NAVIGATION_ROW_START_INDEX + i, FILLER_GLASS_SLOT);
        }
        // Set the sort slot
        setSlot(SORT_SLOT_INDEX, sortTypeNode.getNodeValue().getSlot());
        // If the page is not the first page, then we need to put a previous arrow button
        if (page > 1) {
            setSlot(PREVIOUS_PAGE_SLOT_INDEX, PREVIOUS_PAGE_SLOT);
        }
        // If the page is not the max page, then we need to put a next arrow button
        if (page < getMaximumPage()) {
            setSlot(NEXT_PAGE_SLOT_INDEX, NEXT_PAGE_SLOT);
        }
    }

    /**
     * Paints the abilities for a given page.
     *
     * @param page The page to paint the abilities for.
     */
    private void paintAbilities(int page) {
        List<Ability> sortedAbilities = getSortedAbilitiesForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < sortedAbilities.size()) {
                setSlot(i, new AbilitySlot(mcRPGPlayer, sortedAbilities.get(i)));
            } else {
                removeSlot(i);
            }
        }
    }

    /**
     * Gets a {@link List} of {@link Ability Abilities}, sorted based on the current {@link #getSortTypeNode()}.
     *
     * @param page The page to get the list for.
     * @return A {@link List} of {@link Ability Abilities} sorted based on the current {@link #getSortTypeNode()}
     * to be displayed on the provided page.
     */
    @NotNull
    private List<Ability> getSortedAbilitiesForPage(int page) {
        AbilityGuiSortType sortType = this.sortTypeNode.getNodeValue();
        List<Ability> abilities;
        if (cachedSorts.containsKey(sortType)) {
            abilities = cachedSorts.get(sortType);
        } else {
            abilities = mcRPGPlayer.asSkillHolder()
                    .getAvailableAbilities()
                    .stream()
                    .map(namespacedKey -> McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(namespacedKey)).toList();
            abilities = sortType.filter(mcRPGPlayer, abilities);
            abilities = abilities
                    .stream()
                    .sorted(sortType.getAbilityComparator())
                    .toList();
            cachedSorts.put(sortType, abilities);
        }

        int currentPage = getPage();

        // Get the abilities that need to be displayed on this page
        int startRange = ((currentPage - 1) * NAVIGATION_ROW_START_INDEX);
        int endRange = Math.min(abilities.size(), currentPage * NAVIGATION_ROW_START_INDEX);
        return abilities.subList(startRange, endRange);
    }

    /**
     * Sets the sort node to the provided one.
     *
     * @param sortTypeNode The new sort node to use for this gui.
     */
    void setAbilitySortNode(@NotNull LinkedNode<AbilityGuiSortType> sortTypeNode) {
        this.sortTypeNode = sortTypeNode;
    }

    /**
     * Get the sort node for this gui.
     *
     * @return The sort node for this gui.
     */
    @NotNull
    LinkedNode<AbilityGuiSortType> getSortTypeNode() {
        return sortTypeNode;
    }

    /**
     * This enum contains all the different sorts and filters for this gui
     */
    private enum AbilityGuiSortType {
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
                SKILL.getAbilityComparator());;

        private final static LinkedNode<AbilityGuiSortType> FIRST_SORT_TYPE = new LinkedNode<>(AbilityGuiSortType.SKILL);

        static {
            LinkedNode<AbilityGuiSortType> prev = FIRST_SORT_TYPE;
            // Using definition order as the link order
            for (AbilityGuiSortType type : values()) {
                // Skip our first node type
                if (type != FIRST_SORT_TYPE.getNodeValue()) {
                    LinkedNode<AbilityGuiSortType> next = new LinkedNode<>(type);
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

        AbilityGuiSortType(@NotNull Material displayMaterial, @NotNull String displayName, @Nullable PlayerContextFilter<Ability> filter, @NotNull Comparator<Ability> abilityComparator) {
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
         * Gets the filtered collection to display based on
         *
         * @param abilities
         * @return
         */
        @NotNull
        public List<Ability> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull List<Ability> abilities) {
            return filter == null ? abilities : List.copyOf(filter.filter(mcRPGPlayer, abilities));
        }

        @NotNull
        public Slot getSlot() {
            return new Slot() {
                @Override
                public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
                    var guiOptional = CorePlugin.getInstance().getGuiTrackerv2().getOpenedGui(corePlayer);
                    guiOptional.ifPresent(gui -> {
                        if (gui instanceof AbilityGui abilityGui) {
                            corePlayer.getAsBukkitPlayer().ifPresent(player -> {
                                abilityGui.setPage(1);
                                abilityGui.setAbilitySortNode(abilityGui.getSortTypeNode().getNextNode());
                                abilityGui.refreshGUI();
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
        public static LinkedNode<AbilityGuiSortType> getFirstSortType() {
            return FIRST_SORT_TYPE;
        }
    }

}
