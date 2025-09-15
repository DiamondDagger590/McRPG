package us.eunoians.mcrpg.command.redeem;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.MockSkill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class RedeemLevelsCommandTest extends McRPGBaseTest {

    private MockSkill skill;
    private McRPGLocalizationManager localizationManager;

    @BeforeEach
    public void setup() {
        SkillRegistry skillRegistry = new SkillRegistry(mcRPG);
        RegistryAccess.registryAccess().register(skillRegistry);
        skill = spy(MockSkill.class);
        skillRegistry.register(skill);

        localizationManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
    }

    @DisplayName("Given a player with zero redeemable levels, when redeeming levels for a skill, then a 'not enough levels' message is sent and no state changes")
    @Test
    public void redeemLevels_sendsNotEnoughMessage_whenNoRedeemableLevels(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderData(skill);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();
        Component message = mcRPG.getMiniMessage().deserialize("You do not have enough redeemable levels");
        when(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_LEVELS_NOT_ENOUGH_LEVELS_MESSAGE)).thenReturn(message);
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        RedeemLevelsCommand.redeemLevels(mcRPGPlayer, skill, 1000);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
    }

    @DisplayName("Given a skill already at its maximum level, when redeeming levels, then a 'skill already maxed' message is sent and no state changes")
    @Test
    public void redeemLevels_sendsMaxLevelMessage_whenSkillAtMaxLevel(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableLevels(1000);
        mcRPGPlayer.asSkillHolder().addSkillHolderData(skill);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();
        skillHolderData.addLevels(999);
        when(skill.getMaxLevel()).thenReturn(1000);

        Component message = mcRPG.getMiniMessage().deserialize("You are at the max level");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REDEEMABLE_LEVELS_SKILL_ALREADY_MAXED_MESSAGE), any())).thenReturn(message);

        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1000, skillHolderData.getCurrentLevel());
        assertEquals(1000, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        RedeemLevelsCommand.redeemLevels(mcRPGPlayer, skill, 1000);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1000, skillHolderData.getCurrentLevel());
        assertEquals(1000, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
    }

    @DisplayName("Given enough redeemable levels to reach the maximum, when redeeming levels, then extra levels are refunded after reaching max level")
    @Test
    public void redeemLevels_refundsUnusedLevels_whenMaxLevelReached(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableLevels(1000);
        mcRPGPlayer.asSkillHolder().addSkillHolderData(skill);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();
        when(skill.getMaxLevel()).thenReturn(2);
        when(skill.getLevelUpEquation()).thenReturn(new Parser("50"));

        Component message = mcRPG.getMiniMessage().deserialize("You redeemed levels!");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REDEEMABLE_LEVELS_REDEEMED_LEVELS_MESSAGE), any())).thenReturn(message);

        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(1000, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        RedeemLevelsCommand.redeemLevels(mcRPGPlayer, skill, 1000);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(2, skillHolderData.getCurrentLevel());
        assertEquals(999, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
    }

    @DisplayName("Given insufficient redeemable levels to reach the maximum, when redeeming levels, then all levels are consumed and the skill levels up accordingly")
    @Test
    public void redeemLevels_consumesAllLevels_whenMaxLevelNotReached(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableLevels(2);
        mcRPGPlayer.asSkillHolder().addSkillHolderData(skill);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();
        when(skill.getMaxLevel()).thenReturn(5);
        when(skill.getLevelUpEquation()).thenReturn(new Parser("50"));

        Component message = mcRPG.getMiniMessage().deserialize("You redeemed experience!");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REDEEMABLE_LEVELS_REDEEMED_LEVELS_MESSAGE), any())).thenReturn(message);

        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(2, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        RedeemLevelsCommand.redeemLevels(mcRPGPlayer, skill, 2);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(3, skillHolderData.getCurrentLevel());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
    }
}
