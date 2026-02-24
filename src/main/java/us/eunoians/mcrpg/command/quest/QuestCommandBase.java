package us.eunoians.mcrpg.command.quest;

import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.McRPGCommandBase;

/**
 * Base class for all quest commands, providing shared permission constants.
 */
public abstract class QuestCommandBase extends McRPGCommandBase {

    protected static final Permission QUEST_BASE_PERMISSION = Permission.of("mcrpg.quest.*");
    protected static final Permission QUEST_ADMIN_PERMISSION = Permission.of("mcrpg.quest.admin.*");
}
