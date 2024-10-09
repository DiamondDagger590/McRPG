package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.setting.PlayerSetting;

/**
 * A {@link McRPGContentPack} containing all of the {@link PlayerSetting}s provided by a given
 * {@link ContentExpansion}.
 */
public class PlayerSettingContentPack extends McRPGContentPack<PlayerSetting> {

    public PlayerSettingContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
