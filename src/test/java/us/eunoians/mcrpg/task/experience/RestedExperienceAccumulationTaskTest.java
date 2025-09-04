package us.eunoians.mcrpg.task.experience;


import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.experience.McRPGBaseTest;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceOnlineAccumulationSetting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class RestedExperienceAccumulationTaskTest extends McRPGBaseTest {

    private YamlDocument mockConfig;

    @BeforeEach
    public void setup(){
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
    }

    @AfterEach
    public void tearDown(){

    }

    @Test
    public void whenIntervalComplete_awardZeroRestedExperience_forOnlineAccumulationDisabled(@NotNull McRPGPlayer mcRPGPlayer) {
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION))
                .thenReturn(RestedExperienceOnlineAccumulationSetting.DISABLED.toString());


    }



}
