package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.diamonddagger590.mccore.command.DisplayNameCommand;
import com.diamonddagger590.mccore.command.LoreCommand;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.HomeGUICommand;
import us.eunoians.mcrpg.command.ability.AbilityGuiCommand;
import us.eunoians.mcrpg.command.admin.DebugCommand;
import us.eunoians.mcrpg.command.admin.ReloadPluginCommand;
import us.eunoians.mcrpg.command.admin.reset.ResetPlayerCommand;
import us.eunoians.mcrpg.command.admin.reset.ResetSkillCommand;
import us.eunoians.mcrpg.command.give.GiveExperienceCommand;
import us.eunoians.mcrpg.command.give.GiveLevelsCommand;
import us.eunoians.mcrpg.command.link.LinkChestCommand;
import us.eunoians.mcrpg.command.link.UnlinkChestCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutEditCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutSetCommand;
import us.eunoians.mcrpg.command.quest.TestQuestStartCommand;
import us.eunoians.mcrpg.command.setting.SettingGuiCommand;
import us.eunoians.mcrpg.command.skill.SkillGuiCommand;

/**
 * This registrar is in charge of registering commands for McRPG.
 */
final class McRPGCommandRegistrar implements Registrar<McRPG> {

    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
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

        // Reset commands
        ResetSkillCommand.registerCommand();
        ResetPlayerCommand.registerCommand();

        // Debug Command
        DebugCommand.registerCommand();

        // Quest Command
        TestQuestStartCommand.registerCommand();

        // Reload command
        ReloadPluginCommand.registerCommand();

        // Link commands
        LinkChestCommand.registerCommand();
        UnlinkChestCommand.registerCommand();

        // Test commands
        LoreCommand.registerCommand();
        DisplayNameCommand.registerCommand();
    }
}
