package us.eunoians.mcrpg.command;

public class TestGuiCommand {

//    public static void registerCommand() {
//        CommandManager<CommandSender> commandManager = McRPG.getInstance().getBukkitCommandManager();
//        commandManager.command(commandManager.commandBuilder("test").handler(commandContext -> {
//            CommandSender commandSender = commandContext.getSender();
//            if (commandSender instanceof Player player) {
//                PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
//                playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
//                    if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
//                        HomeGui homeGui = new HomeGui(mcRPGPlayer);
//                        McRPG.getInstance().getGuiTracker().trackPlayerGui(player, homeGui);
//                        player.openInventory(homeGui.getInventory());
//                    }
//                });
//            }
//        }));
//
//        commandManager.command(commandManager.commandBuilder("skill").handler(commandContext -> {
//            CommandSender commandSender = commandContext.getSender();
//            if (commandSender instanceof Player player) {
//                PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
//                playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
//                    if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
//                        AbilityGui abilityGui = new AbilityGui(mcRPGPlayer);
//                        McRPG.getInstance().getGuiTracker().trackPlayerGui(player, abilityGui);
//                        player.openInventory(abilityGui.getInventory());
//                    }
//                });
//            }
//        }));
//    }
}
