package us.eunoians.mcrpg.gui.loadout.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutSelectionGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

import java.util.Optional;
import java.util.Set;

import static com.diamonddagger590.mccore.registry.RegistryAccess.registryAccess;

/**
 * This slot is used to select a specific {@link Loadout} to edit.
 */
public class LoadoutSelectionSlot implements McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;
    private final Loadout loadout;

    public LoadoutSelectionSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        var guiOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
        guiOptional.ifPresent(gui -> {
            if (isPlayerOnGeyser() || clickType != ClickType.RIGHT) {
                LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadout);
                mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutGui);
                player.openInventory(loadoutGui.getInventory());
            } else {
                player.performCommand("mcrpg loadout set " + loadout.getLoadoutSlot());
                gui.refreshGUI();
            }
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        ItemBuilder itemBuilder;
        if (isPlayerOnGeyser()) {
            itemBuilder = ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedSection(mcRPGPlayer,
                            isLoadoutActive() ? LocalizationKey.LOADOUT_SELECTION_GUI_ACTIVE_LOADOUT_SELECTION_SLOT_GEYSER_DISPLAY_ITEM :
                                    LocalizationKey.LOADOUT_SELECTION_GUI_INACTIVE_LOADOUT_SELECTION_SLOT_GEYSER_DISPLAY_ITEM), loadout.getDisplay().getDisplayItem());
        } else {
            itemBuilder = ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedSection(mcRPGPlayer,
                            isLoadoutActive() ? LocalizationKey.LOADOUT_SELECTION_GUI_ACTIVE_LOADOUT_SELECTION_SLOT_DISPLAY_ITEM :
                                    LocalizationKey.LOADOUT_SELECTION_GUI_INACTIVE_LOADOUT_SELECTION_SLOT_DISPLAY_ITEM), loadout.getDisplay().getDisplayItem());
        }
        itemBuilder.addPlaceholder("name", loadout.getDisplay().getDisplayName().orElse(Integer.toString(loadout.getLoadoutSlot())));
        return itemBuilder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(LoadoutSelectionGui.class);
    }

    private boolean isLoadoutActive() {
        return loadout.getLoadoutSlot() == mcRPGPlayer.asSkillHolder().getCurrentLoadoutSlot();
    }

    private boolean isPlayerOnGeyser() {
        return registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.GEYSER).map(geyserHook -> geyserHook.isBedrockPlayer(mcRPGPlayer.getUUID())).orElse(false);
    }
}
