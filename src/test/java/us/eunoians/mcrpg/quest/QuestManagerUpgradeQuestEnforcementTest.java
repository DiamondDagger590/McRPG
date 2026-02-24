package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.MobKillObjectiveType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeNextTierRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.CommandRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ExperienceRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestManagerUpgradeQuestEnforcementTest extends McRPGBaseTest {

    @Test
    public void givenMissingUpgradeQuestDefinition_whenQuestManagerLoads_thenTierableAbilityIsUnregistered() {
        // Ensure required registries exist for quest loading
        RegistryAccess registryAccess = RegistryAccess.registryAccess();

        // Ability registry is not registered by TestBootstrap; register it for this test.
        AbilityRegistry abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        if (abilityRegistry == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
            abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        }

        // Objective/reward types are needed to parse default quest resources during QuestManager construction.
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

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:missing_upgrade_def");
        abilityRegistry.register(new MissingUpgradeQuestAbility(mcRPG, abilityKey));

        QuestManager questManager = new QuestManager(mcRPG);
        questManager.loadQuestDefinitions();

        assertFalse(abilityRegistry.registered(abilityKey));
    }

    @Test
    public void givenInferredUpgradeQuestDefinitionExists_whenQuestManagerLoads_thenTierableAbilityRemainsRegistered() throws Exception {
        // Ensure required registries exist for quest loading
        RegistryAccess registryAccess = RegistryAccess.registryAccess();

        AbilityRegistry abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        if (abilityRegistry == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
            abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
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

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:inferred_pass");
        abilityRegistry.register(new InferredUpgradeQuestAbility(mcRPG, abilityKey));

        File questsDir = new File(mcRPG.getDataFolder(), "quests");
        File upgradesDir = new File(questsDir, "upgrades");
        upgradesDir.mkdirs();

        File inferredQuestFile = new File(questsDir, "inferred_pass_upgrade.yml");
        inferredQuestFile.deleteOnExit();
        String yaml = "quests:\n" +
                "  mcrpg:inferred_pass_upgrade:\n" +
                "    scope: \"mcrpg:single_player\"\n" +
                "    phases:\n" +
                "      phase:\n" +
                "        completion-mode: ALL\n" +
                "        stages:\n" +
                "          stage:\n" +
                "            key: \"mcrpg:stage_1\"\n" +
                "            objectives:\n" +
                "              objective:\n" +
                "                key: \"mcrpg:obj_1\"\n" +
                "                type: \"mcrpg:block_break\"\n" +
                "                required-progress: 1\n";
        Files.writeString(inferredQuestFile.toPath(), yaml);

        QuestManager questManager = new QuestManager(mcRPG);
        questManager.loadQuestDefinitions();

        assertTrue(abilityRegistry.registered(abilityKey));
    }

    private static final class MissingUpgradeQuestAbility implements TierableAbility {
        private final McRPG plugin;
        private final NamespacedKey key;

        private MissingUpgradeQuestAbility(@NotNull McRPG plugin, @NotNull NamespacedKey key) {
            this.plugin = plugin;
            this.key = key;
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
        public @NotNull Optional<NamespacedKey> getUpgradeQuestKey(int tier) {
            // Point at a quest definition that does not exist to trigger enforcement.
            return Optional.of(NamespacedKey.fromString("mcrpg:this_quest_does_not_exist"));
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return plugin;
        }

        @Override
        public @NotNull NamespacedKey getAbilityKey() {
            return key;
        }

        @Override
        public @NotNull Set<NamespacedKey> getApplicableAttributes() {
            return Set.of();
        }

        @Override
        public @NotNull String getDatabaseName() {
            return "missing_upgrade_def";
        }

        @Override
        public @NotNull String getName(@NotNull McRPGPlayer player) {
            return "Missing";
        }

        @Override
        public @NotNull String getName() {
            return "Missing";
        }

        @Override
        public Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("Missing");
        }

        @Override
        public Component getDisplayName() {
            return Component.text("Missing");
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

    private static final class InferredUpgradeQuestAbility implements ConfigurableTierableAbility {
        private final McRPG plugin;
        private final NamespacedKey key;

        private InferredUpgradeQuestAbility(@NotNull McRPG plugin, @NotNull NamespacedKey key) {
            this.plugin = plugin;
            this.key = key;
        }

        @Override
        public int getMaxTier() {
            return 5;
        }

        @Override
        public @NotNull YamlDocument getYamlDocument() {
            YamlDocument doc = Mockito.mock(YamlDocument.class);
            Mockito.when(doc.contains(ArgumentMatchers.any(Route.class)))
                    .thenReturn(false);
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
        public int getUnlockLevelForTier(int tier) {
            return 1;
        }

        @Override
        public int getUpgradeCostForTier(int tier) {
            return 0;
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return plugin;
        }

        @Override
        public @NotNull NamespacedKey getAbilityKey() {
            return key;
        }

        @Override
        public @NotNull Set<NamespacedKey> getApplicableAttributes() {
            return Set.of();
        }

        @Override
        public @NotNull String getDatabaseName() {
            return "inferred_pass";
        }

        @Override
        public @NotNull String getName(@NotNull McRPGPlayer player) {
            return "InferredPass";
        }

        @Override
        public @NotNull String getName() {
            return "InferredPass";
        }

        @Override
        public Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("InferredPass");
        }

        @Override
        public Component getDisplayName() {
            return Component.text("InferredPass");
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

