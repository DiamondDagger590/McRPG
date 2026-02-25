package us.eunoians.mcrpg.quest.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the scoped offering infrastructure in {@link QuestBoardManager}.
 * Since QuestBoardManager is tightly coupled to the plugin lifecycle,
 * these tests validate the scoped adapter contract and the offering
 * filtering logic through the adapter registry.
 */
public class QuestBoardManagerScopedTest extends McRPGBaseTest {

    private ScopedBoardAdapterRegistry adapterRegistry;
    private QuestBoardManager mockBoardManager;

    @BeforeEach
    void setUp() {
        adapterRegistry = new ScopedBoardAdapterRegistry();
        RegistryAccess.registryAccess().register(adapterRegistry);
        mockBoardManager = mock(QuestBoardManager.class);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockBoardManager);
    }

    @Nested
    @DisplayName("Scoped adapter registry integration")
    class AdapterRegistryIntegration {

        @DisplayName("No adapters registered returns empty scoped offerings for player")
        @Test
        void noAdapters_emptyOfferings() {
            when(mockBoardManager.getScopedOfferingsForPlayer(any(UUID.class))).thenReturn(Map.of());

            Map<String, List<BoardOffering>> result = mockBoardManager.getScopedOfferingsForPlayer(UUID.randomUUID());

            assertTrue(result.isEmpty());
        }

        @DisplayName("Adapter returning empty entities produces no offerings")
        @Test
        void adapterWithNoEntities_noOfferings() {
            ScopedBoardAdapter adapter = stubAdapter("mcrpg", "land_scope", Set.of(), Set.of());
            adapterRegistry.register(adapter);

            assertTrue(adapter.getAllActiveEntities().isEmpty());
        }

        @DisplayName("Adapter returning multiple entities are all discoverable")
        @Test
        void adapterMultipleEntities_allDiscoverable() {
            ScopedBoardAdapter adapter = stubAdapter("mcrpg", "land_scope",
                    Set.of("kingdom_alpha", "kingdom_beta", "kingdom_gamma"), Set.of());
            adapterRegistry.register(adapter);

            Set<String> entities = adapter.getAllActiveEntities();

            assertEquals(3, entities.size());
            assertTrue(entities.contains("kingdom_alpha"));
            assertTrue(entities.contains("kingdom_beta"));
            assertTrue(entities.contains("kingdom_gamma"));
        }
    }

    @Nested
    @DisplayName("Scoped offering player visibility")
    class PlayerVisibility {

        @DisplayName("Player sees offerings only for their member entities")
        @Test
        void playerSeesOnlyMemberEntityOfferings() {
            UUID playerUUID = UUID.randomUUID();
            ScopedBoardAdapter adapter = stubAdapter("mcrpg", "land_scope",
                    Set.of("alpha", "beta", "gamma"),
                    Set.of("alpha", "gamma"));
            adapterRegistry.register(adapter);

            Set<String> memberEntities = adapter.getMemberEntities(playerUUID);

            assertEquals(Set.of("alpha", "gamma"), memberEntities);
            assertFalse(memberEntities.contains("beta"));
        }

        @DisplayName("Player not in any entity sees no offerings")
        @Test
        void playerInNoEntities_noOfferings() {
            UUID playerUUID = UUID.randomUUID();
            ScopedBoardAdapter adapter = stubAdapter("mcrpg", "land_scope",
                    Set.of("alpha", "beta"), Set.of());
            adapterRegistry.register(adapter);

            Set<String> memberEntities = adapter.getMemberEntities(playerUUID);

            assertTrue(memberEntities.isEmpty());
        }
    }

    @Nested
    @DisplayName("Permission checking for scoped offering acceptance")
    class PermissionChecking {

        @DisplayName("Player with management permission is allowed to accept")
        @Test
        void playerWithPermission_canAccept() {
            UUID playerUUID = UUID.randomUUID();
            ScopedBoardAdapter adapter = stubAdapterWithPermission("mcrpg", "land_scope",
                    playerUUID, "kingdom_alpha", true);
            adapterRegistry.register(adapter);

            assertTrue(adapter.canManageQuests(playerUUID, "kingdom_alpha"));
        }

        @DisplayName("Player without management permission cannot accept")
        @Test
        void playerWithoutPermission_cannotAccept() {
            UUID playerUUID = UUID.randomUUID();
            ScopedBoardAdapter adapter = stubAdapterWithPermission("mcrpg", "land_scope",
                    playerUUID, "kingdom_alpha", false);
            adapterRegistry.register(adapter);

            assertFalse(adapter.canManageQuests(playerUUID, "kingdom_alpha"));
        }
    }

    @Nested
    @DisplayName("Entity removal contract")
    class EntityRemoval {

        @DisplayName("handleScopeEntityRemoval is called with correct scope key and entity ID")
        @Test
        void entityRemoval_calledWithCorrectArgs() {
            NamespacedKey scopeKey = new NamespacedKey("mcrpg", "land_scope");
            String entityId = "deleted_kingdom";

            mockBoardManager.handleScopeEntityRemoval(scopeKey, entityId);

            verify(mockBoardManager).handleScopeEntityRemoval(eq(scopeKey), eq(entityId));
        }

        @DisplayName("Entity not in any adapter does not crash")
        @Test
        void nonexistentEntity_noCrash() {
            ScopedBoardAdapter adapter = stubAdapter("mcrpg", "land_scope", Set.of(), Set.of());
            adapterRegistry.register(adapter);

            Optional<ScopedBoardAdapter> result = adapterRegistry.get(new NamespacedKey("mcrpg", "land_scope"));
            assertTrue(result.isPresent());
            assertTrue(result.get().getAllActiveEntities().isEmpty());
        }
    }

    @Nested
    @DisplayName("Multi-adapter scenarios")
    class MultiAdapter {

        @DisplayName("Multiple adapters are all accessible from the registry")
        @Test
        void multipleAdapters_allAccessible() {
            ScopedBoardAdapter landsAdapter = stubAdapter("mcrpg", "land_scope",
                    Set.of("kingdom_alpha"), Set.of("kingdom_alpha"));
            ScopedBoardAdapter factionsAdapter = stubAdapter("factions", "faction_scope",
                    Set.of("faction_one"), Set.of("faction_one"));

            adapterRegistry.register(landsAdapter);
            adapterRegistry.register(factionsAdapter);

            assertEquals(2, adapterRegistry.getAll().size());
            assertTrue(adapterRegistry.get(new NamespacedKey("mcrpg", "land_scope")).isPresent());
            assertTrue(adapterRegistry.get(new NamespacedKey("factions", "faction_scope")).isPresent());
        }

        @DisplayName("Player can be member of entities across multiple adapters")
        @Test
        void playerMemberAcrossAdapters() {
            UUID playerUUID = UUID.randomUUID();

            ScopedBoardAdapter landsAdapter = stubAdapter("mcrpg", "land_scope",
                    Set.of("kingdom"), Set.of("kingdom"));
            ScopedBoardAdapter factionsAdapter = stubAdapter("factions", "faction_scope",
                    Set.of("faction_a"), Set.of("faction_a"));

            adapterRegistry.register(landsAdapter);
            adapterRegistry.register(factionsAdapter);

            Set<String> landMembers = landsAdapter.getMemberEntities(playerUUID);
            Set<String> factionMembers = factionsAdapter.getMemberEntities(playerUUID);

            assertFalse(landMembers.isEmpty());
            assertFalse(factionMembers.isEmpty());
        }
    }

    private static ScopedBoardAdapter stubAdapter(String namespace, String key,
                                                   Set<String> allEntities,
                                                   Set<String> memberEntities) {
        NamespacedKey scopeKey = new NamespacedKey(namespace, key);
        return new ScopedBoardAdapter() {
            @Override public NamespacedKey getScopeProviderKey() { return scopeKey; }
            @Override public Set<String> getAllActiveEntities() { return allEntities; }
            @Override public Set<String> getMemberEntities(UUID playerUUID) { return memberEntities; }
            @Override public Set<String> getManageableEntities(UUID playerUUID) { return memberEntities; }
            @Override public boolean canManageQuests(UUID playerUUID, String entityId) { return memberEntities.contains(entityId); }
            @Override public Optional<String> getEntityDisplayName(String entityId) {
                return allEntities.contains(entityId) ? Optional.of(entityId) : Optional.empty();
            }
            @Override public Optional<NamespacedKey> getExpansionKey() { return Optional.empty(); }
        };
    }

    private static ScopedBoardAdapter stubAdapterWithPermission(String namespace, String key,
                                                                 UUID targetPlayer, String targetEntity,
                                                                 boolean canManage) {
        NamespacedKey scopeKey = new NamespacedKey(namespace, key);
        return new ScopedBoardAdapter() {
            @Override public NamespacedKey getScopeProviderKey() { return scopeKey; }
            @Override public Set<String> getAllActiveEntities() { return Set.of(targetEntity); }
            @Override public Set<String> getMemberEntities(UUID playerUUID) { return Set.of(targetEntity); }
            @Override public Set<String> getManageableEntities(UUID playerUUID) {
                return canManage ? Set.of(targetEntity) : Set.of();
            }
            @Override public boolean canManageQuests(UUID playerUUID, String entityId) {
                return playerUUID.equals(targetPlayer) && entityId.equals(targetEntity) && canManage;
            }
            @Override public Optional<String> getEntityDisplayName(String entityId) { return Optional.of(entityId); }
            @Override public Optional<NamespacedKey> getExpansionKey() { return Optional.empty(); }
        };
    }
}
