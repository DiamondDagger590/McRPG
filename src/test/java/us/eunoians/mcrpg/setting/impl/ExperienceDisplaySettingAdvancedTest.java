package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.display.impl.ActionBarExperienceDisplay;
import us.eunoians.mcrpg.display.impl.BossBarExperienceDisplay;
import us.eunoians.mcrpg.display.impl.ExperienceDisplay;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ExperienceDisplaySetting} methods that require MockBukkit
 * infrastructure: {@link ExperienceDisplaySetting#getExperienceDisplay},
 * {@link ExperienceDisplaySetting#onSettingChange}, and
 * {@link ExperienceDisplaySetting#sendExperienceUpdate}.
 */
@ExtendWith(McRPGPlayerExtension.class)
class ExperienceDisplaySettingAdvancedTest extends McRPGBaseTest {

    private McRPGPlayer mcRPGPlayer;
    private DisplayManager displayManager;

    @BeforeEach
    void setUp(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        addPlayerToServer(mcRPGPlayer);

        displayManager = mock(DisplayManager.class);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(displayManager);
    }

    @Nested
    @DisplayName("getExperienceDisplay")
    class GetExperienceDisplay {

        @Test
        @DisplayName("BOSS_BAR creates BossBarExperienceDisplay")
        void getExperienceDisplay_bossBar_createsBossBarDisplay() {
            ExperienceDisplay display = ExperienceDisplaySetting.BOSS_BAR.getExperienceDisplay(mcRPGPlayer);

            assertNotNull(display);
            assertInstanceOf(BossBarExperienceDisplay.class, display);
        }

        @Test
        @DisplayName("ACTION_BAR creates ActionBarExperienceDisplay")
        void getExperienceDisplay_actionBar_createsActionBarDisplay() {
            ExperienceDisplay display = ExperienceDisplaySetting.ACTION_BAR.getExperienceDisplay(mcRPGPlayer);

            assertNotNull(display);
            assertInstanceOf(ActionBarExperienceDisplay.class, display);
        }

        @ParameterizedTest
        @EnumSource(ExperienceDisplaySetting.class)
        @DisplayName("Every variant produces a non-null display")
        void getExperienceDisplay_allVariants_produceNonNull(ExperienceDisplaySetting setting) {
            ExperienceDisplay display = setting.getExperienceDisplay(mcRPGPlayer);

            assertNotNull(display);
        }

        @Test
        @DisplayName("BOSS_BAR display reports BOSS_BAR setting")
        void getExperienceDisplay_bossBar_displayReportsSetting() {
            ExperienceDisplay display = ExperienceDisplaySetting.BOSS_BAR.getExperienceDisplay(mcRPGPlayer);

            assertEquals(ExperienceDisplaySetting.BOSS_BAR, display.getSetting());
        }

        @Test
        @DisplayName("ACTION_BAR display reports ACTION_BAR setting")
        void getExperienceDisplay_actionBar_displayReportsSetting() {
            ExperienceDisplay display = ExperienceDisplaySetting.ACTION_BAR.getExperienceDisplay(mcRPGPlayer);

            assertEquals(ExperienceDisplaySetting.ACTION_BAR, display.getSetting());
        }
    }

    @Nested
    @DisplayName("onSettingChange")
    class OnSettingChange {

        @Test
        @DisplayName("Rebuilds display when one already exists")
        void onSettingChange_existingDisplay_rebuilds() {
            ExperienceDisplay existingDisplay = mock(ExperienceDisplay.class);
            when(displayManager.hasDisplay(eq(mcRPGPlayer), eq(ExperienceDisplay.class))).thenReturn(true);
            mcRPGPlayer.setPlayerSetting(ExperienceDisplaySetting.ACTION_BAR);

            ExperienceDisplaySetting.ACTION_BAR.onSettingChange(mcRPGPlayer, Optional.of(ExperienceDisplaySetting.BOSS_BAR));

            verify(displayManager).setDisplay(eq(mcRPGPlayer), eq(ExperienceDisplay.class), any(ExperienceDisplay.class));
        }

        @Test
        @DisplayName("Does not create display when none exists")
        void onSettingChange_noExistingDisplay_doesNotCreate() {
            when(displayManager.hasDisplay(eq(mcRPGPlayer), eq(ExperienceDisplay.class))).thenReturn(false);

            ExperienceDisplaySetting.ACTION_BAR.onSettingChange(mcRPGPlayer, Optional.empty());

            verify(displayManager, never()).setDisplay(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("sendExperienceUpdate")
    class SendExperienceUpdate {

        @Test
        @DisplayName("Short-circuits when config display is disabled")
        void sendExperienceUpdate_configDisabled_noOp() {
            FileManager fileManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.FILE);
            YamlDocument mainConfig = mock(YamlDocument.class);
            when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
            when(mainConfig.getBoolean(MainConfigFile.DISPLAY_EXPERIENCE_UPDATES_ENABLED, false)).thenReturn(false);

            ExperienceDisplaySetting.sendExperienceUpdate(mcRPGPlayer,
                    new NamespacedKey("mcrpg", "swords"));

            verify(displayManager, never()).getDisplay(any(), any());
        }
    }
}
