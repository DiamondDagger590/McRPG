package us.eunoians.mcrpg.ability.attribute;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityAttributeRegistryTest extends McRPGBaseTest {

    private AbilityAttributeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AbilityAttributeRegistry();
    }

    @Nested
    @DisplayName("Default registration")
    class DefaultRegistration {

        @DisplayName("tier attribute is registered by default")
        @Test
        void tierAttribute_registeredByDefault() {
            assertTrue(registry.registered(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
        }

        @DisplayName("cooldown attribute is registered by default")
        @Test
        void cooldownAttribute_registeredByDefault() {
            assertTrue(registry.registered(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY));
        }

        @DisplayName("toggled off attribute is registered by default")
        @Test
        void toggledOffAttribute_registeredByDefault() {
            assertTrue(registry.registered(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY));
        }

        @DisplayName("unlocked attribute is registered by default")
        @Test
        void unlockedAttribute_registeredByDefault() {
            assertTrue(registry.registered(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE));
        }

        @DisplayName("quest attribute is registered by default")
        @Test
        void questAttribute_registeredByDefault() {
            assertTrue(registry.registered(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE));
        }

        @DisplayName("location attribute is registered by default")
        @Test
        void locationAttribute_registeredByDefault() {
            assertTrue(registry.registered(AbilityAttributeRegistry.ABILITY_LOCATION_ATTRIBUTE));
        }

        @DisplayName("remote transfer item set attribute is registered by default")
        @Test
        void remoteTransferItemSetAttribute_registeredByDefault() {
            assertTrue(registry.registered(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE));
        }

        @DisplayName("mass harvest pull items attribute is registered by default")
        @Test
        void massHarvestPullItemsAttribute_registeredByDefault() {
            assertTrue(registry.registered(AbilityAttributeRegistry.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE));
        }
    }

    @Nested
    @DisplayName("getAttribute by NamespacedKey")
    class GetAttributeByKey {

        @DisplayName("returns present for registered key")
        @Test
        void getAttribute_byKey_returnsPresent() {
            Optional<AbilityAttribute<?>> result = registry.getAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            assertTrue(result.isPresent());
        }

        @DisplayName("returned attribute has default content")
        @Test
        void getAttribute_byKey_hasDefaultContent() {
            Optional<AbilityAttribute<?>> result = registry.getAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            assertTrue(result.isPresent());
            assertEquals(1, result.get().getContent());
        }

        @DisplayName("returns empty for unregistered key")
        @Test
        void getAttribute_byKey_returnsEmpty_forUnregistered() {
            NamespacedKey unknownKey = new NamespacedKey("test", "unknown_attr");
            assertTrue(registry.getAttribute(unknownKey).isEmpty());
        }
    }

    @Nested
    @DisplayName("getAttribute by database name")
    class GetAttributeByDatabaseName {

        @DisplayName("returns present for tier database name")
        @Test
        void getAttribute_byDatabaseName_tier_returnsPresent() {
            assertTrue(registry.getAttribute("tier").isPresent());
        }

        @DisplayName("returns present for cooldown database name")
        @Test
        void getAttribute_byDatabaseName_cooldown_returnsPresent() {
            assertTrue(registry.getAttribute("cooldown").isPresent());
        }

        @DisplayName("returns present for toggled database name")
        @Test
        void getAttribute_byDatabaseName_toggled_returnsPresent() {
            assertTrue(registry.getAttribute("toggled").isPresent());
        }

        @DisplayName("returns present for unlocked database name")
        @Test
        void getAttribute_byDatabaseName_unlocked_returnsPresent() {
            assertTrue(registry.getAttribute("unlocked").isPresent());
        }

        @DisplayName("returns empty for unknown database name")
        @Test
        void getAttribute_byDatabaseName_unknown_returnsEmpty() {
            assertTrue(registry.getAttribute("nonexistent").isEmpty());
        }
    }

    @Nested
    @DisplayName("registered by AbilityAttribute object")
    class RegisteredByObject {

        @DisplayName("returns true for a registered attribute instance")
        @Test
        void registered_byObject_returnsTrue() {
            var tier = new AbilityTierAttribute();
            assertTrue(registry.registered(tier));
        }

        @DisplayName("returns false for an unregistered attribute with unknown key")
        @Test
        void registered_byObject_returnsFalse_forUnknown() {
            NamespacedKey unknownKey = new NamespacedKey("test", "custom_attr");
            var custom = new AbilityTierAttribute(1) {
                @Override
                public NamespacedKey getNamespacedKey() {
                    return unknownKey;
                }
            };
            assertFalse(registry.registered(custom));
        }
    }

    @Nested
    @DisplayName("Custom attribute registration")
    class CustomRegistration {

        @DisplayName("register adds custom attribute accessible by key")
        @Test
        void register_customAttribute_accessibleByKey() {
            NamespacedKey customKey = new NamespacedKey("test", "custom_attr");
            var custom = new AbilityTierAttribute(99) {
                @Override
                public NamespacedKey getNamespacedKey() {
                    return customKey;
                }

                @Override
                public String getDatabaseKeyName() {
                    return "custom_db";
                }
            };
            registry.register(custom);
            assertTrue(registry.registered(customKey));
            assertTrue(registry.getAttribute(customKey).isPresent());
        }

        @DisplayName("register adds custom attribute accessible by database name")
        @Test
        void register_customAttribute_accessibleByDatabaseName() {
            NamespacedKey customKey = new NamespacedKey("test", "custom_attr2");
            var custom = new AbilityTierAttribute(99) {
                @Override
                public NamespacedKey getNamespacedKey() {
                    return customKey;
                }

                @Override
                public String getDatabaseKeyName() {
                    return "custom_db_name";
                }
            };
            registry.register(custom);
            assertTrue(registry.getAttribute("custom_db_name").isPresent());
        }

        @DisplayName("re-registering same key overwrites previous attribute")
        @Test
        void register_sameKey_overwritesPrevious() {
            var first = new AbilityTierAttribute(1);
            var second = new AbilityTierAttribute(99);
            registry.register(first);
            registry.register(second);
            Optional<AbilityAttribute<?>> result = registry.getAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            assertTrue(result.isPresent());
            assertEquals(99, result.get().getContent());
        }
    }
}
