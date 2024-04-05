package us.eunoians.mcrpg.command.admin;

import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.McRPGCommandBase;

/**
 * Contains the base permission for all admin commands
 */
public class AdminBaseCommand extends McRPGCommandBase {

    protected static final Permission ADMIN_BASE_PERMISSION = Permission.of("mcrpg.admin.*");
}
