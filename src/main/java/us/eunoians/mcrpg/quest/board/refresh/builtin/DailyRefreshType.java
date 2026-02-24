package us.eunoians.mcrpg.quest.board.refresh.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.refresh.RefreshType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.ZonedDateTime;

/**
 * Time-based refresh type that triggers once per calendar day in the configured timezone.
 * <p>
 * The epoch value is {@link java.time.LocalDate#toEpochDay()}.
 */
public final class DailyRefreshType extends RefreshType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "daily");

    public DailyRefreshType() {
        super(KEY);
    }

    @Override
    public boolean isTimeBased() {
        return true;
    }

    @Override
    public boolean shouldRefresh(long lastRefreshEpoch, @NotNull ZonedDateTime now) {
        return now.toLocalDate().toEpochDay() > lastRefreshEpoch;
    }
}
