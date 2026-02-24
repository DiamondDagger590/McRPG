package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import net.kyori.adventure.text.Component;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestManagerSanityCheckUpgradeQuestsTest extends McRPGBaseTest {

    private AbilityRegistry abilityRegistry;

    @BeforeEach
    public void setup() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();

        abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        if (abilityRegistry == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
            abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        }

        // Needed because QuestManager constructor loads/parses default quest resources.
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

    @DisplayName("Given a stale AbilityUpgradeQuestAttribute, when sanity check runs, then it clears the attribute")
    @Test
    public void sanityCheckUpgradeQuests_clearsStaleUpgradeQuestAttribute() {
        // Create the QuestManager BEFORE registering the dummy ability so the constructor
        // doesn't unregister it due to missing upgrade quest key validation.
        QuestManager questManager = new QuestManager(mcRPG);

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:test_sanity_clear");
        TierableAbility ability = new DummyTierableAbility(mcRPG, abilityKey, Optional.empty());
        abilityRegistry.register(ability);

        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        SkillHolder abilityHolder = mock(SkillHolder.class);
        AbilityData abilityData = mock(AbilityData.class);
        UUID playerUUID = UUID.randomUUID();

        when(mcRPGPlayer.asSkillHolder()).thenReturn(abilityHolder);
        when(mcRPGPlayer.getUUID()).thenReturn(playerUUID);
        when(abilityHolder.getAbilityData(ability)).thenReturn(Optional.of(abilityData));
        when(abilityData.getAbilityAttribute(eq(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE)))
                .thenReturn(Optional.of(new AbilityUpgradeQuestAttribute(UUID.randomUUID())));

        questManager.sanityCheckUpgradeQuests(mcRPGPlayer);

        verify(abilityData, atLeastOnce()).addAttribute(eq(new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID())));
    }

    @DisplayName("Given eligible tierable ability and quest definition, when sanity check runs, then it starts quest and stores attribute")
    @Test
    public void sanityCheckUpgradeQuests_startsQuestAndSetsAttribute() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();

        // Provide a synchronous fake DB manager so the async submit path runs in-test.
        McRPGDatabaseManager mockDbManager = mock(McRPGDatabaseManager.class);
        Database mockDatabase = mock(Database.class);
        when(mockDbManager.getDatabase()).thenReturn(mockDatabase);
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        when(mockExecutor.submit(any(Runnable.class))).thenAnswer(inv -> {
            inv.<Runnable>getArgument(0).run();
            return null;
        });
        when(mockDatabase.getDatabaseExecutorService()).thenReturn(mockExecutor);
        when(mockDatabase.getConnection()).thenReturn(mock(Connection.class));
        try {
            registryAccess.registry(RegistryKey.MANAGER).register(mockDbManager);
        } catch (Exception ignored) {
        }

        UUID playerUUID = UUID.randomUUID();
        PlayerMock player = new PlayerMock(server, "Verum", playerUUID);
        server.addPlayer(player);

        NamespacedKey questKey = NamespacedKey.fromString("mcrpg:test_sanity_start_quest");
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("test_sanity_start_quest");

        // Create the QuestManager BEFORE registering the dummy ability so the constructor
        // doesn't unregister it due to missing upgrade quest key validation.
        QuestManager questManager = new TestQuestManager(mcRPG);
        questManager.getQuestDefinitionRegistry().register(def);

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:test_sanity_start");
        TierableAbility ability = new DummyTierableAbility(mcRPG, abilityKey, Optional.of(questKey));
        abilityRegistry.register(ability);

        AbilityData abilityData = mock(AbilityData.class);
        when(abilityData.getAbilityAttribute(eq(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE)))
                .thenReturn(Optional.of(new AbilityUpgradeQuestAttribute(UUID.randomUUID())));

        SkillHolder abilityHolder = mock(SkillHolder.class);
        when(abilityHolder.getAbilityData(ability)).thenReturn(Optional.of(abilityData));

        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.asSkillHolder()).thenReturn(abilityHolder);
        when(mcRPGPlayer.getUUID()).thenReturn(playerUUID);

        questManager.sanityCheckUpgradeQuests(mcRPGPlayer);

        // Run the scheduled sync task that performs the final startQuest() call.
        server.getScheduler().performOneTick();

        // First: stale attribute cleared. Second: new quest UUID stored.
        verify(abilityData, times(2)).addAttribute(any(AbilityUpgradeQuestAttribute.class));
    }

    private static final class TestQuestManager extends QuestManager {
        public TestQuestManager(McRPG plugin) {
            super(plugin);
        }

        @Override
        public boolean canPlayerStartQuest(@NotNull Connection connection,
                                           @NotNull UUID playerUUID,
                                           @NotNull QuestDefinition definition) {
            return true;
        }
    }

    private static final class DummyTierableAbility implements TierableAbility {
        private final Plugin plugin;
        private final NamespacedKey key;
        private final Optional<NamespacedKey> questKey;

        private DummyTierableAbility(Plugin plugin, NamespacedKey key, Optional<NamespacedKey> questKey) {
            this.plugin = plugin;
            this.key = key;
            this.questKey = questKey;
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
        public int getCurrentAbilityTier(@NotNull AbilityHolder abilityHolder) {
            return 1;
        }

        @Override
        public @NotNull Optional<NamespacedKey> getUpgradeQuestKey(int tier) {
            return questKey;
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
            return key.getKey();
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

