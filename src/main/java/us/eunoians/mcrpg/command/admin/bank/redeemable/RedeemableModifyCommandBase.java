package us.eunoians.mcrpg.command.admin.bank.redeemable;

import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.admin.bank.AdminBankCommandBase;

/**
 * A base command for all {@code /mcrpg admin bank} commands.
 */
public abstract class RedeemableModifyCommandBase extends AdminBankCommandBase {

    protected static final Permission REDEEMABLE_BANK_ROOT_PERMISSION = Permission.of("mcrpg.admin.exp-bank.redeemable.*");
    protected static final Permission REDEEMABLE_BANK_GIVE_ROOT_PERMISSION = Permission.of("mcrpg.admin.exp-bank.redeemable.give.*");
    protected static final Permission REDEEMABLE_BANK_REMOVE_ROOT_PERMISSION = Permission.of("mcrpg.admin.exp-bank.redeemable.remove.*");
    protected static final Permission REDEEMABLE_BANK_RESET_ROOT_PERMISSION = Permission.of("mcrpg.admin.exp-bank.redeemable.reset.*");

}
