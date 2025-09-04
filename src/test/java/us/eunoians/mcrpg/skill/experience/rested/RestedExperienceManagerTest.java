package us.eunoians.mcrpg.skill.experience.rested;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
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
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasNotFiredEventInstance;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    public void getRestedExperience_returnsOne_forOnlineAccumulation() {
        float restedExperience = restedExperienceManager.getRestedExperience(10, RestedExperienceAccumulationType.ONLINE);
        assertEquals(1, restedExperience);
    }

    @Test
    public void getRestedExperience_returnsTwo_forOfflineAccumulation() {
        float restedExperience = restedExperienceManager.getRestedExperience(10, RestedExperienceAccumulationType.OFFLINE);
        assertEquals(2, restedExperience);
    }

    @Test
    public void getRestedExperience_returnsThree_forOnlineSafeZoneAccumulation() {
        float restedExperience = restedExperienceManager.getRestedExperience(10, RestedExperienceAccumulationType.ONLINE_SAFE_ZONE);
        assertEquals(3, restedExperience);
    }

    @Test
    public void getRestedExperience_returnsFour_forOfflineSafeZoneAccumulation() {
        float restedExperience = restedExperienceManager.getRestedExperience(10, RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE);
        assertEquals(4, restedExperience);
    }

    @Test
    public void awardPlayerRestedExperience_awardsTwo_forOfflineAccumulation(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component component = mcRPG.getMiniMessage().deserialize("You gained x rested experience while offline");
        when(mcRPGLocalizationManager.getLocalizedMessageAsComponent(any(Audience.class), eq(LocalizationKey.OFFLINE_RESTED_EXPERIENCE_AWARDED_MESSAGE), any(Map.class)))
                .thenReturn(component);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(true);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.OFFLINE, true);
        hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class);
        assertEquals(2, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

    @Test
    public void awardPlayerRestedExperience_awardsTwoWithNoMessage_forOfflineAccumulation(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(true);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.OFFLINE, false);
        hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class);
        assertEquals(2, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertNull(playerMock.nextComponentMessage());
    }

    @Test
    public void awardPlayerRestedExperience_awardsTwo_forOfflineSafeZoneAccumulationWithSafeZoneDisabled(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component component = mcRPG.getMiniMessage().deserialize("You gained x rested experience while offline");
        when(mcRPGLocalizationManager.getLocalizedMessageAsComponent(any(Audience.class), eq(LocalizationKey.OFFLINE_RESTED_EXPERIENCE_AWARDED_MESSAGE), any(Map.class)))
                .thenReturn(component);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(false);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE, true);
        hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class);
        assertEquals(2, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

    @Test
    public void awardPlayerRestedExperience_awardsOne_forOnlineSafeZoneAccumulationWithSafeZoneDisabled(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(false);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.ONLINE_SAFE_ZONE, false);
        hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class);
        assertEquals(1, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertNull(playerMock.nextComponentMessage());
    }

    @Test
    public void awardPlayerRestedExperience_awardsFour_forOfflineSafeZoneAccumulationWithSafeZoneEnabled(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component component = mcRPG.getMiniMessage().deserialize("You gained x rested experience while offline");
        when(mcRPGLocalizationManager.getLocalizedMessageAsComponent(any(Audience.class), eq(LocalizationKey.OFFLINE_RESTED_EXPERIENCE_AWARDED_MESSAGE), any(Map.class)))
                .thenReturn(component);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(true);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 10, RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE, true);
        hasFiredEventInstance(PlayerAwardedRestedExperienceEvent.class);
        assertEquals(4, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

    @Test
    public void awardPlayerRestedExperience_awardsNoneAndNotifies_whenExperienceIsMax(McRPGPlayer mcRPGPlayer) {
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
        hasNotFiredEventInstance(PlayerAwardedRestedExperienceEvent.class);
        assertEquals(5f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

    @Test
    public void awardPlayerRestedExperience_awardsNoneAndNotifies_whenExperienceIsAccumulatedAboveMax(McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager mcRPGLocalizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component component = mcRPG.getMiniMessage().deserialize("You have reached the maximum accumulation");
        when(mcRPGLocalizationManager.getLocalizedMessageAsComponent(any(Audience.class), eq(LocalizationKey.MAXIMUM_RESTED_EXPERIENCE_REACHED_MESSAGE)))
                .thenReturn(component);
        addPlayerToServer(mcRPGPlayer);
        PlayerMock playerMock = (PlayerMock) server.getPlayer(mcRPGPlayer.getUUID());
        when(mockConfig.getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)).thenReturn(true);
        when(mockConfig.getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION)).thenReturn(5f);
        assertEquals(0f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        restedExperienceManager.awardRestedExperience(mcRPGPlayer, 40, RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE, true);
        hasNotFiredEventInstance(PlayerAwardedRestedExperienceEvent.class);
        assertEquals(5f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(component, playerMock.nextComponentMessage());
    }

}
