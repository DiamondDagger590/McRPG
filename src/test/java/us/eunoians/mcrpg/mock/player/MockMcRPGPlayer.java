package us.eunoians.mcrpg.mock.player;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

public class MockMcRPGPlayer extends McRPGPlayer {

    public MockMcRPGPlayer(@NotNull PlayerMock player, @NotNull McRPG mcRPG) {
        super(player, mcRPG);
    }
}
