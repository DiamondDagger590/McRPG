package us.eunoians.mcrpg.display.hud.content;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.display.hud.ActionBarCenterContent;
import us.eunoians.mcrpg.display.hud.FontWidthTable;

import java.util.Optional;

/**
 * {@link ActionBarCenterContent} that renders a fixed component forever. The
 * owning caller is responsible for explicitly clearing the slot — this content
 * never self-evicts.
 * <p>
 * Used for ongoing state displays like combo progress dots, which should
 * persist until the combo resets, completes, or times out.
 */
public final class IndefiniteCenterContent implements ActionBarCenterContent {

    private final Component content;
    /**
     * Cached pixel width of {@link #content}. Resolved on first query and
     * reused for the lifetime of this instance since the backing component is
     * immutable. Sentinel {@code -1} means "not yet computed".
     */
    private int cachedWidth = -1;

    /**
     * @param content The component to display until the slot is cleared.
     */
    public IndefiniteCenterContent(@NotNull Component content) {
        this.content = content;
    }

    /**
     * @return The component rendered by this content.
     */
    @NotNull
    public Component getContent() {
        return content;
    }

    @NotNull
    @Override
    public Optional<Component> render(long currentTick) {
        return Optional.of(content);
    }

    @Override
    public int getPixelWidth(@NotNull FontWidthTable widths, long currentTick) {
        if (cachedWidth < 0) {
            cachedWidth = widths.getWidth(content);
        }
        return cachedWidth;
    }
}
