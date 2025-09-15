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

import static us.eunoians.mcrpg.command.CommandPlaceholders.REDEEMABLE_LEVELS;

/**
 * Command used to give a player redeemable levels.
 */
public class RedeemableLevelsModifyCommand extends RedeemableModifyCommandBase {

    private static final Permission GIVE_REDEEMABLE_LEVELS_PERMISSION = Permission.of("mcrpg.admin.exp-bank.redeemable.give.levels");
    private static final Permission REMOVE_REDEEMABLE_LEVELS_PERMISSION = Permission.of("mcrpg.admin.exp-bank.redeemable.remove.levels");
    private static final Permission RESET_REDEEMABLE_LEVELS_PERMISSION = Permission.of("mcrpg.admin.exp-bank.redeemable.reset.levels");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("give")
                .literal("redeemable", "redeem")
                .literal("levels", "lv", "lvs")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to give something to")))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of redeemable levels to give")))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_GIVE_COMMAND_ROOT_PERMISSION, GIVE_REDEEMABLE_LEVELS_PERMISSION, RedeemableModifyCommandBase.REDEEMABLE_BANK_GIVE_ROOT_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);
                    CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                    int levelAmount = commandContext.get(amountKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, levelAmount);
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, levelAmount);
                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        giveRedeemableLevels(mcRPGPlayer, levelAmount, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_REDEEMABLE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_REDEEMABLE_LEVELS_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));

                })
        );

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("remove", "minus")
                .literal("redeemable", "redeem")
                .literal("levels", "lv", "lvs")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to remove something from")))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of redeemable levels to remove")))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_REMOVE_COMMAND_ROOT_PERMISSION, REMOVE_REDEEMABLE_LEVELS_PERMISSION, RedeemableModifyCommandBase.REDEEMABLE_BANK_REMOVE_ROOT_PERMISSION))
                .handler(commandContext -> {
                    CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                    Player player = commandContext.get(playerKey);
                    CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                    int levelAmount = commandContext.get(amountKey);

                    Audience senderAudience = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                    Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, levelAmount);
                    Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, levelAmount);
                    if (playerOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = playerOptional.get();
                        removeRedeemableLevels(mcRPGPlayer, levelAmount, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.REMOVE_REDEEMABLE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.REMOVE_REDEEMABLE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));

                })
        );

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("exp-bank", "experience-bank", "bank")
                .literal("reset")
                .literal("redeemable", "redeem")
                .literal("levels", "lv", "lvs")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to reset")))
                .permission(Permission.anyOf(McRPGCommandBase.ROOT_PERMISSION, AdminBaseCommand.ADMIN_BASE_PERMISSION, AdminBankCommandBase.BANK_MODIFY_COMMAND_ROOT_PERMISSION,
                        AdminBankCommandBase.BANK_RESET_COMMAND_ROOT_PERMISSION, RESET_REDEEMABLE_LEVELS_PERMISSION, RedeemableModifyCommandBase.REDEEMABLE_BANK_RESET_ROOT_PERMISSION))
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
                        resetRedeemableLevels(mcRPGPlayer, receiverPlaceholders);
                        // Only send a message if the sender is not the receiver or the sender is console
                        if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_REDEEMABLE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                        }
                        return;
                    }
                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_REDEEMABLE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));

                })
        );
    }

    /**
     * Gives redeemable levels to the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to be given levels.
     * @param levelsToGive         The amount of levels to be given.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void giveRedeemableLevels(@NotNull McRPGPlayer mcRPGPlayer, int levelsToGive, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().modifyRedeemableLevels(levelsToGive);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.GIVE_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    /**
     * Removes redeemable levels from the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to have levels removed.
     * @param levelsToRemove         The amount of levels to be removed.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void removeRedeemableLevels(@NotNull McRPGPlayer mcRPGPlayer, int levelsToRemove, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().modifyRedeemableLevels(-1 * Math.min(levelsToRemove, mcRPGPlayer.getExperienceExtras().getRedeemableLevels()));
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REMOVE_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    /**
     * Resets the redeemable levels for the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer          The player to be reset.
     * @param receiverPlaceholders The placeholders for the messages to be sent to the player.
     */
    public static void resetRedeemableLevels(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Map<String, String> receiverPlaceholders) {
        mcRPGPlayer.getExperienceExtras().setRedeemableLevels(0);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.RESET_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
    }

    @NotNull
    private static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience, int levels) {
        Map<String, String> placeholders = new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(REDEEMABLE_LEVELS.getPlaceholder(), Integer.toString(levels));
        return placeholders;
    }
}
