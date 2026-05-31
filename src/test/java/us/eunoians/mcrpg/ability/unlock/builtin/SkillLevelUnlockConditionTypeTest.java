package us.eunoians.mcrpg.ability.unlock.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.exception.UnlockConditionParseException;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SkillLevelUnlockConditionTypeTest extends McRPGBaseTest {

    private static final NamespacedKey SWORDS = new NamespacedKey("mcrpg", "swords");

    @DisplayName("Given the unconfigured prototype, when isMet, then it returns false")
    @Test
    public void isMet_returnsFalseOnPrototype() {
        assertFalse(new SkillLevelUnlockConditionType().isMet(mock(SkillHolder.class)));
    }

    @DisplayName("Given a non-SkillHolder holder, when isMet, then it returns false")
    @Test
    public void isMet_returnsFalseOnNonSkillHolder() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType(SWORDS, 100);
        assertFalse(type.isMet(mock(AbilityHolder.class)));
    }

    @DisplayName("Given a holder at the required level, when isMet, then it returns true")
    @Test
    public void isMet_returnsTrueAtRequiredLevel() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType(SWORDS, 100);
        assertTrue(type.isMet(holderWithLevel(100)));
    }

    @DisplayName("Given a holder above the required level, when isMet, then it returns true")
    @Test
    public void isMet_returnsTrueAboveRequiredLevel() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType(SWORDS, 100);
        assertTrue(type.isMet(holderWithLevel(187)));
    }

    @DisplayName("Given a holder below the required level, when isMet, then it returns false")
    @Test
    public void isMet_returnsFalseBelowRequiredLevel() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType(SWORDS, 250);
        assertFalse(type.isMet(holderWithLevel(100)));
    }

    @DisplayName("Given a holder with no skill data, when isMet, then it returns false")
    @Test
    public void isMet_returnsFalseWhenNoSkillData() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType(SWORDS, 100);
        SkillHolder holder = mock(SkillHolder.class);
        when(holder.getSkillHolderData(SWORDS)).thenReturn(Optional.empty());
        assertFalse(type.isMet(holder));
    }

    @DisplayName("Given a holder at half progress, when getProgress, then it returns 0.5")
    @Test
    public void getProgress_returnsLinearFraction() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType(SWORDS, 100);
        assertEquals(0.5, type.getProgress(holderWithLevel(50)), 0.001);
    }

    @DisplayName("Given a holder above the threshold, when getProgress, then it caps at 1.0")
    @Test
    public void getProgress_capsAtOne() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType(SWORDS, 100);
        assertEquals(1.0, type.getProgress(holderWithLevel(187)), 0.001);
    }

    @DisplayName("Given requiredLevel <= 0, when getProgress, then it returns 0")
    @Test
    public void getProgress_returnsZeroOnZeroRequired() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType(SWORDS, 0);
        assertEquals(0.0, type.getProgress(holderWithLevel(50)), 0.001);
    }

    @DisplayName("Given a section missing 'skill', when parsing, then it throws")
    @Test
    public void parseConfig_throwsOnMissingSkill() {
        Section section = mock(Section.class);
        when(section.getString("skill")).thenReturn(null);
        assertThrows(UnlockConditionParseException.class,
                () -> new SkillLevelUnlockConditionType().parseConfig(section));
    }

    @DisplayName("Given a section missing 'level', when parsing, then it throws")
    @Test
    public void parseConfig_throwsOnMissingLevel() {
        Section section = mock(Section.class);
        when(section.getString("skill")).thenReturn("mcrpg:swords");
        when(section.contains("level")).thenReturn(false);
        assertThrows(UnlockConditionParseException.class,
                () -> new SkillLevelUnlockConditionType().parseConfig(section));
    }

    @DisplayName("Given a valid section, when parsing, then a configured instance is returned")
    @Test
    public void parseConfig_returnsConfiguredInstance() {
        Section section = mock(Section.class);
        when(section.getString("skill")).thenReturn("mcrpg:swords");
        when(section.contains("level")).thenReturn(true);
        when(section.getInt("level")).thenReturn(250);
        SkillLevelUnlockConditionType parsed = (SkillLevelUnlockConditionType)
                new SkillLevelUnlockConditionType().parseConfig(section);
        assertEquals(SWORDS, parsed.getSkillKey().orElseThrow());
        assertEquals(250, parsed.getRequiredLevel());
    }

    @DisplayName("Given a bare skill name in config, when parsing, then it is namespaced under mcrpg")
    @Test
    public void parseConfig_acceptsBareSkillName() {
        Section section = mock(Section.class);
        when(section.getString("skill")).thenReturn("swords");
        when(section.contains("level")).thenReturn(true);
        when(section.getInt("level")).thenReturn(100);
        SkillLevelUnlockConditionType parsed = (SkillLevelUnlockConditionType)
                new SkillLevelUnlockConditionType().parseConfig(section);
        assertEquals(SWORDS, parsed.getSkillKey().orElseThrow());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType();
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    private static SkillHolder holderWithLevel(int currentLevel) {
        SkillHolder holder = mock(SkillHolder.class);
        SkillHolder.SkillHolderData data = mock(SkillHolder.SkillHolderData.class);
        when(data.getCurrentLevel()).thenReturn(currentLevel);
        when(holder.getSkillHolderData(SWORDS)).thenReturn(Optional.of(data));
        return holder;
    }
}
