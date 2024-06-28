package us.eunoians.mcrpg.command.admin.reset;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

import java.sql.Connection;
import java.util.Optional;

public class ResetPlayerCommand extends ResetBaseCommand {

    private static final Permission RESET_PLAYER_PERMISSION = Permission.of("mcrpg.admin.reset.player");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().getCommandManager();
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

                            Optional<AbilityHolder> abilityHolderOptional = McRPG.getInstance().getEntityManager().getAbilityHolder(player.getUniqueId());
                            if (abilityHolderOptional.isPresent() && abilityHolderOptional.get() instanceof SkillHolder skillHolder) {
                                for (NamespacedKey skill : skillHolder.getSkills()) {
                                    Optional<SkillHolder.SkillHolderData> skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
                                    if (skillHolderDataOptional.isPresent()) {
                                        SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                                        skillHolderData.resetSkill();
                                    }
                                    Connection connection = McRPG.getInstance().getDatabaseManager().getDatabase().getConnection();
                                    SkillDAO.savePlayerSkillData(connection, skillHolder).exceptionally(throwable -> {
                                        senderAudience.sendMessage(miniMessage.deserialize(String.format("<red>There was an error trying to save data for %s after resetting their %s skill. Please have an admin check console.", player.getDisplayName(), skill.value())));
                                        throwable.printStackTrace();
                                        return null;
                                    });
                                    receiverAudience.sendMessage(miniMessage.deserialize("<green>You have had your McRPG data reset."));
                                    // Only send a message if the sender is not the receiver or the sender is console
                                    if (!(commandContext.sender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                                        senderAudience.sendMessage(miniMessage.deserialize(String.format("<green>You have reset <gold>%s's <green>McRPG data.", player.getDisplayName())));
                                    }
                                    return;
                                }

                                senderAudience.sendMessage(miniMessage.deserialize(String.format("<red>Unable to reset McRPG data for %s.", player.displayName())));
                            }
                        }
                ));
    }
}
