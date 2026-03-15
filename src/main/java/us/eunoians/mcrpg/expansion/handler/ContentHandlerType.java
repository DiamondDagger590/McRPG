package us.eunoians.mcrpg.expansion.handler;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.AbilityContentPack;
import us.eunoians.mcrpg.expansion.content.LocalizationContentPack;
import us.eunoians.mcrpg.expansion.content.PlayerSettingContentPack;
import us.eunoians.mcrpg.expansion.content.SkillContentPack;
import us.eunoians.mcrpg.expansion.content.StatisticContentPack;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This enum provides all the content pack processors native to McRPG.
 * <p>
 * These will all be loaded natively and will support third party content expansions that
 * use the handled content packs. If a third party plugin wishes to have their own type of content pack,
 * they will need to create and register their own processor for it via {@link us.eunoians.mcrpg.expansion.ContentExpansionManager#registerContentHandler(ContentPackProcessor)}.
 */
public enum ContentHandlerType {

    /**
     * This processor handles processing {@link SkillContentPack}s.
     */
    SKILL((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof SkillContentPack skillContent) {
            skillContent.getContent().forEach(skill -> mcRPG.registryAccess().registry(McRPGRegistryKey.SKILL).register(skill));
            return true;
        }
        return false;
    }),
    /**
     * This processor handles processing {@link AbilityContentPack}s.
     */
    ABILITY((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof AbilityContentPack abilityContent) {
            abilityContent.getContent().forEach(ability -> mcRPG.registryAccess().registry(McRPGRegistryKey.ABILITY).register(ability));
            return true;
        }
        return false;
    }),
    /**
     * This processor handles processing {@link PlayerSettingContentPack}s.
     */
    SETTING((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof PlayerSettingContentPack playerSettingContent) {
            playerSettingContent.getContent().forEach(playerSetting -> mcRPG.registryAccess().registry(RegistryKey.PLAYER_SETTING).register(playerSetting));
            return true;
        }
        return false;
    }),
    /**
     * This processor handles processing {@link LocalizationContentPack}s.
     */
    LOCALIZATION(((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof LocalizationContentPack localizationContent) {
            localizationContent.getContent().forEach(localization -> mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).registerLanguageFile(localization));
            return true;
        }
        return false;
    })),
    /**
     * This processor handles processing {@link StatisticContentPack}s.
     * <p>
     * Each {@link us.eunoians.mcrpg.expansion.content.StatisticContent} in the pack is
     * unwrapped and registered to McCore's {@link StatisticRegistry}.
     */
    STATISTIC((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof StatisticContentPack statisticContent) {
            StatisticRegistry registry = mcRPG.registryAccess().registry(RegistryKey.STATISTIC);
            statisticContent.getContent().forEach(content -> registry.register(content.getStatistic()));
            return true;
        }
        return false;
    });

    private final ContentPackProcessor contentPackProcessor;

    ContentHandlerType(@NotNull ContentPackProcessor contentPackProcessor) {
        this.contentPackProcessor = contentPackProcessor;
    }

    /**
     * Gets the {@link ContentPackProcessor} provided.
     *
     * @return The {@link ContentPackProcessor} provided.
     */
    @NotNull
    public ContentPackProcessor getContentHandler() {
        return contentPackProcessor;
    }
}
