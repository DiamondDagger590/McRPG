package us.eunoians.mcrpg.command.give;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.command.parser.SkillParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static us.eunoians.mcrpg.command.CommandPlaceholders.LEVEL;
import static us.eunoians.mcrpg.command.CommandPlaceholders.SKILL;

/**
 * Command used to give a player levels in one of their skills
 */
public class GiveLevelsCommand extends GiveCommandBase {

    private static final Permission GIVE_LEVELS_PERMISSION = Permission.of("mcrpg.give.level");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("give").commandDescription(RichDescription.richDescription(miniMessage.deserialize("<gray>The subcommand for all commands that give targets something")))
                .literal("level", "levels")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to give something to to")))
                .required("skill", SkillParser.skillParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The skill to give levels for")))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of levels to give")))
                .flag(commandManager.flagBuilder("reset_experience").withAliases("r")).commandDescription(RichDescription.richDescription(miniMessage.deserialize("<gray>If the player's current experience should be reset to 0 after levels are given.")))
                .permission(Permission.anyOf(ROOT_PERMISSION, GIVE_COMMAND_ROOT_PERMISSION, GIVE_LEVELS_PERMISSION))
                .handler(commandContext -> {
                            CloudKey<Skill> skillKey = CloudKey.of("skill", Skill.class);
                            Skill skill = commandContext.get(skillKey);
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            Player player = commandContext.get(playerKey);
                            CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                            boolean resetExperience = commandContext.flags().isPresent("reset_experience");

                            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
                            Audience senderAudience = adventure.sender(commandContext.sender().getSender());
                            Audience receiverAudience = adventure.player(player);
                            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                            Optional<McRPGPlayer> senderOptional = commandContext.sender() instanceof Player sender ? McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                    .manager(McRPGManagerKey.PLAYER).getPlayer(sender.getUniqueId()) : Optional.empty();
                            Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());

                            Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, receiverAudience);
                            Map<String, String> receiverPlaceholders = getPlaceholders(receiverAudience, senderAudience, receiverAudience);
                            if (playerOptional.isPresent()) {
                                McRPGPlayer mcRPGPlayer = playerOptional.get();
                                SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
                                var skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
                                if (skillHolderDataOptional.isPresent()) {
                                    SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                                    /*
                                     We can't give them a level amount that would put them over the max level, so we only give the difference between the max and current level if that amount is smaller than the provided amount.
                                     While there is a sanity check inside the add levels method, we want to ensure
                                     */
                                    int levelAmount = Math.min(commandContext.get(amountKey), skill.getMaxLevel() - skillHolderData.getCurrentLevel());

                                    skillHolderData.addLevel(levelAmount, resetExperience);
                                    receiverAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_LEVELS_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
                                    // Only send a message if the sender is not the receiver or the sender is console
                                    if (!(commandContext.sender() instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                                        senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                                    }
                                    return;
                                }
                            }
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_LEVELS_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));
                        }
                ));
    }

    @NotNull
    public static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience, int levels,
                                                      @NotNull Skill skill, @Nullable McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(SKILL.getPlaceholder(), mcRPGPlayer == null ? skill.getName() : skill.getName(mcRPGPlayer));
        placeholders.put(LEVEL.getPlaceholder(), Integer.toString(levels));
        return placeholders;
    }
}
