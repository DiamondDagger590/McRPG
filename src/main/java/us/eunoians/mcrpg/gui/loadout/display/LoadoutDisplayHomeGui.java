package us.eunoians.mcrpg.gui.loadout.display;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.FillerItemGui;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.gui.loadout.slot.ToggleLoadoutActiveSlot;
import us.eunoians.mcrpg.gui.loadout.slot.display.LoadoutDisplayItemSlot;
import us.eunoians.mcrpg.gui.loadout.slot.display.LoadoutDisplayNameEditSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;
import com.diamonddagger590.mccore.gui.KeyedGui;
import org.bukkit.NamespacedKey;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * This GUI is used as an entry point for players to go through the workflow
 * of editing the item representing a loadout.
 */
public class LoadoutDisplayHomeGui extends BaseGui<McRPGPlayer> implements FillerItemGui, KeyedGui {

    public static final NamespacedKey GUI_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "loadout_display");

    private static final int NAME_EDIT_SLOT = 10;
    private static final int ITEM_EDIT_SLOT = 13;
    private static final int ACTIVE_TOGGLE_SLOT = 16;
    private static final int BACK_SLOT = 18;

    private final Player player;
    private final Loadout loadout;

    public LoadoutDisplayHomeGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        super(mcRPGPlayer);
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.loadout = loadout;
    }

    @Override
    protected void buildInventory() {
        if (this.inventory != null) {
            throw new InventoryAlreadyExistsForGuiException(this);
        } else {
            String loadoutName = loadout.getDisplay().getDisplayName().orElse(Integer.toString(loadout.getLoadoutSlot()));
            this.inventory = Bukkit.createInventory(player, 27, RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.LOADOUT_DISPLAY_HOME_GUI_TITLE, Map.of("loadout-name", loadoutName)));
            paintInventory();
        }
    }

    @Override
    public void paintInventory() {
        Slot<McRPGPlayer> fillerSlot = getFillerItemSlot();
        for (int i = 0; i < inventory.getSize(); i++) {
            setSlot(i, fillerSlot);
        }
        setSlot(NAME_EDIT_SLOT, new LoadoutDisplayNameEditSlot(loadout));
        setSlot(ITEM_EDIT_SLOT, new LoadoutDisplayItemSlot(loadout));
        setSlot(ACTIVE_TOGGLE_SLOT, new ToggleLoadoutActiveSlot(getCreatingPlayer(), loadout));
        setSlot(BACK_SLOT, getPreviousGuiSlot());
    }

    /**
     * Creates the back button slot that returns to the {@link LoadoutGui} for the current loadout.
     *
     * @return A {@link McRPGPreviousGuiSlot} that navigates back to the loadout editor.
     */
    @NotNull
    private McRPGPreviousGuiSlot getPreviousGuiSlot() {
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
                return LocalizationKey.LOADOUT_DISPLAY_HOME_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
            }
        };
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
    @NotNull
    public Optional<NamespacedKey> getGuiKey() {
        return Optional.of(GUI_KEY);
    }
}
