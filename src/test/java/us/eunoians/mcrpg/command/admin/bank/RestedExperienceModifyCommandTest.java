package us.eunoians.mcrpg.command.admin.bank;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class RestedExperienceModifyCommandTest extends McRPGBaseTest {

    private McRPGLocalizationManager localizationManager;

    @BeforeEach
    public void setup() {
        localizationManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
    }

    @Test
    public void giveRestedExperience_sendsSuccessMessage_whenGranted(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Component message = miniMessage.deserialize("You got rested experience");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.GIVE_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE), any())).thenReturn(message);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        RestedExperienceModifyCommand.giveRestedExperience(mcRPGPlayer, 5, Map.of());
        assertEquals(5, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(message, player.nextComponentMessage());
    }

    @Test
    public void removeRestedExperience_sendsSuccessMessage_whenRemoved(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRestedExperience(5);
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Component message = miniMessage.deserialize("You lost rested experience");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REMOVE_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE), any())).thenReturn(message);
        assertEquals(5, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        RestedExperienceModifyCommand.removeRestedExperience(mcRPGPlayer, 5, Map.of());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(message, player.nextComponentMessage());
    }

    @Test
    public void removeRestedExperience_setsToZero_whenRemovalExceedsBalance(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRestedExperience(5);
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Component message = miniMessage.deserialize("You lost rested experience");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REMOVE_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE), any())).thenReturn(message);
        assertEquals(5, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        RestedExperienceModifyCommand.removeRestedExperience(mcRPGPlayer, 10, Map.of());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(message, player.nextComponentMessage());
    }

    @Test
    public void resetRestedExperience_sendsSuccessMessage_whenReset(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRestedExperience(5);
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Component message = miniMessage.deserialize("Your experience is reset.");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.RESET_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE), any())).thenReturn(message);
        assertEquals(5, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        RestedExperienceModifyCommand.resetRestedExperience(mcRPGPlayer, Map.of());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(message, player.nextComponentMessage());
    }
}
