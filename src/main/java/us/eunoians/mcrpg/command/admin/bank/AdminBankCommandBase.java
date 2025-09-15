package us.eunoians.mcrpg.command.admin.bank;

import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.McRPGCommandBase;

/**
 * A base command for experience bank commands.
 */
public abstract class AdminBankCommandBase extends McRPGCommandBase {

    protected static final Permission BANK_MODIFY_COMMAND_ROOT_PERMISSION = Permission.of("mcrpg.admin.exp-bank.*");

}
