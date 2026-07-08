package us.eunoians.mcrpg.quest.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestMessageDelivererTest extends McRPGBaseTest {

    private McRPGLocalizationManager localizationManager;
    private Logger logger;
    private PlayerMock player;
    private McRPGPlayer mcRPGPlayer;

    @BeforeEach
    public void setup() {
        localizationManager = mock(McRPGLocalizationManager.class);
        logger = mock(Logger.class);
        player = server.addPlayer();
        mcRPGPlayer = mock(McRPGPlayer.class);
    }

    @Test
    @DisplayName("Given a valid locale key, when deliver is called with an McRPGPlayer, then the localized component is sent exactly once")
    public void deliver_sendsLocalizedMessage_whenLocaleKeyResolvesSuccessfully() {
        Component expected = Component.text("Localized quest message");
        when(localizationManager.getLocalizedMessageAsComponent(any(McRPGPlayer.class), any()))
                .thenReturn(expected);

        QuestMessageDeliverer deliverer = new QuestMessageDeliverer(localizationManager, MiniMessage.miniMessage(), logger);
        deliverer.deliver(player, mcRPGPlayer, "quest.tutorial.welcome", List.of("<bold>Fallback</bold>"));

        assertEquals(expected, player.nextComponentMessage());
        assertNull(player.nextComponentMessage());
    }

    @Test
    @DisplayName("Given a locale key that fails resolution, when deliver is called, then inline messages are parsed and sent as fallback")
    public void deliver_fallsBackToInline_whenLocaleKeyResolutionFails() {
        when(localizationManager.getLocalizedMessageAsComponent(any(McRPGPlayer.class), any()))
                .thenThrow(new RuntimeException("No locale found"));

        QuestMessageDeliverer deliverer = new QuestMessageDeliverer(localizationManager, MiniMessage.miniMessage(), logger);
        deliverer.deliver(player, mcRPGPlayer, "quest.missing.key", List.of("<bold>Hello</bold>"));

        Component expectedInline = MiniMessage.miniMessage().deserialize("<bold>Hello</bold>");
        assertEquals(expectedInline, player.nextComponentMessage());
    }

    @Test
    @DisplayName("Given no locale key provided, when deliver is called, then inline messages are parsed and sent directly")
    public void deliver_sendsInlineMessages_whenNoLocaleKeyProvided() {
        QuestMessageDeliverer deliverer = new QuestMessageDeliverer(localizationManager, MiniMessage.miniMessage(), logger);
        deliverer.deliver(player, mcRPGPlayer, (String) null, List.of("<bold>Welcome</bold>", "<italic>Enjoy</italic>"));

        Component first = MiniMessage.miniMessage().deserialize("<bold>Welcome</bold>");
        Component second = MiniMessage.miniMessage().deserialize("<italic>Enjoy</italic>");
        assertEquals(first, player.nextComponentMessage());
        assertEquals(second, player.nextComponentMessage());
    }

    @Test
    @DisplayName("Given an inline message that fails parsing, when deliver is called, then the failed message is skipped, subsequent messages are sent, and a warning is logged")
    public void deliver_logsWarningAndContinues_whenInlineParsingFails() {
        MiniMessage mockMiniMessage = mock(MiniMessage.class);
        Component validComponent = Component.text("Valid");

        when(mockMiniMessage.deserialize("bad-message"))
                .thenThrow(new RuntimeException("Parse failure"));
        when(mockMiniMessage.deserialize("good-message"))
                .thenReturn(validComponent);

        QuestMessageDeliverer deliverer = new QuestMessageDeliverer(localizationManager, mockMiniMessage, logger);
        deliverer.deliver(player, null, (String) null, List.of("bad-message", "good-message"));

        assertEquals(validComponent, player.nextComponentMessage());
        assertNull(player.nextComponentMessage());
        verify(logger).log(eq(Level.WARNING), anyString(), any(Exception.class));
    }
}
