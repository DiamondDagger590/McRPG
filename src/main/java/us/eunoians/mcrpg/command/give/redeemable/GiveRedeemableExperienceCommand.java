package us.eunoians.mcrpg.command.give.redeemable;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static us.eunoians.mcrpg.command.CommandPlaceholders.REDEEMABLE_EXPERIENCE;

/**
 * Command used to give a player redeemable experience.
 */
public class GiveRedeemableExperienceCommand extends GiveRedeemableCommandBase {

    private static final Permission GIVE_REDEEMABLE_EXPERIENCE_PERMISSION = Permission.of("mcrpg.give.redeemable.experience");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("give")
                .literal("redeemable", "redeem")
                .literal("experience", "exp")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to give something to to")))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of redeemable experience to give")))
                .permission(Permission.anyOf(ROOT_PERMISSION, GIVE_COMMAND_ROOT_PERMISSION, GIVE_REDEEM_COMMAND_ROOT_PERMISSION, GIVE_REDEEMABLE_EXPERIENCE_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);
                    CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                    int experienceAmount = commandContext.get(amountKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Optional<McRPGPlayer> senderOptional = senderAudience instanceof Player sender ? McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.PLAYER).getPlayer(sender.getUniqueId()) : Optional.empty();
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, experienceAmount, senderOptional.orElse(null));
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, experienceAmount, playerOptional.orElse(null));

                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        giveRedeemableExperience(mcRPGPlayer, experienceAmount, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );
    }

    public static void giveRedeemableExperience(@NotNull McRPGPlayer mcRPGPlayer, int expToGive, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().modifyRedeemableExperience(expToGive);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.GIVE_REDEEMABLE_EXPERIENCE_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    @NotNull
    private static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience, int experience, @Nullable McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(REDEEMABLE_EXPERIENCE.getPlaceholder(), Integer.toString(experience));
        return placeholders;
    }
}
