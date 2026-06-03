package us.eunoians.mcrpg.ability;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityCooldownAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityToggledOffAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityDataTest extends McRPGBaseTest {

    private static final NamespacedKey TEST_ABILITY_KEY = new NamespacedKey("test", "ability");

    private AbilityData data;

    @BeforeEach
    void setUp() {
        data = new AbilityData(TEST_ABILITY_KEY);
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @DisplayName("varargs constructor stores all provided attributes")
        @Test
        void varargsConstructor_storesAllAttributes() {
            var tier = new AbilityTierAttribute(3);
            var unlocked = new AbilityUnlockedAttribute(true);
            var result = new AbilityData(TEST_ABILITY_KEY, tier, unlocked);

            assertTrue(result.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
            assertTrue(result.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE));
        }

        @DisplayName("collection constructor stores all provided attributes")
        @Test
        void collectionConstructor_storesAllAttributes() {
            var tier = new AbilityTierAttribute(3);
            var unlocked = new AbilityUnlockedAttribute(true);
            var result = new AbilityData(TEST_ABILITY_KEY, List.of(tier, unlocked));

            assertTrue(result.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
            assertTrue(result.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE));
        }

        @DisplayName("getAbilityKey returns provided key")
        @Test
        void getAbilityKey_returnsProvidedKey() {
            assertEquals(TEST_ABILITY_KEY, data.getAbilityKey());
        }
    }

    @Nested
    @DisplayName("addAttribute and doesAbilityHaveAttribute")
    class AddAndHasAttribute {

        @DisplayName("empty data has no attributes")
        @Test
        void emptyData_hasNoAttributes() {
            assertFalse(data.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
        }

        @DisplayName("added attribute is present")
        @Test
        void addAttribute_attributeIsPresent() {
            data.addAttribute(new AbilityTierAttribute(1));
            assertTrue(data.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
        }

        @DisplayName("doesAbilityHaveAttribute by object delegates to key check")
        @Test
        void doesAbilityHaveAttribute_byObject_delegatesToKeyCheck() {
            var tier = new AbilityTierAttribute(1);
            data.addAttribute(tier);
            assertTrue(data.doesAbilityHaveAttribute(tier));
        }

        @DisplayName("adding same attribute key overwrites value")
        @Test
        void addAttribute_sameKey_overwritesValue() {
            data.addAttribute(new AbilityTierAttribute(1));
            data.addAttribute(new AbilityTierAttribute(5));
            var attr = data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            assertTrue(attr.isPresent());
            assertEquals(5, ((AbilityTierAttribute) attr.get()).getContent());
        }
    }

    @Nested
    @DisplayName("getAbilityAttribute")
    class GetAbilityAttribute {

        @DisplayName("returns empty for missing attribute")
        @Test
        void getAbilityAttribute_returnEmpty_whenMissing() {
            assertTrue(data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY).isEmpty());
        }

        @DisplayName("returns attribute when present")
        @Test
        void getAbilityAttribute_returnsAttribute_whenPresent() {
            var tier = new AbilityTierAttribute(3);
            data.addAttribute(tier);
            var result = data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            assertTrue(result.isPresent());
            assertEquals(tier, result.get());
        }
    }

    @Nested
    @DisplayName("removeAttribute")
    class RemoveAttribute {

        @DisplayName("removeAttribute by object removes it")
        @Test
        void removeAttribute_byObject_removesIt() {
            var tier = new AbilityTierAttribute(1);
            data.addAttribute(tier);
            data.removeAttribute(tier);
            assertFalse(data.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
        }

        @DisplayName("removeAttribute by key removes it")
        @Test
        void removeAttribute_byKey_removesIt() {
            data.addAttribute(new AbilityTierAttribute(1));
            data.removeAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            assertFalse(data.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
        }

        @DisplayName("removing nonexistent attribute does nothing")
        @Test
        void removeAttribute_nonExistent_doesNothing() {
            data.removeAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            assertFalse(data.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
        }
    }

    @Nested
    @DisplayName("getAllAttributeKeys and getAllAttributes")
    class GetAllKeys {

        @DisplayName("getAllAttributeKeys returns empty set for empty data")
        @Test
        void getAllAttributeKeys_emptyData_returnsEmptySet() {
            assertTrue(data.getAllAttributeKeys().isEmpty());
        }

        @DisplayName("getAllAttributeKeys returns all registered keys")
        @Test
        void getAllAttributeKeys_returnsAllKeys() {
            data.addAttribute(new AbilityTierAttribute(1));
            data.addAttribute(new AbilityUnlockedAttribute(true));
            assertEquals(2, data.getAllAttributeKeys().size());
            assertTrue(data.getAllAttributeKeys().contains(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
            assertTrue(data.getAllAttributeKeys().contains(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE));
        }

        @DisplayName("getAllAttributes returns all registered attribute objects")
        @Test
        void getAllAttributes_returnsAllAttributes() {
            var tier = new AbilityTierAttribute(1);
            var unlocked = new AbilityUnlockedAttribute(true);
            data.addAttribute(tier);
            data.addAttribute(unlocked);
            assertEquals(2, data.getAllAttributes().size());
        }
    }

    @Nested
    @DisplayName("hasAttribute")
    class HasAttribute {

        @DisplayName("hasAttribute returns false for missing key")
        @Test
        void hasAttribute_returnsFalse_whenMissing() {
            assertFalse(data.hasAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
        }

        @DisplayName("hasAttribute returns true for present key")
        @Test
        void hasAttribute_returnsTrue_whenPresent() {
            data.addAttribute(new AbilityTierAttribute(1));
            assertTrue(data.hasAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
        }
    }

    @Nested
    @DisplayName("updateAttribute")
    class UpdateAttribute {

        @DisplayName("updateAttribute replaces content for existing attribute")
        @Test
        void updateAttribute_replacesContent() {
            var tier = new AbilityTierAttribute(1);
            data.addAttribute(tier);
            data.updateAttribute(tier, 5);
            var result = data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            assertTrue(result.isPresent());
            assertEquals(5, ((AbilityTierAttribute) result.get()).getContent());
        }

        @DisplayName("updateAttribute adds attribute if not yet present")
        @Test
        void updateAttribute_addsIfNotPresent() {
            var tier = new AbilityTierAttribute(1);
            data.updateAttribute(tier, 3);
            assertTrue(data.doesAbilityHaveAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
            var result = data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            assertTrue(result.isPresent());
            assertEquals(3, ((AbilityTierAttribute) result.get()).getContent());
        }
    }

    @Nested
    @DisplayName("Multiple attribute types coexist")
    class MultipleAttributes {

        @DisplayName("different attribute types can coexist")
        @Test
        void differentTypes_coexist() {
            data.addAttribute(new AbilityTierAttribute(3));
            data.addAttribute(new AbilityUnlockedAttribute(true));
            data.addAttribute(new AbilityToggledOffAttribute(false));
            data.addAttribute(new AbilityCooldownAttribute(1000L));

            assertEquals(4, data.getAllAttributeKeys().size());
            assertTrue(data.hasAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
            assertTrue(data.hasAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE));
            assertTrue(data.hasAttribute(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY));
            assertTrue(data.hasAttribute(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY));
        }

        @DisplayName("removing one attribute does not affect others")
        @Test
        void removeOne_doesNotAffectOthers() {
            data.addAttribute(new AbilityTierAttribute(3));
            data.addAttribute(new AbilityUnlockedAttribute(true));
            data.removeAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);

            assertFalse(data.hasAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY));
            assertTrue(data.hasAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE));
        }
    }
}
