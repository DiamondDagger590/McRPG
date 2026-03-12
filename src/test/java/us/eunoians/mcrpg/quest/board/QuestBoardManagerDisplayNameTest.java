package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestBoardManagerDisplayNameTest extends McRPGBaseTest {

    @Test
    @DisplayName("returns quest definition display name when offering resolves")
    void getOfferingDisplayName_returnsDefinitionDisplayName_whenDefinitionExists() {
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        QuestDefinition definition = mock(QuestDefinition.class);
        BoardOffering offering = createOffering("gen_template_undead_purge_c92b0fa5");
        QuestBoardManager boardManager = mock(QuestBoardManager.class);

        when(boardManager.resolveDefinitionForOffering(offering)).thenReturn(definition);
        when(boardManager.getOfferingDisplayName(mcRPGPlayer, offering)).thenCallRealMethod();
        when(definition.getDisplayName(mcRPGPlayer)).thenReturn("Undead Purge");

        String displayName = boardManager.getOfferingDisplayName(mcRPGPlayer, offering);

        assertEquals("Undead Purge", displayName);
        verify(definition).getDisplayName(mcRPGPlayer);
    }

    @Test
    @DisplayName("falls back to quest definition key when offering does not resolve")
    void getOfferingDisplayName_fallsBackToDefinitionKey_whenDefinitionMissing() {
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        BoardOffering offering = createOffering("gen_template_undead_purge_c92b0fa5");
        QuestBoardManager boardManager = mock(QuestBoardManager.class);

        when(boardManager.resolveDefinitionForOffering(offering)).thenReturn(null);
        when(boardManager.getOfferingDisplayName(mcRPGPlayer, offering)).thenCallRealMethod();

        String displayName = boardManager.getOfferingDisplayName(mcRPGPlayer, offering);

        assertEquals("gen_template_undead_purge_c92b0fa5", displayName);
        verify(boardManager).resolveDefinitionForOffering(offering);
    }

    private BoardOffering createOffering(String questDefinitionKey) {
        return new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "default_board"),
                0,
                new NamespacedKey("mcrpg", questDefinitionKey),
                new NamespacedKey("mcrpg", "common"),
                "personal",
                Duration.ofDays(1)
        );
    }
}
