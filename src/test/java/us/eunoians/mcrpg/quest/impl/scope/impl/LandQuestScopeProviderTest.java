package us.eunoians.mcrpg.quest.impl.scope.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link LandQuestScopeProvider} factory logic.
 * <p>
 * These tests cover the pure logic of configuring and creating scopes,
 * without requiring Bukkit or registry setup.
 */
class LandQuestScopeProviderTest {

    private LandQuestScopeProvider provider;

    @BeforeEach
    void setUp() {
        provider = new LandQuestScopeProvider();
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("Given a provider, when getKey is called, then returns LAND_SCOPE_KEY")
        void getKey_returnsLandScopeKey() {
            assertEquals(LandQuestScope.LAND_SCOPE_KEY, provider.getKey());
        }
    }

    @Nested
    @DisplayName("getExpansionKey")
    class GetExpansionKey {

        @Test
        @DisplayName("Given a provider, when getExpansionKey is called, then returns a present optional")
        void getExpansionKey_returnsPresent() {
            assertNotNull(provider.getExpansionKey());
            assertTrue(provider.getExpansionKey().isPresent());
        }
    }

    @Nested
    @DisplayName("withLandName")
    class WithLandName {

        @Test
        @DisplayName("Given a provider, when withLandName is called, then returns the same provider for chaining")
        void withLandName_returnsSameProviderForChaining() {
            LandQuestScopeProvider result = provider.withLandName("TestLand");
            assertSame(provider, result);
        }
    }

    @Nested
    @DisplayName("createNewScope")
    class CreateNewScope {

        @Test
        @DisplayName("Given withLandName was called, when createNewScope is called, then returns a scope with the correct land name")
        void createNewScope_returnsScope_withCorrectLandName() {
            UUID questUUID = UUID.randomUUID();
            provider.withLandName("MyLand");

            LandQuestScope scope = provider.createNewScope(questUUID);

            assertNotNull(scope);
            assertEquals(questUUID, scope.getQuestUUID());
            assertEquals("MyLand", scope.getLandName().orElseThrow());
        }

        @Test
        @DisplayName("Given withLandName was NOT called, when createNewScope is called, then throws IllegalStateException")
        void createNewScope_throwsIllegalStateException_whenLandNameNotSet() {
            UUID questUUID = UUID.randomUUID();

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> provider.createNewScope(questUUID));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Given a different land name, when createNewScope is called, then scope reflects that name")
        void createNewScope_reflectsUpdatedLandName() {
            UUID questUUID = UUID.randomUUID();
            provider.withLandName("AnotherLand");

            LandQuestScope scope = provider.createNewScope(questUUID);

            assertEquals("AnotherLand", scope.getLandName().orElseThrow());
        }

        @Test
        @DisplayName("Given createNewScope is called multiple times, then each scope has its own quest UUID")
        void createNewScope_eachScopeHasOwnQuestUUID() {
            UUID questUUID1 = UUID.randomUUID();
            UUID questUUID2 = UUID.randomUUID();
            provider.withLandName("SharedLand");

            LandQuestScope scope1 = provider.createNewScope(questUUID1);
            LandQuestScope scope2 = provider.createNewScope(questUUID2);

            assertEquals(questUUID1, scope1.getQuestUUID());
            assertEquals(questUUID2, scope2.getQuestUUID());
        }
    }
}
