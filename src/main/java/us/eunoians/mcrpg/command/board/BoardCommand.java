package us.eunoians.mcrpg.command.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Command: {@code /mcrpg board} and {@code /board}
 * <p>
 * Opens the quest board GUI for the executing player.
 */
public class BoardCommand extends McRPGCommandBase {

    private static final Permission BOARD_PERMISSION = Permission.of("mcrpg.command.board");

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();

        // Primary: /mcrpg board
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("board")
                .permission(Permission.anyOf(ROOT_PERMISSION, BOARD_PERMISSION))
                .handler(commandContext -> {
                    if (!(commandContext.sender().getSender() instanceof Player player)) {
                        return;
                    }

                    McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.PLAYER)
                            .getPlayer(player.getUniqueId())
                            .ifPresent(mcRPGPlayer -> {
                                QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer);
                                McRPG.getInstance().registryAccess()
                                        .registry(RegistryKey.MANAGER)
                                        .manager(McRPGManagerKey.GUI)
                                        .trackPlayerGui(player, gui);
                                player.openInventory(gui.getInventory());
                            });
                }));

        // Alias: /board
        commandManager.command(commandManager.commandBuilder("board")
                .permission(Permission.anyOf(ROOT_PERMISSION, BOARD_PERMISSION))
                .handler(commandContext -> {
                    if (!(commandContext.sender().getSender() instanceof Player player)) {
                        return;
                    }

                    McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.PLAYER)
                            .getPlayer(player.getUniqueId())
                            .ifPresent(mcRPGPlayer -> {
                                QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer);
                                McRPG.getInstance().registryAccess()
                                        .registry(RegistryKey.MANAGER)
                                        .manager(McRPGManagerKey.GUI)
                                        .trackPlayerGui(player, gui);
                                player.openInventory(gui.getInventory());
                            });
                }));
    }
}
