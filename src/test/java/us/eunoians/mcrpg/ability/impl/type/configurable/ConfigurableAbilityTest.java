package us.eunoians.mcrpg.ability.impl.type.configurable;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SkillConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the default method implementations provided by
 * {@link ConfigurableAbility} through the concrete {@link Bleed} ability.
 * <p>
 * The existing {@code BleedTest} exercises Bleed-specific behavior
 * (tier damage, component wiring); this test class targets the
 * interface defaults that all {@link ConfigurableAbility} implementors
 * inherit: {@code isAbilityEnabled}, {@code getName(McRPGPlayer)},
 * {@code getName()}, {@code getColoredName(McRPGPlayer)},
 * {@code getDisplayName(McRPGPlayer)}, and {@code getDisplayName()}.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class ConfigurableAbilityTest extends McRPGBaseTest {

    private Bleed bleed;
    private YamlDocument swordsConfig;
    private McRPGLocalizationManager localizationManager;

    @BeforeEach
    void setUp() {
        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        when(swordsConfig.getBoolean(SkillConfigFile.SKILL_ENABLED)).thenReturn(true);

        Swords swords = new Swords(mcRPG);
        skillRegistry.register(swords);

        localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        bleed = new Bleed(mcRPG);
    }

    @Nested
    @DisplayName("isAbilityEnabled")
    class IsAbilityEnabled {

        @Test
        @DisplayName("Given ability-enabled is true in config, when calling isAbilityEnabled, then returns true")
        void isAbilityEnabled_returnsTrue_whenConfiguredTrue() {
            when(swordsConfig.getBoolean(SwordsConfigFile.BLEED_ENABLED)).thenReturn(true);

            assertTrue(bleed.isAbilityEnabled());
        }

        @Test
        @DisplayName("Given ability-enabled is false in config, when calling isAbilityEnabled, then returns false")
        void isAbilityEnabled_returnsFalse_whenConfiguredFalse() {
            when(swordsConfig.getBoolean(SwordsConfigFile.BLEED_ENABLED)).thenReturn(false);

            assertFalse(bleed.isAbilityEnabled());
        }
    }

    @Nested
    @DisplayName("getName")
    class GetName {

        @Test
        @DisplayName("Given a localized ability name, when calling getName with a player, then delegates to the localization manager")
        void getName_delegatesToLocalization_whenCalledWithPlayer(@NotNull McRPGPlayer mcRPGPlayer) {
            when(localizationManager.getLocalizedMessage(eq(mcRPGPlayer), any(Route.class))).thenReturn("Bleed");

            String name = bleed.getName(mcRPGPlayer);

            assertEquals("Bleed", name);
            verify(localizationManager).getLocalizedMessage(eq(mcRPGPlayer), any(Route.class));
        }

        @Test
        @DisplayName("Given a localized ability name, when calling getName without a player, then delegates to the no-player localization")
        void getName_delegatesToLocalization_whenCalledWithoutPlayer() {
            when(localizationManager.getLocalizedMessage(any(Route.class))).thenReturn("Bleed");

            String name = bleed.getName();

            assertEquals("Bleed", name);
        }
    }

    @Nested
    @DisplayName("getColoredName")
    class GetColoredName {

        @Test
        @DisplayName("Given localized name and colored name, when calling getColoredName, then returns the colored version")
        void getColoredName_returnsColoredVersion(@NotNull McRPGPlayer mcRPGPlayer) {
            when(localizationManager.getLocalizedMessage(eq(mcRPGPlayer), any(Route.class)))
                    .thenReturn("Bleed");
            when(localizationManager.getLocalizedMessage(eq(mcRPGPlayer), any(Route.class), any(Map.class)))
                    .thenReturn("<color:#FF6666>Bleed</color:#FF6666>");

            String coloredName = bleed.getColoredName(mcRPGPlayer);

            assertEquals("<color:#FF6666>Bleed</color:#FF6666>", coloredName);
        }
    }

    @Nested
    @DisplayName("getDisplayName")
    class GetDisplayName {

        @Test
        @DisplayName("Given a localized display name, when calling getDisplayName with a player, then returns a Component")
        void getDisplayName_returnsComponent_whenCalledWithPlayer(@NotNull McRPGPlayer mcRPGPlayer) {
            Component expected = Component.text("Bleed");
            when(localizationManager.getLocalizedMessage(eq(mcRPGPlayer), any(Route.class)))
                    .thenReturn("Bleed");
            when(localizationManager.getLocalizedMessageAsComponent(eq(mcRPGPlayer), any(Route.class), any(Map.class)))
                    .thenReturn(expected);

            Component result = bleed.getDisplayName(mcRPGPlayer);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Given a localized display name, when calling getDisplayName without a player, then returns a Component")
        void getDisplayName_returnsComponent_whenCalledWithoutPlayer() {
            Component expected = Component.text("Bleed");
            when(localizationManager.getLocalizedMessage(any(Route.class))).thenReturn("Bleed");
            when(localizationManager.getLocalizedMessageAsComponent(any(Route.class), any(Map.class)))
                    .thenReturn(expected);

            Component result = bleed.getDisplayName();

            assertEquals(expected, result);
        }
    }
}
