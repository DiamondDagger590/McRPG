package us.eunoians.mcrpg.gui.experiencebank.redeemable.experience;

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
import us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.slot.RedeemExperienceAllSlot;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.slot.RedeemExperienceAmountSlot;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.slot.RedeemExperienceCustomSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Map;
import java.util.Optional;

/**
 * This GUI allows players to spend their redeemable experience.
 */
public class RedeemableExperienceGui extends BaseGui<McRPGPlayer> implements FillerItemGui {

    private static final int REDEEM_CUSTOM_EXPERIENCE_SLOT = 0;
    private static final int REDEEM_500_EXPERIENCE_SLOT = 2;
    private static final int REDEEM_1000_EXPERIENCE_SLOT = 4;
    private static final int REDEEM_10000_EXPERIENCE_SLOT = 6;
    private static final int REDEEM_ALL_EXPERIENCE_SLOT = 8;

    private final Player player;
    private final Skill skill;

    public RedeemableExperienceGui(@NotNull McRPGPlayer creatingPlayer, @NotNull Skill skill) {
        super(creatingPlayer);
        Optional<Player> playerOptional = creatingPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(creatingPlayer);
        }
        this.player = playerOptional.get();
        this.skill = skill;
    }

    @Override
    protected void buildInventory() {
        if (this.inventory != null) {
            throw new InventoryAlreadyExistsForGuiException(this);
        } else {
            this.inventory = Bukkit.createInventory(player, 9,
                    RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.LOCALIZATION)
                            .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.REDEEMABLE_EXPERIENCE_GUI_TITLE, Map.of("skill", skill.getName(getCreatingPlayer()))));
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
        setSlot(REDEEM_CUSTOM_EXPERIENCE_SLOT, new RedeemExperienceCustomSlot(skill));
        setSlot(REDEEM_500_EXPERIENCE_SLOT, new RedeemExperienceAmountSlot(skill, 500));
        setSlot(REDEEM_1000_EXPERIENCE_SLOT, new RedeemExperienceAmountSlot(skill, 1000));
        setSlot(REDEEM_10000_EXPERIENCE_SLOT, new RedeemExperienceAmountSlot(skill, 10000));
        setSlot(REDEEM_ALL_EXPERIENCE_SLOT, new RedeemExperienceAllSlot(skill));
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
