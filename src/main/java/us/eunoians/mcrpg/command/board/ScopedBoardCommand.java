package us.eunoians.mcrpg.command.board;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.gui.board.BoardGuiMode;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Command: {@code /mcrpg board group} and {@code /groupboard}
 * <p>
 * Opens the quest board GUI in scoped (group) mode for the executing player.
 */
public class ScopedBoardCommand extends McRPGCommandBase {

    private static final Permission GROUP_BOARD_PERMISSION = Permission.of("mcrpg.command.board.group");

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();

        // Primary: /mcrpg board group
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("board")
                .literal("group")
                .permission(Permission.anyOf(ROOT_PERMISSION, GROUP_BOARD_PERMISSION))
                .handler(commandContext -> {
                    if (!(commandContext.sender().getSender() instanceof Player player)) {
                        return;
                    }
                    openScopedBoard(player);
                }));

        // Alias: /groupboard
        commandManager.command(commandManager.commandBuilder("groupboard")
                .permission(Permission.anyOf(ROOT_PERMISSION, GROUP_BOARD_PERMISSION))
                .handler(commandContext -> {
                    if (!(commandContext.sender().getSender() instanceof Player player)) {
                        return;
                    }
                    openScopedBoard(player);
                }));
    }

    private static void openScopedBoard(@NotNull Player player) {
        McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId())
                .ifPresent(mcRPGPlayer -> {
                    QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer, BoardGuiMode.SCOPED);
                    McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.GUI)
                            .trackPlayerGui(player, gui);
                    player.openInventory(gui.getInventory());
                });
    }
}
