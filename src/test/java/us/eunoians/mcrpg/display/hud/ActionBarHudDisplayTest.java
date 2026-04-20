package us.eunoians.mcrpg.display.hud;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.display.hud.content.IndefiniteCenterContent;
import us.eunoians.mcrpg.display.hud.content.TimedCenterContent;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.display.ActionBarSlotSetEvent;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link ActionBarHudDisplay}'s priority-slot resolver — the core
 * contract that lets the action bar host multiple overlapping content sources
 * (XP, safe-zone flashes, combo dots, ability feedback) without them stomping
 * each other.
 * <p>
 * We drive the display directly and inspect {@link ActionBarHudDisplay#resolveCenter(long)}
 * rather than going through {@code tick()}, so these tests stay agnostic to
 * whether HP/mana rendering is on or off and focus on the priority semantics
 * in isolation. The MockBukkit server is still required because
 * {@link ActionBarHudDisplay#setSlot(int, ActionBarCenterContent)} and the
 * eviction path fire Bukkit events.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class ActionBarHudDisplayTest extends McRPGBaseTest {

    private ActionBarHudRenderer newRenderer() {
        return newRenderer(true);
    }

    private ActionBarHudRenderer newRenderer(boolean persistentPoolEnabled) {
        YamlDocument doc = mock(YamlDocument.class);
        when(doc.getBoolean(any(Route.class), anyBoolean())).thenReturn(persistentPoolEnabled);
        ReloadableContent<Boolean> flag = new ReloadableContent<>(
                doc,
                Route.fromString("hud.action-bar.persistent-pool-display"),
                (d, r) -> d.getBoolean(r, true));
        return new ActionBarHudRenderer(new MinecraftDefaultFontWidthTable(), flag);
    }

    private String plain(Component component) {
        StringBuilder sb = new StringBuilder();
        ComponentFlattener.basic().flatten(component, sb::append);
        return sb.toString();
    }

    @Test
    @DisplayName("Given multiple active slots at different priorities, when resolveCenter is called, then the highest-priority slot wins")
    void resolveCenter_returnsHighestPriorityContent_whenMultipleSlotsActive(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer());

        Component combo = Component.text("COMBO");
        Component ability = Component.text("COOLDOWN");
        hud.setSlot(CenterContentPriority.COMBO_STATE, new IndefiniteCenterContent(combo));
        hud.setSlot(CenterContentPriority.ABILITY_FEEDBACK, new TimedCenterContent(ability, 100L));

        Optional<Component> resolved = hud.resolveCenter(0L);

        assertTrue(resolved.isPresent());
        assertSame(ability, resolved.get());
    }

    @Test
    @DisplayName("Given a higher-priority timed slot has expired, when resolveCenter is called, then the expired slot is evicted and the lower-priority slot is revealed")
    void resolveCenter_evictsExpiredSlotAndRevealsLower_whenTopSlotExpires(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer());

        Component combo = Component.text("COMBO");
        Component ability = Component.text("COOLDOWN");
        hud.setSlot(CenterContentPriority.COMBO_STATE, new IndefiniteCenterContent(combo));
        hud.setSlot(CenterContentPriority.ABILITY_FEEDBACK, new TimedCenterContent(ability, 100L));

        Optional<Component> resolved = hud.resolveCenter(500L);

        assertTrue(resolved.isPresent());
        assertSame(combo, resolved.get());
        assertFalse(hud.getSlot(CenterContentPriority.ABILITY_FEEDBACK).isPresent(),
                "Expired slot should have been evicted from the priority map");
    }

    @Test
    @DisplayName("Given every active slot has expired, when resolveCenter is called, then it returns empty and all slots are evicted")
    void resolveCenter_returnsEmptyAndClearsAllSlots_whenEverySlotExpired(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer());

        hud.setSlot(CenterContentPriority.AMBIENT_FEEDBACK,
                new TimedCenterContent(Component.text("XP"), 10L));
        hud.setSlot(CenterContentPriority.ABILITY_FEEDBACK,
                new TimedCenterContent(Component.text("COOLDOWN"), 20L));

        Optional<Component> resolved = hud.resolveCenter(100L);

        assertFalse(resolved.isPresent());
        assertTrue(hud.getSlots().isEmpty(), "All expired slots should have been evicted");
    }

    @Test
    @DisplayName("Given two populated slots, when clearSlot is called on one priority, then only the targeted slot is removed")
    void clearSlot_removesTargetedSlotOnly_whenOtherSlotsPopulated(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer());

        hud.setSlot(CenterContentPriority.COMBO_STATE,
                new IndefiniteCenterContent(Component.text("COMBO")));
        hud.setSlot(CenterContentPriority.AMBIENT_FEEDBACK,
                new TimedCenterContent(Component.text("XP"), 1_000L));

        hud.clearSlot(CenterContentPriority.COMBO_STATE);

        assertFalse(hud.getSlot(CenterContentPriority.COMBO_STATE).isPresent());
        assertTrue(hud.getSlot(CenterContentPriority.AMBIENT_FEEDBACK).isPresent());
    }

    @Test
    @DisplayName("Given a listener that cancels ActionBarSlotSetEvent, when setSlot is called, then the previous content remains in place")
    void setSlot_leavesSlotUnchanged_whenSetEventIsCancelled(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer());

        Component original = Component.text("COMBO");
        hud.setSlot(CenterContentPriority.COMBO_STATE, new IndefiniteCenterContent(original));

        Listener canceller = new Listener() {
            @EventHandler
            public void onSet(ActionBarSlotSetEvent event) {
                event.setCancelled(true);
            }
        };
        Bukkit.getPluginManager().registerEvents(canceller, mcRPG);

        try {
            Component replacement = Component.text("NEW");
            hud.setSlot(CenterContentPriority.COMBO_STATE, new IndefiniteCenterContent(replacement));

            Optional<Component> resolved = hud.resolveCenter(0L);
            assertTrue(resolved.isPresent());
            assertSame(original, resolved.get(),
                    "Cancelling the set event should leave the previous content in place");
        } finally {
            // MockBukkit shares its PluginManager across tests in the suite, so
            // a leaked cancel listener would silently break every later setSlot
            // call in this class.
            HandlerList.unregisterAll(canceller);
        }
    }

    @Test
    @DisplayName("Given a listener that swaps the new content via setNewContent, when setSlot is called, then the listener-provided content is written to the slot")
    void setSlot_writesListenerReplacement_whenEventReplacesNewContent(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer());

        Component original = Component.text("ORIGINAL");
        Component replacement = Component.text("REPLACEMENT");
        IndefiniteCenterContent replacementContent = new IndefiniteCenterContent(replacement);

        Listener swapper = new Listener() {
            @EventHandler
            public void onSet(ActionBarSlotSetEvent event) {
                event.setNewContent(replacementContent);
            }
        };
        Bukkit.getPluginManager().registerEvents(swapper, mcRPG);

        try {
            hud.setSlot(CenterContentPriority.ABILITY_FEEDBACK, new TimedCenterContent(original, 100L));

            Optional<ActionBarCenterContent> slot = hud.getSlot(CenterContentPriority.ABILITY_FEEDBACK);
            assertTrue(slot.isPresent());
            assertSame(replacementContent, slot.get(),
                    "Listener's setNewContent replacement should be what lands in the slot");
        } finally {
            HandlerList.unregisterAll(swapper);
        }
    }

    @Test
    @DisplayName("Given the persistent pool display is enabled, when tick is called with active center content, then the full HUD component is sent to the player")
    void tick_sendsFullHudToPlayer_whenPersistentPoolEnabled(McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer(true));
        hud.setSlot(CenterContentPriority.AMBIENT_FEEDBACK,
                new TimedCenterContent(Component.text("XP-GAIN"), 100L));

        hud.tick(0L, 0.1);

        Component sent = player.nextActionBar();
        assertNotNull(sent, "tick should have pushed an action bar frame when persistent pool is enabled");
        String text = plain(sent);
        assertTrue(text.contains("XP-GAIN"),
                "Full HUD frame should include the winning center content; got: " + text);
        assertTrue(text.contains("/"),
                "Full HUD frame should include HP/mana stat zones (expected a '/' separator); got: " + text);
    }

    @Test
    @DisplayName("Given the persistent pool display is disabled, when tick is called with active center content, then only the center content is sent to the player")
    void tick_sendsCenterOnlyFrame_whenPersistentPoolDisabledAndContentPresent(McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer(false));
        hud.setSlot(CenterContentPriority.ABILITY_FEEDBACK,
                new TimedCenterContent(Component.text("COOLDOWN"), 100L));

        hud.tick(0L, 0.1);

        Component sent = player.nextActionBar();
        assertNotNull(sent);
        String text = plain(sent);
        assertTrue(text.contains("COOLDOWN"),
                "Center-only frame should carry the winning content; got: " + text);
        assertFalse(text.contains("/"),
                "Center-only frame should not include HP/mana stat zones; got: " + text);
    }

    @Test
    @DisplayName("Given the persistent pool is disabled and no center content is active, when tick is called for the first time, then no action bar frame is sent")
    void tick_sendsNothing_whenPersistentPoolDisabledAndNoCenterContent(McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer(false));

        hud.tick(0L, 0.1);

        assertNull(player.nextActionBar(),
                "Disabled pool + no center content on a clean HUD should not push any frame");
    }

    @Test
    @DisplayName("Given center content just expired with the persistent pool disabled, when tick is called, then a single empty frame is sent to clear the stale HUD line")
    void tick_sendsEmptyClearFrame_whenPersistentPoolDisabledAndCenterContentJustEvicted(McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        ActionBarHudDisplay hud = new ActionBarHudDisplay(mcRPGPlayer, newRenderer(false));
        hud.setSlot(CenterContentPriority.ABILITY_FEEDBACK,
                new TimedCenterContent(Component.text("COOLDOWN"), 20L));

        hud.tick(0L, 0.1);
        // Drop the first frame — we only care about the transition-to-empty
        // behavior.
        assertNotNull(player.nextActionBar());

        hud.tick(100L, 0.1);
        Component sent = player.nextActionBar();
        assertNotNull(sent, "A clear-frame should be emitted on the tick where content disappears");
        assertEquals("", plain(sent),
                "Clear-frame should be Component.empty() so the vanilla auto-fade takes over");

        hud.tick(101L, 0.1);
        assertNull(player.nextActionBar(),
                "Subsequent empty ticks should not keep emitting clear-frames");
    }
}
