package us.eunoians.mcrpg.expansion;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.Statistic;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.ability.impl.herbalism.InstantIrrigation;
import us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest;
import us.eunoians.mcrpg.ability.impl.herbalism.TooManyPlants;
import us.eunoians.mcrpg.ability.impl.herbalism.VerdantSurge;
import us.eunoians.mcrpg.ability.impl.mining.ExtraOre;
import us.eunoians.mcrpg.ability.impl.mining.ItsATriple;
import us.eunoians.mcrpg.ability.impl.mining.OreScanner;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.ability.impl.swords.DeeperWound;
import us.eunoians.mcrpg.ability.impl.swords.EnhancedBleed;
import us.eunoians.mcrpg.ability.impl.swords.RageSpike;
import us.eunoians.mcrpg.ability.impl.swords.SerratedStrikes;
import us.eunoians.mcrpg.ability.impl.swords.Vampire;
import us.eunoians.mcrpg.ability.impl.woodcutting.DryadsGift;
import us.eunoians.mcrpg.ability.impl.woodcutting.ExtraLumber;
import us.eunoians.mcrpg.ability.impl.woodcutting.HeavySwing;
import us.eunoians.mcrpg.ability.impl.woodcutting.NymphsVitality;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.AbilityContentPack;
import us.eunoians.mcrpg.expansion.content.LocalizationContentPack;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;
import us.eunoians.mcrpg.expansion.content.PlayerSettingContentPack;
import us.eunoians.mcrpg.expansion.content.QuestContentPack;
import us.eunoians.mcrpg.expansion.content.QuestObjectiveTypeContentPack;
import us.eunoians.mcrpg.expansion.content.QuestRarityContentPack;
import us.eunoians.mcrpg.expansion.content.QuestRewardTypeContentPack;
import us.eunoians.mcrpg.expansion.content.QuestScopeProviderContentPack;
import us.eunoians.mcrpg.expansion.content.RewardDistributionTypeContentPack;
import us.eunoians.mcrpg.expansion.content.TemplateConditionContentPack;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ContributionThresholdDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.MembershipDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ParticipatedDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.QuestAcceptorDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.TopPlayersDistributionType;
import us.eunoians.mcrpg.quest.board.template.condition.ChanceCondition;
import us.eunoians.mcrpg.quest.board.template.condition.CompletionPrerequisiteCondition;
import us.eunoians.mcrpg.quest.board.template.condition.CompoundCondition;
import us.eunoians.mcrpg.quest.board.template.condition.PermissionCondition;
import us.eunoians.mcrpg.quest.board.template.condition.RarityCondition;
import us.eunoians.mcrpg.quest.board.template.condition.VariableCondition;
import us.eunoians.mcrpg.expansion.content.QuestSourceContentPack;
import us.eunoians.mcrpg.expansion.content.SkillContentPack;
import us.eunoians.mcrpg.expansion.content.StatisticContent;
import us.eunoians.mcrpg.expansion.content.StatisticContentPack;
import us.eunoians.mcrpg.localization.DynamicLocale;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.MobKillObjectiveType;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeNextTierRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ScalableCommandRewardType;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.BoardLandQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.BoardPersonalQuestSource;
import us.eunoians.mcrpg.quest.impl.scope.impl.PermissionQuestScopeProvider;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScopeProvider;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.CommandRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ExperienceRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ItemRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.DisableBonusExperienceConsumptionSetting;
import us.eunoians.mcrpg.setting.impl.ExperienceDisplaySetting;
import us.eunoians.mcrpg.setting.impl.KeepHandEmptySetting;
import us.eunoians.mcrpg.setting.impl.KeepHotbarSlotEmptySetting;
import us.eunoians.mcrpg.setting.impl.LocaleSetting;
import us.eunoians.mcrpg.setting.impl.QuestProgressNotificationSetting;
import us.eunoians.mcrpg.setting.impl.RequireEmptyOffhandSetting;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;
import us.eunoians.mcrpg.statistic.McRPGStatistic;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Set;

/**
 * The native content expansion for McRPG that contains all out of the box
 * content such as abilities and skills that come with the default installation of the plugin.
 */
public final class McRPGExpansion extends ContentExpansion {

    public static final NamespacedKey EXPANSION_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mcrpg-expansion");
    private final McRPG mcRPG;

    public McRPGExpansion(@NotNull McRPG mcRPG) {
        super(EXPANSION_KEY);
        this.mcRPG = mcRPG;
    }

    @NotNull
    @Override
    public Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent() {
        List<Skill> skills = createSkills();
        List<Ability> abilities = createAbilities();
        return Set.of(getSkillContent(skills), getAbilityContent(abilities),
                getStatisticContent(skills, abilities), getPlayerSettingContent(), getLocalizationContent(),
                getQuestObjectiveTypeContent(), getQuestRewardTypeContent(), getQuestContent(),
                getQuestSourceContent(), getQuestRarityContent(), getQuestScopeProviderContent(),
                getRewardDistributionTypeContent(), getTemplateConditionContent());
    }

    @NotNull
    @Override
    public String getExpansionName(@NotNull McRPGPlayer player) {
        return mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(player, LocalizationKey.MCRPG_EXPANSION_NAME);
    }

    /**
     * Creates all native McRPG skill instances. This list is shared between
     * {@link #getSkillContent(List)} and {@link #getStatisticContent(List, List)} so that
     * skills are only instantiated once.
     *
     * @return A {@link List} of all native McRPG skills.
     */
    @NotNull
    private List<Skill> createSkills() {
        return List.of(
                new Swords(mcRPG), new Mining(mcRPG),
                new WoodCutting(mcRPG), new Herbalism(mcRPG)
        );
    }

    /**
     * Gets the native {@link SkillContentPack} for McRPG.
     *
     * @param skills The shared list of native skill instances.
     * @return The native {@link SkillContentPack} for McRPG.
     */
    @NotNull
    private SkillContentPack getSkillContent(@NotNull List<Skill> skills) {
        SkillContentPack skillContent = new SkillContentPack(this);
        skills.forEach(skillContent::addContent);
        return skillContent;
    }

    /**
     * Creates all native McRPG ability instances. This list is shared between
     * {@link #getAbilityContent(List)} and {@link #getStatisticContent(List, List)} so that
     * abilities are only instantiated once.
     *
     * @return A {@link List} of all native McRPG abilities.
     */
    @NotNull
    private List<Ability> createAbilities() {
        return List.of(
                // Swords
                new Bleed(mcRPG), new DeeperWound(mcRPG), new Vampire(mcRPG),
                new EnhancedBleed(mcRPG), new RageSpike(mcRPG), new SerratedStrikes(mcRPG),
                // Mining
                new ExtraOre(mcRPG), new ItsATriple(mcRPG), new RemoteTransfer(mcRPG), new OreScanner(mcRPG),
                // Woodcutting
                new ExtraLumber(mcRPG), new HeavySwing(mcRPG), new DryadsGift(mcRPG), new NymphsVitality(mcRPG),
                // Herbalism
                new InstantIrrigation(mcRPG), new TooManyPlants(mcRPG), new VerdantSurge(mcRPG), new MassHarvest(mcRPG)
        );
    }

    /**
     * Gets the native {@link AbilityContentPack} for McRPG.
     *
     * @param abilities The shared list of native ability instances.
     * @return The native {@link AbilityContentPack} for McRPG.
     */
    @NotNull
    private AbilityContentPack getAbilityContent(@NotNull List<Ability> abilities) {
        AbilityContentPack abilityContent = new AbilityContentPack(this);
        abilities.forEach(abilityContent::addContent);
        return abilityContent;
    }

    /**
     * Gets the native {@link StatisticContentPack} for McRPG.
     * <p>
     * Includes all statically-defined statistics from {@link McRPGStatistic}, the default
     * statistics provided by each skill (e.g., per-skill experience and max level), and the
     * default statistics provided by each ability (e.g., activation counts for active abilities).
     * Third-party {@link ContentExpansion} plugins should follow the same pattern — include
     * their own statistics in their expansion's {@link StatisticContentPack}.
     *
     * @param skills    The shared list of native skill instances.
     * @param abilities The shared list of native ability instances.
     * @return The native {@link StatisticContentPack} for McRPG.
     */
    @NotNull
    private StatisticContentPack getStatisticContent(@NotNull List<Skill> skills, @NotNull List<Ability> abilities) {
        StatisticContentPack statisticContent = new StatisticContentPack(this);

        // Global statistics (blocks mined, damage dealt, total XP, etc.)
        for (Statistic statistic : McRPGStatistic.ALL_STATIC_STATISTICS) {
            statisticContent.addContent(new StatisticContent(statistic, EXPANSION_KEY));
        }

        // Per-skill statistics (e.g., experience and max level from McRPGSkill.getDefaultStatistics())
        for (Skill skill : skills) {
            for (Statistic statistic : skill.getDefaultStatistics()) {
                statisticContent.addContent(new StatisticContent(statistic, EXPANSION_KEY));
            }
        }

        // Per-ability statistics (e.g., activation counts from ActiveAbility.getDefaultStatistics())
        for (Ability ability : abilities) {
            for (Statistic statistic : ability.getDefaultStatistics()) {
                statisticContent.addContent(new StatisticContent(statistic, EXPANSION_KEY));
            }
        }

        return statisticContent;
    }

    /**
     * Gets the native {@link PlayerSettingContentPack} for McRPG.
     *
     * @return The native {@link PlayerSettingContentPack} for McRPG.
     */
    @NotNull
    public PlayerSettingContentPack getPlayerSettingContent() {
        PlayerSettingContentPack playerSettingContent = new PlayerSettingContentPack(this);
        playerSettingContent.addContent(ExperienceDisplaySetting.values()[0]);
        playerSettingContent.addContent(KeepHandEmptySetting.values()[0]);
        playerSettingContent.addContent(KeepHotbarSlotEmptySetting.values()[0]);
        playerSettingContent.addContent(RequireEmptyOffhandSetting.values()[0]);
        playerSettingContent.addContent(DisableBonusExperienceConsumptionSetting.values()[0]);
        playerSettingContent.addContent(LocaleSetting.values()[0]);
        playerSettingContent.addContent(QuestProgressNotificationSetting.values()[0]);
        return playerSettingContent;
    }

    /**
     * Gets the native {@link LocalizationContentPack} for McRPG.
     * <p>
     * Locale files are dynamically loaded from the localization folder.
     *
     * @return The native {@link LocalizationContentPack} for McRPG.
     */
    public LocalizationContentPack getLocalizationContent() {
        LocalizationContentPack localizationContent = new LocalizationContentPack(this);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE)
                .getLocalizationFiles()
                .forEach(yamlDocument -> localizationContent.addContent(new DynamicLocale(yamlDocument)));
        return localizationContent;
    }

    /**
     * Gets the native {@link QuestObjectiveTypeContentPack} for McRPG, populated with the
     * built-in objective types (block break, mob kill).
     *
     * @return The native {@link QuestObjectiveTypeContentPack} for McRPG.
     */
    @NotNull
    private QuestObjectiveTypeContentPack getQuestObjectiveTypeContent() {
        QuestObjectiveTypeContentPack pack = new QuestObjectiveTypeContentPack(this);
        pack.addContent(new BlockBreakObjectiveType());
        pack.addContent(new MobKillObjectiveType());
        return pack;
    }

    /**
     * Gets the native {@link QuestRewardTypeContentPack} for McRPG, populated with the
     * built-in reward types (experience, command, ability upgrade).
     *
     * @return The native {@link QuestRewardTypeContentPack} for McRPG.
     */
    @NotNull
    private QuestRewardTypeContentPack getQuestRewardTypeContent() {
        QuestRewardTypeContentPack pack = new QuestRewardTypeContentPack(this);
        pack.addContent(new ExperienceRewardType());
        pack.addContent(new CommandRewardType());
        pack.addContent(new AbilityUpgradeRewardType());
        pack.addContent(new AbilityUpgradeNextTierRewardType());
        pack.addContent(new ScalableCommandRewardType());
        pack.addContent(new ItemRewardType());
        return pack;
    }

    /**
     * Gets the native {@link QuestContentPack} for McRPG. This pack is empty because native
     * quest definitions are loaded from YAML config files via {@code QuestConfigLoader}, not
     * through the expansion system. The pack is included to signal that the quest system supports
     * expansion-based quest registration.
     *
     * @return The native {@link QuestContentPack} for McRPG (empty).
     */
    @NotNull
    private QuestContentPack getQuestContent() {
        return new QuestContentPack(this);
    }

    /**
     * Gets the native {@link QuestSourceContentPack} for McRPG, populated with the
     * built-in quest source types.
     *
     * @return The native {@link QuestSourceContentPack} for McRPG.
     */
    @NotNull
    private QuestSourceContentPack getQuestSourceContent() {
        QuestSourceContentPack pack = new QuestSourceContentPack(this);
        pack.addContent(new BoardPersonalQuestSource());
        pack.addContent(new BoardLandQuestSource());
        pack.addContent(new AbilityUpgradeQuestSource());
        pack.addContent(new ManualQuestSource());
        return pack;
    }

    /**
     * Gets the native {@link QuestRarityContentPack} for McRPG. This pack is empty because
     * native rarities are loaded from {@code board.yml} config via {@link us.eunoians.mcrpg.quest.board.configuration.ReloadableRarityConfig}.
     *
     * @return The native {@link QuestRarityContentPack} for McRPG (empty).
     */
    @NotNull
    private QuestRarityContentPack getQuestRarityContent() {
        return new QuestRarityContentPack(this);
    }

    /**
     * Gets the native {@link QuestScopeProviderContentPack} for McRPG, populated with the
     * built-in scope providers (single player, permission). Third-party providers like
     * Lands are registered by their respective plugin hooks.
     *
     * @return The native {@link QuestScopeProviderContentPack} for McRPG.
     */
    @NotNull
    private QuestScopeProviderContentPack getQuestScopeProviderContent() {
        QuestScopeProviderContentPack pack = new QuestScopeProviderContentPack(this);
        pack.addContent(new SinglePlayerQuestScopeProvider());
        pack.addContent(new PermissionQuestScopeProvider());
        return pack;
    }

    /**
     * Gets the native {@link RewardDistributionTypeContentPack} for McRPG, populated with the
     * built-in distribution types (top players, contribution threshold, participated, membership).
     *
     * @return The native {@link RewardDistributionTypeContentPack} for McRPG.
     */
    @NotNull
    private RewardDistributionTypeContentPack getRewardDistributionTypeContent() {
        RewardDistributionTypeContentPack pack = new RewardDistributionTypeContentPack(this);
        pack.addContent(new TopPlayersDistributionType());
        pack.addContent(new ContributionThresholdDistributionType());
        pack.addContent(new ParticipatedDistributionType());
        pack.addContent(new MembershipDistributionType());
        pack.addContent(new QuestAcceptorDistributionType());
        return pack;
    }

    /**
     * Gets the native {@link TemplateConditionContentPack} for McRPG, populated with the
     * built-in condition types (rarity, chance, variable, compound, permission, completion prerequisite).
     *
     * @return The native {@link TemplateConditionContentPack} for McRPG.
     */
    @NotNull
    private TemplateConditionContentPack getTemplateConditionContent() {
        TemplateConditionContentPack pack = new TemplateConditionContentPack(this);
        pack.addContent(new RarityCondition());
        pack.addContent(new ChanceCondition());
        pack.addContent(new VariableCondition());
        pack.addContent(new CompoundCondition());
        pack.addContent(new PermissionCondition());
        pack.addContent(new CompletionPrerequisiteCondition());
        return pack;
    }
}
