package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Set;
import java.util.UUID;

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

    @Nested
    @DisplayName("MassHarvestPullItemsAttribute")
    class MassHarvestPullItems {

        @DisplayName("getDefaultContent returns true")
        @Test
        void getDefaultContent_returnsTrue() {
            var attr = new MassHarvestPullItemsAttribute();
            assertTrue(attr.getDefaultContent());
        }

        @DisplayName("default constructor uses default content")
        @Test
        void defaultConstructor_usesDefaultContent() {
            var attr = new MassHarvestPullItemsAttribute();
            assertTrue(attr.getContent());
        }

        @DisplayName("value constructor stores provided value")
        @Test
        void valueConstructor_storesProvidedValue() {
            var attr = new MassHarvestPullItemsAttribute(false);
            assertFalse(attr.getContent());
        }

        @DisplayName("create returns new instance with given value")
        @Test
        void create_returnsNewInstance() {
            var template = new MassHarvestPullItemsAttribute();
            MassHarvestPullItemsAttribute created = template.create(false);
            assertFalse(created.getContent());
            assertNotSame(template, created);
        }

        @DisplayName("convertContent parses true string")
        @Test
        void convertContent_parsesTrue() {
            var attr = new MassHarvestPullItemsAttribute();
            assertTrue(attr.convertContent("true"));
        }

        @DisplayName("convertContent parses false string")
        @Test
        void convertContent_parsesFalse() {
            var attr = new MassHarvestPullItemsAttribute();
            assertFalse(attr.convertContent("false"));
        }

        @DisplayName("convertContent returns false for non-boolean string")
        @Test
        void convertContent_returnsFalse_forNonBooleanString() {
            var attr = new MassHarvestPullItemsAttribute();
            assertFalse(attr.convertContent("notaboolean"));
        }

        @DisplayName("shouldContentBeSaved returns false when enabled (true)")
        @Test
        void shouldContentBeSaved_returnsFalse_whenEnabled() {
            var attr = new MassHarvestPullItemsAttribute(true);
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns true when disabled (false)")
        @Test
        void shouldContentBeSaved_returnsTrue_whenDisabled() {
            var attr = new MassHarvestPullItemsAttribute(false);
            assertTrue(attr.shouldContentBeSaved());
        }

        @DisplayName("getDatabaseKeyName returns mass_harvest_pull_items_toggled")
        @Test
        void getDatabaseKeyName_returnsExpectedKey() {
            var attr = new MassHarvestPullItemsAttribute();
            assertEquals("mass_harvest_pull_items_toggled", attr.getDatabaseKeyName());
        }

        @DisplayName("getNamespacedKey matches registry constant")
        @Test
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new MassHarvestPullItemsAttribute();
            assertEquals(AbilityAttributeRegistry.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE, attr.getNamespacedKey());
        }

        @DisplayName("getDisplayPriority returns 40")
        @Test
        void getDisplayPriority_returnsForty() {
            var attr = new MassHarvestPullItemsAttribute();
            assertEquals(40, attr.getDisplayPriority());
        }

        @DisplayName("serializeContent returns string representation")
        @Test
        void serializeContent_returnsStringRepresentation() {
            var attr = new MassHarvestPullItemsAttribute(false);
            assertEquals("false", attr.serializeContent());
        }
    }

    @Nested
    @DisplayName("AbilityUpgradeQuestAttribute")
    class UpgradeQuestAttribute {

        private static final UUID SENTINEL_UUID = UUID.fromString("b94b32a4-09e8-4378-905b-0df7805916c1");

        @DisplayName("defaultUUID returns the sentinel UUID")
        @Test
        void defaultUUID_returnsSentinel() {
            assertEquals(SENTINEL_UUID, AbilityUpgradeQuestAttribute.defaultUUID());
        }

        @DisplayName("getDefaultContent returns sentinel UUID")
        @Test
        void getDefaultContent_returnsSentinel() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals(SENTINEL_UUID, attr.getDefaultContent());
        }

        @DisplayName("default constructor uses sentinel UUID")
        @Test
        void defaultConstructor_usesSentinelUUID() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals(SENTINEL_UUID, attr.getContent());
        }

        @DisplayName("value constructor stores provided UUID")
        @Test
        void valueConstructor_storesProvidedUUID() {
            UUID questId = UUID.randomUUID();
            var attr = new AbilityUpgradeQuestAttribute(questId);
            assertEquals(questId, attr.getContent());
        }

        @DisplayName("create returns new instance with given UUID")
        @Test
        void create_returnsNewInstance() {
            UUID questId = UUID.randomUUID();
            var template = new AbilityUpgradeQuestAttribute();
            AbilityAttribute<UUID> created = template.create(questId);
            assertEquals(questId, created.getContent());
            assertNotSame(template, created);
        }

        @DisplayName("create from string parses UUID")
        @Test
        void create_fromString_parsesUUID() {
            UUID questId = UUID.randomUUID();
            var template = new AbilityUpgradeQuestAttribute();
            AbilityAttribute<UUID> created = template.create(questId.toString());
            assertEquals(questId, created.getContent());
        }

        @DisplayName("convertContent parses valid UUID string")
        @Test
        void convertContent_parsesValidUUID() {
            UUID expected = UUID.randomUUID();
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals(expected, attr.convertContent(expected.toString()));
        }

        @DisplayName("convertContent throws for invalid UUID string")
        @Test
        void convertContent_throwsForInvalidUUID() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertThrows(IllegalArgumentException.class, () -> attr.convertContent("not-a-uuid"));
        }

        @DisplayName("shouldContentBeSaved returns false when sentinel UUID")
        @Test
        void shouldContentBeSaved_returnsFalse_whenSentinel() {
            var attr = new AbilityUpgradeQuestAttribute(SENTINEL_UUID);
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns false for default constructor")
        @Test
        void shouldContentBeSaved_returnsFalse_forDefaultConstructor() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns true when non-sentinel UUID")
        @Test
        void shouldContentBeSaved_returnsTrue_whenNonSentinel() {
            var attr = new AbilityUpgradeQuestAttribute(UUID.randomUUID());
            assertTrue(attr.shouldContentBeSaved());
        }

        @DisplayName("getDatabaseKeyName returns quest")
        @Test
        void getDatabaseKeyName_returnsQuest() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals("quest", attr.getDatabaseKeyName());
        }

        @DisplayName("getNamespacedKey matches registry constant")
        @Test
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE, attr.getNamespacedKey());
        }

        @DisplayName("getDisplayPriority returns 20")
        @Test
        void getDisplayPriority_returnsTwenty() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals(20, attr.getDisplayPriority());
        }

        @DisplayName("serializeContent returns UUID string")
        @Test
        void serializeContent_returnsUUIDString() {
            UUID questId = UUID.randomUUID();
            var attr = new AbilityUpgradeQuestAttribute(questId);
            assertEquals(questId.toString(), attr.serializeContent());
        }
    }

    @Nested
    @DisplayName("AbilityLocationAttribute")
    class LocationAttribute {

        @DisplayName("getDefaultContent returns location with null world")
        @Test
        void getDefaultContent_returnsLocationWithNullWorld() {
            var attr = new AbilityLocationAttribute();
            Location defaultLoc = attr.getDefaultContent();
            assertEquals(0, defaultLoc.getX());
            assertEquals(0, defaultLoc.getY());
            assertEquals(0, defaultLoc.getZ());
            assertFalse(defaultLoc.isWorldLoaded());
        }

        @DisplayName("default constructor uses default content")
        @Test
        void defaultConstructor_usesDefaultContent() {
            var attr = new AbilityLocationAttribute();
            assertFalse(attr.getContent().isWorldLoaded());
        }

        @DisplayName("value constructor stores provided location")
        @Test
        void valueConstructor_storesProvidedLocation() {
            World world = server.addSimpleWorld("test_world");
            Location loc = new Location(world, 10, 64, -20);
            var attr = new AbilityLocationAttribute(loc);
            assertEquals(loc, attr.getContent());
        }

        @DisplayName("create returns new instance with given location")
        @Test
        void create_returnsNewInstance() {
            World world = server.addSimpleWorld("location_test");
            Location loc = new Location(world, 5, 100, 5);
            var template = new AbilityLocationAttribute();
            AbilityAttribute<Location> created = template.create(loc);
            assertEquals(loc, created.getContent());
            assertNotSame(template, created);
        }

        @DisplayName("shouldContentBeSaved returns false when world is null")
        @Test
        void shouldContentBeSaved_returnsFalse_whenWorldIsNull() {
            var attr = new AbilityLocationAttribute();
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns true when world is present")
        @Test
        void shouldContentBeSaved_returnsTrue_whenWorldPresent() {
            World world = server.addSimpleWorld("save_test_world");
            var attr = new AbilityLocationAttribute(new Location(world, 1, 2, 3));
            assertTrue(attr.shouldContentBeSaved());
        }

        @DisplayName("getDatabaseKeyName returns location")
        @Test
        void getDatabaseKeyName_returnsLocation() {
            var attr = new AbilityLocationAttribute();
            assertEquals("location", attr.getDatabaseKeyName());
        }

        @DisplayName("getNamespacedKey matches registry constant")
        @Test
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityLocationAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_LOCATION_ATTRIBUTE, attr.getNamespacedKey());
        }

        @DisplayName("getDisplayPriority returns 30")
        @Test
        void getDisplayPriority_returnsThirty() {
            var attr = new AbilityLocationAttribute();
            assertEquals(30, attr.getDisplayPriority());
        }

        @DisplayName("serializeContent produces expected format")
        @Test
        void serializeContent_producesExpectedFormat() {
            World world = server.addSimpleWorld("roundtrip_world");
            Location original = new Location(world, 100, 64, -200);
            var attr = new AbilityLocationAttribute(original);
            String serialized = attr.serializeContent();
            String[] parts = serialized.split(";");
            assertEquals(4, parts.length);
            assertEquals(world.getUID().toString(), parts[3]);
        }

        @DisplayName("convertContent throws for wrong number of segments")
        @Test
        void convertContent_throwsForWrongSegments() {
            var attr = new AbilityLocationAttribute();
            assertThrows(Exception.class, () -> attr.convertContent("not;a;valid;location;format"));
        }

        @DisplayName("convertContent throws for non-numeric coordinates")
        @Test
        void convertContent_throwsForNonNumericCoords() {
            var attr = new AbilityLocationAttribute();
            assertThrows(Exception.class, () -> attr.convertContent("abc;def;ghi;" + UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("RemoteTransferItemSetAttribute")
    class RemoteTransferItemSet {

        @DisplayName("getDefaultContent returns empty set")
        @Test
        void getDefaultContent_returnsEmptySet() {
            var attr = new RemoteTransferItemSetAttribute();
            assertTrue(attr.getDefaultContent().isEmpty());
        }

        @DisplayName("default constructor uses empty set")
        @Test
        void defaultConstructor_usesEmptySet() {
            var attr = new RemoteTransferItemSetAttribute();
            assertTrue(attr.getContent().isEmpty());
        }

        @DisplayName("value constructor stores provided set")
        @Test
        void valueConstructor_storesProvidedSet() {
            Set<CustomItemWrapper> items = Set.of(new CustomItemWrapper(Material.STONE));
            var attr = new RemoteTransferItemSetAttribute(items);
            assertEquals(1, attr.getContent().size());
        }

        @DisplayName("create returns new instance with given set")
        @Test
        void create_returnsNewInstance() {
            Set<CustomItemWrapper> items = Set.of(new CustomItemWrapper(Material.IRON_ORE));
            var template = new RemoteTransferItemSetAttribute();
            AbilityAttribute<Set<CustomItemWrapper>> created = template.create(items);
            assertEquals(1, created.getContent().size());
            assertNotSame(template, created);
        }

        @DisplayName("shouldContentBeSaved returns false when empty")
        @Test
        void shouldContentBeSaved_returnsFalse_whenEmpty() {
            var attr = new RemoteTransferItemSetAttribute();
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns true when non-empty")
        @Test
        void shouldContentBeSaved_returnsTrue_whenNonEmpty() {
            Set<CustomItemWrapper> items = Set.of(new CustomItemWrapper(Material.DIAMOND_ORE));
            var attr = new RemoteTransferItemSetAttribute(items);
            assertTrue(attr.shouldContentBeSaved());
        }

        @DisplayName("convertContent parses CSV string into set")
        @Test
        void convertContent_parsesCSV() {
            var attr = new RemoteTransferItemSetAttribute();
            Set<CustomItemWrapper> result = attr.convertContent("STONE,IRON_ORE");
            assertEquals(2, result.size());
        }

        @DisplayName("convertContent handles single item")
        @Test
        void convertContent_handlesSingleItem() {
            var attr = new RemoteTransferItemSetAttribute();
            Set<CustomItemWrapper> result = attr.convertContent("GOLD_ORE");
            assertEquals(1, result.size());
        }

        @DisplayName("serializeContent produces CSV from materials")
        @Test
        void serializeContent_producesCSV() {
            Set<CustomItemWrapper> items = Set.of(new CustomItemWrapper(Material.STONE));
            var attr = new RemoteTransferItemSetAttribute(items);
            String serialized = attr.serializeContent();
            assertEquals("STONE", serialized);
        }

        @DisplayName("serializeContent returns empty string for empty set")
        @Test
        void serializeContent_returnsEmpty_forEmptySet() {
            var attr = new RemoteTransferItemSetAttribute();
            assertEquals("", attr.serializeContent());
        }

        @DisplayName("isCustomItemWrapperStored returns true for stored item")
        @Test
        void isCustomItemWrapperStored_returnsTrue_forStoredItem() {
            CustomItemWrapper wrapper = new CustomItemWrapper(Material.COAL_ORE);
            var attr = new RemoteTransferItemSetAttribute(Set.of(wrapper));
            assertTrue(attr.isCustomItemWrapperStored(wrapper));
        }

        @DisplayName("isCustomItemWrapperStored returns false for unstored item")
        @Test
        void isCustomItemWrapperStored_returnsFalse_forUnstoredItem() {
            var attr = new RemoteTransferItemSetAttribute();
            assertFalse(attr.isCustomItemWrapperStored(new CustomItemWrapper(Material.STONE)));
        }

        @DisplayName("getDatabaseKeyName returns remote_transfer_material_set")
        @Test
        void getDatabaseKeyName_returnsExpectedKey() {
            var attr = new RemoteTransferItemSetAttribute();
            assertEquals("remote_transfer_material_set", attr.getDatabaseKeyName());
        }

        @DisplayName("getNamespacedKey matches registry constant")
        @Test
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new RemoteTransferItemSetAttribute();
            assertEquals(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE, attr.getNamespacedKey());
        }

        @DisplayName("getDisplayPriority returns 40")
        @Test
        void getDisplayPriority_returnsForty() {
            var attr = new RemoteTransferItemSetAttribute();
            assertEquals(40, attr.getDisplayPriority());
        }
    }
}
