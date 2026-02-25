package us.eunoians.mcrpg.quest.board.scope;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.enums.FlagTarget;
import me.angeschossen.lands.api.flags.enums.RoleFlagCategory;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.role.Role;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.external.lands.LandScopedBoardAdapter;
import us.eunoians.mcrpg.quest.impl.scope.impl.LandQuestScope;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ScopedBoardAdapterTest extends McRPGBaseTest {

    private LandsIntegration landsIntegration;
    private LandScopedBoardAdapter adapter;
    private RoleFlag mockFlag;

    @BeforeEach
    void setUp() {
        landsIntegration = mock(LandsIntegration.class);
        when(landsIntegration.getLands()).thenReturn(List.of());
        mockFlag = mock(RoleFlag.class);

        try (MockedStatic<RoleFlag> roleFlagStatic = mockStatic(RoleFlag.class)) {
            roleFlagStatic.when(() -> RoleFlag.of(
                    any(LandsIntegration.class),
                    any(FlagTarget.class),
                    any(RoleFlagCategory.class),
                    anyString()
            )).thenReturn(mockFlag);
            adapter = new LandScopedBoardAdapter(landsIntegration);
        }
    }

    @DisplayName("getScopeProviderKey returns the land scope key")
    @Test
    void getScopeProviderKey_returnsLandScopeKey() {
        assertEquals(LandQuestScope.LAND_SCOPE_KEY, adapter.getScopeProviderKey());
    }

    @Nested
    @DisplayName("getAllActiveEntities")
    class GetAllActiveEntities {

        @DisplayName("returns all land names")
        @Test
        void returnsAllLandNames() {
            Land land1 = mockLand("kingdom_alpha", UUID.randomUUID());
            Land land2 = mockLand("kingdom_beta", UUID.randomUUID());
            when(landsIntegration.getLands()).thenReturn(List.of(land1, land2));

            Set<String> entities = adapter.getAllActiveEntities();

            assertEquals(Set.of("kingdom_alpha", "kingdom_beta"), entities);
        }

        @DisplayName("returns empty set when no lands exist")
        @Test
        void returnsEmptyWhenNoLands() {
            when(landsIntegration.getLands()).thenReturn(List.of());

            Set<String> entities = adapter.getAllActiveEntities();

            assertTrue(entities.isEmpty());
        }
    }

    @Nested
    @DisplayName("getMemberEntities")
    class GetMemberEntities {

        @DisplayName("returns lands where player is trusted")
        @Test
        void returnsLandsWherePlayerTrusted() {
            UUID playerUUID = UUID.randomUUID();
            Land trusted = mockLand("trusted_land", UUID.randomUUID());
            when(trusted.isTrusted(playerUUID)).thenReturn(true);
            Land notTrusted = mockLand("other_land", UUID.randomUUID());
            when(notTrusted.isTrusted(playerUUID)).thenReturn(false);
            when(landsIntegration.getLands()).thenReturn(List.of(trusted, notTrusted));

            Set<String> members = adapter.getMemberEntities(playerUUID);

            assertEquals(Set.of("trusted_land"), members);
        }

        @DisplayName("returns empty when player is in no lands")
        @Test
        void returnsEmptyWhenNotMemberOfAny() {
            UUID playerUUID = UUID.randomUUID();
            Land land = mockLand("some_land", UUID.randomUUID());
            when(land.isTrusted(playerUUID)).thenReturn(false);
            when(landsIntegration.getLands()).thenReturn(List.of(land));

            Set<String> members = adapter.getMemberEntities(playerUUID);

            assertTrue(members.isEmpty());
        }
    }

    @Nested
    @DisplayName("canManageQuests")
    class CanManageQuests {

        @DisplayName("land owner can always manage quests")
        @Test
        void landOwner_alwaysTrue() {
            UUID ownerUUID = UUID.randomUUID();
            Land land = mockLandWithRole("kingdom", ownerUUID, ownerUUID, true);
            when(landsIntegration.getLandByName("kingdom")).thenReturn(land);

            assertTrue(adapter.canManageQuests(ownerUUID, "kingdom"));
        }

        @DisplayName("non-owner with manage flag can manage quests")
        @Test
        void nonOwnerWithFlag_canManage() {
            UUID ownerUUID = UUID.randomUUID();
            UUID memberUUID = UUID.randomUUID();
            Land land = mockLandWithRole("kingdom", ownerUUID, memberUUID, true);
            when(landsIntegration.getLandByName("kingdom")).thenReturn(land);

            assertTrue(adapter.canManageQuests(memberUUID, "kingdom"));
        }

        @DisplayName("non-owner without manage flag cannot manage quests")
        @Test
        void nonOwnerWithoutFlag_cannotManage() {
            UUID ownerUUID = UUID.randomUUID();
            UUID memberUUID = UUID.randomUUID();
            Land land = mockLandWithRole("kingdom", ownerUUID, memberUUID, false);
            when(landsIntegration.getLandByName("kingdom")).thenReturn(land);

            assertFalse(adapter.canManageQuests(memberUUID, "kingdom"));
        }

        @DisplayName("non-member cannot manage quests")
        @Test
        void nonMember_cannotManage() {
            UUID ownerUUID = UUID.randomUUID();
            UUID strangerUUID = UUID.randomUUID();
            Land land = mockLand("kingdom", ownerUUID);
            when(land.isTrusted(strangerUUID)).thenReturn(false);
            when(landsIntegration.getLandByName("kingdom")).thenReturn(land);

            assertFalse(adapter.canManageQuests(strangerUUID, "kingdom"));
        }

        @DisplayName("nonexistent land returns false")
        @Test
        void nonexistentLand_returnsFalse() {
            when(landsIntegration.getLandByName("nonexistent")).thenReturn(null);

            assertFalse(adapter.canManageQuests(UUID.randomUUID(), "nonexistent"));
        }
    }

    @Nested
    @DisplayName("getEntityDisplayName")
    class GetEntityDisplayName {

        @DisplayName("returns land name for existing land")
        @Test
        void existingLand_returnsName() {
            Land land = mockLand("kingdom_alpha", UUID.randomUUID());
            when(landsIntegration.getLandByName("kingdom_alpha")).thenReturn(land);

            Optional<String> name = adapter.getEntityDisplayName("kingdom_alpha");

            assertTrue(name.isPresent());
            assertEquals("kingdom_alpha", name.get());
        }

        @DisplayName("returns empty for nonexistent land")
        @Test
        void nonexistentLand_returnsEmpty() {
            when(landsIntegration.getLandByName("nonexistent")).thenReturn(null);

            Optional<String> name = adapter.getEntityDisplayName("nonexistent");

            assertTrue(name.isEmpty());
        }
    }

    @Nested
    @DisplayName("getManageableEntities")
    class GetManageableEntities {

        @DisplayName("returns only lands where player can manage")
        @Test
        void returnsOnlyManageableLands() {
            UUID ownerUUID = UUID.randomUUID();
            UUID memberUUID = UUID.randomUUID();

            Land ownedLand = mockLandWithRole("owned_land", ownerUUID, ownerUUID, false);
            when(landsIntegration.getLandByName("owned_land")).thenReturn(ownedLand);

            Land managedLand = mockLandWithRole("managed_land", UUID.randomUUID(), memberUUID, true);
            when(landsIntegration.getLandByName("managed_land")).thenReturn(managedLand);

            Land unmanageable = mockLandWithRole("unmanageable_land", UUID.randomUUID(), memberUUID, false);
            when(landsIntegration.getLandByName("unmanageable_land")).thenReturn(unmanageable);

            when(landsIntegration.getLands()).thenReturn(List.of(ownedLand, managedLand, unmanageable));

            Set<String> manageable = adapter.getManageableEntities(memberUUID);

            assertEquals(Set.of("managed_land"), manageable);
        }
    }

    private Land mockLand(String name, UUID ownerUUID) {
        Land land = mock(Land.class);
        when(land.getName()).thenReturn(name);
        when(land.getOwnerUID()).thenReturn(ownerUUID);
        when(land.isTrusted(ownerUUID)).thenReturn(true);
        return land;
    }

    private Land mockLandWithRole(String name, UUID ownerUUID, UUID targetPlayerUUID, boolean hasFlag) {
        Land land = mockLand(name, ownerUUID);
        when(land.isTrusted(targetPlayerUUID)).thenReturn(true);

        Role role = mock(Role.class);
        when(role.hasFlag(any(RoleFlag.class))).thenReturn(hasFlag);

        Area defaultArea = mock(Area.class);
        when(defaultArea.getRole(targetPlayerUUID)).thenReturn(role);
        when(land.getDefaultArea()).thenReturn(defaultArea);

        return land;
    }
}
