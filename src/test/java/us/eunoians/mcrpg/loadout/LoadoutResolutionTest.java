package us.eunoians.mcrpg.loadout;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LoadoutResolution")
class LoadoutResolutionTest extends McRPGBaseTest {

    private Loadout createLoadout(int slot) {
        return new Loadout(UUID.randomUUID(), slot);
    }

    @Nested
    @DisplayName("Found")
    class FoundTests {

        @Test
        @DisplayName("wraps single loadout")
        void wrapsSingleLoadout() {
            Loadout loadout = createLoadout(0);
            LoadoutResolution resolution = new LoadoutResolution.Found(loadout);

            assertInstanceOf(LoadoutResolution.Found.class, resolution);
            assertEquals(loadout, ((LoadoutResolution.Found) resolution).loadout());
        }

        @Test
        @DisplayName("equals matches same loadout")
        void equals_sameLoadout() {
            Loadout loadout = createLoadout(0);
            LoadoutResolution.Found a = new LoadoutResolution.Found(loadout);
            LoadoutResolution.Found b = new LoadoutResolution.Found(loadout);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("hashCode matches for equal records")
        void hashCode_matchesForEqualRecords() {
            Loadout loadout = createLoadout(0);
            LoadoutResolution.Found a = new LoadoutResolution.Found(loadout);
            LoadoutResolution.Found b = new LoadoutResolution.Found(loadout);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("different loadouts produce different Found instances")
        void differentLoadouts_notEqual() {
            LoadoutResolution.Found a = new LoadoutResolution.Found(createLoadout(0));
            LoadoutResolution.Found b = new LoadoutResolution.Found(createLoadout(1));
            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("Ambiguous")
    class AmbiguousTests {

        @Test
        @DisplayName("wraps multiple loadouts")
        void wrapsMultipleLoadouts() {
            Loadout l1 = createLoadout(0);
            Loadout l2 = createLoadout(1);
            LoadoutResolution resolution = new LoadoutResolution.Ambiguous(List.of(l1, l2));

            assertInstanceOf(LoadoutResolution.Ambiguous.class, resolution);
            List<Loadout> matches = ((LoadoutResolution.Ambiguous) resolution).matches();
            assertEquals(2, matches.size());
            assertTrue(matches.contains(l1));
            assertTrue(matches.contains(l2));
        }

        @Test
        @DisplayName("equals matches same list content")
        void equals_sameListContent() {
            Loadout l1 = createLoadout(0);
            List<Loadout> list = List.of(l1);
            LoadoutResolution.Ambiguous a = new LoadoutResolution.Ambiguous(list);
            LoadoutResolution.Ambiguous b = new LoadoutResolution.Ambiguous(list);
            assertEquals(a, b);
        }
    }

    @Nested
    @DisplayName("NotFound")
    class NotFoundTests {

        @Test
        @DisplayName("is a singleton-like record")
        void isSingletonLikeRecord() {
            LoadoutResolution resolution = new LoadoutResolution.NotFound();
            assertInstanceOf(LoadoutResolution.NotFound.class, resolution);
        }

        @Test
        @DisplayName("two NotFound instances are equal")
        void twoInstances_areEqual() {
            LoadoutResolution.NotFound a = new LoadoutResolution.NotFound();
            LoadoutResolution.NotFound b = new LoadoutResolution.NotFound();
            assertEquals(a, b);
        }

        @Test
        @DisplayName("hashCode is consistent")
        void hashCode_isConsistent() {
            LoadoutResolution.NotFound a = new LoadoutResolution.NotFound();
            LoadoutResolution.NotFound b = new LoadoutResolution.NotFound();
            assertEquals(a.hashCode(), b.hashCode());
        }
    }

    @Nested
    @DisplayName("Sealed interface type discrimination")
    class TypeDiscrimination {

        @Test
        @DisplayName("Found is distinct from Ambiguous and NotFound")
        void found_isDistinctFromOtherVariants() {
            LoadoutResolution found = new LoadoutResolution.Found(createLoadout(0));
            LoadoutResolution ambiguous = new LoadoutResolution.Ambiguous(List.of(createLoadout(0)));
            LoadoutResolution notFound = new LoadoutResolution.NotFound();

            assertInstanceOf(LoadoutResolution.Found.class, found);
            assertInstanceOf(LoadoutResolution.Ambiguous.class, ambiguous);
            assertInstanceOf(LoadoutResolution.NotFound.class, notFound);

            assertNotEquals(found, ambiguous);
            assertNotEquals(found, notFound);
            assertNotEquals(ambiguous, notFound);
        }
    }
}
