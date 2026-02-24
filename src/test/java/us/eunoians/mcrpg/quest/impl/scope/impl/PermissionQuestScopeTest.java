package us.eunoians.mcrpg.quest.impl.scope.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermissionQuestScopeTest extends McRPGBaseTest {

    private PermissionQuestScope scope;
    private UUID questUUID;

    @BeforeEach
    public void setup() {
        questUUID = UUID.randomUUID();
        scope = new PermissionQuestScope(questUUID, null);
    }

    @DisplayName("Given a new scope, when setting permission node once, then it succeeds")
    @Test
    public void setPermissionNode_succeedsOnce() {
        scope.setPermissionNode("mcrpg.quest.test");
        assertTrue(scope.getPermissionNode().isPresent());
    }

    @DisplayName("Given a scope with permission set, when setting again, then it throws IllegalStateException")
    @Test
    public void setPermissionNode_throwsOnSecondCall() {
        scope.setPermissionNode("mcrpg.quest.test");
        assertThrows(IllegalStateException.class, () -> scope.setPermissionNode("mcrpg.quest.other"));
    }

    @DisplayName("Given a scope without permission, when checking isScopeValid, then it returns false")
    @Test
    public void isScopeValid_returnsFalse_whenPermissionNull() {
        assertFalse(scope.isScopeValid());
    }

    @DisplayName("Given a scope with permission, when checking isScopeValid, then it returns true")
    @Test
    public void isScopeValid_returnsTrue_whenPermissionSet() {
        scope.setPermissionNode("mcrpg.quest.test");
        assertTrue(scope.isScopeValid());
    }

    @DisplayName("Given a scope with permission, when online player has the permission, then isPlayerInScope returns true")
    @Test
    public void isPlayerInScope_returnsTrue_whenOnlinePlayerHasPermission() {
        scope.setPermissionNode("mcrpg.quest.test");
        PlayerMock player = server.addPlayer();
        player.addAttachment(mcRPG, "mcrpg.quest.test", true);
        assertTrue(scope.isPlayerInScope(player.getUniqueId()));
    }

    @DisplayName("Given a scope with permission, when online player lacks permission, then isPlayerInScope returns false")
    @Test
    public void isPlayerInScope_returnsFalse_whenOnlinePlayerLacksPermission() {
        scope.setPermissionNode("mcrpg.quest.test");
        PlayerMock player = server.addPlayer();
        assertFalse(scope.isPlayerInScope(player.getUniqueId()));
    }

    @DisplayName("Given a scope without permission, when getting players, then it returns empty set")
    @Test
    public void getCurrentPlayersInScope_returnsEmpty_whenPermissionNull() {
        assertTrue(scope.getCurrentPlayersInScope().isEmpty());
    }

    @DisplayName("Given a scope with permission, when getting players, then only matching online players are included")
    @Test
    public void getCurrentPlayersInScope_filtersOnlinePlayersByPermission() {
        scope.setPermissionNode("mcrpg.quest.test");
        PlayerMock playerWith = server.addPlayer();
        playerWith.addAttachment(mcRPG, "mcrpg.quest.test", true);
        server.addPlayer();
        assertTrue(scope.getCurrentPlayersInScope().contains(playerWith.getUniqueId()));
        assertTrue(scope.getCurrentPlayersInScope().size() >= 1);
    }

    @DisplayName("Given an offline player without Vault, when checking scope, then it returns false")
    @Test
    public void isPlayerInScope_returnsFalse_forOfflinePlayerWithoutVault() {
        scope.setPermissionNode("mcrpg.quest.test");
        assertFalse(scope.isPlayerInScope(UUID.randomUUID()));
    }
}
