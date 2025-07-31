package us.eunoians.mcrpg.gui.home;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.FillerItemGui;
import us.eunoians.mcrpg.gui.slot.home.HomeAbilitiesSlot;
import us.eunoians.mcrpg.gui.slot.home.HomeLoadoutSlot;
import us.eunoians.mcrpg.gui.slot.home.HomeSettingsSlot;
import us.eunoians.mcrpg.gui.slot.home.HomeSkillsSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

/**
 * The main gui for players to interact with McRPG through
 */
public class HomeGui extends BaseGui<McRPGPlayer> implements FillerItemGui {

    private static final int SETTINGS_SLOT_INDEX = 10;
    private static final int SKILLS_SLOT_INDEX = 12;
    private static final int ABILITIES_SLOT_INDEX = 14;
    private static final int LOADOUT_SLOT_INDEX = 16;

    private final Player player;

    public HomeGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
    }


    @Override
    protected void buildInventory() {
        if (this.inventory != null) {
            throw new InventoryAlreadyExistsForGuiException(this);
        } else {
            this.inventory = Bukkit.createInventory(player, 27,
                    RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.LOCALIZATION)
                            .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.HOME_GUI_TITLE));
            paintInventory();
        }
    }

    @Override
    public void paintInventory() {
        Slot<McRPGPlayer> fillerSlot = getFillerItemSlot();
        for (int i = 0; i < inventory.getSize(); i++) {
            setSlot(i, fillerSlot);
        }
        // Set the main slots for this gui
        setSlot(SETTINGS_SLOT_INDEX, new HomeSettingsSlot(getCreatingPlayer()));
        setSlot(SKILLS_SLOT_INDEX, new HomeSkillsSlot());
        setSlot(ABILITIES_SLOT_INDEX, new HomeAbilitiesSlot());
        setSlot(LOADOUT_SLOT_INDEX, new HomeLoadoutSlot());
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
