package us.eunoians.mcrpg.gui.experiencebank;

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
import us.eunoians.mcrpg.gui.experiencebank.slot.BoostedExperienceSlot;
import us.eunoians.mcrpg.gui.experiencebank.slot.RedeemableExperienceSlot;
import us.eunoians.mcrpg.gui.experiencebank.slot.RedeemableLevelsSlot;
import us.eunoians.mcrpg.gui.experiencebank.slot.RestedExperienceSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

public final class ExperienceBankGui extends BaseGui<McRPGPlayer> implements FillerItemGui {

    private static final int RESTED_EXPERIENCE_SLOT_INDEX = 10;
    private static final int REDEEMABLE_EXPERIENCE_SLOT_INDEX = 12;
    private static final int REDEEMABLE_LEVELS_SLOT_INDEX = 14;
    private static final int BOOSTED_EXPERIENCE_SLOT_INDEX = 16;

    private final Player player;

    public ExperienceBankGui(@NotNull McRPGPlayer creatingPlayer) {
        super(creatingPlayer);
        Optional<Player> playerOptional = creatingPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(creatingPlayer);
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
                            .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.EXPERIENCE_BANK_GUI_TITLE));
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
        setSlot(RESTED_EXPERIENCE_SLOT_INDEX, new RestedExperienceSlot());
        setSlot(REDEEMABLE_EXPERIENCE_SLOT_INDEX, new RedeemableExperienceSlot());
        setSlot(REDEEMABLE_LEVELS_SLOT_INDEX, new RedeemableLevelsSlot());
        setSlot(BOOSTED_EXPERIENCE_SLOT_INDEX, new BoostedExperienceSlot());
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
