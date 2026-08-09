package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecializedAbilityAttributeCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("AbilityLocationAttribute")
    class LocationAttribute {

        @Test
        @DisplayName("default constructor uses default location")
        void defaultConstructor_usesDefaultLocation() {
            var attr = new AbilityLocationAttribute();
            Location loc = attr.getContent();
            assertNotNull(loc);
            assertEquals(0, loc.getBlockX());
            assertEquals(0, loc.getBlockY());
            assertEquals(0, loc.getBlockZ());
        }

        @Test
        @DisplayName("getDefaultContent returns location with null world")
        void getDefaultContent_returnsLocationWithNullWorld() {
            var attr = new AbilityLocationAttribute();
            Location defaultLoc = attr.getDefaultContent();
            assertNotNull(defaultLoc);
            assertEquals(null, defaultLoc.getWorld());
        }

        @Test
        @DisplayName("value constructor stores provided location")
        void valueConstructor_storesProvidedLocation() {
            World world = server.addSimpleWorld("test_world");
            Location loc = new Location(world, 10, 64, -30);
            var attr = new AbilityLocationAttribute(loc);
            assertEquals(loc, attr.getContent());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns false when world is null")
        void shouldContentBeSaved_returnsFalse_whenWorldIsNull() {
            var attr = new AbilityLocationAttribute();
            assertFalse(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns true when world is non-null")
        void shouldContentBeSaved_returnsTrue_whenWorldIsNonNull() {
            World world = server.addSimpleWorld("test_world");
            var attr = new AbilityLocationAttribute(new Location(world, 1, 2, 3));
            assertTrue(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("create returns new instance with given location")
        void create_returnsNewInstance() {
            var template = new AbilityLocationAttribute();
            World world = server.addSimpleWorld("test_world");
            Location loc = new Location(world, 5, 10, 15);
            AbilityAttribute<Location> created = template.create(loc);
            assertEquals(loc, created.getContent());
            assertNotSame(template, created);
        }

        @Test
        @DisplayName("getDisplayPriority returns 30")
        void getDisplayPriority_returnsThirty() {
            var attr = new AbilityLocationAttribute();
            assertEquals(30, attr.getDisplayPriority());
        }

        @Test
        @DisplayName("getDatabaseKeyName returns location")
        void getDatabaseKeyName_returnsLocation() {
            var attr = new AbilityLocationAttribute();
            assertEquals("location", attr.getDatabaseKeyName());
        }

        @Test
        @DisplayName("getNamespacedKey matches registry constant")
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityLocationAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_LOCATION_ATTRIBUTE, attr.getNamespacedKey());
        }

        @Test
        @DisplayName("serializeContent produces delimited string with world UUID")
        void serializeContent_producesDelimitedString() {
            World world = server.addSimpleWorld("serialize_world");
            Location loc = new Location(world, 10, 64, -30);
            var attr = new AbilityLocationAttribute(loc);
            String serialized = attr.serializeContent();
            assertNotNull(serialized);
            assertFalse(serialized.isEmpty());
            assertTrue(serialized.contains(world.getUID().toString()));
        }

        @Test
        @DisplayName("convertContent throws when world UUID is unresolvable")
        void convertContent_throws_whenWorldUnresolvable() {
            var attr = new AbilityLocationAttribute();
            assertThrows(RuntimeException.class,
                    () -> attr.convertContent("10;64;-30;" + UUID.randomUUID()));
        }

        @Test
        @DisplayName("convertContent throws on invalid serialized location")
        void convertContent_throwsOnInvalidInput() {
            var attr = new AbilityLocationAttribute();
            assertThrows(RuntimeException.class, () -> attr.convertContent("not_a_location"));
        }
    }

    @Nested
    @DisplayName("RemoteTransferItemSetAttribute")
    class RemoteTransferItemSetTests {

        @Test
        @DisplayName("default constructor uses empty set")
        void defaultConstructor_usesEmptySet() {
            var attr = new RemoteTransferItemSetAttribute();
            assertNotNull(attr.getContent());
            assertTrue(attr.getContent().isEmpty());
        }

        @Test
        @DisplayName("getDefaultContent returns empty set")
        void getDefaultContent_returnsEmptySet() {
            var attr = new RemoteTransferItemSetAttribute();
            Set<CustomItemWrapper> defaultContent = attr.getDefaultContent();
            assertNotNull(defaultContent);
            assertTrue(defaultContent.isEmpty());
        }

        @Test
        @DisplayName("value constructor stores provided set")
        void valueConstructor_storesProvidedSet() {
            Set<CustomItemWrapper> items = Set.of(new CustomItemWrapper("STONE"), new CustomItemWrapper("IRON_ORE"));
            var attr = new RemoteTransferItemSetAttribute(new HashSet<>(items));
            assertEquals(2, attr.getContent().size());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns false when set is empty")
        void shouldContentBeSaved_returnsFalse_whenEmpty() {
            var attr = new RemoteTransferItemSetAttribute();
            assertFalse(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns true when set is non-empty")
        void shouldContentBeSaved_returnsTrue_whenNonEmpty() {
            Set<CustomItemWrapper> items = new HashSet<>();
            items.add(new CustomItemWrapper("DIAMOND_ORE"));
            var attr = new RemoteTransferItemSetAttribute(items);
            assertTrue(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("create returns new instance with given set")
        void create_returnsNewInstance() {
            var template = new RemoteTransferItemSetAttribute();
            Set<CustomItemWrapper> items = new HashSet<>();
            items.add(new CustomItemWrapper("GOLD_ORE"));
            AbilityAttribute<Set<CustomItemWrapper>> created = template.create(items);
            assertEquals(1, created.getContent().size());
            assertNotSame(template, created);
        }

        @Test
        @DisplayName("convertContent parses comma-separated materials")
        void convertContent_parsesCommaSeparated() {
            var attr = new RemoteTransferItemSetAttribute();
            Set<CustomItemWrapper> result = attr.convertContent("STONE,IRON_ORE,DIAMOND_ORE");
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("convertContent with single value returns singleton set")
        void convertContent_singleValue_returnsSingleton() {
            var attr = new RemoteTransferItemSetAttribute();
            Set<CustomItemWrapper> result = attr.convertContent("COAL_ORE");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("serializeContent returns comma-separated string")
        void serializeContent_returnsCommaSeparated() {
            Set<CustomItemWrapper> items = new HashSet<>();
            items.add(new CustomItemWrapper("STONE"));
            var attr = new RemoteTransferItemSetAttribute(items);
            String serialized = attr.serializeContent();
            assertEquals("STONE", serialized);
        }

        @Test
        @DisplayName("serializeContent returns empty string when set is empty")
        void serializeContent_returnsEmpty_whenSetIsEmpty() {
            var attr = new RemoteTransferItemSetAttribute();
            assertEquals("", attr.serializeContent());
        }

        @Test
        @DisplayName("isCustomItemWrapperStored returns true for stored item")
        void isCustomItemWrapperStored_returnsTrue_forStoredItem() {
            Set<CustomItemWrapper> items = new HashSet<>();
            CustomItemWrapper wrapper = new CustomItemWrapper("STONE");
            items.add(wrapper);
            var attr = new RemoteTransferItemSetAttribute(items);
            assertTrue(attr.isCustomItemWrapperStored(wrapper));
        }

        @Test
        @DisplayName("isCustomItemWrapperStored returns false for unstored item")
        void isCustomItemWrapperStored_returnsFalse_forUnstoredItem() {
            var attr = new RemoteTransferItemSetAttribute();
            assertFalse(attr.isCustomItemWrapperStored(new CustomItemWrapper("STONE")));
        }

        @Test
        @DisplayName("getDisplayPriority returns 40")
        void getDisplayPriority_returnsForty() {
            var attr = new RemoteTransferItemSetAttribute();
            assertEquals(40, attr.getDisplayPriority());
        }

        @Test
        @DisplayName("getDatabaseKeyName returns remote_transfer_material_set")
        void getDatabaseKeyName_returnsExpected() {
            var attr = new RemoteTransferItemSetAttribute();
            assertEquals("remote_transfer_material_set", attr.getDatabaseKeyName());
        }

        @Test
        @DisplayName("getNamespacedKey matches registry constant")
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new RemoteTransferItemSetAttribute();
            assertEquals(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE, attr.getNamespacedKey());
        }
    }

    @Nested
    @DisplayName("MassHarvestPullItemsAttribute")
    class MassHarvestPullItemsTests {

        @Test
        @DisplayName("default constructor uses true as default")
        void defaultConstructor_usesTrue() {
            var attr = new MassHarvestPullItemsAttribute();
            assertTrue(attr.getContent());
        }

        @Test
        @DisplayName("getDefaultContent returns true")
        void getDefaultContent_returnsTrue() {
            var attr = new MassHarvestPullItemsAttribute();
            assertTrue(attr.getDefaultContent());
        }

        @Test
        @DisplayName("value constructor stores provided value")
        void valueConstructor_storesProvidedValue() {
            var attr = new MassHarvestPullItemsAttribute(false);
            assertFalse(attr.getContent());
        }

        @DisplayName("shouldContentBeSaved returns false when content is true (default)")
        @Test
        void shouldContentBeSaved_returnsFalse_whenTrue() {
            var attr = new MassHarvestPullItemsAttribute(true);
            assertFalse(attr.shouldContentBeSaved());
        }

        @DisplayName("shouldContentBeSaved returns true when content is false (non-default)")
        @Test
        void shouldContentBeSaved_returnsTrue_whenFalse() {
            var attr = new MassHarvestPullItemsAttribute(false);
            assertTrue(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("create returns new instance with given value")
        void create_returnsNewInstance() {
            var template = new MassHarvestPullItemsAttribute();
            MassHarvestPullItemsAttribute created = template.create(false);
            assertFalse(created.getContent());
            assertNotSame(template, created);
        }

        @Test
        @DisplayName("convertContent parses true string")
        void convertContent_parsesTrue() {
            var attr = new MassHarvestPullItemsAttribute();
            assertTrue(attr.convertContent("true"));
        }

        @Test
        @DisplayName("convertContent parses false string")
        void convertContent_parsesFalse() {
            var attr = new MassHarvestPullItemsAttribute();
            assertFalse(attr.convertContent("false"));
        }

        @Test
        @DisplayName("convertContent returns false for non-boolean string")
        void convertContent_returnsFalse_forNonBooleanInput() {
            var attr = new MassHarvestPullItemsAttribute();
            assertFalse(attr.convertContent("notaboolean"));
        }

        @Test
        @DisplayName("getDisplayPriority returns 40")
        void getDisplayPriority_returnsForty() {
            var attr = new MassHarvestPullItemsAttribute();
            assertEquals(40, attr.getDisplayPriority());
        }

        @Test
        @DisplayName("getDatabaseKeyName returns mass_harvest_pull_items_toggled")
        void getDatabaseKeyName_returnsExpected() {
            var attr = new MassHarvestPullItemsAttribute();
            assertEquals("mass_harvest_pull_items_toggled", attr.getDatabaseKeyName());
        }

        @Test
        @DisplayName("getNamespacedKey matches registry constant")
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new MassHarvestPullItemsAttribute();
            assertEquals(AbilityAttributeRegistry.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE, attr.getNamespacedKey());
        }

        @Test
        @DisplayName("serializeContent returns boolean string")
        void serializeContent_returnsBooleanString() {
            var attrTrue = new MassHarvestPullItemsAttribute(true);
            assertEquals("true", attrTrue.serializeContent());

            var attrFalse = new MassHarvestPullItemsAttribute(false);
            assertEquals("false", attrFalse.serializeContent());
        }
    }

    @Nested
    @DisplayName("AbilityUpgradeQuestAttribute")
    class UpgradeQuestAttributeTests {

        @Test
        @DisplayName("defaultUUID returns consistent sentinel value")
        void defaultUUID_returnsConsistentSentinel() {
            UUID first = AbilityUpgradeQuestAttribute.defaultUUID();
            UUID second = AbilityUpgradeQuestAttribute.defaultUUID();
            assertEquals(first, second);
            assertNotNull(first);
        }

        @Test
        @DisplayName("default constructor uses default UUID")
        void defaultConstructor_usesDefaultUUID() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals(AbilityUpgradeQuestAttribute.defaultUUID(), attr.getContent());
        }

        @Test
        @DisplayName("getDefaultContent returns default UUID")
        void getDefaultContent_returnsDefaultUUID() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals(AbilityUpgradeQuestAttribute.defaultUUID(), attr.getDefaultContent());
        }

        @Test
        @DisplayName("value constructor stores provided UUID")
        void valueConstructor_storesProvidedUUID() {
            UUID questId = UUID.randomUUID();
            var attr = new AbilityUpgradeQuestAttribute(questId);
            assertEquals(questId, attr.getContent());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns false when content is default UUID")
        void shouldContentBeSaved_returnsFalse_whenDefault() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertFalse(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns true when content is non-default UUID")
        void shouldContentBeSaved_returnsTrue_whenNonDefault() {
            var attr = new AbilityUpgradeQuestAttribute(UUID.randomUUID());
            assertTrue(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("create returns new instance with given UUID")
        void create_returnsNewInstance() {
            var template = new AbilityUpgradeQuestAttribute();
            UUID questId = UUID.randomUUID();
            AbilityAttribute<UUID> created = template.create(questId);
            assertEquals(questId, created.getContent());
            assertNotSame(template, created);
        }

        @Test
        @DisplayName("convertContent parses UUID string")
        void convertContent_parsesUUIDString() {
            var attr = new AbilityUpgradeQuestAttribute();
            UUID expected = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
            assertEquals(expected, attr.convertContent("a1b2c3d4-e5f6-7890-abcd-ef1234567890"));
        }

        @Test
        @DisplayName("convertContent throws on invalid UUID string")
        void convertContent_throwsOnInvalidInput() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertThrows(IllegalArgumentException.class, () -> attr.convertContent("not-a-uuid"));
        }

        @Test
        @DisplayName("getDisplayPriority returns 20")
        void getDisplayPriority_returnsTwenty() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals(20, attr.getDisplayPriority());
        }

        @Test
        @DisplayName("getDatabaseKeyName returns quest")
        void getDatabaseKeyName_returnsQuest() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals("quest", attr.getDatabaseKeyName());
        }

        @Test
        @DisplayName("getNamespacedKey matches registry constant")
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE, attr.getNamespacedKey());
        }

        @Test
        @DisplayName("serializeContent returns UUID string representation")
        void serializeContent_returnsUUIDString() {
            UUID questId = UUID.fromString("12345678-1234-1234-1234-123456789abc");
            var attr = new AbilityUpgradeQuestAttribute(questId);
            assertEquals("12345678-1234-1234-1234-123456789abc", attr.serializeContent());
        }

        @Test
        @DisplayName("create from string parses UUID")
        void create_fromString_parsesUUID() {
            var template = new AbilityUpgradeQuestAttribute();
            UUID expected = UUID.fromString("deadbeef-dead-beef-dead-beefdeadbeef");
            AbilityAttribute<UUID> created = template.create("deadbeef-dead-beef-dead-beefdeadbeef");
            assertEquals(expected, created.getContent());
        }

        @Test
        @DisplayName("getSlot throws for non-TierableAbility")
        void getSlot_throwsForNonTierableAbility() {
            var attr = new AbilityUpgradeQuestAttribute();
            us.eunoians.mcrpg.ability.Ability nonTierable = org.mockito.Mockito.mock(us.eunoians.mcrpg.ability.Ability.class);
            us.eunoians.mcrpg.entity.player.McRPGPlayer player = org.mockito.Mockito.mock(us.eunoians.mcrpg.entity.player.McRPGPlayer.class);
            org.mockito.Mockito.when(nonTierable.getName(player)).thenReturn("TestAbility");
            assertThrows(IllegalArgumentException.class, () -> attr.getSlot(player, nonTierable));
        }

        @Test
        @DisplayName("implements GuiModifiableAttribute")
        void implementsGuiModifiable() {
            var attr = new AbilityUpgradeQuestAttribute();
            assertInstanceOf(GuiModifiableAttribute.class, attr);
        }
    }

    @Nested
    @DisplayName("AbilityTierAttribute")
    class TierAttributeTests {

        @Test
        @DisplayName("default constructor uses default tier of 1")
        void defaultConstructor_usesDefaultTier() {
            var attr = new AbilityTierAttribute();
            assertEquals(1, attr.getContent());
        }

        @Test
        @DisplayName("getDefaultContent returns 1")
        void getDefaultContent_returnsOne() {
            var attr = new AbilityTierAttribute();
            assertEquals(1, attr.getDefaultContent());
        }

        @Test
        @DisplayName("value constructor stores provided tier")
        void valueConstructor_storesProvidedTier() {
            var attr = new AbilityTierAttribute(5);
            assertEquals(5, attr.getContent());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns false when tier is 1")
        void shouldContentBeSaved_returnsFalse_whenTierIsOne() {
            var attr = new AbilityTierAttribute(1);
            assertFalse(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns true when tier is greater than 1")
        void shouldContentBeSaved_returnsTrue_whenTierGreaterThanOne() {
            var attr = new AbilityTierAttribute(2);
            assertTrue(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("create returns new instance with given tier")
        void create_returnsNewInstance() {
            var template = new AbilityTierAttribute();
            AbilityTierAttribute created = template.create(3);
            assertEquals(3, created.getContent());
            assertNotSame(template, created);
        }

        @Test
        @DisplayName("convertContent parses integer string")
        void convertContent_parsesIntegerString() {
            var attr = new AbilityTierAttribute();
            assertEquals(7, attr.convertContent("7"));
        }

        @Test
        @DisplayName("convertContent throws on non-integer string")
        void convertContent_throwsOnNonIntegerString() {
            var attr = new AbilityTierAttribute();
            assertThrows(NumberFormatException.class, () -> attr.convertContent("not_a_number"));
        }

        @Test
        @DisplayName("getPlaceholderName returns tier")
        void getPlaceholderName_returnsTier() {
            var attr = new AbilityTierAttribute();
            assertEquals("tier", attr.getPlaceholderName());
        }

        @Test
        @DisplayName("getDisplayableContent returns tier as string")
        void getDisplayableContent_returnsTierAsString() {
            var attr = new AbilityTierAttribute(4);
            assertEquals("4", attr.getDisplayableContent());
        }

        @Test
        @DisplayName("getDatabaseKeyName returns tier")
        void getDatabaseKeyName_returnsTier() {
            var attr = new AbilityTierAttribute();
            assertEquals("tier", attr.getDatabaseKeyName());
        }

        @Test
        @DisplayName("getNamespacedKey matches registry constant")
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityTierAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY, attr.getNamespacedKey());
        }

        @Test
        @DisplayName("implements DisplayableAttribute")
        void implementsDisplayableAttribute() {
            var attr = new AbilityTierAttribute();
            assertInstanceOf(DisplayableAttribute.class, attr);
        }

        @Test
        @DisplayName("serializeContent returns tier string")
        void serializeContent_returnsTierString() {
            var attr = new AbilityTierAttribute(10);
            assertEquals("10", attr.serializeContent());
        }

        @Test
        @DisplayName("create from string parses integer")
        void create_fromString_parsesInteger() {
            var template = new AbilityTierAttribute();
            AbilityAttribute<Integer> created = template.create("6");
            assertEquals(6, created.getContent());
        }
    }

    @Nested
    @DisplayName("AbilityUnlockedAttribute")
    class UnlockedAttributeTests {

        @Test
        @DisplayName("default constructor uses false")
        void defaultConstructor_usesFalse() {
            var attr = new AbilityUnlockedAttribute();
            assertFalse(attr.getContent());
        }

        @Test
        @DisplayName("getDefaultContent returns false")
        void getDefaultContent_returnsFalse() {
            var attr = new AbilityUnlockedAttribute();
            assertFalse(attr.getDefaultContent());
        }

        @Test
        @DisplayName("value constructor stores provided value")
        void valueConstructor_storesProvidedValue() {
            var attr = new AbilityUnlockedAttribute(true);
            assertTrue(attr.getContent());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns false when not unlocked")
        void shouldContentBeSaved_returnsFalse_whenNotUnlocked() {
            var attr = new AbilityUnlockedAttribute(false);
            assertFalse(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns true when unlocked")
        void shouldContentBeSaved_returnsTrue_whenUnlocked() {
            var attr = new AbilityUnlockedAttribute(true);
            assertTrue(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("create returns new instance with given value")
        void create_returnsNewInstance() {
            var template = new AbilityUnlockedAttribute();
            AbilityAttribute<Boolean> created = template.create(true);
            assertTrue(created.getContent());
            assertNotSame(template, created);
        }

        @Test
        @DisplayName("convertContent parses true string")
        void convertContent_parsesTrue() {
            var attr = new AbilityUnlockedAttribute();
            assertTrue(attr.convertContent("true"));
        }

        @Test
        @DisplayName("convertContent parses false string")
        void convertContent_parsesFalse() {
            var attr = new AbilityUnlockedAttribute();
            assertFalse(attr.convertContent("false"));
        }

        @Test
        @DisplayName("convertContent returns false for non-boolean string")
        void convertContent_returnsFalse_forNonBooleanString() {
            var attr = new AbilityUnlockedAttribute();
            assertFalse(attr.convertContent("notaboolean"));
        }

        @Test
        @DisplayName("getDatabaseKeyName returns unlocked")
        void getDatabaseKeyName_returnsUnlocked() {
            var attr = new AbilityUnlockedAttribute();
            assertEquals("unlocked", attr.getDatabaseKeyName());
        }

        @Test
        @DisplayName("getNamespacedKey matches registry constant")
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityUnlockedAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE, attr.getNamespacedKey());
        }

        @Test
        @DisplayName("serializeContent returns boolean string")
        void serializeContent_returnsBooleanString() {
            var attrTrue = new AbilityUnlockedAttribute(true);
            assertEquals("true", attrTrue.serializeContent());

            var attrFalse = new AbilityUnlockedAttribute(false);
            assertEquals("false", attrFalse.serializeContent());
        }
    }

    @Nested
    @DisplayName("AbilityCooldownAttribute")
    class CooldownAttributeTests {

        @Test
        @DisplayName("default constructor uses 0L")
        void defaultConstructor_usesZero() {
            var attr = new AbilityCooldownAttribute();
            assertEquals(0L, attr.getContent());
        }

        @Test
        @DisplayName("getDefaultContent returns 0L")
        void getDefaultContent_returnsZero() {
            var attr = new AbilityCooldownAttribute();
            assertEquals(0L, attr.getDefaultContent());
        }

        @Test
        @DisplayName("value constructor stores provided value")
        void valueConstructor_storesProvidedValue() {
            var attr = new AbilityCooldownAttribute(5000L);
            assertEquals(5000L, attr.getContent());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns false when cooldown is 0")
        void shouldContentBeSaved_returnsFalse_whenZero() {
            var attr = new AbilityCooldownAttribute(0L);
            assertFalse(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns false when cooldown is in the past")
        void shouldContentBeSaved_returnsFalse_whenInThePast() {
            var attr = new AbilityCooldownAttribute(1L);
            assertFalse(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns true when cooldown is in the future")
        void shouldContentBeSaved_returnsTrue_whenInTheFuture() {
            long futureTime = System.currentTimeMillis() + 60_000;
            var attr = new AbilityCooldownAttribute(futureTime);
            assertTrue(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("create returns new instance with given value")
        void create_returnsNewInstance() {
            var template = new AbilityCooldownAttribute();
            AbilityCooldownAttribute created = template.create(12345L);
            assertEquals(12345L, created.getContent());
            assertNotSame(template, created);
        }

        @Test
        @DisplayName("convertContent parses long string")
        void convertContent_parsesLongString() {
            var attr = new AbilityCooldownAttribute();
            assertEquals(999999L, attr.convertContent("999999"));
        }

        @Test
        @DisplayName("convertContent throws on non-numeric string")
        void convertContent_throwsOnNonNumericString() {
            var attr = new AbilityCooldownAttribute();
            assertThrows(NumberFormatException.class, () -> attr.convertContent("not_a_number"));
        }

        @Test
        @DisplayName("getDatabaseKeyName returns cooldown")
        void getDatabaseKeyName_returnsCooldown() {
            var attr = new AbilityCooldownAttribute();
            assertEquals("cooldown", attr.getDatabaseKeyName());
        }

        @Test
        @DisplayName("getNamespacedKey matches registry constant")
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityCooldownAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY, attr.getNamespacedKey());
        }

        @Test
        @DisplayName("serializeContent returns long string")
        void serializeContent_returnsLongString() {
            var attr = new AbilityCooldownAttribute(42L);
            assertEquals("42", attr.serializeContent());
        }
    }

    @Nested
    @DisplayName("AbilityToggledOffAttribute")
    class ToggledOffAttributeTests {

        @Test
        @DisplayName("default constructor uses false")
        void defaultConstructor_usesFalse() {
            var attr = new AbilityToggledOffAttribute();
            assertFalse(attr.getContent());
        }

        @Test
        @DisplayName("getDefaultContent returns false")
        void getDefaultContent_returnsFalse() {
            var attr = new AbilityToggledOffAttribute();
            assertFalse(attr.getDefaultContent());
        }

        @Test
        @DisplayName("value constructor stores provided value")
        void valueConstructor_storesProvidedValue() {
            var attr = new AbilityToggledOffAttribute(true);
            assertTrue(attr.getContent());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns false when not toggled off")
        void shouldContentBeSaved_returnsFalse_whenNotToggledOff() {
            var attr = new AbilityToggledOffAttribute(false);
            assertFalse(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("shouldContentBeSaved returns true when toggled off")
        void shouldContentBeSaved_returnsTrue_whenToggledOff() {
            var attr = new AbilityToggledOffAttribute(true);
            assertTrue(attr.shouldContentBeSaved());
        }

        @Test
        @DisplayName("create returns new instance with given value")
        void create_returnsNewInstance() {
            var template = new AbilityToggledOffAttribute();
            AbilityAttribute<Boolean> created = template.create(true);
            assertTrue(created.getContent());
            assertNotSame(template, created);
        }

        @Test
        @DisplayName("convertContent parses true string")
        void convertContent_parsesTrue() {
            var attr = new AbilityToggledOffAttribute();
            assertTrue(attr.convertContent("true"));
        }

        @Test
        @DisplayName("convertContent parses false string")
        void convertContent_parsesFalse() {
            var attr = new AbilityToggledOffAttribute();
            assertFalse(attr.convertContent("false"));
        }

        @Test
        @DisplayName("getDatabaseKeyName returns toggled")
        void getDatabaseKeyName_returnsToggled() {
            var attr = new AbilityToggledOffAttribute();
            assertEquals("toggled", attr.getDatabaseKeyName());
        }

        @Test
        @DisplayName("getNamespacedKey matches registry constant")
        void getNamespacedKey_matchesRegistryConstant() {
            var attr = new AbilityToggledOffAttribute();
            assertEquals(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY, attr.getNamespacedKey());
        }

        @Test
        @DisplayName("getDisplayPriority returns 10")
        void getDisplayPriority_returnsTen() {
            var attr = new AbilityToggledOffAttribute();
            assertEquals(10, attr.getDisplayPriority());
        }

        @Test
        @DisplayName("implements GuiModifiableAttribute")
        void implementsGuiModifiable() {
            var attr = new AbilityToggledOffAttribute();
            assertInstanceOf(GuiModifiableAttribute.class, attr);
        }

        @Test
        @DisplayName("serializeContent returns boolean string")
        void serializeContent_returnsBooleanString() {
            var attrTrue = new AbilityToggledOffAttribute(true);
            assertEquals("true", attrTrue.serializeContent());

            var attrFalse = new AbilityToggledOffAttribute(false);
            assertEquals("false", attrFalse.serializeContent());
        }
    }
}
