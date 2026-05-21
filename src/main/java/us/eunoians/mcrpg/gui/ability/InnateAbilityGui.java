package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.slot.AbilitySlot;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;

/**
 * A specialised view of {@link AbilityGui} that opens pre-filtered to innate abilities and returns
 * to a specific {@link LoadoutGui} when the back button is clicked.
 * <p>
 * Innate abilities are always active regardless of loadout. This GUI lets players inspect and configure
 * them from within the loadout editing flow, without having to navigate back to the home menu.
 */
public class InnateAbilityGui extends AbilityGui {

    private final Loadout loadout;

    /**
     * Creates an {@link InnateAbilityGui} pre-set to the {@link AbilitySortType#INNATE_ABILITIES} filter.
     *
     * @param mcRPGPlayer The player opening this GUI.
     * @param loadout     The loadout to return to when the back button is clicked.
     */
    public InnateAbilityGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        super(mcRPGPlayer);
        this.loadout = loadout;
        setAbilitySortNode(AbilitySortType.getInnateAbilitiesNode());
    }

    /**
     * Gets the {@link Loadout} this GUI was opened from.
     *
     * @return The originating {@link Loadout}.
     */
    @NotNull
    public Loadout getLoadout() {
        return loadout;
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(getPlayer(), 54,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.INNATE_ABILITY_GUI_TITLE));
    }

    @NotNull
    @Override
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                if (mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadout);
                    player.openInventory(loadoutGui.getInventory());
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutGui);
                }
                return true;
            }

            @NotNull
            @Override
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.INNATE_ABILITY_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
            }
        };
    }

    @Override
    protected void paintAbilities(int page) {
        List<Ability> sortedAbilities = getSortedAbilitiesForPage(page);
        for (int i = 0; i < getNavigationRowStartIndex(); i++) {
            if (i < sortedAbilities.size()) {
                Ability ability = sortedAbilities.get(i);
                setSlot(i, new AbilitySlot(getCreatingPlayer(), ability,
                        () -> new AbilityAttributeEditGui(getCreatingPlayer(), ability,
                                () -> new InnateAbilityGui(getCreatingPlayer(), loadout))));
            } else {
                removeSlot(i);
            }
        }
    }

}
