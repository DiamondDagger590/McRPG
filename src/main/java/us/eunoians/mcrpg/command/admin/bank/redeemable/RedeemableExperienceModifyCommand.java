package us.eunoians.mcrpg.command.admin.bank.redeemable;

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
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;
import us.eunoians.mcrpg.command.admin.bank.AdminBankCommandBase;
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
public class RedeemableExperienceModifyCommand extends RedeemableModifyCommandBase {

    private static final Permission GIVE_REDEEMABLE_EXPERIENCE_PERMISSION = Permission.of("mcrpg.admin.exp-bank.give.redeemable.experience");
    private static final Permission REMOVE_REDEEMABLE_EXPERIENCE_PERMISSION = Permission.of("mcrpg.admin.exp-bank.remove.redeemable.experience");
    private static final Permission RESET_REDEEMABLE_EXPERIENCE_PERMISSION = Permission.of("mcrpg.admin.exp-bank.reset.redeemable.experience");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("give")
                .literal("redeemable", "redeem")
                .literal("experience", "exp")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to give something to")))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of redeemable experience to give")))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_GIVE_COMMAND_ROOT_PERMISSION, GIVE_REDEEMABLE_EXPERIENCE_PERMISSION, RedeemableModifyCommandBase.REDEEMABLE_BANK_GIVE_ROOT_PERMISSION))
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

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("remove")
                .literal("redeemable", "redeem")
                .literal("experience", "exp")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to remove something from")))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of redeemable experience to remove")))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_REMOVE_COMMAND_ROOT_PERMISSION, REMOVE_REDEEMABLE_EXPERIENCE_PERMISSION, RedeemableModifyCommandBase.REDEEMABLE_BANK_REMOVE_ROOT_PERMISSION))
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
                        removeRedeemableExperience(mcRPGPlayer, experienceAmount, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("reset")
                .literal("redeemable", "redeem")
                .literal("experience", "exp")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to remove something from")))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_RESET_COMMAND_ROOT_PERMISSION, RESET_REDEEMABLE_EXPERIENCE_PERMISSION, RedeemableModifyCommandBase.REDEEMABLE_BANK_RESET_ROOT_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Optional<McRPGPlayer> senderOptional = senderAudience instanceof Player sender ? McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.PLAYER).getPlayer(sender.getUniqueId()) : Optional.empty();
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, 0, senderOptional.orElse(null));
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, 0, playerOptional.orElse(null));

                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        resetRedeemableExperience(mcRPGPlayer, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );
    }

    /**
     * Gives redeemable experience to the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to be given experience.
     * @param expToGive            The amount of experience to be given.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void giveRedeemableExperience(@NotNull McRPGPlayer mcRPGPlayer, int expToGive, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().modifyRedeemableExperience(expToGive);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.GIVE_REDEEMABLE_EXPERIENCE_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    /**
     * Removes redeemable experience from the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to have experience removed.
     * @param expToRemove          The amount of experience to be removed.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void removeRedeemableExperience(@NotNull McRPGPlayer mcRPGPlayer, int expToRemove, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().modifyRedeemableExperience(-1 * Math.min(expToRemove, mcRPGPlayer.getExperienceExtras().getRedeemableExperience()));
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    /**
     * Resets the redeemable experience for the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to be reset.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void resetRedeemableExperience(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().setRedeemableExperience(0);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.RESET_REDEEMABLE_EXPERIENCE_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    @NotNull
    private static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience, int experience, @Nullable McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>(getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(REDEEMABLE_EXPERIENCE.getPlaceholder(), Integer.toString(experience));
        return placeholders;
    }
}
