package us.eunoians.mcrpg.command.give;

import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.McRPGCommandBase;

public abstract class GiveCommandBase extends McRPGCommandBase {

    protected static final Permission GIVE_COMMAND_ROOT_PERMISSION = Permission.of("mcrpg.give.*");
}
