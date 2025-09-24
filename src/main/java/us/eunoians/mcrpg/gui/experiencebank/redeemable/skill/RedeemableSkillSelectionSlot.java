package us.eunoians.mcrpg.gui.experiencebank.redeemable.skill;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.RedeemableType;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.levels.RedeemableLevelsGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.List;
import java.util.Set;

/**
 * This slot is used in the {@link RedeemableSkillSelectionGui} to
 * allow players to choose a skill to redeem experience or levels into.
 * <p>
 * Clicking this slot will open the {@link RedeemableLevelsGui}
 * or the {@link us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.RedeemableExperienceGui}.
 */
public class RedeemableSkillSelectionSlot implements McRPGSlot {

    private final Skill skill;
    private final RedeemableType redeemableType;

    public RedeemableSkillSelectionSlot(@NotNull Skill skill, @NotNull RedeemableType redeemableType) {
        this.skill = skill;
        this.redeemableType = redeemableType;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        ItemBuilder itemBuilder = skill.getDisplayItemBuilder(mcRPGPlayer);
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            List<Component> appendedLore = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessageAsComponents(player, LocalizationKey.REDEEMABLE_SKILL_SELECT_GUI_LORE);
            itemBuilder.addDisplayLoreComponent(appendedLore);
        });
        return itemBuilder;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            Gui<McRPGPlayer> gui = redeemableType.createGui(mcRPGPlayer, skill);
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI).trackPlayerGui(player, gui);
            player.openInventory(gui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(RedeemableSkillSelectionGui.class);
    }
}
