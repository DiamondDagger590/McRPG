package us.eunoians.mcrpg.ability;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class ConfigurableTierableAbilityUpgradeQuestKeyTest extends McRPGBaseTest {

    @Test
    public void getUpgradeQuestKey_infersGenericKeyWhenMissing() {
        YamlDocument doc = Mockito.mock(YamlDocument.class);
        Mockito.when(doc.contains(any(Route.class))).thenReturn(false);

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:dummy_ability");
        DummyTierableAbility ability = new DummyTierableAbility(mcRPG, abilityKey, doc);

        Optional<NamespacedKey> questKey = ability.getUpgradeQuestKey(2);
        assertTrue(questKey.isPresent());
        assertEquals(NamespacedKey.fromString("mcrpg:dummy_ability_upgrade"), questKey.get());
    }

    @Test
    public void getUpgradeQuestKey_substitutesTierPlaceholder() {
        YamlDocument doc = Mockito.mock(YamlDocument.class);
        Mockito.when(doc.contains(any(Route.class))).thenReturn(true);
        Mockito.when(doc.getString(any(Route.class))).thenReturn("mcrpg:dummy_tier{tier}");

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:dummy");
        DummyTierableAbility ability = new DummyTierableAbility(mcRPG, abilityKey, doc);

        Optional<NamespacedKey> questKey = ability.getUpgradeQuestKey(3);
        assertTrue(questKey.isPresent());
        assertEquals(NamespacedKey.fromString("mcrpg:dummy_tier3"), questKey.get());
    }

    @Test
    public void getUpgradeQuestKey_prefersTierSpecificOverAllTiers() throws Exception {
        Path tmp = Files.createTempFile("tier_override_test", ".yml");
        tmp.toFile().deleteOnExit();
        String yaml =
                "ability:\n" +
                        "  tier-configuration:\n" +
                        "    all-tiers:\n" +
                        "      upgrade-quest: \"mcrpg:all_tiers_upgrade\"\n" +
                        "    tier-2:\n" +
                        "      upgrade-quest: \"mcrpg:specific_tier2\"\n";
        Files.writeString(tmp, yaml);
        YamlDocument doc = YamlDocument.create(tmp.toFile());

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:dummy_override");
        DummyTierableAbility ability = new DummyTierableAbility(mcRPG, abilityKey, doc);

        Optional<NamespacedKey> questKey = ability.getUpgradeQuestKey(2);
        assertTrue(questKey.isPresent());
        assertEquals(NamespacedKey.fromString("mcrpg:specific_tier2"), questKey.get());
    }

    private static final class DummyTierableAbility implements ConfigurableTierableAbility {
        private final McRPG plugin;
        private final NamespacedKey abilityKey;
        private final YamlDocument doc;

        private DummyTierableAbility(McRPG plugin, NamespacedKey abilityKey, YamlDocument doc) {
            this.plugin = plugin;
            this.abilityKey = abilityKey;
            this.doc = doc;
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return plugin;
        }

        @Override
        public @NotNull NamespacedKey getAbilityKey() {
            return abilityKey;
        }

        @Override
        public @NotNull Set<NamespacedKey> getApplicableAttributes() {
            return Set.of();
        }

        @Override
        public @NotNull String getDatabaseName() {
            return "dummy";
        }

        @Override
        public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        }

        @Override
        public boolean isPassive() {
            return true;
        }

        @Override
        public int getMaxTier() {
            return 5;
        }

        @Override
        public @NotNull YamlDocument getYamlDocument() {
            return doc;
        }

        @Override
        public @NotNull Route getDisplayItemRoute() {
            return Route.fromString("dummy");
        }

        @Override
        public @NotNull Route getAbilityEnabledRoute() {
            return Route.fromString("dummy.enabled");
        }

        @Override
        public @NotNull Route getAbilityTierConfigurationRoute() {
            return Route.fromString("ability.tier-configuration");
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}

