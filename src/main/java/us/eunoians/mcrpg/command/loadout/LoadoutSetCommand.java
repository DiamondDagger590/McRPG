package us.eunoians.mcrpg.command.loadout;

import com.diamonddagger590.mccore.registry.RegistryAccess;
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
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.LOADOUT_SLOT;

/**
 * This command sets the player's current {@link us.eunoians.mcrpg.loadout.Loadout} to
 * the slot provided.
 */
public class LoadoutSetCommand extends McRPGCommandBase {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("loadout")
                .literal("set")
                .required("slot", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The loadout to set.")))
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    CloudKey<Integer> amountKey = CloudKey.of("slot", Integer.class);
                    int loadoutSlot = commandContext.get(amountKey);
                    if (commandSender instanceof Player player) {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        McRPGPlayerManager playerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                        Map<String, String> placeholders = getPlaceholders(loadoutSlot);
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                            LoadoutHolder loadoutHolder = mcRPGPlayer.asSkillHolder();
                            if (!loadoutHolder.hasLoadout(loadoutSlot)) {
                                audience.sendMessage(localizationManager.getLocalizedMessageAsComponent(audience, LocalizationKey.LOADOUT_SET_COMMAND_NO_LOADOUT_MATCHES_MESSAGE, placeholders));
                                return;
                            }
                            loadoutHolder.setCurrentLoadoutSlot(loadoutSlot);
                            audience.sendMessage(localizationManager.getLocalizedMessageAsComponent(audience, LocalizationKey.LOADOUT_SET_COMMAND_LOADOUT_SET_SUCCESS_MESSAGE, placeholders));
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
