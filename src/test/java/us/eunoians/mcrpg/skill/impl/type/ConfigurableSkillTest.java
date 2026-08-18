package us.eunoians.mcrpg.skill.impl.type;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SkillConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the default method implementations provided by
 * {@link ConfigurableSkill} through the concrete {@link Swords} skill.
 * <p>
 * The existing {@code SwordsTest} exercises Swords-specific behavior
 * (XP calculation, event registration); this test class targets the
 * interface defaults that all {@link ConfigurableSkill} implementors
 * inherit: {@code getMaxLevel}, {@code isSkillEnabled},
 * {@code getLevelUpEquation}, {@code getName(McRPGPlayer)},
 * {@code getName()}, {@code getColoredName(McRPGPlayer)},
 * {@code getDisplayName(McRPGPlayer)}, and {@code getDisplayName()}.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class ConfigurableSkillTest extends McRPGBaseTest {

    private Swords swords;
    private YamlDocument swordsConfig;
    private McRPGLocalizationManager localizationManager;

    @BeforeEach
    void setUp() {
        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);

        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        swords = new Swords(mcRPG);
        skillRegistry.register(swords);
    }

    @Nested
    @DisplayName("getMaxLevel")
    class GetMaxLevel {

        @Test
        @DisplayName("Given a configured max level, when calling getMaxLevel, then returns the configured value")
        void getMaxLevel_returnsConfiguredValue() {
            when(swordsConfig.getInt(SkillConfigFile.MAXIMUM_SKILL_LEVEL)).thenReturn(500);

            assertEquals(500, swords.getMaxLevel());
        }

        @Test
        @DisplayName("Given a different max level, when calling getMaxLevel, then returns the updated value")
        void getMaxLevel_returnsUpdatedValue() {
            when(swordsConfig.getInt(SkillConfigFile.MAXIMUM_SKILL_LEVEL)).thenReturn(1000);

            assertEquals(1000, swords.getMaxLevel());
        }
    }

    @Nested
    @DisplayName("isSkillEnabled")
    class IsSkillEnabled {

        @Test
        @DisplayName("Given skill-enabled is true in config, when calling isSkillEnabled, then returns true")
        void isSkillEnabled_returnsTrue_whenConfiguredTrue() {
            when(swordsConfig.getBoolean(SkillConfigFile.SKILL_ENABLED)).thenReturn(true);

            assertTrue(swords.isSkillEnabled());
        }

        @Test
        @DisplayName("Given skill-enabled is false in config, when calling isSkillEnabled, then returns false")
        void isSkillEnabled_returnsFalse_whenConfiguredFalse() {
            when(swordsConfig.getBoolean(SkillConfigFile.SKILL_ENABLED)).thenReturn(false);

            assertFalse(swords.isSkillEnabled());
        }
    }

    @Nested
    @DisplayName("getLevelUpEquation")
    class GetLevelUpEquation {

        @Test
        @DisplayName("Given a configured equation string, when calling getLevelUpEquation, then returns a Parser with that equation")
        void getLevelUpEquation_returnsParserWithConfiguredEquation() {
            when(swordsConfig.getString(SkillConfigFile.LEVEL_UP_EQUATION)).thenReturn("1000+(20*level)");

            Parser parser = swords.getLevelUpEquation();

            assertNotNull(parser);
            parser.setVariable("level", 5);
            assertEquals(1100, parser.getValue());
        }

        @Test
        @DisplayName("Given a simple constant equation, when calling getLevelUpEquation, then Parser evaluates to that constant")
        void getLevelUpEquation_returnsConstant_whenEquationIsConstant() {
            when(swordsConfig.getString(SkillConfigFile.LEVEL_UP_EQUATION)).thenReturn("500");

            Parser parser = swords.getLevelUpEquation();

            assertEquals(500, parser.getValue());
        }
    }

    @Nested
    @DisplayName("getName")
    class GetName {

        @Test
        @DisplayName("Given a localized skill name, when calling getName with a player, then delegates to the localization manager")
        void getName_delegatesToLocalization_whenCalledWithPlayer(@NotNull McRPGPlayer mcRPGPlayer) {
            when(localizationManager.getLocalizedMessage(eq(mcRPGPlayer), any())).thenReturn("Swords");

            String name = swords.getName(mcRPGPlayer);

            assertEquals("Swords", name);
            verify(localizationManager).getLocalizedMessage(eq(mcRPGPlayer), any());
        }

        @Test
        @DisplayName("Given a localized skill name, when calling getName without a player, then delegates to the no-player localization")
        void getName_delegatesToLocalization_whenCalledWithoutPlayer() {
            when(localizationManager.getLocalizedMessage(any())).thenReturn("Swords");

            String name = swords.getName();

            assertEquals("Swords", name);
        }
    }

    @Nested
    @DisplayName("getColoredName")
    class GetColoredName {

        @Test
        @DisplayName("Given localized name and colored name, when calling getColoredName, then returns the colored version")
        void getColoredName_returnsColoredVersion(@NotNull McRPGPlayer mcRPGPlayer) {
            when(localizationManager.getLocalizedMessage(eq(mcRPGPlayer), any())).thenReturn("Swords");
            when(localizationManager.getLocalizedMessage(eq(mcRPGPlayer), any(), any(Map.class)))
                    .thenReturn("<color:#C75050>Swords</color:#C75050>");

            String coloredName = swords.getColoredName(mcRPGPlayer);

            assertEquals("<color:#C75050>Swords</color:#C75050>", coloredName);
        }
    }

    @Nested
    @DisplayName("getDisplayName")
    class GetDisplayName {

        @Test
        @DisplayName("Given a localized display name, when calling getDisplayName with a player, then returns a Component")
        void getDisplayName_returnsComponent_whenCalledWithPlayer(@NotNull McRPGPlayer mcRPGPlayer) {
            Component expected = Component.text("Swords");
            when(localizationManager.getLocalizedMessage(eq(mcRPGPlayer), any())).thenReturn("Swords");
            when(localizationManager.getLocalizedMessageAsComponent(eq(mcRPGPlayer), any(), any(Map.class)))
                    .thenReturn(expected);

            Component result = swords.getDisplayName(mcRPGPlayer);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Given a localized display name, when calling getDisplayName without a player, then returns a Component")
        void getDisplayName_returnsComponent_whenCalledWithoutPlayer() {
            Component expected = Component.text("Swords");
            when(localizationManager.getLocalizedMessage(any())).thenReturn("Swords");
            when(localizationManager.getLocalizedMessageAsComponent(any(), any(Map.class)))
                    .thenReturn(expected);

            Component result = swords.getDisplayName();

            assertEquals(expected, result);
        }
    }
}
