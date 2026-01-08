package us.eunoians.mcrpg.command.admin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static us.eunoians.mcrpg.command.CommandPlaceholders.EXPERIENCE;
import static us.eunoians.mcrpg.command.CommandPlaceholders.LEVEL;
import static us.eunoians.mcrpg.command.CommandPlaceholders.SKILL;
import static us.eunoians.mcrpg.command.CommandPlaceholders.TARGET;
import static us.eunoians.mcrpg.command.CommandPlaceholders.UPGRADE_POINTS;

/**
 * Command used for development, prints various debug information about a player
 */
public class DebugCommand {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("debug")
                .optional("player", PlayerParser.playerParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_DEBUG_PLAYER)))
                .permission(Permission.of("mcrpg.debug"))
                .handler(commandContext -> {
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            CommandSender sender = commandContext.sender().getSender();
                            Player player = commandContext.getOrDefault(playerKey, (Player) sender);

                    Optional<AbilityHolder> abilityHolderOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).getAbilityHolder(player.getUniqueId());
                            if (abilityHolderOptional.isPresent() && abilityHolderOptional.get() instanceof SkillHolder skillHolder) {
                                Map<String, String> headerPlaceholders = new HashMap<>();
                                headerPlaceholders.put(TARGET.getPlaceholder(), player.getName());
                                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.DEBUG_COMMAND_HEADER_MESSAGE, headerPlaceholders));

                                Map<String, String> upgradePointsPlaceholders = new HashMap<>();
                                upgradePointsPlaceholders.put(UPGRADE_POINTS.getPlaceholder(), Integer.toString(skillHolder.getUpgradePoints()));
                                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.DEBUG_COMMAND_UPGRADE_POINTS_MESSAGE, upgradePointsPlaceholders));

                                for (NamespacedKey skill : skillHolder.getSkills()) {
                                    Optional<SkillHolder.SkillHolderData> skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
                                    skillHolderDataOptional.ifPresent(skillHolderData -> {
                                        Map<String, String> skillPlaceholders = new HashMap<>();
                                        skillPlaceholders.put(SKILL.getPlaceholder(), skillHolderData.getSkillKey().value());
                                        skillPlaceholders.put(LEVEL.getPlaceholder(), Integer.toString(skillHolderData.getCurrentLevel()));
                                        skillPlaceholders.put(EXPERIENCE.getPlaceholder(), Integer.toString(skillHolderData.getCurrentExperience()));
                                        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.DEBUG_COMMAND_SKILL_INFO_MESSAGE, skillPlaceholders));
                                    });
                                }
                            }
                        }
                ));
    }
}
