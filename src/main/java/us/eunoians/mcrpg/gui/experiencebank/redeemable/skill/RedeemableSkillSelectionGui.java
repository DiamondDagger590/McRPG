package us.eunoians.mcrpg.gui.experiencebank.redeemable.skill;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.experiencebank.ExperienceBankGui;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.RedeemableType;
import us.eunoians.mcrpg.gui.skill.SkillGui;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.List;

/**
 * This gui is used to select what {@link Skill} to redeem either experience or levels into.
 */
public class RedeemableSkillSelectionGui extends SkillGui {

    private final RedeemableType redeemableType;

    public RedeemableSkillSelectionGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull RedeemableType redeemableType) {
        super(mcRPGPlayer);
        this.redeemableType = redeemableType;
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(getPlayer(), 54,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.REDEEMABLE_SKILL_SELECT_GUI_TITLE));
    }

    @Override
    protected void paintSkills(int page) {
        List<Skill> sortedSkills = getSortedSkillsForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < sortedSkills.size()) {
                setSlot(i, new RedeemableSkillSelectionSlot(sortedSkills.get(i), redeemableType));
            } else {
                removeSlot(i);
            }
        }
    }

    @NotNull
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                if (mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
                    ExperienceBankGui experienceBankGui = new ExperienceBankGui(mcRPGPlayer);;
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    player.openInventory(experienceBankGui.getInventory());
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, experienceBankGui);
                }
                return true;
            }

            @NotNull
            @Override
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.REDEEMABLE_SKILL_SELECT_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
            }
        };
    }
}
