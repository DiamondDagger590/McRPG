package us.eunoians.mcrpg.api.displays;

import us.eunoians.mcrpg.api.leaderboards.PlayerLeaderboardData;
import us.eunoians.mcrpg.types.LeaderboardType;
import us.eunoians.mcrpg.types.Skills;

import java.util.List;

public interface LeaderboardDisplay {

    LeaderboardType getLeaderboardType();

    List<PlayerLeaderboardData> getStoredData();

    Skills getStoredSkill();

    int getPage();

    void nextPage();

    void previousPage();
}
