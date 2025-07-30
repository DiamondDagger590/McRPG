package us.eunoians.mcrpg.gui.loadout.display;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.FillerItemGui;
import us.eunoians.mcrpg.gui.slot.loadout.ToggleLoadoutActiveSlot;
import us.eunoians.mcrpg.gui.slot.loadout.display.LoadoutDisplayItemSlot;
import us.eunoians.mcrpg.gui.slot.loadout.display.LoadoutDisplayNameEditSlot;
import us.eunoians.mcrpg.loadout.Loadout;

import java.util.Optional;

/**
 * This GUI is used as an entry point for players to go through the workflow
 * of editing the item representing a loadout.
 */
public class LoadoutDisplayHomeGui extends BaseGui<McRPGPlayer> implements FillerItemGui {

    private static final int NAME_EDIT_SLOT = 10;
    private static final int ITEM_EDIT_SLOT = 13;
    private static final int ACTIVE_TOGGLE_SLOT = 16;

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
            this.inventory = Bukkit.createInventory(player, 27, McRPG.getInstance().getMiniMessage().deserialize(loadout.getDisplay().getDisplayName().orElse("<gold>Editing Loadout")));
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
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}
