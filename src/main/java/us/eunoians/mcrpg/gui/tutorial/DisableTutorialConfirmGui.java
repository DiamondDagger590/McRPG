package us.eunoians.mcrpg.gui.tutorial;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.gui.KeyedGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.FillerItemGui;
import us.eunoians.mcrpg.gui.tutorial.slot.DisableTutorialCancelSlot;
import us.eunoians.mcrpg.gui.tutorial.slot.DisableTutorialConfirmSlot;
import us.eunoians.mcrpg.gui.tutorial.slot.DisableTutorialInfoSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Confirmation GUI shown before disabling the tutorial.
 * Displays a confirm button (slot 11), info panel (slot 13), and
 * cancel button (slot 15) in a 3-row layout.
 * <p>
 * On confirm:
 * <ol>
 *   <li>Sets {@link us.eunoians.mcrpg.setting.impl.DisableTutorialSetting} to DISABLED</li>
 *   <li>Calls {@link us.eunoians.mcrpg.quest.chain.QuestChainManager#abandonChain} for the tutorial chain</li>
 *   <li>Closes the inventory</li>
 * </ol>
 */
public class DisableTutorialConfirmGui extends BaseGui<McRPGPlayer> implements FillerItemGui, KeyedGui {

    public static final NamespacedKey GUI_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "disable_tutorial_confirm");

    private static final int CONFIRM_SLOT_INDEX = 11;
    private static final int INFO_SLOT_INDEX = 13;
    private static final int CANCEL_SLOT_INDEX = 15;

    private final Player player;

    /**
     * Creates a new confirm GUI for the given player.
     *
     * @param mcRPGPlayer the player who is considering disabling the tutorial
     * @throws CorePlayerOfflineException if the player is not online
     */
    public DisableTutorialConfirmGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
        this.player = mcRPGPlayer.getAsBukkitPlayer()
                .orElseThrow(() -> new CorePlayerOfflineException(mcRPGPlayer));
    }

    /**
     * Builds the inventory for this GUI, creating a 3-row (27 slot) inventory with
     * the localized title.
     *
     * @throws InventoryAlreadyExistsForGuiException if the inventory was already built
     */
    @Override
    protected void buildInventory() {
        if (this.inventory != null) {
            throw new InventoryAlreadyExistsForGuiException(this);
        }
        this.inventory = Bukkit.createInventory(player, 27,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(),
                                LocalizationKey.DISABLE_TUTORIAL_CONFIRM_GUI_TITLE));
        paintInventory();
    }

    /**
     * Fills the inventory with filler items and places the confirm, info, and cancel slots
     * at their designated positions.
     */
    @Override
    public void paintInventory() {
        Slot<McRPGPlayer> fillerSlot = getFillerItemSlot();
        for (int i = 0; i < inventory.getSize(); i++) {
            setSlot(i, fillerSlot);
        }
        setSlot(CONFIRM_SLOT_INDEX, new DisableTutorialConfirmSlot());
        setSlot(INFO_SLOT_INDEX, new DisableTutorialInfoSlot());
        setSlot(CANCEL_SLOT_INDEX, new DisableTutorialCancelSlot());
    }

    /**
     * Registers this GUI as a Bukkit event listener.
     */
    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    /**
     * Unregisters this GUI as a Bukkit event listener.
     */
    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    /**
     * Returns the unique key identifying this GUI type.
     *
     * @return {@code mcrpg:disable_tutorial_confirm}
     */
    @NotNull
    @Override
    public Optional<NamespacedKey> getGuiKey() {
        return Optional.of(GUI_KEY);
    }
}
