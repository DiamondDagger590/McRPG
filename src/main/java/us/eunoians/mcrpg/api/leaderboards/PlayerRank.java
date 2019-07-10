package us.eunoians.mcrpg.api.leaderboards;

import lombok.Getter;

public class PlayerRank {

    @Getter
    private int rank;
    @Getter
    private long lastTimeUpdated;

    public PlayerRank(int rank, long lastTimeUpdated){
        this.rank = rank;
        this.lastTimeUpdated = lastTimeUpdated;
    }
}
