package us.eunoians.mcrpg.expansion.handler;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.AbilityContentPack;
import us.eunoians.mcrpg.expansion.content.LocalizationContentPack;
import us.eunoians.mcrpg.expansion.content.PlayerSettingContentPack;
import us.eunoians.mcrpg.expansion.content.QuestContentPack;
import us.eunoians.mcrpg.expansion.content.QuestObjectiveTypeContentPack;
import us.eunoians.mcrpg.expansion.content.QuestRarityContentPack;
import us.eunoians.mcrpg.expansion.content.QuestRewardTypeContentPack;
import us.eunoians.mcrpg.expansion.content.QuestScopeProviderContentPack;
import us.eunoians.mcrpg.expansion.content.QuestSourceContentPack;
import us.eunoians.mcrpg.expansion.content.SkillContentPack;
import us.eunoians.mcrpg.quest.QuestManager;
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
     * This processor handles processing {@link QuestContentPack}s by registering
     * each {@link us.eunoians.mcrpg.quest.definition.QuestDefinition} into the
     * {@link us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry}.
     */
    QUEST((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof QuestContentPack questContent) {
            questContent.getContent().forEach(definition -> mcRPG.registryAccess().registry(McRPGRegistryKey.QUEST_DEFINITION).register(definition));
            return true;
        }
        return false;
    }),
    /**
     * This processor handles processing {@link QuestObjectiveTypeContentPack}s by registering
     * each {@link us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType} into the
     * {@link us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry}.
     */
    QUEST_OBJECTIVE_TYPE((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof QuestObjectiveTypeContentPack typeContent) {
            typeContent.getContent().forEach(type -> mcRPG.registryAccess().registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE).register(type));
            return true;
        }
        return false;
    }),
    /**
     * This processor handles processing {@link QuestRewardTypeContentPack}s by registering
     * each {@link us.eunoians.mcrpg.quest.reward.QuestRewardType} into the
     * {@link us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry}.
     */
    QUEST_REWARD_TYPE((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof QuestRewardTypeContentPack typeContent) {
            typeContent.getContent().forEach(type -> mcRPG.registryAccess().registry(McRPGRegistryKey.QUEST_REWARD_TYPE).register(type));
            return true;
        }
        return false;
    }),
    QUEST_SOURCE((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof QuestSourceContentPack sourceContent) {
            sourceContent.getContent().forEach(source -> mcRPG.registryAccess().registry(McRPGRegistryKey.QUEST_SOURCE).register(source));
            return true;
        }
        return false;
    }),
    QUEST_RARITY((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof QuestRarityContentPack rarityContent) {
            rarityContent.getContent().forEach(rarity -> mcRPG.registryAccess().registry(McRPGRegistryKey.QUEST_RARITY).register(rarity));
            return true;
        }
        return false;
    }),
    /**
     * This processor handles processing {@link QuestScopeProviderContentPack}s by registering
     * each {@link us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider} into the
     * {@link us.eunoians.mcrpg.quest.impl.scope.QuestScopeProviderRegistry} and registering
     * scope-change listeners with the {@link QuestManager} if it is available.
     */
    QUEST_SCOPE_PROVIDER((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof QuestScopeProviderContentPack providerContent) {
            providerContent.getContent().forEach(provider -> {
                mcRPG.registryAccess().registry(McRPGRegistryKey.QUEST_SCOPE_PROVIDER).register(provider);
                if (mcRPG.registryAccess().registry(RegistryKey.MANAGER).registered(McRPGManagerKey.QUEST)) {
                    QuestManager questManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
                    provider.registerScopeChangeListeners(questManager);
                }
            });
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
