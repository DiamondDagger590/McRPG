package us.eunoians.mcrpg.expansion.content;

import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.setting.McRPGSetting;

/**
 * A {@link McRPGContentPack} containing all of the {@link PlayerSetting}s provided by a given
 * {@link ContentExpansion}.
 */
public final class PlayerSettingContentPack extends McRPGContentPack<McRPGSetting> {

    public PlayerSettingContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
