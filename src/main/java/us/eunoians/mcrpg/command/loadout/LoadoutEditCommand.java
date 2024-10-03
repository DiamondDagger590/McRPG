package us.eunoians.mcrpg.command.loadout;

import com.diamonddagger590.mccore.player.PlayerManager;
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
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;

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
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().getCommandManager().getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("loadout")
                .literal("edit")
                .optional("slot", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The loadout to edit.")))
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    CloudKey<Integer> slotKey = CloudKey.of("slot", Integer.class);
                    if (commandSender instanceof Player player) {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
                            if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                                LoadoutHolder loadoutHolder = mcRPGPlayer.asSkillHolder();
                                int loadoutSlot = commandContext.getOrDefault(slotKey, loadoutHolder.getCurrentLoadoutSlot());
                                if (!loadoutHolder.hasLoadout(loadoutSlot)) {
                                    audience.sendMessage(miniMessage.deserialize("<red>You do not have a loadout slot with that id."));
                                    return;
                                }
                                LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadoutHolder.getLoadout(loadoutSlot));
                                McRPG.getInstance().getGuiTracker().trackPlayerGui(player, loadoutGui);
                                player.openInventory(loadoutGui.getInventory());
                            }
                        });
                    }
                }));
    }
}
