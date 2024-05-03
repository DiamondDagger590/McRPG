package us.eunoians.mcrpg.command.quest;

import com.diamonddagger590.mccore.util.Methods;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.BlockBreakQuestObjective;
import us.eunoians.mcrpg.quest.objective.QuestObjective;

import java.util.UUID;

public class TestQuestStartCommand {

        public static void registerCommand() {
        CommandManager<CommandSender> commandManager = McRPG.getInstance().getCommandManager();
        commandManager.command(commandManager.commandBuilder("quest").literal("start").handler(commandContext -> {
            CommandSender commandSender = commandContext.sender();
            Player player = (Player) commandSender;
            QuestManager questManager = McRPG.getInstance().getQuestManager();
            var questHolder = McRPG.getInstance().getEntityManager().getQuestHolder(player.getUniqueId());
            Quest quest = new Quest("test");
            BlockBreakQuestObjective objective1 = new BlockBreakQuestObjective(quest, 10);
            objective1.addAllowedBlocks(Material.GRASS_BLOCK);
            BlockBreakQuestObjective objective2 = new BlockBreakQuestObjective(quest, 15);
            objective2.addBannedBlocks(Material.GRASS_BLOCK);
            quest.addQuestObjective(objective1, objective2);

            questManager.addActiveQuest(quest);
            questManager.trackQuestForHolder(questHolder.get(), quest);
            quest.startQuest();
        }));

        commandManager.command(commandManager.commandBuilder("quest").literal("info").handler(commandContext -> {
            CommandSender commandSender = commandContext.sender();
            if (commandSender instanceof Player player) {
                Audience audience = McRPG.getInstance().getAdventure().player(player);
                MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                Component newline = miniMessage.deserialize("");
                audience.sendMessage(miniMessage.deserialize("<gray>Displaying all active quests:"));
                audience.sendMessage(newline);

                QuestManager questManager = McRPG.getInstance().getQuestManager();
                QuestHolder questHolder = McRPG.getInstance().getEntityManager().getQuestHolder(player.getUniqueId()).get();

                for (UUID questUUID : questHolder.getActiveQuests()) {
                    Quest quest = questManager.getActiveQuest(questUUID).get();
                    audience.sendMessage(miniMessage.deserialize("<gray>  Quest " + quest.getUUID() + " Info</gray>"));
                    audience.sendMessage(miniMessage.deserialize("<gray>  Completed: " + (quest.isCompleted() ? "<green>true<green>" : "<red>false</red>") + "</gray>"));
                    audience.sendMessage(miniMessage.deserialize("<gray>  Current Progression - ").append(Methods.getProgressBar(quest.getQuestProgress(), 20)));
                    int objectiveCounter = 1;
                    for (QuestObjective objective : quest.getQuestObjectives()) {
                        audience.sendMessage(newline);
                        Component spacing = miniMessage.deserialize("    ");
                        audience.sendMessage(spacing.append(miniMessage.deserialize("<gray>Objective " + objectiveCounter + ": </gray>")).append(objective.getObjectiveTitle()));
                        audience.sendMessage(spacing.append(miniMessage.deserialize("<gray>Progress (<gold>" + objective.getCurrentProgression() + "<gray>/</gray>" + objective.getRequiredProgression() + "</gold>) ").append(Methods.getProgressBar(objective.getObjectiveProgress(), 30))));
                        objective.getObjectiveInfoText().forEach(component -> audience.sendMessage(spacing.append(component)));
                        objectiveCounter++;
                    }
                }
            }
        }));
    }
}
