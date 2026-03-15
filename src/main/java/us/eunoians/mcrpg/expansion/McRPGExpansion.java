package us.eunoians.mcrpg.expansion;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.Statistic;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
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
import us.eunoians.mcrpg.expansion.content.SkillContentPack;
import us.eunoians.mcrpg.expansion.content.StatisticContent;
import us.eunoians.mcrpg.expansion.content.StatisticContentPack;
import us.eunoians.mcrpg.localization.DynamicLocale;
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
        List<Ability> abilities = createAbilities();
        return Set.of(getSkillContent(), getAbilityContent(abilities), getStatisticContent(abilities),
                getPlayerSettingContent(), getLocalizationContent());
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
     * Creates all native McRPG ability instances. This list is shared between
     * {@link #getAbilityContent(List)} and {@link #getStatisticContent(List)} so that
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
     * Includes all statically-defined statistics from {@link McRPGStatistic} as well as
     * the default statistics provided by each ability (e.g., activation counts for active
     * abilities). Third-party {@link ContentExpansion} plugins should follow the same
     * pattern — include their own statistics in their expansion's {@link StatisticContentPack}.
     *
     * @param abilities The shared list of native ability instances.
     * @return The native {@link StatisticContentPack} for McRPG.
     */
    @NotNull
    private StatisticContentPack getStatisticContent(@NotNull List<Ability> abilities) {
        StatisticContentPack statisticContent = new StatisticContentPack(this);

        // Global and per-skill statistics
        for (Statistic statistic : McRPGStatistic.ALL_STATIC_STATISTICS) {
            statisticContent.addContent(new StatisticContent(statistic, EXPANSION_KEY));
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
}
