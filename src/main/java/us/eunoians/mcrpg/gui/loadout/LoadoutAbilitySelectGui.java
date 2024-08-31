package us.eunoians.mcrpg.gui.loadout;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.ChainPlayerContextFilter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.AbilitySortType;
import us.eunoians.mcrpg.gui.ability.PaginatedSortedAbilityGui;
import us.eunoians.mcrpg.gui.slot.loadout.LoadoutSelectAbilitySlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.util.filter.key.AbilityKeyInLoadoutFilter;
import us.eunoians.mcrpg.util.filter.key.AbilityKeyUnlockedFilter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This gui is used when a player is trying to select an {@link Ability} to go into
 * their {@link Loadout}.
 * <p>
 * Abilities in this gui are automatically filtered out if the ability can't be added to the selected slot. Since
 * a loadout can only contain one {@link us.eunoians.mcrpg.ability.impl.ActiveAbility} per {@link us.eunoians.mcrpg.skill.Skill},
 * if a player is trying to replace an active ability, all other active abilities for that skill are filtered out and not shown as options.
 */
public class LoadoutAbilitySelectGui extends PaginatedSortedAbilityGui {

    private static final Slot FILLER_GLASS_SLOT;
    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;
    private static final ChainPlayerContextFilter<NamespacedKey> ABILITY_KEY_FILTER = new ChainPlayerContextFilter<>(new AbilityKeyUnlockedFilter(), new AbilityKeyInLoadoutFilter());

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

    private final Loadout loadout;
    private final Optional<NamespacedKey> oldAbilityKey;

    public LoadoutAbilitySelectGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        super(mcRPGPlayer);
        this.loadout = loadout;
        this.oldAbilityKey = Optional.empty();
    }

    public LoadoutAbilitySelectGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull NamespacedKey oldAbilityKey) {
        super(mcRPGPlayer);
        this.loadout = loadout;
        this.oldAbilityKey = Optional.of(oldAbilityKey);
    }

    @Override
    public int getNavigationRowStartIndex() {
        return NAVIGATION_ROW_START_INDEX;
    }

    @Override
    public @NotNull Set<NamespacedKey> getUnsortedAbilities() {
        return Set.copyOf(ABILITY_KEY_FILTER.filter(getMcRPGPlayer(), getMcRPGPlayer().asSkillHolder().getAvailableAbilities()));
    }

    @Override
    protected void paintAbilities(int page) {
        List<Ability> sortedAbilities = getSortedAbilitiesForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < sortedAbilities.size()) {
                if (oldAbilityKey.isPresent()) {
                    setSlot(i, new LoadoutSelectAbilitySlot(getMcRPGPlayer(), loadout, sortedAbilities.get(i), oldAbilityKey.get()));
                } else {
                    setSlot(i, new LoadoutSelectAbilitySlot(getMcRPGPlayer(), loadout, sortedAbilities.get(i)));
                }
            } else {
                removeSlot(i);
            }
        }
    }

    @Override
    protected void paintNavigationBar(int page) {
        // Paint the nav bar with filler glass
        for (int i = 0; i < 9; i++) {
            setSlot(NAVIGATION_ROW_START_INDEX + i, FILLER_GLASS_SLOT);
        }
        // Set the sort slot
        setSlot(SORT_SLOT_INDEX, getAbilitySortNode().getNodeValue().getSlot());
        // If the page is not the first page, then we need to put a previous arrow button
        if (page > 1) {
            setSlot(PREVIOUS_PAGE_SLOT_INDEX, PREVIOUS_PAGE_SLOT);
        }
        // If the page is not the max page, then we need to put a next arrow button
        if (page < getMaximumPage()) {
            setSlot(NEXT_PAGE_SLOT_INDEX, NEXT_PAGE_SLOT);
        }
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(getPlayer(), 54, McRPG.getInstance().getMiniMessage().deserialize("<gold>Select ability to use."));
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    @Override
    protected @NotNull Set<AbilitySortType> getSkippedSortTypes() {
        return Set.of(AbilitySortType.UPGRADEABLE_ABILITIES, AbilitySortType.DEFAULT_ABILITIES);
    }
}
