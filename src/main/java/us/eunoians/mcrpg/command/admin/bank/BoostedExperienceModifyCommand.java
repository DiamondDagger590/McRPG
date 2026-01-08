package us.eunoians.mcrpg.command.admin.bank;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
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

import static us.eunoians.mcrpg.command.CommandPlaceholders.BOOSTED_EXPERIENCE;

/**
 * Command to modify a player's boosted experience.
 */
public class BoostedExperienceModifyCommand extends AdminBankCommandBase{

    private static final Permission GIVE_BOOSTED_EXPERIENCE_PERMISSION = Permission.of("mcrpg.admin.exp-bank.give.boosted-experience");
    private static final Permission REMOVE_BOOSTED_EXPERIENCE_PERMISSION = Permission.of("mcrpg.admin.exp-bank.remove.boosted-experience");
    private static final Permission RESET_BOOSTED_EXPERIENCE_PERMISSION = Permission.of("mcrpg.admin.exp-bank.reset.boosted-experience");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("give")
                .literal("boosted-exp", "boosted")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_EXP_BANK_PLAYER)))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_EXP_BANK_BOOSTED_AMOUNT)))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_GIVE_COMMAND_ROOT_PERMISSION, GIVE_BOOSTED_EXPERIENCE_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);
                    CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                    int expAmount = commandContext.get(amountKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, expAmount);
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, expAmount);
                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        giveBoostedExperience(mcRPGPlayer, expAmount, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_BOOSTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_BOOSTED_EXP_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("remove", "minus")
                .literal("boosted-exp", "boosted")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_EXP_BANK_PLAYER)))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_EXP_BANK_BOOSTED_AMOUNT)))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_REMOVE_COMMAND_ROOT_PERMISSION, REMOVE_BOOSTED_EXPERIENCE_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);
                    CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                    int expAmount = commandContext.get(amountKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, expAmount);
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, expAmount);
                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        removeBoostedExperience(mcRPGPlayer, expAmount, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.REMOVE_BOOSTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.REMOVE_BOOSTED_EXP_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("reset")
                .literal("boosted-exp", "boosted")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_EXP_BANK_RESET_PLAYER)))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_RESET_COMMAND_ROOT_PERMISSION, RESET_BOOSTED_EXPERIENCE_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, 0);
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, 0);
                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        resetBoostedExperience(mcRPGPlayer, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_BOOSTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_BOOSTED_EXP_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );
    }

    /**
     * Gives boosted experience to the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to be given boosted experience.
     * @param expToGive            The amount of boosted experience to be given.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void giveBoostedExperience(@NotNull McRPGPlayer mcRPGPlayer, int expToGive, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().modifyBoostedExperience(expToGive);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.GIVE_BOOSTED_EXP_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    /**
     * Removes boosted experience from the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to have boosted experience removed.
     * @param expToRemove          The amount of boosted experience to be removed.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void removeBoostedExperience(@NotNull McRPGPlayer mcRPGPlayer, int expToRemove, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().modifyBoostedExperience(-1 * Math.min(expToRemove, mcRPGPlayer.getExperienceExtras().getBoostedExperience()));
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REMOVE_BOOSTED_EXP_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    /**
     * Resets the boosted experience for the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to be reset.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void resetBoostedExperience(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().setBoostedExperience(0);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.RESET_BOOSTED_EXP_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    @NotNull
    private static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience, int experience) {
        Map<String, String> placeholders = new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(BOOSTED_EXPERIENCE.getPlaceholder(), Integer.toString(experience));
        return placeholders;
    }
}
