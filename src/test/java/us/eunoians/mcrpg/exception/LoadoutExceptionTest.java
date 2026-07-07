package us.eunoians.mcrpg.exception;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.exception.entity.SkillHolderMissingSkillException;
import us.eunoians.mcrpg.exception.loadout.LoadoutMaxSizeExceededException;
import us.eunoians.mcrpg.exception.loadout.SelectedLoadoutAboveMaxException;
import us.eunoians.mcrpg.loadout.Loadout;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

@DisplayName("Entity and Loadout Exceptions")
class LoadoutExceptionTest extends McRPGBaseTest {

    @Nested
    @DisplayName("LoadoutMaxSizeExceededException")
    class LoadoutMaxSizeExceededExceptionTests {

        @Test
        @DisplayName("no-message constructor preserves loadout")
        void noMessageConstructor_preservesLoadout() {
            Loadout loadout = new Loadout(UUID.randomUUID(), 0);
            LoadoutMaxSizeExceededException ex = new LoadoutMaxSizeExceededException(loadout);

            assertSame(loadout, ex.getLoadout());
            assertNull(ex.getMessage());
        }

        @Test
        @DisplayName("message constructor preserves loadout and message")
        void messageConstructor_preservesLoadoutAndMessage() {
            Loadout loadout = new Loadout(UUID.randomUUID(), 1);
            String message = "Loadout is full";
            LoadoutMaxSizeExceededException ex = new LoadoutMaxSizeExceededException(loadout, message);

            assertSame(loadout, ex.getLoadout());
            assertEquals(message, ex.getMessage());
        }

        @Test
        @DisplayName("extends RuntimeException")
        void extendsRuntimeException() {
            Loadout loadout = new Loadout(UUID.randomUUID(), 0);
            LoadoutMaxSizeExceededException ex = new LoadoutMaxSizeExceededException(loadout);
            assertInstanceOf(RuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("SelectedLoadoutAboveMaxException")
    class SelectedLoadoutAboveMaxExceptionTests {

        @Test
        @DisplayName("preserves holder and slot")
        void preservesHolderAndSlot() {
            LoadoutHolder holder = mock(LoadoutHolder.class);
            SelectedLoadoutAboveMaxException ex = new SelectedLoadoutAboveMaxException(holder, 5);

            assertSame(holder, ex.getLoadoutHolder());
            assertEquals(5, ex.getLoadoutSlot());
        }

        @Test
        @DisplayName("zero slot is valid")
        void zeroSlot_isValid() {
            LoadoutHolder holder = mock(LoadoutHolder.class);
            SelectedLoadoutAboveMaxException ex = new SelectedLoadoutAboveMaxException(holder, 0);

            assertEquals(0, ex.getLoadoutSlot());
        }

        @Test
        @DisplayName("negative slot is preserved")
        void negativeSlot_isPreserved() {
            LoadoutHolder holder = mock(LoadoutHolder.class);
            SelectedLoadoutAboveMaxException ex = new SelectedLoadoutAboveMaxException(holder, -1);

            assertEquals(-1, ex.getLoadoutSlot());
        }

        @Test
        @DisplayName("extends RuntimeException")
        void extendsRuntimeException() {
            LoadoutHolder holder = mock(LoadoutHolder.class);
            SelectedLoadoutAboveMaxException ex = new SelectedLoadoutAboveMaxException(holder, 0);
            assertInstanceOf(RuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("SkillHolderMissingSkillException")
    class SkillHolderMissingSkillExceptionTests {

        @Test
        @DisplayName("no-message constructor preserves holder and key")
        void noMessageConstructor_preservesHolderAndKey() {
            SkillHolder holder = mock(SkillHolder.class);
            NamespacedKey skillKey = new NamespacedKey("mcrpg", "swords");

            SkillHolderMissingSkillException ex = new SkillHolderMissingSkillException(holder, skillKey);

            assertSame(holder, ex.getSkillHolder());
            assertEquals(skillKey, ex.getSkillKey());
            assertNull(ex.getMessage());
        }

        @Test
        @DisplayName("message constructor preserves holder, key, and message")
        void messageConstructor_preservesAll() {
            SkillHolder holder = mock(SkillHolder.class);
            NamespacedKey skillKey = new NamespacedKey("mcrpg", "mining");
            String message = "Missing skill data for mining";

            SkillHolderMissingSkillException ex =
                    new SkillHolderMissingSkillException(holder, skillKey, message);

            assertSame(holder, ex.getSkillHolder());
            assertEquals(skillKey, ex.getSkillKey());
            assertEquals(message, ex.getMessage());
        }

        @Test
        @DisplayName("extends RuntimeException")
        void extendsRuntimeException() {
            SkillHolder holder = mock(SkillHolder.class);
            NamespacedKey key = new NamespacedKey("mcrpg", "swords");
            SkillHolderMissingSkillException ex = new SkillHolderMissingSkillException(holder, key);
            assertInstanceOf(RuntimeException.class, ex);
        }
    }
}
