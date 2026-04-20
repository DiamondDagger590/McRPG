package us.eunoians.mcrpg.display.hud.content;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.display.hud.ActionBarCenterContent;
import us.eunoians.mcrpg.display.hud.FontWidthTable;

import java.util.Optional;

/**
 * {@link ActionBarCenterContent} that renders a static {@link Component} until
 * the current server tick reaches {@code expiryTick}, after which it renders
 * empty so the owning slot is evicted.
 * <p>
 * Used for short-lived transient messages such as XP gains, "Not Enough Mana",
 * and safe-zone notifications.
 */
public final class TimedCenterContent implements ActionBarCenterContent {

    private final Component content;
    private final long expiryTick;
    /**
     * Cached pixel width of {@link #content}. Resolved on first query and
     * reused for the lifetime of this instance since the backing component is
     * immutable. Sentinel {@code -1} means "not yet computed".
     */
    private int cachedWidth = -1;

    /**
     * @param content    The component to display.
     * @param expiryTick The server tick at which this content expires.
     */
    public TimedCenterContent(@NotNull Component content, long expiryTick) {
        this.content = content;
        this.expiryTick = expiryTick;
    }

    /**
     * @return The component rendered by this content.
     */
    @NotNull
    public Component getContent() {
        return content;
    }

    /**
     * @return The server tick at which this content expires.
     */
    public long getExpiryTick() {
        return expiryTick;
    }

    @NotNull
    @Override
    public Optional<Component> render(long currentTick) {
        if (currentTick >= expiryTick) {
            return Optional.empty();
        }
        return Optional.of(content);
    }

    @Override
    public int getPixelWidth(@NotNull FontWidthTable widths, long currentTick) {
        if (currentTick >= expiryTick) {
            return 0;
        }
        if (cachedWidth < 0) {
            cachedWidth = widths.getWidth(content);
        }
        return cachedWidth;
    }
}
