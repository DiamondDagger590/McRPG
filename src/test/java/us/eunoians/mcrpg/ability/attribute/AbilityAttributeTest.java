package us.eunoians.mcrpg.ability.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityAttributeTest extends McRPGBaseTest {

    @Nested
    @DisplayName("AbilityTierAttribute")
    class TierAttribute {

        @DisplayName("getDefaultContent returns 1")
        @Test
        void getDefaultContent_returnsOne() {
            var attr = new AbilityTierAttribute();
            assertEquals(1, attr.getDefaultContent());
        }

        @DisplayName("default constructor uses default content")
        @Test
        void defaultConstructor_usesDefaultContent() {
            var attr = new AbilityTierAttribute();
            assertEquals(1, attr.getContent());
        }

        @DisplayName("value constructor stores provided tier")
        @Test
        void valueConstructor_storesProvidedTier() {
            var attr = new AbilityTierAttribute(5);
            assertEquals(5, attr.getContent());
        }

        @DisplayName("create returns new instance with given value")
        @Test
        void create_returnsNewInstance() {
            var template = new AbilityTierAttribute();
            AbilityTierAttribute created = template.create(3);
            assertEquals(3, created.getContent());
            assertNotSame(template, created);
        }

        @DisplayName("create from string parses integer")
        @Test
        void create_fromString_parsesInteger() {
            var template = new AbilityTierAttribute();
            AbilityAttribute<Integer> created = template.create("7");
            assertEquals(7, created.getContent());
        }

        @DisplayName("convertContent parses valid integer string")
        @Test
        void convertContent_parsesValidInteger() {
            var attr = new AbilityTierAttribute();
            assertEquals(42, attr.convertContent("42"));
        }

        @DisplayName("convertContent throws NumberFormatException for non-numeric input")
        @Test
        void convertContent_throwsNumberFormatException_forNonNumericInput() {
            var attr = new AbilityTierAttribute();
            assertThrows(NumberFormatException.class, () -> attr.convertContent("abc"));
        }

        @DisplayName("getDatabaseKeyName returns tier")
        @Test
        void getDatabaseKeyName_returnsTier() {
            var attr = new AbilityTierAttribute();
            assertEquals("tier", attr.getDatabaseKeyName());
        }

        @DisplayName("getNamespacedKey matches registry constant")
        @Test
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityTierAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY, attr.getNamespacedKey());
        }

        @DisplayName("shouldContentBeSaved returns false when tier is 1")
        @Test
        void shouldContentBeSaved_returnsFalse_whenTierIsDefault() {
            var attr = new AbilityTierAttribute(1);
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns true when tier is greater than 1")
        @Test
        void shouldContentBeSaved_returnsTrue_whenTierAboveDefault() {
            var attr = new AbilityTierAttribute(2);
            assertTrue(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns false when tier is 0")
        @Test
        void shouldContentBeSaved_returnsFalse_whenTierIsZero() {
            var attr = new AbilityTierAttribute(0);
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("serializeContent returns string of tier value")
        @Test
        void serializeContent_returnsStringRepresentation() {
            var attr = new AbilityTierAttribute(5);
            assertEquals("5", attr.serializeContent());
        }

        @DisplayName("getPlaceholderName returns tier")
        @Test
        void getPlaceholderName_returnsTier() {
            var attr = new AbilityTierAttribute();
            assertEquals("tier", attr.getPlaceholderName());
        }

        @DisplayName("getDisplayableContent returns tier as string")
        @Test
        void getDisplayableContent_returnsTierAsString() {
            var attr = new AbilityTierAttribute(3);
            assertEquals("3", attr.getDisplayableContent());
        }
    }

    @Nested
    @DisplayName("AbilityCooldownAttribute")
    class CooldownAttribute {

        @DisplayName("getDefaultContent returns 0")
        @Test
        void getDefaultContent_returnsZero() {
            var attr = new AbilityCooldownAttribute();
            assertEquals(0L, attr.getDefaultContent());
        }

        @DisplayName("default constructor uses default content")
        @Test
        void defaultConstructor_usesDefaultContent() {
            var attr = new AbilityCooldownAttribute();
            assertEquals(0L, attr.getContent());
        }

        @DisplayName("value constructor stores provided cooldown")
        @Test
        void valueConstructor_storesProvidedCooldown() {
            var attr = new AbilityCooldownAttribute(1000L);
            assertEquals(1000L, attr.getContent());
        }

        @DisplayName("create returns new instance with given value")
        @Test
        void create_returnsNewInstance() {
            var template = new AbilityCooldownAttribute();
            AbilityCooldownAttribute created = template.create(5000L);
            assertEquals(5000L, created.getContent());
            assertNotSame(template, created);
        }

        @DisplayName("create from string parses long")
        @Test
        void create_fromString_parsesLong() {
            var template = new AbilityCooldownAttribute();
            AbilityAttribute<Long> created = template.create("9999");
            assertEquals(9999L, created.getContent());
        }

        @DisplayName("convertContent parses valid long string")
        @Test
        void convertContent_parsesValidLong() {
            var attr = new AbilityCooldownAttribute();
            assertEquals(12345L, attr.convertContent("12345"));
        }

        @DisplayName("getDatabaseKeyName returns cooldown")
        @Test
        void getDatabaseKeyName_returnsCooldown() {
            var attr = new AbilityCooldownAttribute();
            assertEquals("cooldown", attr.getDatabaseKeyName());
        }

        @DisplayName("getNamespacedKey matches registry constant")
        @Test
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityCooldownAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY, attr.getNamespacedKey());
        }

        @DisplayName("shouldContentBeSaved returns false when cooldown is 0")
        @Test
        void shouldContentBeSaved_returnsFalse_whenZero() {
            var attr = new AbilityCooldownAttribute(0L);
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns false when cooldown is in the past")
        @Test
        void shouldContentBeSaved_returnsFalse_whenInPast() {
            long pastTime = mcRPG.getTimeProvider().now().toEpochMilli() - 60_000;
            var attr = new AbilityCooldownAttribute(pastTime);
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns true when cooldown is in the future")
        @Test
        void shouldContentBeSaved_returnsTrue_whenInFuture() {
            long futureTime = mcRPG.getTimeProvider().now().toEpochMilli() + 60_000;
            var attr = new AbilityCooldownAttribute(futureTime);
            assertTrue(attr.shouldContentBeSaved());
        }

        @DisplayName("convertContent throws NumberFormatException for non-numeric input")
        @Test
        void convertContent_throwsNumberFormatException_forNonNumericInput() {
            var attr = new AbilityCooldownAttribute();
            assertThrows(NumberFormatException.class, () -> attr.convertContent("abc"));
        }

        @DisplayName("serializeContent returns string of cooldown value")
        @Test
        void serializeContent_returnsStringRepresentation() {
            var attr = new AbilityCooldownAttribute(5000L);
            assertEquals("5000", attr.serializeContent());
        }
    }

    @Nested
    @DisplayName("AbilityUnlockedAttribute")
    class UnlockedAttribute {

        @DisplayName("getDefaultContent returns false")
        @Test
        void getDefaultContent_returnsFalse() {
            var attr = new AbilityUnlockedAttribute();
            assertFalse(attr.getDefaultContent());
        }

        @DisplayName("default constructor uses default content")
        @Test
        void defaultConstructor_usesDefaultContent() {
            var attr = new AbilityUnlockedAttribute();
            assertFalse(attr.getContent());
        }

        @DisplayName("value constructor stores true")
        @Test
        void valueConstructor_storesTrue() {
            var attr = new AbilityUnlockedAttribute(true);
            assertTrue(attr.getContent());
        }

        @DisplayName("create returns new instance with given value")
        @Test
        void create_returnsNewInstance() {
            var template = new AbilityUnlockedAttribute();
            AbilityAttribute<Boolean> created = template.create(true);
            assertTrue(created.getContent());
            assertNotSame(template, created);
        }

        @DisplayName("create from string parses boolean")
        @Test
        void create_fromString_parsesBoolean() {
            var template = new AbilityUnlockedAttribute();
            AbilityAttribute<Boolean> created = template.create("true");
            assertTrue(created.getContent());
        }

        @DisplayName("convertContent parses true string")
        @Test
        void convertContent_parsesTrue() {
            var attr = new AbilityUnlockedAttribute();
            assertTrue(attr.convertContent("true"));
        }

        @DisplayName("convertContent parses false string")
        @Test
        void convertContent_parsesFalse() {
            var attr = new AbilityUnlockedAttribute();
            assertFalse(attr.convertContent("false"));
        }

        @DisplayName("convertContent returns false for non-boolean string")
        @Test
        void convertContent_returnsFalse_forNonBooleanString() {
            var attr = new AbilityUnlockedAttribute();
            assertFalse(attr.convertContent("notaboolean"));
        }

        @DisplayName("getDatabaseKeyName returns unlocked")
        @Test
        void getDatabaseKeyName_returnsUnlocked() {
            var attr = new AbilityUnlockedAttribute();
            assertEquals("unlocked", attr.getDatabaseKeyName());
        }

        @DisplayName("getNamespacedKey matches registry constant")
        @Test
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityUnlockedAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE, attr.getNamespacedKey());
        }

        @DisplayName("shouldContentBeSaved returns false when not unlocked")
        @Test
        void shouldContentBeSaved_returnsFalse_whenNotUnlocked() {
            var attr = new AbilityUnlockedAttribute(false);
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns true when unlocked")
        @Test
        void shouldContentBeSaved_returnsTrue_whenUnlocked() {
            var attr = new AbilityUnlockedAttribute(true);
            assertTrue(attr.shouldContentBeSaved());
        }
    }

    @Nested
    @DisplayName("AbilityToggledOffAttribute")
    class ToggledOffAttribute {

        @DisplayName("getDefaultContent returns false")
        @Test
        void getDefaultContent_returnsFalse() {
            var attr = new AbilityToggledOffAttribute();
            assertFalse(attr.getDefaultContent());
        }

        @DisplayName("default constructor uses default content")
        @Test
        void defaultConstructor_usesDefaultContent() {
            var attr = new AbilityToggledOffAttribute();
            assertFalse(attr.getContent());
        }

        @DisplayName("value constructor stores true")
        @Test
        void valueConstructor_storesTrue() {
            var attr = new AbilityToggledOffAttribute(true);
            assertTrue(attr.getContent());
        }

        @DisplayName("create returns new instance with given value")
        @Test
        void create_returnsNewInstance() {
            var template = new AbilityToggledOffAttribute();
            AbilityAttribute<Boolean> created = template.create(true);
            assertTrue(created.getContent());
            assertNotSame(template, created);
        }

        @DisplayName("create from string parses boolean")
        @Test
        void create_fromString_parsesBoolean() {
            var template = new AbilityToggledOffAttribute();
            AbilityAttribute<Boolean> created = template.create("true");
            assertTrue(created.getContent());
        }

        @DisplayName("convertContent parses true string")
        @Test
        void convertContent_parsesTrue() {
            var attr = new AbilityToggledOffAttribute();
            assertTrue(attr.convertContent("true"));
        }

        @DisplayName("convertContent parses false string")
        @Test
        void convertContent_parsesFalse() {
            var attr = new AbilityToggledOffAttribute();
            assertFalse(attr.convertContent("false"));
        }

        @DisplayName("getDatabaseKeyName returns toggled")
        @Test
        void getDatabaseKeyName_returnsToggled() {
            var attr = new AbilityToggledOffAttribute();
            assertEquals("toggled", attr.getDatabaseKeyName());
        }

        @DisplayName("getNamespacedKey matches registry constant")
        @Test
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityToggledOffAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY, attr.getNamespacedKey());
        }

        @DisplayName("shouldContentBeSaved returns false when not toggled off")
        @Test
        void shouldContentBeSaved_returnsFalse_whenNotToggledOff() {
            var attr = new AbilityToggledOffAttribute(false);
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns true when toggled off")
        @Test
        void shouldContentBeSaved_returnsTrue_whenToggledOff() {
            var attr = new AbilityToggledOffAttribute(true);
            assertTrue(attr.shouldContentBeSaved());
        }

        @DisplayName("getDisplayPriority returns 10")
        @Test
        void getDisplayPriority_returnsTen() {
            var attr = new AbilityToggledOffAttribute();
            assertEquals(10, attr.getDisplayPriority());
        }
    }

    @Nested
    @DisplayName("AbilityAttribute equals and hashCode")
    class EqualsAndHashCode {

        @DisplayName("equal attributes with same key and content are equal")
        @Test
        void equals_sameKeyAndContent_areEqual() {
            var attr1 = new AbilityTierAttribute(3);
            var attr2 = new AbilityTierAttribute(3);
            assertEquals(attr1, attr2);
        }

        @DisplayName("attributes with different content are not equal")
        @Test
        void equals_differentContent_areNotEqual() {
            var attr1 = new AbilityTierAttribute(3);
            var attr2 = new AbilityTierAttribute(5);
            assertNotEquals(attr1, attr2);
        }

        @DisplayName("attributes of different types are not equal")
        @Test
        void equals_differentTypes_areNotEqual() {
            var tier = new AbilityTierAttribute(1);
            var unlocked = new AbilityUnlockedAttribute(true);
            assertNotEquals(tier, unlocked);
        }

        @DisplayName("attribute is not equal to null")
        @Test
        void equals_null_returnsFalse() {
            var attr = new AbilityTierAttribute(1);
            assertNotEquals(null, attr);
        }

        @DisplayName("attribute is not equal to non-attribute object")
        @Test
        void equals_nonAttribute_returnsFalse() {
            var attr = new AbilityTierAttribute(1);
            assertNotEquals("not an attribute", attr);
        }

        @DisplayName("equal attributes have same hashCode")
        @Test
        void hashCode_equalAttributes_sameHash() {
            var attr1 = new AbilityTierAttribute(3);
            var attr2 = new AbilityTierAttribute(3);
            assertEquals(attr1.hashCode(), attr2.hashCode());
        }

        @DisplayName("getAbilityType returns empty for default instances")
        @Test
        void getAbilityType_returnsEmpty_forDefaultInstances() {
            var attr = new AbilityTierAttribute();
            assertTrue(attr.getAbilityType().isEmpty());
        }

        @DisplayName("getAbilityType returns empty for value-constructed instances")
        @Test
        void getAbilityType_returnsEmpty_forValueConstructed() {
            var attr = new AbilityTierAttribute(5);
            assertTrue(attr.getAbilityType().isEmpty());
        }

        @DisplayName("toString contains key and content info")
        @Test
        void toString_containsInfo() {
            var attr = new AbilityTierAttribute(3);
            String str = attr.toString();
            assertTrue(str.contains("3"));
            assertTrue(str.contains("tier"));
        }
    }
}
