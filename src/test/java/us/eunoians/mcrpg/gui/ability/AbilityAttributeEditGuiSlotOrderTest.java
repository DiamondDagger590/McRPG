package us.eunoians.mcrpg.gui.ability;

import org.bukkit.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.ability.attribute.AbilityLocationAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityToggledOffAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.attribute.GuiModifiableAttribute;
import us.eunoians.mcrpg.ability.attribute.MassHarvestPullItemsAttribute;
import us.eunoians.mcrpg.ability.attribute.RemoteTransferItemSetAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that {@link GuiModifiableAttribute#getDisplayPriority()} sorting produces a stable,
 * predictable slot order and that the default priority contract holds for third-party attributes.
 */
class AbilityAttributeEditGuiSlotOrderTest {

    @Test
    @DisplayName("Default getDisplayPriority() on anonymous GuiModifiableAttribute returns 50")
    void defaultDisplayPriority_returns50() {
        GuiModifiableAttribute anon = (player, ability) -> null;
        assertEquals(50, anon.getDisplayPriority());
    }

    @Test
    @DisplayName("Sorting attributes by priority places toggle before quest before location")
    void sortByPriority_toggleBeforeQuestBeforeLocation() {
        AbilityToggledOffAttribute toggle = new AbilityToggledOffAttribute(false);
        AbilityUpgradeQuestAttribute quest = new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID());
        AbilityLocationAttribute location = new AbilityLocationAttribute(new Location(null, 0, 0, 0));

        List<GuiModifiableAttribute> attrs = new ArrayList<>(List.of(location, quest, toggle));
        attrs.sort(Comparator.comparingInt(GuiModifiableAttribute::getDisplayPriority));

        assertEquals(toggle, attrs.get(0));
        assertEquals(quest, attrs.get(1));
        assertEquals(location, attrs.get(2));
    }

    @Test
    @DisplayName("All built-in attribute priorities are below the default 50 for third-party attributes")
    void builtInPriorities_allBelow50() {
        List<GuiModifiableAttribute> builtIn = List.of(
                new AbilityToggledOffAttribute(false),
                new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID()),
                new AbilityLocationAttribute(new Location(null, 0, 0, 0)),
                new MassHarvestPullItemsAttribute(true),
                new RemoteTransferItemSetAttribute(new HashSet<>()));
        for (GuiModifiableAttribute attr : builtIn) {
            assertTrue(attr.getDisplayPriority() < 50,
                    attr.getClass().getSimpleName() + " priority must be < 50");
        }
    }
}
