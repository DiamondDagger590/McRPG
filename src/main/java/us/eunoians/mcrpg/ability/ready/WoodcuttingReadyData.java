package us.eunoians.mcrpg.ability.ready;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;

/**
 * Ready data used for the {@link WoodCutting} skill.
 */
public class WoodcuttingReadyData extends ReadyData {

    @NotNull
    @Override
    public Component getReadyMessage(@NotNull McRPGPlayer player) {
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        return localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.WOODCUTTING_READY_MESSAGE);
    }

    @NotNull
    @Override
    public Component getUnreadyMessage(@NotNull McRPGPlayer player) {
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        return localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.WOODCUTTING_UNREADY_MESSAGE);
    }
}
