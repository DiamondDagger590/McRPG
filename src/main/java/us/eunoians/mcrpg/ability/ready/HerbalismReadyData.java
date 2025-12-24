package us.eunoians.mcrpg.ability.ready;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Ready data used for the {@link us.eunoians.mcrpg.skill.impl.herbalism.Herbalism} skill.
 */
public class HerbalismReadyData extends ReadyData {

    @NotNull
    @Override
    public Component getReadyMessage(@NotNull McRPGPlayer player) {
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        return localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.HERBALISM_READY_MESSAGE);
    }

    @NotNull
    @Override
    public Component getUnreadyMessage(@NotNull McRPGPlayer player) {
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        return localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.HERBALISM_UNREADY_MESSAGE);
    }
}
