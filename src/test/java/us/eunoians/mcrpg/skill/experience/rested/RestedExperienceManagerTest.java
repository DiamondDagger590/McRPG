package us.eunoians.mcrpg.skill.experience.rested;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.entity.player.PlayerAwardedRestedExperienceEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasNotFiredEventInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class RestedExperienceManagerTest extends McRPGBaseTest {

    private RestedExperienceManager restedExperienceManager;
    private YamlDocument mockConfig;

    @BeforeEach
    public void beforeEach() {
        server.getPluginManager().clearEvents();
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getString(MainConfigFile.ONLINE_RESTED_EXPERIENCE_ACCUMULATION_RATE)).thenReturn("0.1*time'");
        when(mockConfig.getString(MainConfigFile.OFFLINE_RESTED_EXPERIENCE_ACCUMULATION_RATE)).thenReturn("0.2*time");
        when(mockConfig.getString(MainConfigFile.ONLINE_SAFE_ZONE_RESTED_EXPERIENCE_ACCUMULATION_RATE)).thenReturn("0.3*time");
        when(mockConfig.getString(MainConfigFile.OFFLINE_SAFE_ZONE_RESTED_EXPERIENCE_ACCUMULATION_RATE)).thenReturn("0.4*time");
        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).register(reloadableContentManager);
        restedExperienceManager = spy(new RestedExperienceManager(mcRPG));
        RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).register(restedExperienceManager);
    }

    @DisplayName("Given ONLINE accumulation and time=10, when computing rested experience, then it returns 1.0")
    @Test
    public void getRestedExperience_returnsOne_whenOnline() {
        float restedExperience = restedExperienceManager.getRestedExperience(10, RestedExperienceAccumulationType.ONLINE);
        assertEquals(1, restedExperience);
    }

    @DisplayName("Given OFFLINE accumulation and time=10, when computing rested experience, then it returns 2.0")
    @Test
    public void getRestedExperience_returnsTwo_whenOffline() {
        float restedExperience = restedExperienceManager.getRestedExperience(10, RestedExperienceAccumulationType.OFFLINE);
        assertEquals(2, restedExperience);
    }

    @DisplayName("Given ONLINE safe-zone accumulation and time=10, when computing rested experience, then it returns 3.0")
    @Test
    public void getRestedExperience_returnsThree_whenOnlineSafeZone() {
        float restedExperience = restedExperienceManager.getRestedExperience(10, RestedExperienceAccumulationType.ONLINE_SAFE_ZONE);
        assertEquals(3, restedExperience);
    }

    @DisplayName("Given OFFLINE safe-zone accumulation and time=10, when computing rested experience, then it returns 4.0")
    @Test
    public void getRestedExperience_returnsFour_whenOfflineSafeZone() {
        float restedExperience = restedExperienceManager.getRestedExperience(10, RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE);
        assertEquals(4, restedExperience);
    }

    @DisplayName("Given OFFLINE accumulation and notifications ENABLED with max=5 and safe-zone allowed, when awarding for time=10, then it awards 2, fires the event, and sends a message")
    @Test
    public void awardRestedExperience_awardsTwo_whenOfflineNotifyTrue(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component component = mcRPG.getMiniMessage().deserialize("You gained x rested experience while offline");
        when(mcRPGLocalizationManager.getLocalizedMessageAsComponent(any(Audience.class), eq(LocalizationKey.OFFLINE_RESTED_EXPERIENCE_AWARDED_MESSAGE), anyMap()))
                .thenReturn(component);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(true);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.OFFLINE, true);
        assertThat(server.getPluginManager(), hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class));
        assertEquals(2, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

    @DisplayName("Given OFFLINE accumulation and notifications DISABLED with max=5 and safe-zone allowed, when awarding for time=10, then it awards 2, fires the event, and sends no message")
    @Test
    public void awardRestedExperience_awardsTwo_whenOfflineNotifyFalse(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(true);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.OFFLINE, false);
        assertThat(server.getPluginManager(), hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class));
        assertEquals(2, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertNull(playerMock.nextComponentMessage());
    }

    @DisplayName("Given OFFLINE safe-zone accumulation while safe-zone accrual DISABLED and notifications ENABLED, when awarding for time=10, then it awards 2, fires the event, and sends a message")
    @Test
    public void awardRestedExperience_awardsTwo_whenOfflineSafeZoneDisabledNotifyTrue(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component component = mcRPG.getMiniMessage().deserialize("You gained x rested experience while offline");
        when(mcRPGLocalizationManager.getLocalizedMessageAsComponent(any(Audience.class), eq(LocalizationKey.OFFLINE_RESTED_EXPERIENCE_AWARDED_MESSAGE), anyMap()))
                .thenReturn(component);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(false);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE, true);
        assertThat(server.getPluginManager(), hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class));
        assertEquals(2, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

    @DisplayName("Given ONLINE safe-zone accumulation while safe-zone accrual DISABLED and notifications DISABLED, when awarding for time=10, then it awards 1, fires the event, and sends no message")
    @Test
    public void awardRestedExperience_awardsOne_whenOnlineSafeZoneDisabledNotifyFalse(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(false);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.ONLINE_SAFE_ZONE, false);
        assertThat(server.getPluginManager(), hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class));
        assertEquals(1, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertNull(playerMock.nextComponentMessage());
    }

    @DisplayName("Given OFFLINE safe-zone accumulation while safe-zone accrual ENABLED and notifications ENABLED, when awarding for time=10, then it awards 4, fires the event, and sends a message")
    @Test
    public void awardRestedExperience_awardsFour_whenOfflineSafeZoneEnabledNotifyTrue(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component component = mcRPG.getMiniMessage().deserialize("You gained x rested experience while offline");
        when(mcRPGLocalizationManager.getLocalizedMessageAsComponent(any(Audience.class), eq(LocalizationKey.OFFLINE_RESTED_EXPERIENCE_AWARDED_MESSAGE), anyMap()))
                .thenReturn(component);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(true);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE, true);
        assertThat(server.getPluginManager(), hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class));
        assertEquals(4, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

    @DisplayName("Given rested experience already at MAX=5 and notifications ENABLED, when awarding additional OFFLINE safe-zone accumulation, then it awards none, does not fire the event, and notifies the player")
    @Test
    public void awardRestedExperience_awardsNone_whenAtMaxNotifyTrue(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component component = mcRPG.getMiniMessage().deserialize("You have reached the maximum accumulation");
        when(mcRPGLocalizationManager.getLocalizedMessageAsComponent(any(Audience.class), eq(LocalizationKey.MAXIMUM_RESTED_EXPERIENCE_REACHED_MESSAGE)))
                .thenReturn(component);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(true);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        mcRPGPlayer.getExperienceExtras().setRestedExperience(5f);
        assertEquals(5f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE, true);
        assertThat(server.getPluginManager(), hasNotFiredEventInstance(PlayerAwardedRestedExperienceEvent.class));
        assertEquals(5f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

    @Test
    public void awardRestedExperience_awardsMaxExperience_whenAwardedAmountExceedsMax(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component component = mcRPG.getMiniMessage().deserialize("You have gained rested experience");
        when(mcRPGLocalizationManager.getLocalizedMessageAsComponent(any(Audience.class), eq(LocalizationKey.OFFLINE_RESTED_EXPERIENCE_AWARDED_MESSAGE), any()))
                .thenReturn(component);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(true);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(1.5f);
        assertEquals(0f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10000, RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE, true);
        assertThat(server.getPluginManager(), hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class));
        assertEquals(1.5f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

}
