package us.eunoians.mcrpg.command.redeem;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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

import static us.eunoians.mcrpg.command.CommandPlaceholders.REDEEMED_EXPERIENCE;
import static us.eunoians.mcrpg.command.CommandPlaceholders.SKILL;

/**
 * This class manages the /mcrpg redeem experience command
 */
public class RedeemExperienceCommand extends McRPGCommandBase {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                .manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        Command.Builder<CommandSourceStack> redeemBuilder = commandManager.commandBuilder("mcrpg")
                .literal("redeem").commandDescription(RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_REDEEM)))
                .literal("experience", "exp")
                .required("skill", SkillParser.skillParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_REDEEM_SKILL)));
        commandManager.command(redeemBuilder
                .required("amount", IntegerParser.integerParser(), RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_REDEEM_EXPERIENCE_AMOUNT)))
                .handler(commandContext -> {
                            CommandSender commandSender = commandContext.sender().getSender();
                            CloudKey<Skill> skillKey = CloudKey.of("skill", Skill.class);
                            Skill skill = commandContext.get(skillKey);
                            CloudKey<Integer> amountKey = CloudKey.of("amount", Integer.class);
                            int amount = commandContext.get(amountKey);
                            if (commandSender instanceof Player player) {
                                Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                        .manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
                                playerOptional.ifPresent(mcRPGPlayer -> redeemExperience(mcRPGPlayer, skill, amount));
                                // If they aren't present, it typically means their data isn't loaded yet so it's fine to just no-op
                            } else {
                                commandSender.sendMessage(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.NON_PLAYER_COMMAND_ERROR));
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
                        playerOptional.ifPresent(mcRPGPlayer -> redeemExperience(mcRPGPlayer, skill, mcRPGPlayer.getExperienceExtras().getRedeemableExperience()));
                        // If they aren't present, it typically means their data isn't loaded yet so it's fine to just no-op
                    } else {
                        commandSender.sendMessage(localizationManager.getLocalizedMessageAsComponent(LocalizationKey.NON_PLAYER_COMMAND_ERROR));
                    }
                }));
    }

    /**
     * Redeems redeemable experience into the provided {@link Skill}.
     *
     * @param mcRPGPlayer The player to redeem experience for.
     * @param skill       The skill to redeem experience into.
     * @param amount      The amount of experience to redeem.
     */
    public static void redeemExperience(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill, int amount) {
        PlayerExperienceExtras playerExperienceExtras = mcRPGPlayer.getExperienceExtras();
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            if (amount > playerExperienceExtras.getRedeemableExperience()) {
                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_EXPERIENCE_NOT_ENOUGH_EXPERIENCE_MESSAGE));
                return;
            }
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            var skillDataOptional = skillHolder.getSkillHolderData(skill);
            if (skillDataOptional.isPresent()) {
                SkillHolder.SkillHolderData skillData = skillDataOptional.get();
                if (skillData.getCurrentLevel() >= skill.getMaxLevel()) {
                    player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_EXPERIENCE_SKILL_ALREADY_MAXED_MESSAGE, Map.of(SKILL.getPlaceholder(), skill.getName(mcRPGPlayer))));
                    return;
                }
                // Cap redeem amount at XP needed to reach max level — prevents overflow past max
                int expToMaxLevel = skillData.calculateTotalExperienceForLevel(skill.getMaxLevel()) - skillData.getTotalExperience();
                int effectiveAmount = Math.min(amount, Math.max(0, expToMaxLevel));
                skillData.addExperience(effectiveAmount, us.eunoians.mcrpg.skill.experience.context.McRPGGainReason.REDEEM);
                playerExperienceExtras.modifyRedeemableExperience(effectiveAmount * -1);
                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.REDEEMABLE_EXPERIENCE_REDEEMED_EXPERIENCE_MESSAGE, Map.of(SKILL.getPlaceholder(), skill.getName(mcRPGPlayer),
                        REDEEMED_EXPERIENCE.getPlaceholder(), Integer.toString(effectiveAmount))));
            }
        });
    }
}
