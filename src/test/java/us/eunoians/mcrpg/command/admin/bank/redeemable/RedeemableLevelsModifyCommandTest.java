package us.eunoians.mcrpg.command.admin.bank.redeemable;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
public class RedeemableLevelsModifyCommandTest extends McRPGBaseTest {

    private McRPGLocalizationManager localizationManager;

    @BeforeEach
    public void setup() {
        localizationManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
    }

    @DisplayName("Given a player, when redeemable levels are granted, then the player receives the levels and a success message is sent")
    @Test
    public void giveRedeemableLevels_sendsSuccessMessage_whenLevelsGranted(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Component message = miniMessage.deserialize("You got redeemable levels");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.GIVE_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE), any())).thenReturn(message);
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        RedeemableLevelsModifyCommand.giveRedeemableLevels(mcRPGPlayer, 5, Map.of());
        assertEquals(5, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        assertEquals(message, player.nextComponentMessage());
    }

    @DisplayName("Given a player with existing redeemable levels, when levels are removed, then the levels decrease and a success message is sent")
    @Test
    public void removeRedeemableLevels_sendsSuccessMessage_whenLevelsRemoved(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableLevels(5);
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Component message = miniMessage.deserialize("You lost redeemable levels");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REMOVE_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE), any())).thenReturn(message);
        assertEquals(5, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        RedeemableLevelsModifyCommand.removeRedeemableLevels(mcRPGPlayer, 5, Map.of());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        assertEquals(message, player.nextComponentMessage());
    }

    @DisplayName("Given a player with fewer redeemable levels than the removal amount, when levels are removed, then the balance is set to zero and a success message is sent")
    @Test
    public void removeRedeemableLevels_setsToZero_whenRemovalExceedsBalance(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableLevels(5);
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Component message = miniMessage.deserialize("You lost redeemable levels");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.REMOVE_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE), any())).thenReturn(message);
        assertEquals(5, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        RedeemableLevelsModifyCommand.removeRedeemableLevels(mcRPGPlayer, 10, Map.of());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        assertEquals(message, player.nextComponentMessage());
    }

    @DisplayName("Given a player with redeemable levels, when the levels are reset, then the balance becomes zero and a success message is sent")
    @Test
    public void resetRedeemableLevels_sendsSuccessMessage_whenLevelsReset(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.getExperienceExtras().setRedeemableLevels(5);
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Component message = miniMessage.deserialize("Your levels are reset.");
        when(localizationManager.getLocalizedMessageAsComponent(eq(player), eq(LocalizationKey.RESET_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE), any())).thenReturn(message);
        assertEquals(5, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        RedeemableLevelsModifyCommand.resetRedeemableLevels(mcRPGPlayer, Map.of());
        assertEquals(0, mcRPGPlayer.getExperienceExtras().getRedeemableLevels());
        assertEquals(message, player.nextComponentMessage());
    }
}
