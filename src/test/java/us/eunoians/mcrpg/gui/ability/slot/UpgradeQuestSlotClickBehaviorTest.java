package us.eunoians.mcrpg.gui.ability.slot;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.StubTierableAbility;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests click behavior for {@link UpgradeQuestSlot}.
 */
@ExtendWith(McRPGPlayerExtension.class)
class UpgradeQuestSlotClickBehaviorTest extends McRPGBaseTest {

    private StubTierableAbility ability;

    @BeforeEach
    void setup() {
        ability = new StubTierableAbility(mcRPG, new NamespacedKey("mcrpg", "click_test_ability"));
    }

    @Test
    @DisplayName("onClick in LOCKED_BEHIND_LEVEL is a no-op and returns true")
    void onClick_lockedState_returnsTrueAndDoesNothing(McRPGPlayer player) {
        var slot = new UpgradeQuestSlot(player, ability, null, UpgradeQuestSlot.SlotState.LOCKED_BEHIND_LEVEL);
        assertTrue(slot.onClick(player, ClickType.LEFT));
    }

    @Test
    @DisplayName("onClick in MAX_TIER_REACHED is a no-op and returns true")
    void onClick_maxTierState_returnsTrueAndDoesNothing(McRPGPlayer player) {
        var slot = new UpgradeQuestSlot(player, ability, null, UpgradeQuestSlot.SlotState.MAX_TIER_REACHED);
        assertTrue(slot.onClick(player, ClickType.LEFT));
    }

    @Test
    @DisplayName("onClick in ACTIVE_QUEST with null questInstance is a no-op and returns true")
    void onClick_activeQuestStateWithNullInstance_returnsTrueAndDoesNothing(McRPGPlayer player) {
        var slot = new UpgradeQuestSlot(player, ability, null, UpgradeQuestSlot.SlotState.ACTIVE_QUEST);
        assertTrue(slot.onClick(player, ClickType.LEFT));
    }

    @Test
    @DisplayName("onClick returns true for all click types in non-active states")
    void onClick_allClickTypes_returnsTrue(McRPGPlayer player) {
        var lockedSlot = new UpgradeQuestSlot(player, ability, null, UpgradeQuestSlot.SlotState.LOCKED_BEHIND_LEVEL);
        for (ClickType clickType : ClickType.values()) {
            assertTrue(lockedSlot.onClick(player, clickType),
                    "Expected true for click type " + clickType);
        }
    }
}
