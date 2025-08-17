package us.eunoians.mcrpg.skill.experience;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * A base test that provides some utility methods as well as
 * integration with {@link org.mockbukkit.mockbukkit.MockBukkit}.
 * <p>
 * By extending this class, you should have access to the various mocking functionality
 * provided by MockBukkit.
 */
@ExtendWith(MockBukkitExtension.class)
public class McRPGBaseTest {

    @MockBukkitInject
    protected ServerMock server;

    @NotNull
    public PlayerMock addPlayerToServer(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock playerMock = new PlayerMock(server, "Verum", mcRPGPlayer.getUUID());
        server.addPlayer(playerMock);
        return playerMock;
    }
}
