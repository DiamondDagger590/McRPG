package us.eunoians.mcrpg.skill.experience.rested;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.InternalResetTestTools;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGMockExtension;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.experience.McRPGBaseTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(RegistryResetExtension.class)
@ExtendWith(McRPGMockExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
public class RestedExperienceManagerTest extends McRPGBaseTest {

    private static final String RESTED_EXPERIENCE_MANAGER_CLASS_PATH = "us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager";
    private static final McRPG mcRPG = McRPGMockExtension.mcRPG;

    @BeforeEach
    public void setup() {
        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).register(reloadableContentManager);
        RestedExperienceManager restedExperienceManager = spy(new RestedExperienceManager(mcRPG));
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getString(MainConfigFile.ONLINE_RESTED_EXPERIENCE_ACCUMULATION_RATE)).thenReturn("0.00013*time'");
        when(mockConfig.getString(MainConfigFile.OFFLINE_RESTED_EXPERIENCE_ACCUMULATION_RATE)).thenReturn("0.00027*time");
        when(mockConfig.getString(MainConfigFile.ONLINE_SAFE_ZONE_RESTED_EXPERIENCE_ACCUMULATION_RATE)).thenReturn("0.00027*time");
        when(mockConfig.getString(MainConfigFile.OFFLINE_SAFE_ZONE_RESTED_EXPERIENCE_ACCUMULATION_RATE)).thenReturn("0.00027*time");
    }

    @AfterEach
    public void tearDown() {
        InternalResetTestTools.resetRegistryAccess(RESTED_EXPERIENCE_MANAGER_CLASS_PATH);
    }
}
