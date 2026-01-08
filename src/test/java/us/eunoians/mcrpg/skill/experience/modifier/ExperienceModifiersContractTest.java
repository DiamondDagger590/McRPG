package us.eunoians.mcrpg.skill.experience.modifier;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.MockSkill;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistryExtension;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(ExperienceModifierRegistryExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
public class ExperienceModifiersContractTest extends McRPGBaseTest {

    private ExperienceModifierRegistry experienceModifierRegistry;
    private MockSkill mockSkill;

    @BeforeEach
    public void setupBeforeEach() {
        experienceModifierRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        mockSkill = spy(MockSkill.class);
    }

    @DisplayName("Given boosted XP available and a non-spawner entity, when calculating the modifier, then it returns 3.0 and consumes 200 boosted XP")
    @Test
    public void calculateModifier_returns3_whenSpawnerNotApplied(@NotNull McRPGPlayer mcRPGPlayer) {
        SpawnReasonModifier spawnReasonModifier = new SpawnReasonModifier();
        BoostedExperienceModifier boostedExperienceModifier = new BoostedExperienceModifier(mcRPG);
        experienceModifierRegistry.register(spawnReasonModifier);
        experienceModifierRegistry.register(boostedExperienceModifier);

        addPlayerToServer(mcRPGPlayer);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getDouble(MainConfigFile.BOOSTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);

        EntityDamageContext entityDamageContext = SpawnReasonModifierTest.constructEntityDamageContext(mcRPGPlayer, 0.5, false);
        mcRPGPlayer.getExperienceExtras().setBoostedExperience(300);
        double modifier = experienceModifierRegistry.calculateModifierForContext(entityDamageContext);
        assertEquals(3d, modifier);
        assertEquals(100, mcRPGPlayer.getExperienceExtras().getBoostedExperience());
    }

    @DisplayName("Given boosted XP available and a spawner-sourced entity, when calculating the modifier," +
            " then it returns 1.5 and consumes 100 boosted XP")
    @Test
    public void calculateModifier_returns1Point5_whenSpawnerApplied(@NotNull McRPGPlayer mcRPGPlayer) {
        SpawnReasonModifier spawnReasonModifier = new SpawnReasonModifier();
        BoostedExperienceModifier boostedExperienceModifier = new BoostedExperienceModifier(mcRPG);
        experienceModifierRegistry.register(spawnReasonModifier);
        experienceModifierRegistry.register(boostedExperienceModifier);

        addPlayerToServer(mcRPGPlayer);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getDouble(MainConfigFile.BOOSTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);
        EntityDamageContext entityDamageContext = SpawnReasonModifierTest.constructEntityDamageContext(mcRPGPlayer, 0.5d, true);
        mcRPGPlayer.getExperienceExtras().setBoostedExperience(300);
        double modifier = experienceModifierRegistry.calculateModifierForContext(entityDamageContext);
        assertEquals(1.5d, modifier);
        assertEquals(200, mcRPGPlayer.getExperienceExtras().getBoostedExperience());
    }

    @DisplayName("Given boosted and rested XP available and a non-spawner entity, when calculating the modifier," +
            " then it returns 6.0, reduces rested to ~0.0333 and consumes 200 boosted XP")
    @Test
    public void calculateModifier_returns6_whenBoostedAndRestedApplied_andSpawnerNotApplied(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mockSkill.getLevelUpEquation()).thenReturn(new Parser("3000"));
        BoostedExperienceModifier boostedExperienceModifier = new BoostedExperienceModifier(mcRPG);
        RestedExperienceModifier restedExperienceModifier = new RestedExperienceModifier(mcRPG);
        SpawnReasonModifier spawnReasonModifier = new SpawnReasonModifier();
        experienceModifierRegistry.register(restedExperienceModifier);
        experienceModifierRegistry.register(boostedExperienceModifier);
        experienceModifierRegistry.register(spawnReasonModifier);

        addPlayerToServer(mcRPGPlayer);
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        // Add mock skill data
        SkillHolder.SkillHolderData skillHolderData = spy(new SkillHolder.SkillHolderData(skillHolder, mockSkill, 0));
        skillHolder.addSkillHolderData(skillHolderData);

        // Setup files
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getDouble(MainConfigFile.BOOSTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);
        when(mockConfig.getDouble(MainConfigFile.RESTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);

        mcRPGPlayer.getExperienceExtras().setRestedExperience(0.1f);
        mcRPGPlayer.getExperienceExtras().setBoostedExperience(300);
        EntityDamageContext entityDamageContext = SpawnReasonModifierTest.constructEntityDamageContext(mcRPGPlayer, mockSkill, 0.5d, false);

        double modifier = experienceModifierRegistry.calculateModifierForContext(entityDamageContext);
        assertEquals(6d, modifier);
        assertEquals(0.03333333f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(100, mcRPGPlayer.getExperienceExtras().getBoostedExperience());
    }

    @DisplayName("Given boosted and rested XP available and a spawner-sourced entity, when calculating the modifier," +
            " then it returns 3.0, reduces rested to ~0.0666 and consumes 100 boosted XP")
    @Test
    public void calculateModifier_returns3_whenBoostedAndRestedApplied_andSpawnerApplied(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mockSkill.getLevelUpEquation()).thenReturn(new Parser("3000"));
        BoostedExperienceModifier boostedExperienceModifier = new BoostedExperienceModifier(mcRPG);
        RestedExperienceModifier restedExperienceModifier = new RestedExperienceModifier(mcRPG);
        SpawnReasonModifier spawnReasonModifier = new SpawnReasonModifier();
        experienceModifierRegistry.register(restedExperienceModifier);
        experienceModifierRegistry.register(boostedExperienceModifier);
        experienceModifierRegistry.register(spawnReasonModifier);

        addPlayerToServer(mcRPGPlayer);
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        // Add mock skill data
        SkillHolder.SkillHolderData skillHolderData = spy(new SkillHolder.SkillHolderData(skillHolder, mockSkill, 0));
        skillHolder.addSkillHolderData(skillHolderData);

        // Setup files
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getDouble(MainConfigFile.BOOSTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);
        when(mockConfig.getDouble(MainConfigFile.RESTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);

        mcRPGPlayer.getExperienceExtras().setRestedExperience(0.1f);
        mcRPGPlayer.getExperienceExtras().setBoostedExperience(300);
        EntityDamageContext entityDamageContext = SpawnReasonModifierTest.constructEntityDamageContext(mcRPGPlayer, mockSkill, 0.5d, true);

        double modifier = experienceModifierRegistry.calculateModifierForContext(entityDamageContext);
        assertEquals(3d, modifier);
        assertEquals(0.06666666f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(200, mcRPGPlayer.getExperienceExtras().getBoostedExperience());
    }
}
