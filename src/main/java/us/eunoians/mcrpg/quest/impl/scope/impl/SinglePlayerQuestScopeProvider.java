package us.eunoians.mcrpg.quest.impl.scope.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.database.table.quest.scope.SinglePlayerQuestScopeDAO;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provider for {@link SinglePlayerQuestScope} instances.
 * Creates scopes that track a single player as the sole participant.
 */
public class SinglePlayerQuestScopeProvider extends QuestScopeProvider<SinglePlayerQuestScope> {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "single_player_scope");

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    @NotNull
    @Override
    public SinglePlayerQuestScope createNewScope(@NotNull UUID questUUID) {
        return new SinglePlayerQuestScope(questUUID);
    }

    @NotNull
    @Override
    public List<UUID> resolveActiveQuestUUIDs(@NotNull UUID playerUUID, @NotNull Connection connection) {
        return SinglePlayerQuestScopeDAO.findActiveQuestsForPlayer(connection, playerUUID);
    }

    @NotNull
    @Override
    public CompletableFuture<SinglePlayerQuestScope> loadScope(@NotNull UUID questUUID, @NotNull UUID scopeUUID) {
        CompletableFuture<SinglePlayerQuestScope> future = new CompletableFuture<>();
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                SinglePlayerQuestScope scope = new SinglePlayerQuestScope(questUUID);
                scope.setPlayerInScope(SinglePlayerQuestScopeDAO.getPlayerInScope(connection, questUUID));
                future.complete(scope);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
