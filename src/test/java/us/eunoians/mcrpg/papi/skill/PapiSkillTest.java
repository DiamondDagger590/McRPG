package us.eunoians.mcrpg.papi.skill;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.external.papi.McRPGPapiHook;
import us.eunoians.mcrpg.mock.player.MockMcRPGPlayer;
import us.eunoians.mcrpg.mock.skill.MockSkill;
import us.eunoians.mcrpg.external.papi.McRPGPapiExpansion;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class PapiSkillTest {

    private static ServerMock serverMock;
    private static McRPG plugin;
    private static McRPGPapiExpansion mcRPGPapiExpansion;
    private static MockMcRPGPlayer mockMcRPGPlayer;
    private static PlayerMock playerMock;
    private static NamespacedKey skillKey;

    @BeforeAll
    public static void load() {
        serverMock = MockBukkit.mock();
        plugin = spy(MockBukkit.load(McRPG.class));

        // Setup player
        playerMock = serverMock.addPlayer();
        mockMcRPGPlayer = new MockMcRPGPlayer(playerMock, plugin);
        plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).addPlayer(mockMcRPGPlayer);

        loadSkill();

        mcRPGPapiExpansion = new McRPGPapiHook(plugin).getExpansion();
    }

    private static void loadSkill() {
        SkillRegistry skillRegistry = new SkillRegistry(plugin);
        when(plugin.registryAccess().registry(McRPGRegistryKey.SKILL)).thenReturn(skillRegistry);
        skillKey = new NamespacedKey(plugin, "test-skill");
        MockSkill mockSkill = new MockSkill(skillKey);
        skillRegistry.register(mockSkill);
        mockMcRPGPlayer.asSkillHolder().addSkillHolderData(mockSkill);
    }

    @Test
    public void testCurrentLevelPlaceholder() {
        mockMcRPGPlayer.asSkillHolder().getSkillHolderData(skillKey)
                .ifPresent(skillHolderData -> {
                    skillHolderData.addLevel(4);
                });

        String placeholder = "test-skill_current_level";
        String postPlaceholderMessage = "Your current level is 5.";
        assertEquals(postPlaceholderMessage, String.format("Your current level is %s.", mcRPGPapiExpansion.onRequest(playerMock, placeholder)));
    }
}
