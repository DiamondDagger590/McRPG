package us.eunoians.mcrpg.task.experience;


import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.experience.McRPGBaseTest;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceAccumulationType;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceOnlineAccumulationSetting;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class RestedExperienceAccumulationTaskTest extends McRPGBaseTest {

    private YamlDocument mockConfig;
    private RestedExperienceManager restedExperienceManager;

    @BeforeEach
    public void setup() {
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        restedExperienceManager = mock(RestedExperienceManager.class);
        RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).register(restedExperienceManager);
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    public void whenIntervalComplete_doNotAwardRestedExperience_forNonCachedPlayer(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        doNothing().when(restedExperienceManager).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION))
                .thenReturn(RestedExperienceOnlineAccumulationSetting.ENABLED.toString());
        Instant instant = Instant.now();
        TimeProvider timeProvider = McRPG.getInstance().getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);
        RestedExperienceAccumulationTask restedExperienceAccumulationTask = spy(new RestedExperienceAccumulationTask(McRPG.getInstance(), 0, 1f));
        restedExperienceAccumulationTask.runTask();

        // We need to make it complete two intervals in order to have the player be in the previous updated list
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(timeProvider, times(5)).now();
        verify(restedExperienceAccumulationTask, times(1)).onIntervalComplete();
    }

    @Test
    public void whenIntervalComplete_awardRestedExperience_forOnlineAccumulationEnabled(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        doNothing().when(restedExperienceManager).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION))
                .thenReturn(RestedExperienceOnlineAccumulationSetting.ENABLED.toString());
        Instant instant = Instant.now();
        TimeProvider timeProvider = McRPG.getInstance().getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);
        RestedExperienceAccumulationTask restedExperienceAccumulationTask = spy(new RestedExperienceAccumulationTask(McRPG.getInstance(), 0, 1f));
        restedExperienceAccumulationTask.runTask();

        // We need to make it complete two intervals in order to have the player be in the previous updated list
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(timeProvider, times(7)).now();
        verify(restedExperienceManager, times(1)).awardRestedExperience(any(), anyInt(), eq(RestedExperienceAccumulationType.ONLINE), anyBoolean());
        verify(restedExperienceAccumulationTask, times(2)).onIntervalComplete();
    }

    @Test
    public void whenIntervalComplete_doNotAwardExperience_forAfkPlayer(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        when(mcRPGPlayer.isAfk()).thenReturn(true);
        doNothing().when(restedExperienceManager).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION))
                .thenReturn(RestedExperienceOnlineAccumulationSetting.ENABLED.toString());
        when(mockConfig.getBoolean(MainConfigFile.DISABLE_AFK_RESTED_EXPERIENCE_ACCUMULATION))
                .thenReturn(true);
        Instant instant = Instant.now();
        TimeProvider timeProvider = McRPG.getInstance().getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);
        RestedExperienceAccumulationTask restedExperienceAccumulationTask = spy(new RestedExperienceAccumulationTask(McRPG.getInstance(), 0, 1f));
        restedExperienceAccumulationTask.runTask();

        // We need to make it complete two intervals in order to have the player be in the previous updated list
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(timeProvider, times(7)).now();
        verify(restedExperienceManager, times(0)).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        verify(restedExperienceAccumulationTask, times(2)).onIntervalComplete();
    }

    @Test
    public void whenIntervalComplete_awardExperience_forAfkPlayer(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        when(mcRPGPlayer.isAfk()).thenReturn(true);
        doNothing().when(restedExperienceManager).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION))
                .thenReturn(RestedExperienceOnlineAccumulationSetting.ENABLED.toString());
        when(mockConfig.getBoolean(MainConfigFile.DISABLE_AFK_RESTED_EXPERIENCE_ACCUMULATION))
                .thenReturn(false);
        Instant instant = Instant.now();
        TimeProvider timeProvider = McRPG.getInstance().getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);
        RestedExperienceAccumulationTask restedExperienceAccumulationTask = spy(new RestedExperienceAccumulationTask(McRPG.getInstance(), 0, 1f));
        restedExperienceAccumulationTask.runTask();

        // We need to make it complete two intervals in order to have the player be in the previous updated list
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(timeProvider, times(7)).now();
        verify(restedExperienceManager, times(1)).awardRestedExperience(any(), anyInt(), eq(RestedExperienceAccumulationType.ONLINE), anyBoolean());
        verify(restedExperienceAccumulationTask, times(2)).onIntervalComplete();
    }

    @Test
    public void whenIntervalComplete_doNotAwardRestedExperience_forOnlineAccumulationDisabled(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        doNothing().when(restedExperienceManager).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION))
                .thenReturn(RestedExperienceOnlineAccumulationSetting.DISABLED.toString());
        Instant instant = Instant.now();
        TimeProvider timeProvider = McRPG.getInstance().getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);
        RestedExperienceAccumulationTask restedExperienceAccumulationTask = spy(new RestedExperienceAccumulationTask(McRPG.getInstance(), 0, 1f));
        restedExperienceAccumulationTask.runTask();

        // We need to make it complete two intervals in order to have the player be in the previous updated list
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(timeProvider, times(7)).now();
        verify(restedExperienceManager, times(0)).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        verify(restedExperienceAccumulationTask, times(2)).onIntervalComplete();
    }

    @Test
    public void whenIntervalComplete_doNotAwardRestedExperience_forOnlineAccumulationSafeZoneAndPlayerNotInSafeZone(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        when(mcRPGPlayer.isStandingInSafeZone()).thenReturn(false);
        doNothing().when(restedExperienceManager).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION))
                .thenReturn(RestedExperienceOnlineAccumulationSetting.SAFE_ZONE_ONLY.toString());
        Instant instant = Instant.now();
        TimeProvider timeProvider = McRPG.getInstance().getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);
        RestedExperienceAccumulationTask restedExperienceAccumulationTask = spy(new RestedExperienceAccumulationTask(McRPG.getInstance(), 0, 1f));
        restedExperienceAccumulationTask.runTask();

        // We need to make it complete two intervals in order to have the player be in the previous updated list
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(timeProvider, times(7)).now();
        verify(restedExperienceManager, times(0)).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        verify(restedExperienceAccumulationTask, times(2)).onIntervalComplete();
    }

    @Test
    public void whenIntervalComplete_awardRestedExperience_forOnlineAccumulationSafeZoneAndPlayerInSafeZone(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        when(mcRPGPlayer.isStandingInSafeZone()).thenReturn(true);
        doNothing().when(restedExperienceManager).awardRestedExperience(any(), anyInt(), any(), anyBoolean());
        when(mockConfig.getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION))
                .thenReturn(RestedExperienceOnlineAccumulationSetting.SAFE_ZONE_ONLY.toString());
        Instant instant = Instant.now();
        TimeProvider timeProvider = McRPG.getInstance().getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);
        RestedExperienceAccumulationTask restedExperienceAccumulationTask = spy(new RestedExperienceAccumulationTask(McRPG.getInstance(), 0, 1f));
        restedExperienceAccumulationTask.runTask();

        // We need to make it complete two intervals in order to have the player be in the previous updated list
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(timeProvider, times(7)).now();
        verify(restedExperienceManager, times(1)).awardRestedExperience(any(), anyInt(), eq(RestedExperienceAccumulationType.ONLINE_SAFE_ZONE), anyBoolean());
        verify(restedExperienceAccumulationTask, times(2)).onIntervalComplete();
    }
}
