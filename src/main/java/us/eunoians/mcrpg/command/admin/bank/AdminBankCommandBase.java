package us.eunoians.mcrpg.command.admin.bank;

import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;

/**
 * A base command for experience bank commands.
 */
public abstract class AdminBankCommandBase extends AdminBaseCommand {

    protected static final Permission BANK_MODIFY_COMMAND_ROOT_PERMISSION = Permission.of("mcrpg.admin.exp-bank.*");
    protected static final Permission BANK_GIVE_COMMAND_ROOT_PERMISSION = Permission.of("mcrpg.admin.exp-bank.give.*");
    protected static final Permission BANK_REMOVE_COMMAND_ROOT_PERMISSION = Permission.of("mcrpg.admin.exp-bank.remove.*");
    protected static final Permission BANK_RESET_COMMAND_ROOT_PERMISSION = Permission.of("mcrpg.admin.exp-bank.reset.*");

}
