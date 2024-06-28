package us.eunoians.mcrpg.command.admin.reset;

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
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.parser.SkillParser;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

import java.sql.Connection;
import java.util.Optional;

/**
 * Command used to reset a player's skill
 */
public class ResetSkillCommand extends ResetBaseCommand {

    private static final Permission RESET_SKILL_PERMISSION = Permission.of("mcrpg.admin.reset.skill");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("reset").commandDescription(RichDescription.richDescription(miniMessage.deserialize("<gray>The subcommand for all commands that resets a target")))
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to reset something for")))
                .literal("skill")
                .required("reset_skill", SkillParser.skillParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The skill to reset")))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, RESET_COMMAND_BASE_PERMISSION, RESET_SKILL_PERMISSION))
                .handler(commandContext -> {
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            Player player = commandContext.get(playerKey);
                            CloudKey<Skill> skillKey = CloudKey.of("reset_skill", Skill.class);
                            Skill skill = commandContext.get(skillKey);

                            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
                            Audience senderAudience = adventure.sender(commandContext.sender().getSender());
                            Audience receiverAudience = adventure.player(player);

                            Optional<AbilityHolder> abilityHolderOptional = McRPG.getInstance().getEntityManager().getAbilityHolder(player.getUniqueId());
                            if (abilityHolderOptional.isPresent() && abilityHolderOptional.get() instanceof SkillHolder skillHolder) {
                                Optional<SkillHolder.SkillHolderData> skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
                                if (skillHolderDataOptional.isPresent()) {
                                    SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                                    skillHolderData.resetSkill();
                                    receiverAudience.sendMessage(miniMessage.deserialize(String.format("<green>You have has your <gold>%s skill <green>reset.", skill.getDisplayName())));
                                    // Only send a message if the sender is not the receiver or the sender is console
                                    if (!(commandContext.sender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                                        senderAudience.sendMessage(miniMessage.deserialize(String.format("<green>You have reset <gold>%s's %s skill <green>.", player.getDisplayName(), skill.getDisplayName())));
                                    }
                                    Connection connection = McRPG.getInstance().getDatabaseManager().getDatabase().getConnection();
                                    SkillDAO.savePlayerSkillData(connection, skillHolder, skillHolderData.getSkillKey()).exceptionally(throwable -> {
                                        senderAudience.sendMessage(miniMessage.deserialize(String.format("<red>There was an error trying to save data for %s after resetting their skill. Please have an admin check console.", player.getDisplayName())));
                                        throwable.printStackTrace();
                                        return null;
                                    });
                                    return;
                                }
                            }

                            senderAudience.sendMessage(miniMessage.deserialize(String.format("<red>Unable to reset skill for %s.", player.displayName())));
                        }
                ));
    }
}
