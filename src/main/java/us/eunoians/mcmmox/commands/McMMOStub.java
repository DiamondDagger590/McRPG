package us.eunoians.mcmmox.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcmmox.gui.GUI;
import us.eunoians.mcmmox.gui.GUITracker;
import us.eunoians.mcmmox.gui.HomeGUI;
import us.eunoians.mcmmox.players.PlayerManager;

public class McMMOStub implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player p = (Player) sender;
            if(args.length == 0){
                GUI gui = new HomeGUI(PlayerManager.getPlayer(p.getUniqueId()));
                p.openInventory(gui.getGui().getInv());
                GUITracker.trackPlayer(p, gui);
                return true;
            }
            else{
                p.sendMessage("Not added");
            }
        }
        return false;
    }
}
