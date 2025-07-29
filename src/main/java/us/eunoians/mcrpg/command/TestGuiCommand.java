package us.eunoians.mcrpg.command;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.gui.home.HomeGui;
import us.eunoians.mcrpg.gui.ability.AbilityGui;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

public class TestGuiCommand {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        commandManager.command(commandManager.commandBuilder("mcrpg").handler(commandContext -> {
            CommandSender commandSender = commandContext.sender().getSender();
            if (commandSender instanceof Player player) {
                McRPGPlayerManager playerManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                    HomeGui homeGui = new HomeGui(mcRPGPlayer);
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(player, homeGui);
                    player.openInventory(homeGui.getInventory());
                });
            }
        }));

        commandManager.command(commandManager.commandBuilder("skill").handler(commandContext -> {
            CommandSender commandSender = commandContext.sender().getSender();
            if (commandSender instanceof Player player) {
                McRPGPlayerManager playerManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                    AbilityGui abilityGui = new AbilityGui(mcRPGPlayer);
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(player, abilityGui);
                    player.openInventory(abilityGui.getInventory());
                });
            }
        }));
    }
}
