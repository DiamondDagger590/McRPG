package us.eunoians.mcrpg.ability;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class ConfigurableTierableAbilityUpgradeQuestKeyTest extends McRPGBaseTest {

    @Test
    public void getUpgradeQuestKey_infersGenericKeyWhenMissing() {
        YamlDocument doc = Mockito.mock(YamlDocument.class);
        Mockito.when(doc.contains(any(Route.class))).thenReturn(false);

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:dummy_ability");
        StubConfigurableTierableAbility ability = new StubConfigurableTierableAbility(mcRPG, abilityKey, doc);

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
        StubConfigurableTierableAbility ability = new StubConfigurableTierableAbility(mcRPG, abilityKey, doc);

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
        StubConfigurableTierableAbility ability = new StubConfigurableTierableAbility(mcRPG, abilityKey, doc);

        Optional<NamespacedKey> questKey = ability.getUpgradeQuestKey(2);
        assertTrue(questKey.isPresent());
        assertEquals(NamespacedKey.fromString("mcrpg:specific_tier2"), questKey.get());
    }

}

