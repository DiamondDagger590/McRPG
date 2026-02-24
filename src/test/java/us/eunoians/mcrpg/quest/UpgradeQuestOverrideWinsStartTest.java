package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPGBaseTest;
import net.kyori.adventure.text.Component;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.MobKillObjectiveType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeNextTierRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.CommandRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ExperienceRewardType;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpgradeQuestOverrideWinsStartTest extends McRPGBaseTest {

    @BeforeEach
    public void setup() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();

        AbilityRegistry abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        if (abilityRegistry == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
        }

        QuestObjectiveTypeRegistry objectiveTypes = registryAccess.registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        if (objectiveTypes.get(BlockBreakObjectiveType.KEY).isEmpty()) {
            objectiveTypes.register(new BlockBreakObjectiveType());
        }
        if (objectiveTypes.get(MobKillObjectiveType.KEY).isEmpty()) {
            objectiveTypes.register(new MobKillObjectiveType());
        }

        QuestRewardTypeRegistry rewardTypes = registryAccess.registry(McRPGRegistryKey.QUEST_REWARD_TYPE);
        if (rewardTypes.get(ExperienceRewardType.KEY).isEmpty()) {
            rewardTypes.register(new ExperienceRewardType());
        }
        if (rewardTypes.get(CommandRewardType.KEY).isEmpty()) {
            rewardTypes.register(new CommandRewardType());
        }
        if (rewardTypes.get(AbilityUpgradeRewardType.KEY).isEmpty()) {
            rewardTypes.register(new AbilityUpgradeRewardType());
        }
        if (rewardTypes.get(AbilityUpgradeNextTierRewardType.KEY).isEmpty()) {
            rewardTypes.register(new AbilityUpgradeNextTierRewardType());
        }
    }

    @DisplayName("Given both all-tiers and tier-specific upgrade quests, when starting upgrade quest, then tier-specific override is used")
    @Test
    public void overrideWins_whenBothConfigured_startsOverrideQuest() throws Exception {
        Path tmp = Files.createTempFile("override_wins", ".yml");
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

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:dummy_override_e2e");
        DummyTierableAbility ability = new DummyTierableAbility(mcRPG, abilityKey, doc);

        QuestManager questManager = new QuestManager(mcRPG);

        QuestDefinition allTiersDef = QuestTestHelper.singlePhaseQuest("all_tiers_upgrade");
        QuestDefinition tier2Def = QuestTestHelper.singlePhaseQuest("specific_tier2");

        Optional<NamespacedKey> resolvedKey = ability.getUpgradeQuestKey(2);
        assertTrue(resolvedKey.isPresent());
        assertEquals(NamespacedKey.fromString("mcrpg:specific_tier2"), resolvedKey.get());

        Map<NamespacedKey, QuestDefinition> defs = Map.of(
                allTiersDef.getQuestKey(), allTiersDef,
                tier2Def.getQuestKey(), tier2Def
        );
        QuestDefinition chosen = defs.get(resolvedKey.get());
        assertNotNull(chosen);

        var started = questManager.startQuest(chosen, UUID.randomUUID(), Map.of("tier", 2), new AbilityUpgradeQuestSource());
        assertTrue(started.isPresent());
        assertEquals(NamespacedKey.fromString("mcrpg:specific_tier2"), started.get().getQuestKey());
    }

    @DisplayName("Given tier-specific upgrade quest configured but missing definition, when resolving upgrade quest, then all-tiers fallback is used")
    @Test
    public void fallbackToAllTiers_whenOverrideMissing_usesAllTiersQuest() throws Exception {
        Path tmp = Files.createTempFile("override_missing", ".yml");
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

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:dummy_override_missing");
        DummyTierableAbility ability = new DummyTierableAbility(mcRPG, abilityKey, doc);

        QuestManager questManager = new QuestManager(mcRPG);
        QuestDefinition allTiersDef = QuestTestHelper.singlePhaseQuest("all_tiers_upgrade");
        questManager.getQuestDefinitionRegistry().register(allTiersDef);

        Optional<QuestDefinition> resolved = questManager.resolveUpgradeQuestDefinition(ability, 2);
        assertTrue(resolved.isPresent());
        assertEquals(allTiersDef.getQuestKey(), resolved.get().getQuestKey());
    }

    private static final class DummyTierableAbility implements ConfigurableTierableAbility {
        private final Plugin plugin;
        private final NamespacedKey abilityKey;
        private final YamlDocument doc;

        private DummyTierableAbility(Plugin plugin, NamespacedKey abilityKey, YamlDocument doc) {
            this.plugin = plugin;
            this.abilityKey = abilityKey;
            this.doc = doc;
        }

        @Override
        public int getMaxTier() {
            return 5;
        }

        @Override
        public int getUnlockLevelForTier(int tier) {
            return 1;
        }

        @Override
        public int getUpgradeCostForTier(int tier) {
            return 0;
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
        public @NotNull String getName(@NotNull McRPGPlayer player) {
            return "Dummy";
        }

        @Override
        public @NotNull String getName() {
            return "Dummy";
        }

        @Override
        public Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("Dummy");
        }

        @Override
        public Component getDisplayName() {
            return Component.text("Dummy");
        }

        @Override
        public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @Override
        public boolean isPassive() {
            return true;
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}

