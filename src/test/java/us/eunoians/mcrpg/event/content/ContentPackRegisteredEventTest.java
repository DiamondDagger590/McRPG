package us.eunoians.mcrpg.event.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class ContentPackRegisteredEventTest extends McRPGBaseTest {

    @SuppressWarnings("unchecked")
    private final McRPGContentPack<McRPGContent> mockContentPack = mock(McRPGContentPack.class);

    private ContentPackRegisteredEvent event;

    @BeforeEach
    void setUp() {
        event = new ContentPackRegisteredEvent(mockContentPack);
    }

    @Test
    @DisplayName("getContentPack returns constructor content pack")
    void getContentPack_returnsConstructorContentPack() {
        assertSame(mockContentPack, event.getContentPack());
    }

    @Test
    @DisplayName("getHandlers returns non-null")
    void getHandlers_returnsNonNull() {
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("getHandlerList returns same instance as getHandlers")
    void getHandlerList_matchesGetHandlers() {
        assertSame(ContentPackRegisteredEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("getHandlerList returns non-null on static call")
    void getHandlerList_staticCall_returnsNonNull() {
        assertNotNull(ContentPackRegisteredEvent.getHandlerList());
    }
}
