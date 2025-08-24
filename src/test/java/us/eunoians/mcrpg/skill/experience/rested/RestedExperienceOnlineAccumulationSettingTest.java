package us.eunoians.mcrpg.skill.experience.rested;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.experience.McRPGBaseTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(RegistryResetExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
public class RestedExperienceOnlineAccumulationSettingTest extends McRPGBaseTest {

    @Test
    public void getCurrentSetting_returnsNonEmptyOptional_whenProperStringProvided() {
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION)).thenReturn("ENABLED");
        Optional<RestedExperienceOnlineAccumulationSetting> setting = RestedExperienceOnlineAccumulationSetting.getCurrentSetting();
        assertTrue(setting.isPresent());
        assertEquals(RestedExperienceOnlineAccumulationSetting.ENABLED, setting.get());
    }

    @Test
    public void getCurrentSetting_returnsNonEmptyOptional_whenProperLowercaseStringProvided() {
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION)).thenReturn("disabled");
        Optional<RestedExperienceOnlineAccumulationSetting> setting = RestedExperienceOnlineAccumulationSetting.getCurrentSetting();
        assertTrue(setting.isPresent());
        assertEquals(RestedExperienceOnlineAccumulationSetting.DISABLED, setting.get());
    }

    @Test
    public void getCurrentSetting_returnsEmptyOptional_whenInvalidStringProvided() {
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION)).thenReturn("diamond-is-so-cool");
        Optional<RestedExperienceOnlineAccumulationSetting> setting = RestedExperienceOnlineAccumulationSetting.getCurrentSetting();
        assertTrue(setting.isEmpty());
    }
}
