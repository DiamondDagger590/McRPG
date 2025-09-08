package us.eunoians.mcrpg.command.loadout;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.LOADOUT_SLOT;

/**
 * This command is used for editing the player's loadout.
 * <p>
 * The following commands are usable:
 * <ul>
 * <li>/loadout edit 2 -> edits the loadout in the second slot</li>
 * <li>/loadout edit -> edits the player's current loadout</li>
 * </ul>
 */
public class LoadoutEditCommand extends McRPGCommandBase {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("loadout")
                .literal("edit")
                .optional("slot", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The loadout to edit.")))
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    CloudKey<Integer> slotKey = CloudKey.of("slot", Integer.class);
                    if (commandSender instanceof Player player) {
                        McRPGPlayerManager playerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                            LoadoutHolder loadoutHolder = mcRPGPlayer.asSkillHolder();
                            int loadoutSlot = commandContext.getOrDefault(slotKey, loadoutHolder.getCurrentLoadoutSlot());
                            Map<String, String> placeholders = getPlaceholders(loadoutSlot);
                            if (!loadoutHolder.hasLoadout(loadoutSlot)) {
                                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.LOADOUT_EDIT_COMMAND_NO_LOADOUT_MATCHES_MESSAGE, placeholders));
                                return;
                            }
                            LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadoutHolder.getLoadout(loadoutSlot));
                            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(player, loadoutGui);
                            player.openInventory(loadoutGui.getInventory());
                        });
                    }
                }));
    }

    @NotNull
    public static Map<String, String> getPlaceholders(int loadoutSlot) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(LOADOUT_SLOT.getPlaceholder(), Integer.toString(loadoutSlot));
        return placeholders;
    }
}
