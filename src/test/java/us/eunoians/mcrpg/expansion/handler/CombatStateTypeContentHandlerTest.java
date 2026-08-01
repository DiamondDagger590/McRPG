package us.eunoians.mcrpg.expansion.handler;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.state.CombatStateType;
import us.eunoians.mcrpg.combat.state.CombatStateTypeRegistry;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.expansion.content.CombatStateTypeContentPack;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("ContentHandlerType.COMBAT_STATE_TYPE")
class CombatStateTypeContentHandlerTest extends McRPGBaseTest {

    @Test
    @DisplayName("registers each state type in the registry")
    void processContentPack_registersEachStateType() {
        NamespacedKey key = new NamespacedKey("mcrpg", "frenzy_stacks");
        CombatStateType<Integer> stateType = CombatStateType.of(key, Integer.class, 0, null);

        CombatStateTypeContentPack pack = new CombatStateTypeContentPack(mock(ContentExpansion.class));
        pack.addContent(stateType);

        boolean processed = ContentHandlerType.COMBAT_STATE_TYPE.getContentHandler().processContentPack(mcRPG, pack);

        assertTrue(processed);
        CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
        assertTrue(registry.isRegistered(key));
    }
}
