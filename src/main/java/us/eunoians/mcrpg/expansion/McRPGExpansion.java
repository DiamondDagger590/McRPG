package us.eunoians.mcrpg.expansion;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
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
import us.eunoians.mcrpg.expansion.content.QuestSourceContentPack;
import us.eunoians.mcrpg.expansion.content.SkillContentPack;
import us.eunoians.mcrpg.localization.DynamicLocale;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.MobKillObjectiveType;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeNextTierRewardType;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.BoardLandQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.BoardPersonalQuestSource;
import us.eunoians.mcrpg.quest.impl.scope.impl.PermissionQuestScopeProvider;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScopeProvider;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.CommandRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ExperienceRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.DisableBonusExperienceConsumptionSetting;
import us.eunoians.mcrpg.setting.impl.ExperienceDisplaySetting;
import us.eunoians.mcrpg.setting.impl.KeepHandEmptySetting;
import us.eunoians.mcrpg.setting.impl.KeepHotbarSlotEmptySetting;
import us.eunoians.mcrpg.setting.impl.LocaleSetting;
import us.eunoians.mcrpg.setting.impl.RequireEmptyOffhandSetting;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;
import us.eunoians.mcrpg.util.McRPGMethods;

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
        return Set.of(getSkillContent(), getAbilityContent(), getPlayerSettingContent(), getLocalizationContent(),
                getQuestObjectiveTypeContent(), getQuestRewardTypeContent(), getQuestContent(),
                getQuestSourceContent(), getQuestRarityContent(), getQuestScopeProviderContent());
    }

    @NotNull
    @Override
    public String getExpansionName(@NotNull McRPGPlayer player) {
        return mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(player, LocalizationKey.MCRPG_EXPANSION_NAME);
    }

    /**
     * Gets the native {@link SkillContentPack} for McRPG.
     *
     * @return The native {@link SkillContentPack} for McRPG.
     */
    @NotNull
    private SkillContentPack getSkillContent() {
        SkillContentPack skillContent = new SkillContentPack(this);
        skillContent.addContent(new Swords(mcRPG));
        skillContent.addContent(new Mining(mcRPG));
        skillContent.addContent(new WoodCutting(mcRPG));
        skillContent.addContent(new Herbalism(mcRPG));
        return skillContent;
    }

    /**
     * Gets the native {@link AbilityContentPack} for McRPG.
     *
     * @return The native {@link AbilityContentPack} for McRPG.
     */
    @NotNull
    public AbilityContentPack getAbilityContent() {
        AbilityContentPack abilityContent = new AbilityContentPack(this);
        // Swords Abilities
        abilityContent.addContent(new Bleed(mcRPG));
        abilityContent.addContent(new DeeperWound(mcRPG));
        abilityContent.addContent(new Vampire(mcRPG));
        abilityContent.addContent(new EnhancedBleed(mcRPG));
        abilityContent.addContent(new RageSpike(mcRPG));
        abilityContent.addContent(new SerratedStrikes(mcRPG));

        // Mining Abilities
        abilityContent.addContent(new ExtraOre(mcRPG));
        abilityContent.addContent(new ItsATriple(mcRPG));
        abilityContent.addContent(new RemoteTransfer(mcRPG));
        abilityContent.addContent(new OreScanner(mcRPG));

        // Woodcutting Abilities
        abilityContent.addContent(new ExtraLumber(mcRPG));
        abilityContent.addContent(new HeavySwing(mcRPG));
        abilityContent.addContent(new DryadsGift(mcRPG));
        abilityContent.addContent(new NymphsVitality(mcRPG));

        // Herbalism Abilities
        abilityContent.addContent(new InstantIrrigation(mcRPG));
        abilityContent.addContent(new TooManyPlants(mcRPG));
        abilityContent.addContent(new VerdantSurge(mcRPG));
        abilityContent.addContent(new MassHarvest(mcRPG));
        return abilityContent;
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
}
