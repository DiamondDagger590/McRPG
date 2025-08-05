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
import us.eunoians.mcrpg.gui.loadout.display.LoadoutDisplayHomeGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.Set;

/**
 * This slot is used to represent the current active state of a player's
 * {@link Loadout}. If the loadout is not currently the player's active one,
 * clicking this slot will set it as such.
 */
public class ToggleLoadoutActiveSlot implements McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;
    private final Loadout loadout;

    public ToggleLoadoutActiveSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.loadout = loadout;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, isLoadoutActive() ? LocalizationKey.LOADOUT_DISPLAY_HOME_GUI_TOGGLE_LOADOUT_ACTIVE_DISPLAY_ITEM
                        : LocalizationKey.LOADOUT_DISPLAY_HOME_GUI_TOGGLE_LOADOUT_INACTIVE_DISPLAY_ITEM));
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(LoadoutDisplayHomeGui.class);
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        var guiOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
        guiOptional.ifPresent(gui -> {
            if (!isLoadoutActive()) {
                player.performCommand("mcrpg loadout set " + loadout.getLoadoutSlot());
            }
            gui.refreshGUI();
        });
        return true;
    }

    /**
     * Checks to see if the {@link Loadout} is the active one for the player.
     * @return {@code true} if the {@link Loadout} is the active one for the player.
     */
    public boolean isLoadoutActive() {
        return mcRPGPlayer.asSkillHolder().getCurrentLoadoutSlot() == loadout.getLoadoutSlot();
    }
}
