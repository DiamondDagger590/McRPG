package us.eunoians.mcrpg.command.admin.reset;

import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;

/**
 * Contains the base permission for all reset commands
 */
public class ResetBaseCommand extends AdminBaseCommand {

    protected static final Permission RESET_COMMAND_BASE_PERMISSION = Permission.of("mcrpg.admin.reset.*");
}
