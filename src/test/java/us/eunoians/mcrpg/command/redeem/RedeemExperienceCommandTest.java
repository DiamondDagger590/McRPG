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
public class RedeemExperienceCommandTest extends McRPGBaseTest {

    private MockSkill skill;
    private McRPGLocalizationManager localizationManager;

    @BeforeEach
    public void setup() {
        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);
        skill = spy(MockSkill.class);
        skillRegistry.register(skill);

        localizationManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
    }

    @DisplayName("Given a player with zero redeemable experience, when redeeming experience for a skill, then a 'not enough experience' message is sent and no state changes")
    @Test
    public void redeemExperience_sendsNotEnoughMessage_whenNoRedeemableExperience(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 1);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();
        Component message = mcRPG.getMiniMessage().deserialize("You do not have enough redeemable experience");
        when(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_EXPERIENCE_NOT_ENOUGH_EXPERIENCE_MESSAGE)).thenReturn(message);
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
        RedeemExperienceCommand.redeemExperience(mcRPGPlayer, skill, 1000);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
    }

    @DisplayName("Given a player with redeemable experience, when redeeming zero experience, then a 'not enough experience' message is sent and no state changes")
    @Test
    public void redeemExperience_sendsNotEnoughMessage_whenAmountIsZero(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableExperience(1000);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 1);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();
        Component message = mcRPG.getMiniMessage().deserialize("You do not have enough redeemable experience");
        when(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_EXPERIENCE_NOT_ENOUGH_EXPERIENCE_MESSAGE)).thenReturn(message);
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(1000, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
        RedeemExperienceCommand.redeemExperience(mcRPGPlayer, skill, 0);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(1000, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
    }

    @DisplayName("Given a skill already at its maximum level, when redeeming experience, then a 'skill already maxed' message is sent and no state changes")
    @Test
    public void redeemExperience_sendsMaxLevelMessage_whenSkillAtMaxLevel(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableExperience(1000);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 1);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();
        skillHolderData.addLevels(999);
        when(skill.getMaxLevel()).thenReturn(1000);

        Component message = mcRPG.getMiniMessage().deserialize("You are at the max level");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REDEEMABLE_EXPERIENCE_SKILL_ALREADY_MAXED_MESSAGE), any())).thenReturn(message);

        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1000, skillHolderData.getCurrentLevel());
        assertEquals(1000, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
        RedeemExperienceCommand.redeemExperience(mcRPGPlayer, skill, 1000);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1000, skillHolderData.getCurrentLevel());
        assertEquals(1000, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
    }

    @DisplayName("Given enough redeemable experience to pass the max level, when redeeming experience, then excess experience is" +
            " refunded after reaching max level")
    @Test
    public void redeemExperience_refundsUnusedExperience_whenMaxLevelReached(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableExperience(1000);
        // Set up mocks BEFORE creating skill holder data
        when(skill.getMaxLevel()).thenReturn(2);
        when(skill.getLevelUpEquation()).thenReturn(new Parser("50"));
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 1);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        Component message = mcRPG.getMiniMessage().deserialize("You redeemed experience!");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REDEEMABLE_EXPERIENCE_REDEEMED_EXPERIENCE_MESSAGE), any())).thenReturn(message);

        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(1000, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
        RedeemExperienceCommand.redeemExperience(mcRPGPlayer, skill, 1000);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(2, skillHolderData.getCurrentLevel());
        assertEquals(950, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
    }

    @DisplayName("Given insufficient redeemable experience to reach the next level, when redeeming experience, then all redeemable "
            + "experience is consumed and progress increases without leveling up")
    @Test
    public void redeemExperience_consumesAllExperience_whenMaxLevelNotReached(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableExperience(25);
        // Set up mocks BEFORE creating skill holder data
        when(skill.getMaxLevel()).thenReturn(2);
        when(skill.getLevelUpEquation()).thenReturn(new Parser("50"));
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 1);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        Component message = mcRPG.getMiniMessage().deserialize("You redeemed experience!");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REDEEMABLE_EXPERIENCE_REDEEMED_EXPERIENCE_MESSAGE), any())).thenReturn(message);

        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(25, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
        RedeemExperienceCommand.redeemExperience(mcRPGPlayer, skill, 25);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(25, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
    }

    @DisplayName("Given a high max level and non-linear cost, when redeeming five hundred experience, then it spends the full request, "
            + "levels up twice, and carries over one hundred one experience into the next level")
    @Test
    public void redeemExperience_consumesFullExperience_whenLevelingPartialLevel(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        // Set up mocks BEFORE creating skill holder data
        when(skill.getMaxLevel()).thenReturn(1000);
        when(skill.getLevelUpEquation()).thenReturn(new Parser("200+(0.17*(skill_level^1.669))"));
        mcRPGPlayer.getExperienceExtras().setRedeemableExperience(1000);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 1);
        SkillHolder.SkillHolderData skillHolderData = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        Component message = mcRPG.getMiniMessage().deserialize("You redeemed experience!");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REDEEMABLE_EXPERIENCE_REDEEMED_EXPERIENCE_MESSAGE), any())).thenReturn(message);
        assertEquals(0, skillHolderData.getCurrentExperience());
        assertEquals(1, skillHolderData.getCurrentLevel());
        assertEquals(1000, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
        RedeemExperienceCommand.redeemExperience(mcRPGPlayer, skill, 500);
        assertEquals(message, player.nextComponentMessage());
        assertEquals(100, skillHolderData.getCurrentExperience());
        assertEquals(3, skillHolderData.getCurrentLevel());
        assertEquals(500, mcRPGPlayer.getExperienceExtras().getRedeemableExperience());
    }
}
