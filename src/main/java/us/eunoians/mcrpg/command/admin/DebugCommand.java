package us.eunoians.mcrpg.command.admin;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

import java.util.Optional;

/**
 * Command used for development, prints various debug information about a player
 */
public class DebugCommand {

    public static void registerCommand() {
        CommandManager<CommandSender> commandManager = McRPG.getInstance().getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("debug")
                .optional("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to reset something for")))
                .permission(Permission.of("mcrpg.debug"))
                .senderType(Player.class)
                .handler(commandContext -> {
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            Player player = commandContext.getOrDefault(playerKey, commandContext.sender());

                            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
                            Audience senderAudience = adventure.sender(commandContext.sender());

                            Optional<AbilityHolder> abilityHolderOptional = McRPG.getInstance().getEntityManager().getAbilityHolder(player.getUniqueId());
                            if (abilityHolderOptional.isPresent() && abilityHolderOptional.get() instanceof SkillHolder skillHolder) {
                                senderAudience.sendMessage(miniMessage.deserialize(String.format("<gray>Printing debug information for player <gold>%s", player.getDisplayName())));
                                for (NamespacedKey skill : skillHolder.getSkills()) {
                                    Optional<SkillHolder.SkillHolderData> skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
                                    if (skillHolderDataOptional.isPresent()) {
                                        SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                                        senderAudience.sendMessage(miniMessage.deserialize(String.format("<gray>Skill: <gold>%s <gray>Level: <gold>%d <gray>Exp: <gold>%d", skillHolderData.getSkillKey().value(), skillHolderData.getCurrentLevel(), skillHolderData.getCurrentExperience())));
                                        return;
                                    }
                                }
                            }
                        }
                ));
    }
}
