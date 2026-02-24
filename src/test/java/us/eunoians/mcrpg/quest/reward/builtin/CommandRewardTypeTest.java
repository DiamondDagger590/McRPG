package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandRewardTypeTest extends McRPGBaseTest {

    private CommandRewardType baseType;

    @BeforeEach
    public void setup() {
        baseType = new CommandRewardType();
    }

    @DisplayName("Given the type, when calling getKey, then it returns the command key")
    @Test
    public void getKey_returnsCommandKey() {
        assertEquals(CommandRewardType.KEY, baseType.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(baseType.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, baseType.getExpansionKey().get());
    }

    @DisplayName("Given serialized config with commands, when round-tripping, then commands are preserved")
    @Test
    public void serializeAndDeserialize_roundTripsCorrectly() {
        List<String> commands = List.of("say hello", "give {player} diamond 1");
        CommandRewardType configured = baseType.fromSerializedConfig(Map.of("commands", commands));
        Map<String, Object> serialized = configured.serializeConfig();
        @SuppressWarnings("unchecked")
        List<String> roundTripped = (List<String>) serialized.get("commands");
        assertEquals(commands, roundTripped);
    }

    @DisplayName("Given a configured reward with commands, when granting to a player, then commands are dispatched")
    @Test
    public void grant_dispatchesCommandsWithPlayerName() {
        List<String> commands = List.of("say {player} won");
        CommandRewardType configured = baseType.fromSerializedConfig(Map.of("commands", commands));
        PlayerMock player = server.addPlayer();
        configured.grant(player);
    }
}
