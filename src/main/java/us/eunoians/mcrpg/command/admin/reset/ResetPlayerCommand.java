package us.eunoians.mcrpg.command.admin.reset;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CoreTask;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.database.table.board.BoardCooldownDAO;
import us.eunoians.mcrpg.database.table.board.BoardOfferingDAO;
import us.eunoians.mcrpg.database.table.board.PlayerBoardStateDAO;
import us.eunoians.mcrpg.database.table.quest.PendingRewardDAO;
import us.eunoians.mcrpg.database.table.quest.QuestCompletionLogDAO;
import us.eunoians.mcrpg.database.table.quest.QuestInstanceDAO;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This command is used to fully reset a player's data
 */
public class ResetPlayerCommand extends ResetBaseCommand {

    private static final Permission RESET_PLAYER_PERMISSION = Permission.of("mcrpg.admin.reset.player");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("reset").commandDescription(RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_RESET)))
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_RESET_PLAYER)))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, RESET_COMMAND_BASE_PERMISSION, RESET_PLAYER_PERMISSION))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            Player player = commandContext.get(playerKey);

                            Audience senderAudience = commandContext.sender().getSender();
                            Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player);
                            Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player);
                            McRPG mcRPG = McRPG.getInstance();

                            Optional<AbilityHolder> abilityHolderOptional = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).getAbilityHolder(player.getUniqueId());
                            if (abilityHolderOptional.isPresent() && abilityHolderOptional.get() instanceof SkillHolder skillHolder) {
                                // Reset skills
                                skillHolder.getSkills().stream().map(skillHolder::getSkillHolderData).filter(Optional::isPresent).map(Optional::get).forEach(SkillHolder.SkillHolderData::resetSkill);
                                // Reset abilities
                                skillHolder.getAvailableAbilities().stream().map(skillHolder::getAbilityData).filter(Optional::isPresent).map(Optional::get).forEach(AbilityData::resetAbility);
                                // Reset timers
                                skillHolder.cleanupHolder();
                                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.RESET_PLAYER_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
                                // Only send a message if the sender is not the receiver or the sender is console
                                if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_PLAYER_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                                }

                                Database database = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
                                UUID playerUUID = player.getUniqueId();
                                database.getDatabaseExecutorService().submit(() -> {
                                    try (Connection connection = database.getConnection()) {
                                        new FailSafeTransaction(connection, SkillDAO.saveAllSkillHolderInformation(connection, skillHolder)).executeTransaction();
                                        clearBoardData(connection, playerUUID);
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

    /**
     * Clears all board-related data for a player. Abandons personal board offerings,
     * cancels associated solo quest instances, clears cooldowns, pending rewards,
     * completion log, and player board state. Scoped quest contributions are preserved.
     */
    private static void clearBoardData(@NotNull Connection connection, @NotNull UUID playerUUID) throws SQLException {
        // Abandon personal accepted offerings and cancel their quest instances
        List<PlayerBoardStateDAO.AcceptedBoardEntry> accepted =
                PlayerBoardStateDAO.loadAcceptedForPlayer(connection, playerUUID);
        for (PlayerBoardStateDAO.AcceptedBoardEntry entry : accepted) {
            for (PreparedStatement ps : BoardOfferingDAO.updateOfferingState(
                    connection, entry.offeringId(), BoardOffering.State.ABANDONED, null, null)) {
                ps.executeUpdate();
                ps.close();
            }
            if (entry.questInstanceUUID() != null) {
                for (PreparedStatement ps : QuestInstanceDAO.deleteQuestInstance(connection, entry.questInstanceUUID())) {
                    ps.executeUpdate();
                    ps.close();
                }
            }
        }
        // Clear player board state
        PlayerBoardStateDAO.deleteForPlayer(connection, playerUUID);
        // Clear player-scoped cooldowns
        BoardCooldownDAO.deleteCooldowns(connection, "player", playerUUID.toString(), null);
        // Clear pending rewards
        PendingRewardDAO.deleteAllForPlayer(connection, playerUUID);
        // Clear completion log
        QuestCompletionLogDAO.deleteForPlayer(connection, playerUUID);
    }

    @NotNull
    public static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience) {
        return new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
    }
}
