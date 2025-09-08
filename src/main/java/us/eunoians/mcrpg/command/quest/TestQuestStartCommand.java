package us.eunoians.mcrpg.command.quest;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.UUID;

public class TestQuestStartCommand {

        public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        commandManager.command(commandManager.commandBuilder("quest").literal("start").handler(commandContext -> {
            CommandSender commandSender = commandContext.sender().getSender();
            Player player = (Player) commandSender;
            QuestManager questManager = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
            var questHolder = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).getQuestHolder(player.getUniqueId());
            Quest quest = new Quest("test");
            BlockBreakQuestObjective objective1 = new BlockBreakQuestObjective(quest, 10);
            objective1.addAllowedBlocks(Material.GRASS_BLOCK);
            BlockBreakQuestObjective objective2 = new BlockBreakQuestObjective(quest, 15);
            objective2.addBannedBlocks(Material.GRASS_BLOCK);
            quest.addQuestObjective(objective1, objective2);

            questManager.addActiveQuest(quest);
            questManager.addHolderToQuest(questHolder.get(), quest);
            quest.startQuest();
        }));

        commandManager.command(commandManager.commandBuilder("quest").literal("info").handler(commandContext -> {
            CommandSender commandSender = commandContext.sender().getSender();
            if (commandSender instanceof Player player) {
                MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                Component newline = miniMessage.deserialize("");
                player.sendMessage(miniMessage.deserialize("<gray>Displaying all active quests:"));
                player.sendMessage(newline);

                QuestManager questManager = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
                QuestHolder questHolder = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).getQuestHolder(player.getUniqueId()).get();

                for (UUID questUUID : questHolder.getActiveQuests()) {
                    Quest quest = questManager.getActiveQuest(questUUID).get();
                    player.sendMessage(miniMessage.deserialize("<gray>  Quest " + quest.getUUID() + " Info</gray>"));
                    player.sendMessage(miniMessage.deserialize("<gray>  Completed: " + (quest.isCompleted() ? "<green>true<green>" : "<red>false</red>") + "</gray>"));
                    player.sendMessage(miniMessage.deserialize("<gray>  Current Progression - ").append(Methods.getProgressBar(quest.getQuestProgress(), 20)));
                    int objectiveCounter = 1;
                    for (QuestObjective objective : quest.getQuestObjectives()) {
                        player.sendMessage(newline);
                        Component spacing = miniMessage.deserialize("    ");
                        player.sendMessage(spacing.append(miniMessage.deserialize("<gray>Objective " + objectiveCounter + ": </gray>")).append(objective.getObjectiveTitle()));
                        player.sendMessage(spacing.append(miniMessage.deserialize("<gray>Progress (<gold>" + objective.getCurrentProgression() + "<gray>/</gray>" + objective.getRequiredProgression() + "</gold>) ").append(Methods.getProgressBar(objective.getObjectiveProgress(), 30))));
                        objective.getObjectiveInfoText().forEach(component -> player.sendMessage(spacing.append(component)));
                        objectiveCounter++;
                    }
                }
            }
        }));
    }
}
