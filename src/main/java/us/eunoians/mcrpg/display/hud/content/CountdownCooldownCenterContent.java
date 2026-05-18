package us.eunoians.mcrpg.display.hud.content;

import com.diamonddagger590.mccore.util.TimeProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.display.hud.ActionBarCenterContent;
import us.eunoians.mcrpg.display.hud.FontWidthTable;

import java.util.Optional;

/**
 * {@link ActionBarCenterContent} that renders a live-countdown cooldown message
 * (e.g. {@code "On Cooldown (3s)"}). The remaining seconds are recomputed every
 * frame against a pluggable {@link TimeProvider}, so the HUD tick rate
 * controls how responsively the countdown ticks down.
 * <p>
 * Renders empty once {@link #expiryEpochMillis} has elapsed, letting the owning
 * slot self-evict and reveal any lower-priority content.
 * <p>
 * An optional {@code maxExpiryTick} can be supplied to impose an early TTL: if the
 * current server tick reaches {@code maxExpiryTick} before the real-time cooldown
 * expires, the content self-evicts anyway. This is used to show a brief countdown
 * flash (e.g. ~3 seconds) without persisting the full cooldown duration on the HUD.
 * Pass {@link Long#MAX_VALUE} (or use the two-argument constructor) to disable the TTL.
 */
public final class CountdownCooldownCenterContent implements ActionBarCenterContent {

    private final String abilityName;
    private final long expiryEpochMillis;
    private final long maxExpiryTick;
    private final TimeProvider timeProvider;

    /**
     * Memoised rendered component for the last {@code remainingSeconds} value
     * resolved by {@link #render(long)}. The countdown only changes visible
     * state once per second yet {@link #render(long)} is invoked every HUD
     * tick (default 10 Hz), so caching by remaining-seconds skips most of the
     * per-frame {@link Component#text(String, net.kyori.adventure.text.format.TextColor)}
     * + String allocation work.
     */
    private long cachedRemainingSeconds = Long.MIN_VALUE;
    private Component cachedComponent;
    private int cachedWidth = -1;

    /**
     * Constructs a countdown that persists until the cooldown's real-time expiry.
     *
     * @param abilityName       The display name of the ability on cooldown,
     *                          currently unused in the rendered string but kept
     *                          for future per-ability formatting.
     * @param expiryEpochMillis The real-time epoch millis at which the cooldown ends.
     * @param timeProvider      Clock used to compute remaining time each frame.
     */
    public CountdownCooldownCenterContent(@NotNull String abilityName,
                                          long expiryEpochMillis,
                                          @NotNull TimeProvider timeProvider) {
        this(abilityName, expiryEpochMillis, Long.MAX_VALUE, timeProvider);
    }

    /**
     * Constructs a countdown that self-evicts at whichever comes first: the cooldown's
     * real-time expiry or the given server-tick TTL.
     *
     * @param abilityName       The display name of the ability on cooldown,
     *                          currently unused in the rendered string but kept
     *                          for future per-ability formatting.
     * @param expiryEpochMillis The real-time epoch millis at which the cooldown ends.
     * @param maxExpiryTick     The server tick at which this content should stop rendering
     *                          even if the cooldown has not yet expired. Use
     *                          {@link Long#MAX_VALUE} to disable the TTL.
     * @param timeProvider      Clock used to compute remaining time each frame.
     */
    public CountdownCooldownCenterContent(@NotNull String abilityName,
                                          long expiryEpochMillis,
                                          long maxExpiryTick,
                                          @NotNull TimeProvider timeProvider) {
        this.abilityName = abilityName;
        this.expiryEpochMillis = expiryEpochMillis;
        this.maxExpiryTick = maxExpiryTick;
        this.timeProvider = timeProvider;
    }

    /**
     * @return The ability name this countdown was created for.
     */
    @NotNull
    public String getAbilityName() {
        return abilityName;
    }

    /**
     * @return The real-time epoch millis at which this cooldown expires.
     */
    public long getExpiryEpochMillis() {
        return expiryEpochMillis;
    }

    @NotNull
    @Override
    public Optional<Component> render(long currentTick) {
        long nowMillis = timeProvider.now().toEpochMilli();
        if (currentTick >= maxExpiryTick || nowMillis >= expiryEpochMillis) {
            return Optional.empty();
        }
        long remainingSeconds = Math.max(1L, (expiryEpochMillis - nowMillis) / 1000L);
        Component rendered = cachedComponent;
        if (rendered == null || remainingSeconds != cachedRemainingSeconds) {
            rendered = Component.text("On Cooldown (" + remainingSeconds + "s)", NamedTextColor.RED);
            cachedRemainingSeconds = remainingSeconds;
            cachedComponent = rendered;
            cachedWidth = -1;
        }
        return Optional.of(rendered);
    }

    @Override
    public int getPixelWidth(@NotNull FontWidthTable widths, long currentTick) {
        // Prime the cache via render() so the width is consistent with what
        // will actually be drawn this tick.
        Optional<Component> rendered = render(currentTick);
        if (rendered.isEmpty()) {
            return 0;
        }
        if (cachedWidth < 0) {
            cachedWidth = widths.getWidth(cachedComponent);
        }
        return cachedWidth;
    }
}
