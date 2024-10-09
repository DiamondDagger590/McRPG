package us.eunoians.mcrpg.expansion.handler;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.AbilityContentPack;
import us.eunoians.mcrpg.expansion.content.PlayerSettingContentPack;
import us.eunoians.mcrpg.expansion.content.SkillContentPack;

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
            skillContent.getContent().forEach(skill -> mcRPG.getSkillRegistry().registerSkill(skill));
            return true;
        }
        return false;
    }),
    /**
     * This processor handles processing {@link AbilityContentPack}s.
     */
    ABILITY((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof AbilityContentPack abilityContent) {
            abilityContent.getContent().forEach(ability -> mcRPG.getAbilityRegistry().registerAbility(ability));
            return true;
        }
        return false;
    }),
    /**
     * This processor handles processing {@link PlayerSettingContentPack}s.
     */
    SETTING((mcRPG, mcRPGContent) -> {
        if (mcRPGContent instanceof PlayerSettingContentPack playerSettingContent) {
            playerSettingContent.getContent().forEach(playerSetting -> mcRPG.getPlayerSettingRegistry().registerSetting(playerSetting));
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
