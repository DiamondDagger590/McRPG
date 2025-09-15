package us.eunoians.mcrpg.command.give.redeemable;

import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.give.GiveCommandBase;

/**
 * A base command for all {@code /mcrpg give redeem} commands.
 */
public abstract class GiveRedeemableCommandBase extends GiveCommandBase {

    protected static final Permission GIVE_REDEEM_COMMAND_ROOT_PERMISSION = Permission.of("mcrpg.give.redeemable.*");

}
