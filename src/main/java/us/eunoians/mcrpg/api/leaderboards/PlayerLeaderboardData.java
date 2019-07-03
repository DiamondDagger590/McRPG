package us.eunoians.mcrpg.api.leaderboards;

import lombok.Getter;

import java.util.UUID;

public class PlayerLeaderboardData {

    @Getter
    private UUID UUID;
    @Getter
    private int level;

    public PlayerLeaderboardData(UUID uuid, int level){
        this.level = level;
        this.UUID = uuid;
    }
}
