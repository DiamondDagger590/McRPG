package us.eunoians.mcrpg.skill.experience.modifier;

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
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistryExtension;
import us.eunoians.mcrpg.skill.experience.McRPGBaseTest;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * This unit test covers implementation of {@link BoostedExperienceModifier}
 * being registered and being used to calculate experience modifier.
 */
@ExtendWith(ExperienceModifierRegistryExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
public class BoostedExperienceModifierTest extends McRPGBaseTest {

    private ExperienceModifierRegistry experienceModifierRegistry;
    private BoostedExperienceModifier boostedExperienceModifier;

    @BeforeEach
    public void setup() {
        experienceModifierRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        boostedExperienceModifier = new BoostedExperienceModifier(mcRPG);
        experienceModifierRegistry.register(boostedExperienceModifier);
    }

    @Test
    @DisplayName("Given an invalid skill experience context, when checking canProcessContext, then it returns false")
    public void canProcessContext_returnsFalse_whenContextInvalid(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, 100);
        PlayerExperienceExtras playerExperienceExtras = new PlayerExperienceExtras(0, 0, 0, 0);
        mcRPGPlayer.getExperienceExtras().copyExtras(playerExperienceExtras);
        assertFalse(boostedExperienceModifier.canProcessContext(entityDamageContext));
    }

    @Test
    @DisplayName("Given a valid skill experience context with boosted XP, when checking canProcessContext, then it returns true")
    public void canProcessContext_returnsTrue_whenContextValid(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, 100);
        mcRPGPlayer.getExperienceExtras().setBoostedExperience(10);
        assertTrue(boostedExperienceModifier.canProcessContext(entityDamageContext));
    }

    @Test
    @DisplayName("Given an invalid skill experience context, when calculating modifier, then it returns 1.0")
    public void modifier_returnsOne_whenContextInvalid(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, 100);
        assertEquals(1d, experienceModifierRegistry.calculateModifierForContext(entityDamageContext));
    }

    @Test
    @DisplayName("Given a valid skill experience context with insufficient boosted XP, when calculating modifier, then it returns 1.1 and player has 0 remaining boosted XP")
    public void boostedXpModifier_returnsOnePointOne_whenInsufficientXp(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, 100);
        addPlayerToServer(mcRPGPlayer);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getDouble(MainConfigFile.BOOSTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);

        mcRPGPlayer.getExperienceExtras().setBoostedExperience(10);
        assertEquals(1.1, experienceModifierRegistry.calculateModifierForContext(entityDamageContext));
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getBoostedExperience());
    }

    @Test
    @DisplayName("Given a valid skill experience context with enough boosted XP, when calculating modifier, then it returns 4.0 and deducts boosted XP accordingly")
    public void boostedXpModifier_returnsFour_whenSufficientXp(@NotNull McRPGPlayer mcRPGPlayer) {
        EntityDamageContext entityDamageContext = constructEntityDamageContext(mcRPGPlayer, 100);
        addPlayerToServer(mcRPGPlayer);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getDouble(MainConfigFile.BOOSTED_EXPERIENCE_USAGE_RATE)).thenReturn(4.0);

        mcRPGPlayer.getExperienceExtras().setBoostedExperience(400);
        assertEquals(4, experienceModifierRegistry.calculateModifierForContext(entityDamageContext));
        assertEquals(100, mcRPGPlayer.getExperienceExtras().getBoostedExperience());
    }

    @NotNull
    private EntityDamageContext constructEntityDamageContext(@NotNull McRPGPlayer mcRPGPlayer, int baseExperience) {
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        LivingEntity livingEntity = spy(LivingEntity.class);
        LivingEntity attacker = spy(LivingEntity.class);
        doReturn(mcRPGPlayer.getUUID()).when(attacker).getUniqueId();
        doReturn(livingEntity).when(entityDamageByEntityEvent).getEntity();
        doReturn(attacker).when(entityDamageByEntityEvent).getDamager();

        return new EntityDamageContext(mcRPGPlayer.asSkillHolder(), mock(Skill.class), baseExperience, entityDamageByEntityEvent);
    }
}
