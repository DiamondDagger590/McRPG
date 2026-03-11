package us.eunoians.mcrpg.command.loadout;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.loadout.LoadoutResolution;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static us.eunoians.mcrpg.command.CommandPlaceholders.LOADOUT_MATCHES;
import static us.eunoians.mcrpg.command.CommandPlaceholders.LOADOUT_SLOT;

/**
 * This command is used for editing the player's loadout.
 * <p>
 * The following commands are usable:
 * <ul>
 * <li>/loadout edit 2 -> edits the loadout in the second slot</li>
 * <li>/loadout edit "mining loadout" -> edits the loadout named "mining loadout"</li>
 * <li>/loadout edit mining -> edits the loadout whose name contains "mining" (if unambiguous)</li>
 * <li>/loadout edit -> edits the player's current loadout</li>
 * </ul>
 */
public class LoadoutEditCommand extends McRPGCommandBase {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("loadout")
                .literal("edit")
                .optional("slot", StringParser.greedyStringParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_LOADOUT_EDIT_SLOT)))
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    CloudKey<String> slotKey = CloudKey.of("slot", String.class);
                    if (commandSender instanceof Player player) {
                        McRPGPlayerManager playerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                            LoadoutHolder loadoutHolder = mcRPGPlayer.asSkillHolder();
                            String slotInput = commandContext.getOrDefault(slotKey, null);
                            if (slotInput == null) {
                                // No argument provided — edit the current loadout
                                openLoadoutGui(player, mcRPGPlayer, loadoutHolder, loadoutHolder.getLoadout());
                                return;
                            }
                            LoadoutResolution resolution = loadoutHolder.resolveLoadout(slotInput);
                            if (resolution instanceof LoadoutResolution.Found found) {
                                openLoadoutGui(player, mcRPGPlayer, loadoutHolder, found.loadout());
                            } else if (resolution instanceof LoadoutResolution.Ambiguous ambiguous) {
                                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.LOADOUT_EDIT_COMMAND_AMBIGUOUS_MATCHES_MESSAGE, getMatchesPlaceholders(ambiguous.matches())));
                            } else {
                                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.LOADOUT_EDIT_COMMAND_NO_LOADOUT_MATCHES_MESSAGE, Map.of()));
                            }
                        });
                    }
                }));
    }

    private static void openLoadoutGui(@NotNull Player player, @NotNull us.eunoians.mcrpg.entity.player.McRPGPlayer mcRPGPlayer,
                                       @NotNull LoadoutHolder loadoutHolder, @NotNull Loadout loadout) {
        LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadout);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(player, loadoutGui);
        player.openInventory(loadoutGui.getInventory());
    }

    @NotNull
    public static Map<String, String> getPlaceholders(int loadoutSlot) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(LOADOUT_SLOT.getPlaceholder(), Integer.toString(loadoutSlot));
        return placeholders;
    }

    @NotNull
    private static Map<String, String> getMatchesPlaceholders(@NotNull List<Loadout> matches) {
        String matchList = matches.stream()
                .map(loadout -> "Slot " + loadout.getLoadoutSlot() + loadout.getDisplay().getDisplayName().map(n -> " (" + n + ")").orElse(""))
                .collect(Collectors.joining(", "));
        return Map.of(LOADOUT_MATCHES.getPlaceholder(), matchList);
    }
}
