package us.eunoians.mcrpg.event.combat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.log.BroadcastMessagePunishment;
import us.eunoians.mcrpg.combat.log.CombatLogPunishmentType;
import us.eunoians.mcrpg.combat.log.DropItemsPunishment;
import us.eunoians.mcrpg.combat.log.KillOnLogoutPunishment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatLogPunishmentEvent")
class CombatLogPunishmentEventTest extends McRPGBaseTest {

    private final KillOnLogoutPunishment killOnLogout = new KillOnLogoutPunishment((NamespacedKey) null);
    private final DropItemsPunishment dropItems = new DropItemsPunishment((NamespacedKey) null);
    private final BroadcastMessagePunishment broadcastMessage = new BroadcastMessagePunishment((NamespacedKey) null);

    private CombatSession session() {
        return new CombatSession(UUID.randomUUID(), 16, 8000L);
    }

    private Map<CombatLogPunishmentType, Boolean> defaultMap() {
        Map<CombatLogPunishmentType, Boolean> map = new LinkedHashMap<>();
        map.put(killOnLogout, true);
        map.put(dropItems, true);
        map.put(broadcastMessage, false);
        return map;
    }

    @Test
    @DisplayName("constructor stores player, session, combatType, and initial punishment map")
    void constructor_storesAllFields() {
        var player = server.addPlayer();
        CombatSession session = session();

        CombatLogPunishmentEvent event = new CombatLogPunishmentEvent(player, session, CombatType.PVP, defaultMap());

        assertSame(player, event.getPlayer());
        assertSame(session, event.getSession());
        assertEquals(CombatType.PVP, event.getCombatType());
        assertTrue(event.isPunishmentEnabled(killOnLogout));
        assertTrue(event.isPunishmentEnabled(dropItems));
        assertFalse(event.isPunishmentEnabled(broadcastMessage));
    }

    @Test
    @DisplayName("setPunishmentEnabled modifies the enabled state for a specific type")
    void setPunishmentEnabled_modifiesState() {
        var player = server.addPlayer();
        CombatLogPunishmentEvent event = new CombatLogPunishmentEvent(player, session(), CombatType.PVP, defaultMap());

        event.setPunishmentEnabled(dropItems, false);

        assertFalse(event.isPunishmentEnabled(dropItems));
    }

    @Test
    @DisplayName("getEnabledPunishments returns only enabled types")
    void getEnabledPunishments_returnsOnlyEnabled() {
        var player = server.addPlayer();
        CombatLogPunishmentEvent event = new CombatLogPunishmentEvent(player, session(), CombatType.PVP, defaultMap());

        List<CombatLogPunishmentType> enabled = event.getEnabledPunishments();

        assertEquals(2, enabled.size());
        assertTrue(enabled.contains(killOnLogout));
        assertTrue(enabled.contains(dropItems));
        assertFalse(enabled.contains(broadcastMessage));
    }

    @Test
    @DisplayName("hasAnyPunishment returns true when at least one punishment is enabled")
    void hasAnyPunishment_true_whenAtLeastOneEnabled() {
        var player = server.addPlayer();
        CombatLogPunishmentEvent event = new CombatLogPunishmentEvent(player, session(), CombatType.PVP, defaultMap());

        assertTrue(event.hasAnyPunishment());
    }

    @Test
    @DisplayName("hasAnyPunishment returns false when all punishments are disabled")
    void hasAnyPunishment_false_whenAllDisabled() {
        var player = server.addPlayer();
        Map<CombatLogPunishmentType, Boolean> allDisabled = new LinkedHashMap<>();
        allDisabled.put(killOnLogout, false);
        allDisabled.put(dropItems, false);
        allDisabled.put(broadcastMessage, false);
        CombatLogPunishmentEvent event = new CombatLogPunishmentEvent(player, session(), CombatType.PVP, allDisabled);

        assertFalse(event.hasAnyPunishment());
    }

    @Test
    @DisplayName("modifying the original map after construction does not affect the event")
    void originalMapModification_doesNotAffectEvent() {
        var player = server.addPlayer();
        Map<CombatLogPunishmentType, Boolean> map = defaultMap();
        CombatLogPunishmentEvent event = new CombatLogPunishmentEvent(player, session(), CombatType.PVP, map);

        map.put(broadcastMessage, true);

        assertFalse(event.isPunishmentEnabled(broadcastMessage));
    }

    @Test
    @DisplayName("getHandlerList returns a non-null static HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(CombatLogPunishmentEvent.getHandlerList());
    }
}
