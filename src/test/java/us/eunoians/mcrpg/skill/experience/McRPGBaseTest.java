package us.eunoians.mcrpg.skill.experience;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

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
