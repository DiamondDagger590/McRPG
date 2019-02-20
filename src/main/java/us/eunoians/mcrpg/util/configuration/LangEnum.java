package us.eunoians.mcrpg.util.configuration;

import de.articdive.enum_to_yaml.interfaces.ConfigurationEnum;

import java.util.Arrays;

public enum LangEnum implements ConfigurationEnum {

    PLUGIN_INFO_HEADER("Messages.PluginInfo", "", "#Messages relating to the general plugin information"),
    PLUGIN_PREFIX("Messages.PluginInfo.PluginPrefix", "&7[&1McRPG&7]"),
    COMMANDS_HEADER("Messages.Commands", "", "#Messages used by commands"),
    COMMANDS_UTILITY_HEADER("Messages.Commands.Utility", "", "#Messages used by misc commands"),
    NO_PERMS("Messages.Commands.Utility.NoPerms", "&cYou do not have the permissions to execute that command."),
    PLAYER_HAS_NOT_LOGGED_IN("Messages.Commands.Utility.PlayerHasNotLoggedIn", "&cThat player has not logged in before."),
    INVALID_AMOUNT("Messages.Commands.Utility.InvalidAmount", "&cThe amount you entered is invalid. Please try again."),
    ONLY_PLAYERS("Messages.Commands.Utility.OnlyPlayers", "&cOnly players can run this command."),
    HELP_PROMPT("Messages.Commands.Utility.HelpPrompt", "&eUse /mchelp or /mchelp <command> for proper usage."),
    NOT_AN_INT("Messages.Commands.Utility.NotAnInt", "&cThe argument you entered is not an integer."),
    NOT_A_LONG("Messages.Commands.Utility.NotALong", "&cThe argument you entered is not a long."),
    NOT_A_SKILL("Messages.Commands.Utility.NotASkill", "&cThe argument you entered is not a valid skill."),
    NOT_AN_ABILITY("Messages.Commands.Utility.NotAnAbility", "&cThe argument you entered is not a valid ability."),
    NOT_ACTIVE_ABILITY("Messages.Commands.Utility.NotActiveAbility", "&cThe ability you entered is not an active ability."),
    NOT_ENABLED_OR_UNLOCKED("Messages.Commands.Utility.NotEnabledOrUnlocked", "&cThat ability is either disabled or you have yet to unlock it."),
    RELOADED_FILES("Messages.Commands.ReloadFiles", "&aYou have successfully reloaded all files for this plugin."),
    MCHELP_DEFAULT("Messages.Commands.McHelp.Default", Arrays.asList("&e--------------------------", "&7[&6McRPG Command &7]&3 /mcrpg",
            "&3    -Opens main McRPG gui", "&7[&6McDisplay Command &7]&3 /mchelp mcdisplay", "&3    -Help prompt for mcdisplay.",
            "&7[&6McAdmin Command &7]&3 /mchelp mcadmin", "&3    -Help prompt for mcadmin.", "&e--------------------------")),
    MCHELP_MCDISPLAY("Messages.Commands.McHelp.McDisplay", Arrays.asList("&e--------------------------", "&7[&6McDisplay Command &7]&3 /mcdisplay {Skill}",
            "&3    -Opens your display for the skill.", "&7[&6McDisplay Command &7]&3 /mcdisplay clear", "&3    -Clears display.",
            "&e--------------------------")),
    MCHELP_MCADMIN1("Messages.Commands.McHelp.McAdmin1", Arrays.asList("&e--------------------------",
            "&7[&6AbilityPoints Give Command &7]&3 /mcadmin give abilitypoints {Player} {Amount}", "&3    -Opens your display for the skill.",
            "&7[&6Exp Give Command &7]&3 /mcadmin give exp {Player} {Amount} {Skill}", "&3    -Give a player exp in a skill.",
            "&7[&6Level Give Command &7]&3 /mcadmin give level {Player} {Amount} {Skill}", "&3    -Give a player levels in a skill.",
            "&7[&6Ability Give Command &7]&3 /mcadmin give ability {Player} {Ability}", "&3    -Give a player an ability.",
            "&7[&6Ability Replace Command &7]&3 /mcadmin replace {Player} {Ability} {Ability}", "&3    -Replace an ability with another.",
            "&eDo /mchelp mcadmin 2 for more.", "&e--------------------------")),
    MCHELP_MCADMIN2("Messages.Commands.McHelp.McAdmin2", Arrays.asList("&e--------------------------",
            "&7[&6Ability Remove Command &7]&3 /mcadmin remove {Player} {Ability}", "&3    -Removes an ability from a player.",
            "&7[&6View Loadout Command &7]&3 /mcadmin view loadout {Player}", "&3    -Views a players ability loadout.",
            "&7[&6View Skill Command &7]&3 /mcadmin view {Skill} {Player}", "&3    -View information about a players skill.",
            "&7[&6Cooldown Set Command &7]&3 /mcadmin cooldown set {Player} {Ability} {Duration}", "&3    -Sets a players cooldown for an ability.",
            "&7[&6Cooldown Remove Command &7]&3 /mcadmin cooldown remove {Player} {Ability}", "&3    -Removes a players cooldown for an ability.",
            "&eDo /mchelp mcadmin 3 for more.", "&e--------------------------")),
    MCHELP_MCADMIN3("Messages.Commands.McHelp.McAdmin3", Arrays.asList("&e--------------------------",
            "&7[&6Cooldown Add Command &7]&3 /mcadmin cooldown add {Player} {Ability} {Duration}", "&3    -Adds time to a players cooldown for an ability.",
            "&7[&6Reset Skill Command &7]&3 /mcadmin reset skill {Player} {Skill}", "&3    -Resets a players skill.",
            "&7[&6Reset Ability Command &7]&3 /mcadmin reset ability {Player} {Ability}", "&3    -Resets a players ability",
            "&7[&6Reset Player Command &7]&3 /mcadmin reset player {Player}", "&3    -Resets a player.", "&e--------------------------")),
    MCDISPLAY_INVALID_INPUT("Messages.Commands.McDisplay.InvalidInput", "&c%String% is not a skill or display type.", "#%String% is the failed input"),
    MCDISPLAY_NOT_A_TYPE("Messages.Commands.McDisplay.NotAType", "&c%String% is not a proper display type. Use bossbar, actionbar or scoreboard."),
    MCDISPLAY_NOTHING_TO_CLEAR("Messages.Commands.McDisplay.NothingToClear", "&cThere is nothing to remove ");

    private String path;
    private Object defaultValue;
    private String[] comments;


    LangEnum(String path, Object defaultValue, String... comments) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.comments = comments;
    }


    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String[] getComments() {
        return comments;
    }
}
