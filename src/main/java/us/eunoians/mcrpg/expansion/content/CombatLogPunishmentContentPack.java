package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.log.CombatLogPunishmentType;
import us.eunoians.mcrpg.expansion.ContentExpansion;

/**
 * Content pack for registering {@link CombatLogPunishmentType} implementations via the
 * {@link ContentExpansion} system.
 */
public class CombatLogPunishmentContentPack extends McRPGContentPack<CombatLogPunishmentType> {

    /**
     * Constructs a new {@link CombatLogPunishmentContentPack}.
     *
     * @param contentExpansion The {@link ContentExpansion} providing this content.
     */
    public CombatLogPunishmentContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
