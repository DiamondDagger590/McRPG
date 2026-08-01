package us.eunoians.mcrpg.expansion.handler;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.condition.CombatCondition;
import us.eunoians.mcrpg.combat.condition.CombatConditionRegistry;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.expansion.content.CombatConditionContentPack;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ContentHandlerType.COMBAT_CONDITION")
class CombatConditionContentHandlerTest extends McRPGBaseTest {

    @Test
    @DisplayName("registers each condition and starts its evaluation task")
    void processContentPack_registersAndStartsConditions() {
        // The processor resolves the registered CombatTrackerManager; the test bootstrap does not
        // register one, so register a mock to observe the task-start call.
        CombatTrackerManager manager = mock(CombatTrackerManager.class);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER).register(manager);

        CombatCondition condition = mock(CombatCondition.class);
        NamespacedKey key = new NamespacedKey("mcrpg", "proximity");
        when(condition.getKey()).thenReturn(key);

        CombatConditionContentPack pack = new CombatConditionContentPack(mock(ContentExpansion.class));
        pack.addContent(condition);

        boolean processed = ContentHandlerType.COMBAT_CONDITION.getContentHandler().processContentPack(mcRPG, pack);

        assertTrue(processed);
        CombatConditionRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_CONDITION);
        assertTrue(registry.isRegistered(key));
        verify(manager).startConditionTask(condition);
    }
}
