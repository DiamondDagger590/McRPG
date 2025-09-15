package us.eunoians.mcrpg.command.redeem;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.command.parser.SkillParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Map;
import java.util.Optional;

import static us.eunoians.mcrpg.command.CommandPlaceholders.REDEEMED_LEVELS;
import static us.eunoians.mcrpg.command.CommandPlaceholders.SKILL;

/**
 * This class manages the /mcrpg redeem levels command
 */
public class RedeemLevelsCommand extends McRPGCommandBase {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                .manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        Command.Builder<CommandSourceStack> redeemBuilder = commandManager.commandBuilder("mcrpg")
                .literal("redeem").commandDescription(RichDescription.richDescription(miniMessage.deserialize("<gray>The subcommand that allows players to redeem things.")))
                .literal("levels", "lv", "lvs", "level")
                .required("skill", SkillParser.skillParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>The skill to redeem experience in")));
        commandManager.command(redeemBuilder
                .required("amount", IntegerParser.integerParser(), RichDescription.richDescription(miniMessage.deserialize("<gray>How many levels to redeem?")))
                .handler(commandContext -> {
                            CommandSender commandSender = commandContext.sender().getSender();
                            CloudKey<Skill> skillKey = CloudKey.of("skill", Skill.class);
                            Skill skill = commandContext.get(skillKey);
                            CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                            int amount = commandContext.get("amount");
                            if (commandSender instanceof Player player) {
                                Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                        .manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                                playerOptional.ifPresent(mcRPGPlayer -> redeemLevels(mcRPGPlayer, skill, amount));
                                // If they aren't present, it typically means their data isn't loaded yet so it's fine to just no-op
                            } else {
                                // Otherwise it's console which isn't supported
                                commandSender.sendMessage(miniMessage.deserialize("<red>Non-players are not allowed to run this command."));
                            }
                        }
                ));

        commandManager.command(redeemBuilder
                .literal("all")
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    CloudKey<Skill> skillKey = CloudKey.of("skill", Skill.class);
                    Skill skill = commandContext.get(skillKey);
                    if (commandSender instanceof Player player) {
                        Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                .manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                        playerOptional.ifPresent(mcRPGPlayer -> redeemLevels(mcRPGPlayer, skill, mcRPGPlayer.getExperienceExtras().getRedeemableLevels()));
                        // If they aren't present, it typically means their data isn't loaded yet so it's fine to just no-op
                    } else {
                        commandSender.sendMessage(miniMessage.deserialize("<red>Non-players are not allowed to run this command."));
                    }
                }));
    }

    /**
     * Redeems redeemable levels into the provided {@link Skill}.
     * @param mcRPGPlayer The player to redeem levels for.
     * @param skill The skill to redeem levels into.
     * @param amount The amount of levels to redeem.
     */
    public static void redeemLevels(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill, int amount) {
        PlayerExperienceExtras playerExperienceExtras = mcRPGPlayer.getExperienceExtras();
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            if (amount > playerExperienceExtras.getRedeemableLevels()) {
                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_LEVELS_NOT_ENOUGH_LEVELS_MESSAGE));
                return;
            }
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            var skillDataOptional = skillHolder.getSkillHolderData(skill);
            if (skillDataOptional.isPresent()) {
                SkillHolder.SkillHolderData skillData = skillDataOptional.get();
                if (skillData.getCurrentLevel() >= skill.getMaxLevel()) {
                    player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_LEVELS_SKILL_ALREADY_MAXED_MESSAGE, Map.of(SKILL.getPlaceholder(), skill.getName(mcRPGPlayer))));
                    return;
                }

                int gainedLevels = skillData.addLevels(amount);
                int levelsToTakeAway = Math.min(gainedLevels, amount);
                playerExperienceExtras.modifyRedeemableLevels(levelsToTakeAway * -1);
                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_LEVELS_REDEEMED_LEVELS_MESSAGE, Map.of(SKILL.getPlaceholder(), skill.getName(mcRPGPlayer),
                        REDEEMED_LEVELS.getPlaceholder(), Integer.toString(levelsToTakeAway))));
            }
        });
    }
}
