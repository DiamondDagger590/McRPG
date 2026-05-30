package us.eunoians.mcrpg.command.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.gui.quest.QuestHistoryGui;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Command: {@code /mcrpg quest history}
 * <p>
 * Opens the {@link QuestHistoryGui} for the executing player, showing their
 * completed quest and chain run history.
 */
public class QuestHistoryCommand extends QuestCommandBase {

    private static final Permission QUEST_HISTORY_PERMISSION = Permission.of("mcrpg.quest.history");

    /**
     * Registers the {@code /mcrpg quest history} command.
     */
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("history")
                .permission(Permission.anyOf(ROOT_PERMISSION, QUEST_BASE_PERMISSION, QUEST_HISTORY_PERMISSION))
                .handler(commandContext -> {
                    CommandSender sender = commandContext.sender().getSender();
                    if (sender instanceof Player player) {
                        McRPGPlayerManager playerManager = McRPG.getInstance().registryAccess()
                                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                            QuestHistoryGui historyGui = new QuestHistoryGui(mcRPGPlayer);
                            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                    .manager(McRPGManagerKey.GUI).trackPlayerGui(player, historyGui);
                            player.openInventory(historyGui.getInventory());
                        });
                    }
                }));
    }
}
