package us.eunoians.mcrpg.command.link;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGAbilityKey;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.SKILL;

public class UnlinkChestCommand {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("unlink")
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    if (commandSender instanceof Player player) {
                        McRPGPlayerManager playerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                            Map<String, String> placeholders = getPlaceholders(mcRPGPlayer);
                            RemoteTransfer remoteTransfer = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY).ability(McRPGAbilityKey.REMOTE_TRANSFER);
                            AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
                            var abilityDataOptional = abilityHolder.getAbilityData(remoteTransfer);
                            if (abilityDataOptional.isPresent()) {
                                AbilityData abilityData = abilityDataOptional.get();
                                var abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_LOCATION_ATTRIBUTE);
                                if (abilityAttributeOptional.isPresent()) {
                                    abilityData.removeAttribute(abilityAttributeOptional.get());
                                    player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.UNLINK_COMMAND_SUCCESS_MESSAGE, placeholders));
                                } else {
                                    player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.UNLINK_COMMAND_NO_LINKED_CHEST_MESSAGE, placeholders));
                                }
                            }
                        });
                    }
                }));
    }

    @NotNull
    public static Map<String, String> getPlaceholders(@Nullable McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        Ability ability = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY).ability(McRPGAbilityKey.REMOTE_TRANSFER);
        placeholders.put(SKILL.getPlaceholder(), mcRPGPlayer == null ? ability.getName() : ability.getName(mcRPGPlayer));
        return placeholders;
    }
}
