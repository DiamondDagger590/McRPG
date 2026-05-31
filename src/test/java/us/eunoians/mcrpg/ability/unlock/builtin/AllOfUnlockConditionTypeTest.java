package us.eunoians.mcrpg.ability.unlock.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.exception.UnlockConditionParseException;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AllOfUnlockConditionTypeTest extends McRPGBaseTest {

    @DisplayName("Given empty children, when isMet, then it returns false")
    @Test
    public void isMet_returnsFalseOnEmptyChildren() {
        assertFalse(new AllOfUnlockConditionType().isMet(mock(SkillHolder.class)));
    }

    @DisplayName("Given all children met, when isMet, then it returns true")
    @Test
    public void isMet_returnsTrueWhenAllMet() {
        AllOfUnlockConditionType all = new AllOfUnlockConditionType(List.of(
                alwaysMet(), alwaysMet()));
        assertTrue(all.isMet(mock(SkillHolder.class)));
    }

    @DisplayName("Given one child not met, when isMet, then it returns false")
    @Test
    public void isMet_returnsFalseWhenAnyChildNotMet() {
        AllOfUnlockConditionType all = new AllOfUnlockConditionType(List.of(
                alwaysMet(), neverMet(), alwaysMet()));
        assertFalse(all.isMet(mock(SkillHolder.class)));
    }

    @DisplayName("Given children with mixed progress, when getProgress, then it returns the average")
    @Test
    public void getProgress_returnsAverageChildProgress() {
        AllOfUnlockConditionType all = new AllOfUnlockConditionType(List.of(
                fixedProgress(0.8), fixedProgress(0.3), fixedProgress(0.7)));
        assertEquals(0.6, all.getProgress(mock(SkillHolder.class)), 0.001);
    }

    @DisplayName("Given a section missing 'conditions', when parsing, then it throws")
    @Test
    public void parseConfig_throwsOnMissingConditions() {
        Section section = mock(Section.class);
        when(section.getOptionalSection("conditions")).thenReturn(Optional.empty());
        assertThrows(UnlockConditionParseException.class,
                () -> new AllOfUnlockConditionType().parseConfig(section));
    }

    private static UnlockConditionType alwaysMet() {
        return new TestCondition(true, 1.0);
    }

    private static UnlockConditionType neverMet() {
        return new TestCondition(false, 0.0);
    }

    private static UnlockConditionType fixedProgress(double progress) {
        return new TestCondition(progress >= 1.0, progress);
    }

    private record TestCondition(boolean met, double progress) implements UnlockConditionType {
        @Override
        public NamespacedKey getKey() {
            return new NamespacedKey("test", "stub_" + UUID.randomUUID());
        }

        @Override
        public UnlockConditionType parseConfig(dev.dejvokep.boostedyaml.block.implementation.Section section) {
            return this;
        }

        @Override
        public boolean isMet(AbilityHolder holder) {
            return met;
        }

        @Override
        public double getProgress(AbilityHolder holder) {
            return progress;
        }

        @Override
        public Component getDisplayDescription(McRPGPlayer player) {
            return Component.empty();
        }

        @Override
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
