package us.eunoians.mcrpg.api.leaderboards;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A record used for getting data for the McRPG leaderboards.
 */
public record LeaderboardData(@NotNull List<PlayerLeaderboardData> playerLeaderboardData,
                              @NotNull Map<UUID, Integer> playerRankings) {

    /**
     * Gets the {@link List} of {@link PlayerLeaderboardData}, which is an ordered list sorted in order by the highest ranking
     * player all the way to the lowest ranking player
     *
     * @return The {@link List} of {@link PlayerLeaderboardData}, which is an ordered list sorted in order by the highest ranking
     * player all the way to the lowest ranking player
     */
    @Override
    @NotNull
    public Map<UUID, Integer> playerRankings() {
        return playerRankings;
    }

    /**
     * Gets the {@link Map} of all player {@link UUID}'s who are tracked, mapped to an integer representing their ranking
     *
     * @return The {@link Map} of all player {@link UUID}'s who are tracked, mapped to an integer representing their ranking
     */
    @Override
    @NotNull
    public List<PlayerLeaderboardData> playerLeaderboardData() {
        return playerLeaderboardData;
    }
}
