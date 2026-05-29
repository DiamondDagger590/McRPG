package us.eunoians.mcrpg.command.admin.chain;

import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;

/**
 * Base class for all quest chain admin commands, providing the shared chain
 * admin permission constant.
 */
public class ChainAdminCommandBase extends AdminBaseCommand {

    protected static final Permission CHAIN_ADMIN_BASE_PERMISSION =
            Permission.of("mcrpg.quest.admin.chain.*");
}
