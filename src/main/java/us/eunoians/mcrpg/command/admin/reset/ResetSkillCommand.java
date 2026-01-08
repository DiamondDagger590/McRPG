package us.eunoians.mcrpg.command.admin.reset;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
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
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.command.parser.SkillParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static us.eunoians.mcrpg.command.CommandPlaceholders.SKILL;

/**
 * Command used to reset a player's skill
 */
public class ResetSkillCommand extends ResetBaseCommand {

    private static final Permission RESET_SKILL_PERMISSION = Permission.of("mcrpg.admin.reset.skill");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("reset").commandDescription(RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_RESET)))
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_RESET_PLAYER)))
                .literal("skill")
                .required("reset_skill", SkillParser.skillParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_RESET_SKILL)))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, RESET_COMMAND_BASE_PERMISSION, RESET_SKILL_PERMISSION))
                .handler(commandContext -> {
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            Player player = commandContext.get(playerKey);
                            CloudKey<Skill> skillKey = CloudKey.of("reset_skill", Skill.class);
                            Skill skill = commandContext.get(skillKey);
                            Audience senderAudience = commandContext.sender().getSender();

                            Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                            Optional<McRPGPlayer> senderOptional = commandContext.sender().getSender() instanceof Player sender ? McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                    .manager(McRPGManagerKey.PLAYER).getPlayer(sender.getUniqueId()) : Optional.empty();
                            Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, skill, senderOptional.orElse(null));
                            Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, skill, playerOptional.orElse(null));
                            if (playerOptional.isPresent()) {
                                McRPGPlayer mcRPGPlayer = playerOptional.get();
                                SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
                                Optional<SkillHolder.SkillHolderData> skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
                                if (skillHolderDataOptional.isPresent()) {
                                    var skillHolderData = skillHolderDataOptional.get();
                                    skillHolderData.resetSkill();
                                    skillHolder.getAllAbilityDataForSkill(skill).forEach(AbilityData::resetAbility);
                                    player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.RESET_SKILL_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
                                    // Only send a message if the sender is not the receiver or the sender is console
                                    if (!(commandContext.sender().getSender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                                        senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_SKILL_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                                    }

                                    Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
                                    database.getDatabaseExecutorService().submit(() -> {
                                        try (Connection connection = database.getConnection()) {
                                            FailSafeTransaction failsafeTransaction = new FailSafeTransaction(connection);
                                            failsafeTransaction.addAll(SkillDAO.savePlayerSkillData(connection, skillHolder, skillHolderData.getSkillKey()));
                                            failsafeTransaction.addAll(SkillDAO.savePlayerAbilityAttributes(connection, skillHolder, McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getAbilitiesBelongingToSkill(skill)));
                                            failsafeTransaction.executeTransaction();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                            // Go back to main thread
                                            new CoreTask(McRPG.getInstance()) {
                                                @Override
                                                public void run() {
                                                    senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_SKILL_COMMAND_SENDER_ERROR_SAVING_MESSAGE, senderPlaceholders));
                                                }
                                            }.runTask();
                                        }
                                    });
                                    return;
                                }
                            }
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.RESET_SKILL_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));
                        }
                ));
    }

    @NotNull
    public static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience,
                                                      @NotNull Skill skill, @Nullable McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(SKILL.getPlaceholder(), mcRPGPlayer == null ? skill.getName() : skill.getName(mcRPGPlayer));
        return placeholders;
    }
}
