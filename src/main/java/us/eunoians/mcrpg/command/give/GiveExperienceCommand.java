package us.eunoians.mcrpg.command.give;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
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

import static us.eunoians.mcrpg.command.CommandPlaceholders.EXPERIENCE;
import static us.eunoians.mcrpg.command.CommandPlaceholders.SKILL;

/**
 * Command used to give a player experience in their skills
 */
public class GiveExperienceCommand extends GiveCommandBase {

    private static final Permission GIVE_EXPERIENCE_PERMISSION = Permission.of("mcrpg.give.exp");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("give").commandDescription(RichDescription.richDescription(miniMessage.deserialize("<gray>The subcommand for all commands that give targets something")))
                .literal("exp", "experience")
                .required("player", PlayerParser.playerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The player to give something to to")))
                .required("skill", SkillParser.skillParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The skill to give experience for")))
                .required("amount", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The amount of experience to give")))
                .permission(Permission.anyOf(ROOT_PERMISSION, GIVE_COMMAND_ROOT_PERMISSION, GIVE_EXPERIENCE_PERMISSION))
                .handler(commandContext -> {
                            CloudKey<Skill> skillKey = CloudKey.of("skill", Skill.class);
                            Skill skill = commandContext.get(skillKey);
                            CloudKey<Player> playerKey = CloudKey.of("player", Player.class);
                            Player player = commandContext.get(playerKey);
                            CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                            int experienceAmount = commandContext.get(amountKey);

                            Audience senderAudience = commandContext.sender().getSender();
                            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                            Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                            Optional<McRPGPlayer> senderOptional = senderAudience instanceof Player sender ? McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                    .manager(McRPGManagerKey.PLAYER).getPlayer(sender.getUniqueId()) : Optional.empty();
                            Map<String, String> senderPlaceholders = getPlaceholders(senderAudience, senderAudience, player, experienceAmount, skill, senderOptional.orElse(null));
                            Map<String, String> receiverPlaceholders = getPlaceholders(player, senderAudience, player, experienceAmount, skill, playerOptional.orElse(null));

                            if (playerOptional.isPresent()) {
                                McRPGPlayer mcRPGPlayer = playerOptional.get();
                                SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
                                Optional<SkillHolder.SkillHolderData> skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
                                if (skillHolderDataOptional.isPresent()) {
                                    SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                                    skillHolderData.addExperience(experienceAmount);
                                    player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.GIVE_EXPERIENCE_COMMAND_RECIPIENT_MESSAGE, receiverPlaceholders));
                                    // Only send a message if the sender is not the receiver or the sender is console
                                    if (!(senderAudience instanceof Player sender) || !sender.getUniqueId().equals(player.getUniqueId())) {
                                        senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_EXPERIENCE_COMMAND_SENDER_SUCCESS_MESSAGE, senderPlaceholders));
                                    }
                                    return;
                                }
                            }
                            senderAudience.sendMessage(localizationManager.getLocalizedMessageAsComponent(senderAudience, LocalizationKey.GIVE_EXPERIENCE_COMMAND_SENDER_ERROR_MESSAGE, senderPlaceholders));
                        }
                ));
    }

    @NotNull
    private static Map<String, String> getPlaceholders(@NotNull Audience messageAudience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience, int experience,
                                                      @NotNull Skill skill, @Nullable McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(EXPERIENCE.getPlaceholder(), Integer.toString(experience));
        placeholders.put(SKILL.getPlaceholder(), mcRPGPlayer == null ? skill.getName() : skill.getName(mcRPGPlayer));
        return placeholders;
    }
}
