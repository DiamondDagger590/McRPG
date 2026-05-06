package us.eunoians.mcrpg.display.hud;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.display.impl.PlayerDisplay;
import us.eunoians.mcrpg.display.impl.TickablePlayerDisplay;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.display.ActionBarSlotClearEvent;
import us.eunoians.mcrpg.event.display.ActionBarSlotSetEvent;
import us.eunoians.mcrpg.stat.McRPGPlayerStat;
import us.eunoians.mcrpg.stat.instance.PlayerStatData;
import us.eunoians.mcrpg.stat.instance.PlayerStatInstance;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Per-player {@link PlayerDisplay} that owns the action bar HUD and the
 * priority-based center-content slot system that drives it.
 * <p>
 * Every caller that wants to put something in the action bar's center zone —
 * combo dots, cooldown countdowns, XP text, safe-zone flashes, third-party
 * integrations — writes into this display via
 * {@link #setSlot(int, ActionBarCenterContent)}. On each HUD tick the display
 * walks its slot map from highest priority to lowest; the first slot whose
 * {@link ActionBarCenterContent#render(long)} returns a value wins, and any
 * slots that return empty along the way are evicted (firing
 * {@link ActionBarSlotClearEvent}). The resolved component is passed to the
 * shared {@link ActionBarHudRenderer} and sent to the player's audience.
 * <p>
 * When the server owner disables the persistent HP/mana display, this
 * renders center content only. It also sends a single empty component on the
 * transition frame so any stale HUD line from the enabled-mode pipeline is
 * cleared before letting Minecraft's natural auto-fade take over.
 */
public class ActionBarHudDisplay extends PlayerDisplay implements TickablePlayerDisplay {

    private final ActionBarHudRenderer renderer;
    private final TreeMap<Integer, ActionBarCenterContent> slots =
            new TreeMap<>(Comparator.reverseOrder());
    private boolean lastFrameSent;

    /**
     * @param mcRPGPlayer The player this HUD is attached to.
     * @param renderer    Shared renderer responsible for composing the output
     *                    component. Typically sourced from
     *                    {@code DisplayManager#getHudRenderer()}.
     */
    public ActionBarHudDisplay(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ActionBarHudRenderer renderer) {
        super(mcRPGPlayer);
        this.renderer = renderer;
    }

    /**
     * Writes {@code content} into {@code priority}. Fires {@link ActionBarSlotSetEvent}
     * before the write; listeners may cancel the event to veto the change or
     * replace the content via {@link ActionBarSlotSetEvent#setNewContent(ActionBarCenterContent)}.
     *
     * @param priority The slot priority to write. Higher values win against
     *                 lower values during resolve.
     * @param content  The content to write.
     */
    public void setSlot(int priority, @NotNull ActionBarCenterContent content) {
        ActionBarCenterContent previous = slots.get(priority);
        ActionBarSlotSetEvent event = new ActionBarSlotSetEvent(getMcRPGPlayer(), priority, previous, content);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        slots.put(priority, event.getNewContent());
    }

    /**
     * Removes the content at {@code priority} (if any) and fires
     * {@link ActionBarSlotClearEvent}.
     *
     * @param priority The slot priority to clear.
     */
    public void clearSlot(int priority) {
        ActionBarCenterContent removed = slots.remove(priority);
        if (removed != null) {
            Bukkit.getPluginManager().callEvent(new ActionBarSlotClearEvent(getMcRPGPlayer(), priority, removed));
        }
    }

    /**
     * @param priority The slot priority to inspect.
     * @return An {@link Optional} containing the current slot content, or
     * empty if the slot is unoccupied.
     */
    @NotNull
    public Optional<ActionBarCenterContent> getSlot(int priority) {
        return Optional.ofNullable(slots.get(priority));
    }

    /**
     * @return An unmodifiable view of the slot map, highest priority first.
     * Primarily exposed for testing.
     */
    @NotNull
    public Map<Integer, ActionBarCenterContent> getSlots() {
        return Collections.unmodifiableMap(slots);
    }

    /**
     * Walks slots from highest priority to lowest, returning the first non-
     * empty render and evicting any slot that renders empty along the way.
     * Eviction fires {@link ActionBarSlotClearEvent}.
     *
     * @param currentTick The current server tick.
     * @return An {@link Optional} containing the winning component, or empty
     * if no slot has active content.
     */
    @NotNull
    public Optional<Component> resolveCenter(long currentTick) {
        ActionBarCenterContent winner = drainToWinner(currentTick);
        return (winner != null) ? winner.render(currentTick) : Optional.empty();
    }

    /**
     * Walks slots from highest priority to lowest, evicting any that have
     * rendered empty and returning the first content that renders non-empty,
     * or {@code null} if none remain. Factored out of {@link #resolveCenter}
     * so the HUD tick path can ask the winner for its cached pixel width
     * ({@link ActionBarCenterContent#getPixelWidth(FontWidthTable, long)})
     * without losing access to the source content.
     */
    @Nullable
    private ActionBarCenterContent drainToWinner(long currentTick) {
        Iterator<Map.Entry<Integer, ActionBarCenterContent>> it = slots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ActionBarCenterContent> entry = it.next();
            if (entry.getValue().render(currentTick).isPresent()) {
                return entry.getValue();
            }
            it.remove();
            Bukkit.getPluginManager().callEvent(
                    new ActionBarSlotClearEvent(getMcRPGPlayer(), entry.getKey(), entry.getValue()));
        }
        return null;
    }

    @Override
    public void tick(long currentTick, double secondsElapsed) {
        McRPGPlayer mcRPGPlayer = getMcRPGPlayer();
        Optional<Player> playerOpt = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOpt.isEmpty()) {
            return;
        }
        Player player = playerOpt.get();
        PlayerStatData statData = mcRPGPlayer.getPlayerStatData();
        statData.tickRegen(secondsElapsed);

        ActionBarCenterContent winner = drainToWinner(currentTick);
        Component center = (winner != null) ? winner.render(currentTick).orElse(null) : null;

        if (renderer.isPersistentPoolDisplayEnabled()) {
            int healthCurrent = (int) Math.round(player.getHealth());
            int healthMax = (int) Math.round(
                    player.getAttribute(Attribute.MAX_HEALTH) != null
                            ? player.getAttribute(Attribute.MAX_HEALTH).getValue()
                            : 20);
            String healthSymbol = getDisplaySymbol(statData, McRPGPlayerStat.HEALTH, "❤");
            int manaCurrent = getStatCurrent(statData, McRPGPlayerStat.MANA);
            int manaMax = getStatMax(statData, McRPGPlayerStat.MANA);
            String manaSymbol = getDisplaySymbol(statData, McRPGPlayerStat.MANA, "✦");
            int centerWidth = (winner != null) ? winner.getPixelWidth(renderer.getFontWidthTable(), currentTick) : 0;
            Component hud = renderer.buildFull(
                    healthCurrent, healthMax, healthSymbol,
                    manaCurrent, manaMax, manaSymbol,
                    center, centerWidth
            );
            send(player, hud);
            lastFrameSent = true;
        } else if (center != null) {
            send(player, renderer.buildCenterOnly(center));
            lastFrameSent = true;
        } else if (lastFrameSent) {
            send(player, Component.empty());
            lastFrameSent = false;
        }
    }

    @Override
    public void cleanDisplay() {
        slots.clear();
        lastFrameSent = false;
    }

    private void send(@NotNull Player player, @NotNull Component component) {
        Audience audience = player;
        audience.sendActionBar(component);
    }

    /**
     * Returns the display symbol from the stat's registered definition using the
     * player's locale, falling back to {@code fallback} if the stat is not present
     * in the player's stat data.
     *
     * @param statData The player stat data container.
     * @param stat     The built-in stat enum entry to look up.
     * @param fallback The fallback symbol if the stat is absent.
     * @return The display symbol.
     */
    @NotNull
    private String getDisplaySymbol(@NotNull PlayerStatData statData,
                                     @NotNull McRPGPlayerStat stat,
                                     @NotNull String fallback) {
        return statData.getInstance(stat.getKey())
                .map(instance -> instance.getDefinition().getDisplaySymbol(getMcRPGPlayer()))
                .orElse(fallback);
    }

    /**
     * Returns the effective maximum for the given resource-pool stat, rounded to int.
     *
     * @param statData The player stat data container.
     * @param stat     The built-in stat enum entry to look up.
     * @return The rounded effective max, or 0 if absent.
     */
    private int getStatMax(@NotNull PlayerStatData statData, @NotNull McRPGPlayerStat stat) {
        return statData.getInstance(stat.getKey())
                .map(PlayerStatInstance::getEffectiveMax)
                .map(d -> (int) Math.round(d))
                .orElse(0);
    }

    /**
     * Returns the current value for the given resource-pool stat, rounded to int.
     *
     * @param statData The player stat data container.
     * @param stat     The built-in stat enum entry to look up.
     * @return The rounded current value, or 0 if absent.
     */
    private int getStatCurrent(@NotNull PlayerStatData statData, @NotNull McRPGPlayerStat stat) {
        return statData.getInstance(stat.getKey())
                .map(PlayerStatInstance::getCurrent)
                .map(d -> (int) Math.round(d))
                .orElse(0);
    }

    /**
     * @param priority     The slot priority to inspect.
     * @param defaultValue The value to return if the slot is unoccupied.
     * @return The current slot content or {@code defaultValue}.
     */
    @Nullable
    public ActionBarCenterContent getSlotOrDefault(int priority, @Nullable ActionBarCenterContent defaultValue) {
        return slots.getOrDefault(priority, defaultValue);
    }
}
