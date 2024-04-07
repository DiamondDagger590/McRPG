package us.eunoians.mcrpg.command.give;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.parser.SkillParser;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

/**
 * Command used to give a player levels in one of their skills
 */
public class GiveLevelsCommand extends GiveCommandBase {

    private static final Permission GIVE_LEVELS_PERMISSION = Permission.of("mcrpg.give.level");

    public static void registerCommand() {
        CommandManager<CommandSender> commandManager = McRPG.getInstance().getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("give").commandDescription(RichDescription.richDescription(miniMessage.deserialize("<gray>The subcommand for all commands that give targets something")))
                .literal("level", "levels")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to give something to to")))
                .required("skill", SkillParser.skillParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The skill to give levels for")))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of levels to give")))
                .flag(commandManager.flagBuilder("reset_experience").withAliases("r")).commandDescription(RichDescription.richDescription(miniMessage.deserialize("<gray>If the player's current experience should be reset to 0 after levels are given.")))
                .permission(Permission.anyOf(ROOT_PERMISSION, GIVE_COMMAND_ROOT_PERMISSION, GIVE_LEVELS_PERMISSION))
                .senderType(CommandSender.class)
                .handler(commandContext -> {
                            CloudKey<Skill> skillKey = CloudKey.of("skill", Skill.class);
                            Skill skill = commandContext.get(skillKey);
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            Player player = commandContext.get(playerKey);

                            boolean resetExperience = commandContext.flags().isPresent("reset_experience");

                            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
                            Audience senderAudience = adventure.sender(commandContext.sender());
                            Audience receiverAudience = adventure.player(player);

                            var abilityHolderOptional = McRPG.getInstance().getEntityManager().getAbilityHolder(player.getUniqueId());
                            if (abilityHolderOptional.isPresent() && abilityHolderOptional.get() instanceof SkillHolder skillHolder) {
                                var skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
                                if (skillHolderDataOptional.isPresent()) {
                                    SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                                    CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                                    /*
                                     We can't give them a level amount that would put them over the max level, so we only give the difference between the max and current level if that amount is smaller than the provided amount.
                                     While there is a sanity check inside the add levels method, we want to ensure
                                     */
                                    int levelAmount = Math.min(commandContext.get(amountKey), skill.getMaxLevel() - skillHolderData.getCurrentLevel());

                                    skillHolderData.addLevel(levelAmount, resetExperience); // No need to send a message ourselves as this sends one
                                    // Only send a message if the sender is not the receiver or the sender is console
                                    if (!(commandContext.sender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())){
                                        senderAudience.sendMessage(miniMessage.deserialize(String.format("<green>You gave <gold>%d levels <green>in <gold>%s <green>to %s.", levelAmount, skill.getDisplayName(), player.getDisplayName())));
                                    }
                                    return;
                                }
                            }
                            senderAudience.sendMessage(miniMessage.deserialize(String.format("<red>Unable to give <gray>%s <red>levels at the moment.", player.displayName())));
                        }
                ));
    }
}
