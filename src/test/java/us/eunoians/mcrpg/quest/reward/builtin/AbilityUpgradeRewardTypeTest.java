package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbilityUpgradeRewardTypeTest extends McRPGBaseTest {

    @DisplayName("Given the type, when calling getKey, then it returns the ability_upgrade key")
    @Test
    public void getKey_returnsAbilityUpgradeKey() {
        AbilityUpgradeRewardType type = new AbilityUpgradeRewardType();
        assertEquals(AbilityUpgradeRewardType.KEY, type.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        AbilityUpgradeRewardType type = new AbilityUpgradeRewardType();
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }
}
