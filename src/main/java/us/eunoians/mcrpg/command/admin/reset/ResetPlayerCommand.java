package us.eunoians.mcrpg.command.admin.reset;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CoreTask;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This command is used to fully reset a player's data
 */
public class ResetPlayerCommand extends ResetBaseCommand {

    private static final Permission RESET_PLAYER_PERMISSION = Permission.of("mcrpg.admin.reset.player");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("reset").commandDescription(RichDescription.richDescription(miniMessage.deserialize("<gray>The subcommand for all commands that resets a target")))
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to reset something for")))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, RESET_COMMAND_BASE_PERMISSION, RESET_PLAYER_PERMISSION))
                .handler(commandContext -> {
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            Player player = commandContext.get(playerKey);

                            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
                            Audience senderAudience = adventure.sender(commandContext.sender().getSender());
                            Audience receiverAudience = adventure.player(player);
                            Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, receiverAudience);
                            Map<String, String> receiverPlaceholders = getPlaceholders(receiverAudience, senderAudience, receiverAudience);
                            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                            McRPG mcRPG = McRPG.getInstance();

                            Optional<AbilityHolder> abilityHolderOptional = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).getAbilityHolder(player.getUniqueId());
                            if (abilityHolderOptional.isPresent() && abilityHolderOptional.get() instanceof SkillHolder skillHolder) {
                                // Reset skills
                                skillHolder.getSkills().stream().map(skillHolder::getSkillHolderData).filter(Optional::isPresent).map(Optional::get).forEach(SkillHolder.SkillHolderData::resetSkill);
                                // Reset abilities
                                skillHolder.getAvailableAbilities().stream().map(skillHolder::getAbilityData).filter(Optional::isPresent).map(Optional::get).forEach(AbilityData::resetAbility);
                                // Reset timers
                                skillHolder.cleanupHolder();
                                receiverAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(receiverAudience, LocalizationKey.RESET_PLAYER_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
                                // Only send a message if the sender is not the receiver or the sender is console
                                if (!(commandContext.sender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_PLAYER_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                                }

                                Database database = mcRPG.getDatabase();
                                database.getDatabaseExecutorService().submit(() -> {
                                    try (Connection connection = database.getConnection()) {
                                        new FailSafeTransaction(connection, SkillDAO.saveAllSkillHolderInformation(connection, skillHolder)).executeTransaction();
                                    } catch (SQLException e) {
                                        // Go back to main thread
                                        new CoreTask(mcRPG) {
                                            @Override
                                            public void run() {
                                                senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_PLAYER_COMMAND_SENDER_ERROR_SAVING_MESSAGE, senderPlaceholders));
                                            }
                                        }.runTask();
                                        e.printStackTrace();
                                    }
                                });
                            }
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_PLAYER_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));
                        }
                ));
    }

    @NotNull
    public static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience) {
        return new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
    }
}
