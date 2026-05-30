package us.eunoians.mcrpg.ability.unlock.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionParseException;
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

public class AnyOfUnlockConditionTypeTest extends McRPGBaseTest {

    @DisplayName("Given empty children, when isMet, then it returns false")
    @Test
    public void isMet_returnsFalseOnEmptyChildren() {
        assertFalse(new AnyOfUnlockConditionType().isMet(mock(SkillHolder.class)));
    }

    @DisplayName("Given at least one child met, when isMet, then it returns true")
    @Test
    public void isMet_returnsTrueWhenOneChildMet() {
        AnyOfUnlockConditionType any = new AnyOfUnlockConditionType(List.of(
                neverMet(), alwaysMet(), neverMet()));
        assertTrue(any.isMet(mock(SkillHolder.class)));
    }

    @DisplayName("Given no children met, when isMet, then it returns false")
    @Test
    public void isMet_returnsFalseWhenNoneMet() {
        AnyOfUnlockConditionType any = new AnyOfUnlockConditionType(List.of(
                neverMet(), neverMet()));
        assertFalse(any.isMet(mock(SkillHolder.class)));
    }

    @DisplayName("Given children with mixed progress, when getProgress, then it returns the max")
    @Test
    public void getProgress_returnsMaxChildProgress() {
        AnyOfUnlockConditionType any = new AnyOfUnlockConditionType(List.of(
                fixedProgress(0.2), fixedProgress(0.7), fixedProgress(0.4)));
        assertEquals(0.7, any.getProgress(mock(SkillHolder.class)), 0.001);
    }

    @DisplayName("Given a section missing 'conditions', when parsing, then it throws")
    @Test
    public void parseConfig_throwsOnMissingConditions() {
        Section section = mock(Section.class);
        when(section.getOptionalSection("conditions")).thenReturn(Optional.empty());
        assertThrows(UnlockConditionParseException.class,
                () -> new AnyOfUnlockConditionType().parseConfig(section));
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
