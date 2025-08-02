package us.eunoians.mcrpg.command.link;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityLocationAttribute;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.fake.FakeChestOpenEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.SKILL;

/**
 * This command is used to link a player to a specific chest for their {@link RemoteTransfer} ability.
 */
public class LinkChestCommand {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("link")
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    if (commandSender instanceof Player player) {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        McRPGPlayerManager playerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                            Map<String, String> placeholders = getPlaceholders(mcRPGPlayer);
                            RemoteTransfer remoteTransfer = (RemoteTransfer) RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
                            Block block = player.getTargetBlock(null, 100);
                            if (block.getType() != Material.CHEST) {
                                audience.sendMessage(localizationManager.getLocalizedMessageAsComponent(audience, LocalizationKey.LINK_COMMAND_NOT_LOOKING_AT_CHEST_MESSAGE, placeholders));
                                return;
                            }
                            else if (!remoteTransfer.isAbilityEnabled()) {
                                audience.sendMessage(localizationManager.getLocalizedMessageAsComponent(audience, LocalizationKey.LINK_COMMAND_REMOTE_TRANSFER_NOT_ENABLED_MESSAGE, placeholders));
                                return;
                            }
                            FakeChestOpenEvent fakeChestOpenEvent = new FakeChestOpenEvent(player, block.getLocation());
                            Bukkit.getPluginManager().callEvent(fakeChestOpenEvent);
                            if (fakeChestOpenEvent.useInteractedBlock() == Event.Result.ALLOW) {
                                AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
                                var abilityDataOptional = abilityHolder.getAbilityData(remoteTransfer);
                                if (abilityDataOptional.isPresent()) {
                                    AbilityData abilityData = abilityDataOptional.get();
                                    abilityData.addAttribute(new AbilityLocationAttribute(block.getLocation()));
                                    audience.sendMessage(localizationManager.getLocalizedMessageAsComponent(audience, LocalizationKey.LINK_COMMAND_SUCCESS_MESSAGE, placeholders));
                                }
                            }
                        });
                    }
                }));
    }

    @NotNull
    public static Map<String, String> getPlaceholders(@Nullable McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        Ability ability = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        placeholders.put(SKILL.getPlaceholder(), mcRPGPlayer == null ? ability.getName() : ability.getName(mcRPGPlayer));
        return placeholders;
    }
}
