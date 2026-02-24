package us.eunoians.mcrpg.task.quest;

import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.quest.QuestInstanceDAO;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Periodic background task that saves all dirty active quests to the database.
 * Runs on a configurable interval (default 120 seconds) and uses a {@link BatchTransaction}
 * to persist all pending changes in a single database round-trip.
 */
public final class QuestSaveTask extends CancelableCoreTask {

    public QuestSaveTask(@NotNull McRPG plugin, double taskDelay, double taskFrequency) {
        super(plugin, taskDelay, taskFrequency);
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected void onDelayComplete() {
    }

    @Override
    protected void onIntervalStart() {
    }

    @Override
    protected void onIntervalComplete() {
        List<QuestInstance> dirtyQuests = getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST).getActiveQuests().stream()
                .filter(QuestInstance::isDirty)
                .toList();

        if (dirtyQuests.isEmpty()) {
            return;
        }

        try (Connection connection = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase().getConnection()) {
            BatchTransaction batchTransaction = new BatchTransaction(connection);
            for (QuestInstance quest : dirtyQuests) {
                batchTransaction.addAll(QuestInstanceDAO.saveFullQuestTree(connection, quest));
            }
            batchTransaction.executeTransaction();
            dirtyQuests.forEach(QuestInstance::clearDirty);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onIntervalPause() {
    }

    @Override
    protected void onIntervalResume() {
    }
}
