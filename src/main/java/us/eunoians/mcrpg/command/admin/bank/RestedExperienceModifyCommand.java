package us.eunoians.mcrpg.command.admin.bank;

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
import org.incendo.cloud.parser.standard.FloatParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static us.eunoians.mcrpg.command.CommandPlaceholders.RESTED_EXPERIENCE;

/**
 * Command to modify a player's rested experience.
 */
public class RestedExperienceModifyCommand extends AdminBankCommandBase {

    private static final Permission GIVE_RESTED_EXPERIENCE_PERMISSION = Permission.of("mcrpg.admin.exp-bank.give.rested-experience");
    private static final Permission REMOVE_RESTED_EXPERIENCE_PERMISSION = Permission.of("mcrpg.admin.exp-bank.remove.rested-experience");
    private static final Permission RESET_RESTED_EXPERIENCE_PERMISSION = Permission.of("mcrpg.admin.exp-bank.reset.rested-experience");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("give")
                .literal("rested-exp", "rested")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to give something to")))
                .required("amount", FloatParser.floatParser(0), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of rested experience to give")))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_GIVE_COMMAND_ROOT_PERMISSION, GIVE_RESTED_EXPERIENCE_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);
                    CloudKey<Float> amountKey = CloudKey.of("amount", Float.class);
                    float expAmount = commandContext.get(amountKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, expAmount);
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, expAmount);
                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        giveRestedExperience(mcRPGPlayer, expAmount, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_RESTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_RESTED_EXP_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("remove", "minus")
                .literal("rested-exp", "rested")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to remove something from")))
                .required("amount", FloatParser.floatParser(0), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of rested exp to remove")))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_REMOVE_COMMAND_ROOT_PERMISSION, REMOVE_RESTED_EXPERIENCE_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);
                    CloudKey<Float> amountKey = CloudKey.of("amount", Float.class);
                    float expAmount = commandContext.get(amountKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, expAmount);
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, expAmount);
                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        removeRestedExperience(mcRPGPlayer, expAmount, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.REMOVE_RESTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.REMOVE_RESTED_EXP_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("reset")
                .literal("rested-exp", "rested")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to reset")))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_RESET_COMMAND_ROOT_PERMISSION, RESET_RESTED_EXPERIENCE_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, 0);
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, 0);
                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        resetRestedExperience(mcRPGPlayer, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_RESTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_RESTED_EXP_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );
    }

    /**
     * Gives rested experience to the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to be given rested experience.
     * @param expToGive            The amount of rested experience to be given.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void giveRestedExperience(@NotNull McRPGPlayer mcRPGPlayer, float expToGive, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().modifyRestedExperience(expToGive);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.GIVE_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    /**
     * Removes rested experience from the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to have rested experience removed.
     * @param expToRemove          The amount of rested experience to be removed.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void removeRestedExperience(@NotNull McRPGPlayer mcRPGPlayer, float expToRemove, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().modifyRestedExperience(-1 * Math.min(expToRemove, mcRPGPlayer.getExperienceExtras().getRestedExperience()));
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REMOVE_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    /**
     * Resets the rested experience for the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to be reset.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void resetRestedExperience(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().setRestedExperience(0);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.RESET_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    @NotNull
    private static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience, float experience) {
        Map<String, String> placeholders = new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(RESTED_EXPERIENCE.getPlaceholder(), Float.toString(experience));
        return placeholders;
    }
}
