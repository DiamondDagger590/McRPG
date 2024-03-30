package us.eunoians.mcrpg.command;

import org.incendo.cloud.permission.Permission;

public abstract class McRPGCommandBase {

    protected static final Permission ROOT_PERMISSION = Permission.of("mcrpg.*");
}
