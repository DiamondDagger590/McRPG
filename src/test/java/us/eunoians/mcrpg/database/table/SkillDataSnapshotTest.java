package us.eunoians.mcrpg.database.table;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillDataSnapshotTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final NamespacedKey SKILL_KEY = new NamespacedKey("mcrpg", "swords");

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Two-arg constructor sets totalExperience to 0")
        void twoArgConstructor_setsTotalExperienceToZero() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);

            assertEquals(PLAYER_UUID, snapshot.getUUID());
            assertEquals(SKILL_KEY, snapshot.getSkillKey());
            assertEquals(0, snapshot.getTotalExperience());
        }

        @Test
        @DisplayName("Three-arg constructor stores totalExperience")
        void threeArgConstructor_storesTotalExperience() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY, 500);

            assertEquals(PLAYER_UUID, snapshot.getUUID());
            assertEquals(SKILL_KEY, snapshot.getSkillKey());
            assertEquals(500, snapshot.getTotalExperience());
        }
    }

    @Nested
    @DisplayName("setTotalExperience")
    class SetTotalExperienceTests {

        @Test
        @DisplayName("Positive value is stored directly")
        void setTotalExperience_positiveValue() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);

            snapshot.setTotalExperience(1000);

            assertEquals(1000, snapshot.getTotalExperience());
        }

        @Test
        @DisplayName("Zero is stored as-is")
        void setTotalExperience_zero() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY, 500);

            snapshot.setTotalExperience(0);

            assertEquals(0, snapshot.getTotalExperience());
        }

        @Test
        @DisplayName("Negative value is clamped to 0")
        void setTotalExperience_negativeClampedToZero() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY, 500);

            snapshot.setTotalExperience(-100);

            assertEquals(0, snapshot.getTotalExperience());
        }
    }

    @Nested
    @DisplayName("Ability attributes")
    class AbilityAttributeTests {

        @Test
        @DisplayName("getAbilityAttributes returns empty map for unknown ability key")
        void getAbilityAttributes_returnsEmptyMap_whenUnknownKey() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            NamespacedKey unknownKey = new NamespacedKey("mcrpg", "unknown_ability");

            Map<NamespacedKey, AbilityAttribute<?>> result = snapshot.getAbilityAttributes(unknownKey);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("addAttribute stores attribute under ability key")
        void addAttribute_storesAttributeUnderAbilityKey() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "tier");

            AbilityAttribute<?> mockAttribute = mock(AbilityAttribute.class);
            when(mockAttribute.getNamespacedKey()).thenReturn(attributeKey);

            snapshot.addAttribute(abilityKey, mockAttribute);

            Map<NamespacedKey, AbilityAttribute<?>> attributes = snapshot.getAbilityAttributes(abilityKey);
            assertEquals(1, attributes.size());
            assertEquals(mockAttribute, attributes.get(attributeKey));
        }

        @Test
        @DisplayName("addAttribute with multiple attributes on same ability key")
        void addAttribute_multipleAttributesOnSameAbilityKey() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
            NamespacedKey attributeKey1 = new NamespacedKey("mcrpg", "tier");
            NamespacedKey attributeKey2 = new NamespacedKey("mcrpg", "cooldown");

            AbilityAttribute<?> mockAttribute1 = mock(AbilityAttribute.class);
            when(mockAttribute1.getNamespacedKey()).thenReturn(attributeKey1);

            AbilityAttribute<?> mockAttribute2 = mock(AbilityAttribute.class);
            when(mockAttribute2.getNamespacedKey()).thenReturn(attributeKey2);

            snapshot.addAttribute(abilityKey, mockAttribute1);
            snapshot.addAttribute(abilityKey, mockAttribute2);

            Map<NamespacedKey, AbilityAttribute<?>> attributes = snapshot.getAbilityAttributes(abilityKey);
            assertEquals(2, attributes.size());
            assertEquals(mockAttribute1, attributes.get(attributeKey1));
            assertEquals(mockAttribute2, attributes.get(attributeKey2));
        }

        @Test
        @DisplayName("getAbilityAttributes returns new map instance each time for unknown keys")
        void getAbilityAttributes_returnsNewInstance_whenKeyNotStored() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            NamespacedKey unknownKey = new NamespacedKey("mcrpg", "nonexistent");

            Map<NamespacedKey, AbilityAttribute<?>> first = snapshot.getAbilityAttributes(unknownKey);
            Map<NamespacedKey, AbilityAttribute<?>> second = snapshot.getAbilityAttributes(unknownKey);

            assertNotSame(first, second);
        }

        @Test
        @DisplayName("Attributes on different ability keys are isolated")
        void addAttribute_differentAbilityKeysAreIsolated() {
            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            NamespacedKey abilityKey1 = new NamespacedKey("mcrpg", "bleed");
            NamespacedKey abilityKey2 = new NamespacedKey("mcrpg", "extra_ore");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "tier");

            AbilityAttribute<?> mockAttribute1 = mock(AbilityAttribute.class);
            when(mockAttribute1.getNamespacedKey()).thenReturn(attributeKey);

            AbilityAttribute<?> mockAttribute2 = mock(AbilityAttribute.class);
            when(mockAttribute2.getNamespacedKey()).thenReturn(attributeKey);

            snapshot.addAttribute(abilityKey1, mockAttribute1);
            snapshot.addAttribute(abilityKey2, mockAttribute2);

            assertEquals(1, snapshot.getAbilityAttributes(abilityKey1).size());
            assertEquals(1, snapshot.getAbilityAttributes(abilityKey2).size());
            assertEquals(mockAttribute1, snapshot.getAbilityAttributes(abilityKey1).get(attributeKey));
            assertEquals(mockAttribute2, snapshot.getAbilityAttributes(abilityKey2).get(attributeKey));
        }
    }
}
