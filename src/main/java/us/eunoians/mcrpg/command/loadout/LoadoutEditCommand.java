package us.eunoians.mcrpg.command.loadout;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This command is used for editing the player's loadout.
 *
 * The following commands are usable:
 * <ul>
 * <li>/loadout edit 2 -> edits the loadout in the second slot</li>
 * <li>/loadout edit -> edits the player's current loadout</li>
 * </ul>
 */
public class LoadoutEditCommand extends McRPGCommandBase {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("loadout")
                .literal("edit")
                .optional("slot", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The loadout to edit.")))
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    CloudKey<Integer> slotKey = CloudKey.of("slot", Integer.class);
                    if (commandSender instanceof Player player) {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        McRPGPlayerManager playerManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                            LoadoutHolder loadoutHolder = mcRPGPlayer.asSkillHolder();
                            int loadoutSlot = commandContext.getOrDefault(slotKey, loadoutHolder.getCurrentLoadoutSlot());
                            if (!loadoutHolder.hasLoadout(loadoutSlot)) {
                                audience.sendMessage(miniMessage.deserialize("<red>You do not have a loadout slot with that id."));
                                return;
                            }
                            LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadoutHolder.getLoadout(loadoutSlot));
                            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(player, loadoutGui);
                            player.openInventory(loadoutGui.getInventory());
                        });
                    }
                }));
    }
}
