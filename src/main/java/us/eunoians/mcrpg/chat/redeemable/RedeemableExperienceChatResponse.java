package us.eunoians.mcrpg.chat.redeemable;

import com.diamonddagger590.mccore.chat.ChatResponse;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

/**
 * This response allows a player to provide how much rested experience
 * they want to redeem in a given {@link Skill}.
 */
public class RedeemableExperienceChatResponse extends ChatResponse {

    private final McRPGPlayer mcRPGPlayer;
    private final Skill skill;

    public RedeemableExperienceChatResponse(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill) {
        super(mcRPGPlayer.getUUID());
        this.mcRPGPlayer = mcRPGPlayer;
        this.skill = skill;
    }

    /**
     * Gets the {@link Skill} that's having experience redeemed into.
     *
     * @return The {@link Skill} that's having experience redeemed into.
     */
    @NotNull
    public Skill getSkill() {
        return skill;
    }

    @Override
    public long getResponseWaitTime() {
        return RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.LOADOUT_DISPLAY_NAME_RESPONSE_TIMEOUT, 10);
    }

    @Override
    public void onResponse(@NotNull PlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();
        String message = playerChatEvent.getMessage();
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        if (Methods.isInt(message)) {
            int amount = Integer.parseInt(message);
            player.performCommand("mcrpg redeem experience " + skill.getName(mcRPGPlayer) + " " + amount);
        } else {
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_EXPERIENCE_GUI_REDEEM_INVALID_INPUT));
        }
    }

    @Override
    public void onExpire() {
    }
}
