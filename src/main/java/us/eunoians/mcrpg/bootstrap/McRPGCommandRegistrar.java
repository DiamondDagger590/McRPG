package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.diamonddagger590.mccore.command.CoreCommandManager;
import com.diamonddagger590.mccore.command.DisplayNameCommand;
import com.diamonddagger590.mccore.command.LoreCommand;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.processors.cache.SimpleCache;
import org.incendo.cloud.processors.confirmation.ConfirmationContext;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.HomeGUICommand;
import us.eunoians.mcrpg.command.board.BoardCommand;
import us.eunoians.mcrpg.command.board.ScopedBoardCommand;
import us.eunoians.mcrpg.command.admin.board.BoardAdminCooldownCommand;
import us.eunoians.mcrpg.command.admin.board.BoardAdminOfferingsCommand;
import us.eunoians.mcrpg.command.admin.board.BoardAdminPlayerCommand;
import us.eunoians.mcrpg.command.admin.board.BoardAdminPurgeCommand;
import us.eunoians.mcrpg.command.admin.board.BoardAdminRewardsCommand;
import us.eunoians.mcrpg.command.admin.board.BoardAdminRotateCommand;
import us.eunoians.mcrpg.command.admin.board.BoardAdminScopedCommand;
import us.eunoians.mcrpg.command.ability.AbilityGuiCommand;
import us.eunoians.mcrpg.command.admin.DebugCommand;
import us.eunoians.mcrpg.command.admin.ReloadPluginCommand;
import us.eunoians.mcrpg.command.admin.bank.BoostedExperienceModifyCommand;
import us.eunoians.mcrpg.command.admin.bank.RestedExperienceModifyCommand;
import us.eunoians.mcrpg.command.admin.bank.redeemable.RedeemableExperienceModifyCommand;
import us.eunoians.mcrpg.command.admin.bank.redeemable.RedeemableLevelsModifyCommand;
import us.eunoians.mcrpg.command.admin.reset.ResetPlayerCommand;
import us.eunoians.mcrpg.command.admin.reset.ResetSkillCommand;
import us.eunoians.mcrpg.command.give.GiveExperienceCommand;
import us.eunoians.mcrpg.command.give.GiveLevelsCommand;
import us.eunoians.mcrpg.command.link.LinkChestCommand;
import us.eunoians.mcrpg.command.link.UnlinkChestCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutEditCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutSetCommand;
import us.eunoians.mcrpg.command.quest.QuestCancelCommand;
import us.eunoians.mcrpg.command.quest.QuestInfoCommand;
import us.eunoians.mcrpg.command.quest.QuestListCommand;
import us.eunoians.mcrpg.command.quest.QuestStartCommand;
import us.eunoians.mcrpg.command.quest.admin.QuestAdminCompleteCommand;
import us.eunoians.mcrpg.command.quest.admin.QuestAdminRegistryCommand;
import us.eunoians.mcrpg.command.quest.admin.QuestAdminReloadCommand;
import us.eunoians.mcrpg.command.quest.admin.QuestAdminSetProgressCommand;
import us.eunoians.mcrpg.command.redeem.RedeemExperienceCommand;
import us.eunoians.mcrpg.command.redeem.RedeemLevelsCommand;
import us.eunoians.mcrpg.command.setting.SettingGuiCommand;
import us.eunoians.mcrpg.command.skill.SkillGuiCommand;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Duration;

/**
 * This registrar is in charge of registering commands for McRPG.
 */
final class McRPGCommandRegistrar implements Registrar<McRPG> {

    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        registerConfirmationCommandManager();
        // Home GUI command
        HomeGUICommand.registerCommand();
        // Home Sub Menu GUI Commands
        SkillGuiCommand.registerCommand();
        AbilityGuiCommand.registerCommand();
        SettingGuiCommand.registerCommand();

        LoadoutCommand.registerCommand();
        LoadoutEditCommand.registerCommand();
        LoadoutSetCommand.registerCommand();

        // Give Commands
        GiveLevelsCommand.registerCommand();
        GiveExperienceCommand.registerCommand();

        // Redeem commands
        RedeemLevelsCommand.registerCommand();
        RedeemExperienceCommand.registerCommand();

        // Debug Command
        DebugCommand.registerCommand();

        // Quest Commands
        QuestStartCommand.registerCommand();
        QuestCancelCommand.registerCommand();
        QuestListCommand.registerCommand();
        QuestInfoCommand.registerCommand();
        QuestAdminReloadCommand.registerCommand();
        QuestAdminCompleteCommand.registerCommand();
        QuestAdminSetProgressCommand.registerCommand();
        QuestAdminRegistryCommand.registerCommand();

        // Board Commands
        BoardCommand.registerCommand();
        ScopedBoardCommand.registerCommand();

        // Board Admin Commands
        BoardAdminRotateCommand.registerCommand();
        BoardAdminOfferingsCommand.registerCommand();
        BoardAdminScopedCommand.registerCommand();
        BoardAdminPlayerCommand.registerCommand();
        BoardAdminCooldownCommand.registerCommand();
        BoardAdminRewardsCommand.registerCommand();
        BoardAdminPurgeCommand.registerCommand();

        // Reload command
        ReloadPluginCommand.registerCommand();

        // Reset commands
        ResetSkillCommand.registerCommand();
        ResetPlayerCommand.registerCommand();

        // Experience Bank Modify Commands
        RedeemableExperienceModifyCommand.registerCommand();
        RedeemableLevelsModifyCommand.registerCommand();
        RestedExperienceModifyCommand.registerCommand();
        BoostedExperienceModifyCommand.registerCommand();

        // Link commands
        LinkChestCommand.registerCommand();
        UnlinkChestCommand.registerCommand();

        // Test commands
        LoreCommand.registerCommand();
        DisplayNameCommand.registerCommand();

    }

    /**
     * Register the {@link ConfirmationManager} for the plugin.
     */
    private void registerConfirmationCommandManager() {
        CoreCommandManager coreCommandManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        ConfirmationManager<CommandSourceStack> confirmationManager = ConfirmationManager.confirmationManager(builder ->
                builder.cache(SimpleCache.<Object, ConfirmationContext<CommandSourceStack>>of().keyExtractingView(commandSourceStack -> {
                            if (commandSourceStack.getSender() instanceof Player player) {
                                return player.getUniqueId();
                            } else {
                                return "CONSOLE: " + commandSourceStack.getSender().getName();
                            }
                        }))
                        .noPendingCommandNotifier(sender -> sender.getSender()
                                .sendMessage(localizationManager.getLocalizedMessageAsComponent(sender.getSender(), LocalizationKey.NO_PENDING_CONFIRMATION_COMMANDS)))
                        .confirmationRequiredNotifier((sender, ctx) -> sender.getSender()
                                .sendMessage(localizationManager.getLocalizedMessageAsComponent(sender.getSender(), LocalizationKey.CONFIRMATION_COMMAND_REQUIRED)))
                        .expiration(Duration.ofSeconds(15)));
        CommandManager<CommandSourceStack> commandManager = coreCommandManager.getCommandManager();
        commandManager.registerCommandPostProcessor(confirmationManager.createPostprocessor());
        commandManager.command(commandManager.commandBuilder("mcrpg").literal("confirm").handler(confirmationManager.createExecutionHandler()));
    }
}
