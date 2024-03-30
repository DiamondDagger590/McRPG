package us.eunoians.mcrpg.command.give;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.parser.PlayerParser;
import us.eunoians.mcrpg.command.parser.SkillParser;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Optional;

public class GiveExperienceCommand extends GiveCommandBase{

    private static final Permission GIVE_EXPERIENCE_PERMISSION = Permission.of("mcrpg.give.exp");

    public static void registerCommand() {
        CommandManager<CommandSender> commandManager = McRPG.getInstance().getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("give").commandDescription(RichDescription.richDescription(miniMessage.deserialize("<gray>The subcommand for all commands that give targets something")))
                .literal("exp", "experience")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to give something to to")))
                .required("skill", SkillParser.skillParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The skill to give experience for")))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of experience to give")))
                .permission(Permission.anyOf(ROOT_PERMISSION, GIVE_COMMAND_ROOT_PERMISSION, GIVE_EXPERIENCE_PERMISSION))
                .senderType(CommandSender.class)
                .handler(commandContext -> {
                            CloudKey<Skill> skillKey = CloudKey.of("skill", Skill.class);
                            Skill skill = commandContext.get(skillKey);
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            Player player = commandContext.get(playerKey);
                            CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                            int experienceAmount = commandContext.get(amountKey);

                            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
                            Audience senderAudience = adventure.sender(commandContext.sender());
                            Audience receiverAudience = adventure.player(player);

                            Optional<AbilityHolder> abilityHolderOptional = McRPG.getInstance().getEntityManager().getAbilityHolder(player.getUniqueId());
                            if (abilityHolderOptional.isPresent() && abilityHolderOptional.get() instanceof SkillHolder skillHolder) {
                                Optional<SkillHolder.SkillHolderData> skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
                                if (skillHolderDataOptional.isPresent()) {
                                    SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                                    skillHolderData.addExperience(experienceAmount);
                                    receiverAudience.sendMessage(miniMessage.deserialize(String.format("<green>You have been given <gold>%d experience <green>in <gold>%s<green>.", experienceAmount, skill.getDisplayName())));
                                    // Only send a message if the sender is not the receiver or the sender is console
                                    if (!(commandContext.sender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())){
                                        senderAudience.sendMessage(miniMessage.deserialize(String.format("<green>You gave <gold>%d experience <green>in <gold>%s <green>to %s.", experienceAmount, skill.getDisplayName(), player.getDisplayName())));
                                    }
                                    return;
                                }
                            }

                            senderAudience.sendMessage(miniMessage.deserialize(String.format("<red>Unable to give <gray>%s <red>experience at the moment.", player.displayName())));
                        }
                ));
    }
}
