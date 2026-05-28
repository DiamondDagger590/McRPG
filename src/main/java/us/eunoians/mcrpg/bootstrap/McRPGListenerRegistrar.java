package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.hud.HudConfigFile;
import us.eunoians.mcrpg.display.hud.ActionBarHudTask;
import us.eunoians.mcrpg.listener.ability.OnAbilityActivateListener;
import us.eunoians.mcrpg.listener.ability.OnComboCompleteListener;
import us.eunoians.mcrpg.listener.ability.OnComboInputListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityCooldownExpireListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityPutOnCooldownListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityUnlockListener;
import us.eunoians.mcrpg.listener.ability.OnAttackAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnBleedActivateListener;
import us.eunoians.mcrpg.listener.ability.OnBlockBreakListener;
import us.eunoians.mcrpg.listener.ability.OnBlockDropItemListener;
import us.eunoians.mcrpg.listener.ability.OnExtraOreActivateListener;
import us.eunoians.mcrpg.listener.ability.OnFoodLevelChangeAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnInteractAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnPlayerMoveAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnSneakAbilityListener;
import us.eunoians.mcrpg.listener.entity.EntitySpawnListener;
import us.eunoians.mcrpg.listener.entity.player.CorePlayerLoadListener;
import us.eunoians.mcrpg.listener.entity.player.CorePlayerUnloadListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerJoinListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerLeaveListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerPickupItemListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerSafeZoneStateChangeListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerSettingChangeListener;
import us.eunoians.mcrpg.listener.board.BoardRotationNotificationListener;
import us.eunoians.mcrpg.listener.quest.AbilityUpgradeQuestListener;
import us.eunoians.mcrpg.listener.quest.AbilityActivateQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.AbilityUnlockQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.AdvancementCompleteQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.AnvilRepairQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.BlockBreakQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.BlockPlaceQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.BreedAnimalQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.BucketEmptyQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.BucketFillQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.ConsumeItemQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.CraftItemQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.DamageTakenQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.DealDamageQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.DistanceTraveledQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.EnchantItemQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.EnterBedQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.FertilizeBlockQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.FishCatchQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.GainExperienceQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.GuiOpenQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.HarvestCropQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.ItemPickupQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.LaunchProjectileQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.LoadoutEquipQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.MobKillQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.ProjectileHitQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.QuestBoardAcceptQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.QuestStartAutoCompleteListener;
import us.eunoians.mcrpg.listener.quest.QuestStartMessageListener;
import us.eunoians.mcrpg.listener.quest.ShearEntityQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.SkillLevelQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.SmeltItemQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.SmithingQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.TameAnimalQuestProgressListener;
import us.eunoians.mcrpg.listener.quest.VillagerTradeQuestProgressListener;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.board.distribution.DistributionCompletionService;
import us.eunoians.mcrpg.quest.board.distribution.QuestContributionAggregator;
import us.eunoians.mcrpg.quest.board.distribution.QuestRewardDistributionResolver;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionGranter;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.listener.quest.QuestCancelListener;
import us.eunoians.mcrpg.listener.quest.QuestChainCancelListener;
import us.eunoians.mcrpg.listener.quest.QuestChainFirstJoinListener;
import us.eunoians.mcrpg.listener.quest.QuestChainLoginListener;
import us.eunoians.mcrpg.listener.quest.QuestChainProgressListener;
import us.eunoians.mcrpg.listener.quest.QuestCompleteListener;
import us.eunoians.mcrpg.quest.board.QuestBoardTerminator;
import us.eunoians.mcrpg.listener.quest.QuestFeedbackListener;
import us.eunoians.mcrpg.listener.quest.QuestObjectiveCompleteListener;
import us.eunoians.mcrpg.listener.quest.QuestPhaseCompleteListener;
import us.eunoians.mcrpg.listener.quest.QuestProgressNotificationListener;
import us.eunoians.mcrpg.listener.quest.QuestStageCompleteListener;
import us.eunoians.mcrpg.listener.quest.QuestStartListener;
import us.eunoians.mcrpg.listener.skill.OnAttackLevelListener;
import us.eunoians.mcrpg.listener.skill.OnBlockBreakLevelListener;
import us.eunoians.mcrpg.listener.skill.OnSkillLevelUpListener;
import us.eunoians.mcrpg.listener.statistic.AbilityStatisticListener;
import us.eunoians.mcrpg.listener.statistic.CombatStatisticListener;
import us.eunoians.mcrpg.listener.statistic.SkillStatisticListener;
import us.eunoians.mcrpg.listener.world.FakeBlockBreakListener;

/**
 * This registrar is in charge of registering {@link org.bukkit.event.Listener}s
 * for McRPG.
 */
final class McRPGListenerRegistrar implements Registrar<McRPG> {
    
    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        McRPG plugin = context.plugin();
        // Player load/save
        if (context.startupProfile() == StartupProfile.PROD) {
            Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
            Bukkit.getPluginManager().registerEvents(new PlayerLeaveListener(), plugin);
            Bukkit.getPluginManager().registerEvents(new CorePlayerLoadListener(), plugin);
            Bukkit.getPluginManager().registerEvents(new CorePlayerUnloadListener(), plugin);
        }

        // Combo activation listeners (PoC)
        ComboManager comboManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMBO);
        Bukkit.getPluginManager().registerEvents(new OnComboInputListener(comboManager), plugin);
        Bukkit.getPluginManager().registerEvents(new OnComboCompleteListener(), plugin);

        // Action bar HUD task
        int hudInterval = plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.HUD_CONFIG)
                .getInt(HudConfigFile.ACTION_BAR_UPDATE_INTERVAL_TICKS, 2);
        new ActionBarHudTask(plugin, hudInterval).start();

        // Ability activation/ready listeners
        Bukkit.getPluginManager().registerEvents(new OnAttackAbilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnBleedActivateListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnInteractAbilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnSneakAbilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnBlockBreakListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnBlockDropItemListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnExtraOreActivateListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnFoodLevelChangeAbilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnPlayerMoveAbilityListener(), plugin);

        // Skill listeners
        Bukkit.getPluginManager().registerEvents(new OnSkillLevelUpListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnAttackLevelListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnBlockBreakLevelListener(), plugin);

        // Ability listeners
        Bukkit.getPluginManager().registerEvents(new OnAbilityUnlockListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnAbilityCooldownExpireListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnAbilityPutOnCooldownListener(), plugin);

        // Quest Listeners
        QuestManager questManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        QuestChainManager chainManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_CHAIN);
        Bukkit.getPluginManager().registerEvents(new QuestChainProgressListener(chainManager), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestChainCancelListener(chainManager), plugin);
        if (context.startupProfile() == StartupProfile.PROD) {
            Bukkit.getPluginManager().registerEvents(new QuestChainLoginListener(chainManager), plugin);
            Bukkit.getPluginManager().registerEvents(new QuestChainFirstJoinListener(chainManager), plugin);
        }
        QuestBoardTerminator questBoardTerminator = new QuestBoardTerminator(plugin);
        var rarityRegistry = plugin.registryAccess().registry(McRPGRegistryKey.QUEST_RARITY);
        var distTypeRegistry = plugin.registryAccess().registry(McRPGRegistryKey.REWARD_DISTRIBUTION_TYPE);
        var contributionAggregator = new QuestContributionAggregator();
        var distributionResolver = new QuestRewardDistributionResolver(plugin.getLogger());
        var distributionService = new DistributionCompletionService(
                rarityRegistry, distTypeRegistry, new RewardDistributionGranter(plugin), contributionAggregator, distributionResolver);
        Bukkit.getPluginManager().registerEvents(new QuestStartListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestCompleteListener(questBoardTerminator, distributionService, contributionAggregator), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestCancelListener(questBoardTerminator), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestObjectiveCompleteListener(distributionService, contributionAggregator), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestStageCompleteListener(distributionService, contributionAggregator), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestPhaseCompleteListener(distributionService, contributionAggregator), plugin);
        Bukkit.getPluginManager().registerEvents(new AbilityUpgradeQuestListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new BlockBreakQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new MobKillQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new CraftItemQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new ConsumeItemQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new SmeltItemQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new EnchantItemQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new SmithingQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new ItemPickupQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new AnvilRepairQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new BreedAnimalQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new TameAnimalQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new ShearEntityQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new VillagerTradeQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new FishCatchQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new HarvestCropQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new BucketFillQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new BucketEmptyQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new FertilizeBlockQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new DealDamageQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new DamageTakenQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new ProjectileHitQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new LaunchProjectileQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new GainExperienceQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new EnterBedQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new AdvancementCompleteQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new DistanceTraveledQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new SkillLevelQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new GuiOpenQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new AbilityUnlockQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new AbilityActivateQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new LoadoutEquipQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestBoardAcceptQuestProgressListener(questManager), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestFeedbackListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestProgressNotificationListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new BoardRotationNotificationListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestStartMessageListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestStartAutoCompleteListener(questManager), plugin);

        // World listener
        Bukkit.getPluginManager().registerEvents(new FakeBlockBreakListener(), plugin);
        CustomBlockData.registerListener(plugin);

        // Entity Listeners
        Bukkit.getPluginManager().registerEvents(new EntitySpawnListener(), plugin);

        // Debug Listener
        Bukkit.getPluginManager().registerEvents(new OnAbilityActivateListener(), plugin);

        // Setting listener
        Bukkit.getPluginManager().registerEvents(new PlayerSettingChangeListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerPickupItemListener(), plugin);

        // Statistic listeners
        Bukkit.getPluginManager().registerEvents(new SkillStatisticListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new AbilityStatisticListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new CombatStatisticListener(), plugin);

        // Safe zones
        Bukkit.getPluginManager().registerEvents(new PlayerSafeZoneStateChangeListener(), plugin);
    }
}
