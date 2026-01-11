package us.eunoians.mcrpg.skill.experience.modifier;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.DisableBonusExperienceConsumptionSetting;
import us.eunoians.mcrpg.skill.impl.MockSkill;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistryExtension;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(ExperienceModifierRegistryExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
public class RestedExperienceModifierTest extends McRPGBaseTest {

    private ExperienceModifierRegistry experienceModifierRegistry;
    private RestedExperienceModifier restedExperienceModifier;
    private final MockSkill mockSkill = spy(MockSkill.class);

    @BeforeEach
    public void setup() {
        experienceModifierRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        restedExperienceModifier = new RestedExperienceModifier(mcRPG);
        experienceModifierRegistry.register(restedExperienceModifier);
    }

    @Test
    @DisplayName("Given an invalid skill experience context, when checking canProcessContext, then it returns false")
    public void canProcessContext_returnsFalse_whenContextInvalid(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, mcRPGPlayer.asSkillHolder(), 100);
        PlayerExperienceExtras playerExperienceExtras = new PlayerExperienceExtras(0, 0, 0, 0);
        mcRPGPlayer.getExperienceExtras().copyExtras(playerExperienceExtras);
        assertFalse(restedExperienceModifier.canProcessContext(entityDamageContext));
    }

    @Test
    @DisplayName("Given a valid skill experience context with rested XP, when checking canProcessContext, then it returns true")
    public void canProcessContext_returnsTrue_whenContextValid(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, mcRPGPlayer.asSkillHolder(), 100);
        mcRPGPlayer.getExperienceExtras().setRestedExperience(10);
        assertTrue(restedExperienceModifier.canProcessContext(entityDamageContext));
    }

    @Test
    @DisplayName("Given a valid skill experience context with rested XP but DisableBonusExperienceConsumptionSetting enabled, when checking canProcessContext, then it returns false")
    public void canProcessContext_returnsFalse_whenSettingEnabled(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, mcRPGPlayer.asSkillHolder(), 100);
        mcRPGPlayer.getExperienceExtras().setRestedExperience(10);
        mcRPGPlayer.setPlayerSetting(DisableBonusExperienceConsumptionSetting.ENABLED);
        assertFalse(restedExperienceModifier.canProcessContext(entityDamageContext));
    }

    @Test
    @DisplayName("Given a valid skill experience context with rested XP and DisableBonusExperienceConsumptionSetting disabled, when checking canProcessContext, then it returns true")
    public void canProcessContext_returnsTrue_whenSettingDisabled(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, mcRPGPlayer.asSkillHolder(), 100);
        mcRPGPlayer.getExperienceExtras().setRestedExperience(10);
        mcRPGPlayer.setPlayerSetting(DisableBonusExperienceConsumptionSetting.DISABLED);
        assertTrue(restedExperienceModifier.canProcessContext(entityDamageContext));
    }

    @Test
    @DisplayName("Given an invalid skill experience context, when calculating modifier, then it returns 1.0")
    public void modifier_returnsOne_whenContextInvalid(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, mcRPGPlayer.asSkillHolder(), 100);
        assertEquals(1d, experienceModifierRegistry.calculateModifierForContext(entityDamageContext));
    }

    @Test
    @DisplayName("Given a valid skill experience context with not enough rested XP, when calculating modifier, then it returns 2.0 and deducts rested XP accordingly")
    public void restedXpModifier_returnsTwo_whenInsufficientXp(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mockSkill.getLevelUpEquation()).thenReturn(new Parser("1000"));
        SkillHolder skillHolder = spy(mcRPGPlayer.asSkillHolder());
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, skillHolder, 100);
        addPlayerToServer(mcRPGPlayer);
        // Add mock skill data
        SkillHolder.SkillHolderData skillHolderData = spy(new SkillHolder.SkillHolderData(skillHolder, mockSkill, 0));
        skillHolder.addSkillHolderData(skillHolderData);
        // Mock Configuration
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getDouble(MainConfigFile.RESTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);

        mcRPGPlayer.getExperienceExtras().setRestedExperience(0.1f);
        assertEquals(2, experienceModifierRegistry.calculateModifierForContext(entityDamageContext));
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
    }

    @Test
    @DisplayName("Given a valid skill experience context with enough rested XP, when calculating modifier, then it returns 4.0 and deducts rested XP accordingly")
    public void restedXpModifier_returnsFour_whenSufficientExp(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mockSkill.getLevelUpEquation()).thenReturn(new Parser("3000"));
        SkillHolder skillHolder = spy(mcRPGPlayer.asSkillHolder());
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, skillHolder, 100);
        addPlayerToServer(mcRPGPlayer);
        // Add mock skill data
        SkillHolder.SkillHolderData skillHolderData = spy(new SkillHolder.SkillHolderData(skillHolder, mockSkill, 0));
        skillHolder.addSkillHolderData(skillHolderData);
        // Mock Configuration
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getDouble(MainConfigFile.RESTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);

        mcRPGPlayer.getExperienceExtras().setRestedExperience(0.1f);
        assertEquals(3.0, experienceModifierRegistry.calculateModifierForContext(entityDamageContext));
        assertEquals(0.03333333f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
    }

    @NotNull
    private EntityDamageContext constructEntityDamageContext(@NotNull McRPGPlayer mcRPGPlayer, @NotNull SkillHolder skillHolder, int baseExperience) {
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        LivingEntity livingEntity = spy(LivingEntity.class);
        LivingEntity attacker = spy(LivingEntity.class);
        doReturn(mcRPGPlayer.getUUID()).when(attacker).getUniqueId();
        doReturn(livingEntity).when(entityDamageByEntityEvent).getEntity();
        doReturn(attacker).when(entityDamageByEntityEvent).getDamager();
        return new EntityDamageContext(mcRPGPlayer.asSkillHolder(), mockSkill, baseExperience, entityDamageByEntityEvent);
    }
}
