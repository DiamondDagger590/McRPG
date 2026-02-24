package us.eunoians.mcrpg.quest.impl.scope.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import us.eunoians.mcrpg.database.table.quest.scope.SinglePlayerQuestScopeDAO;
import us.eunoians.mcrpg.exception.quest.QuestScopeInvalidStateException;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SinglePlayerQuestScope extends QuestScope {

    private static final NamespacedKey SINGLE_PLAYER_SCOPE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "single_player_scope");

    private final UUID questUUID;
    @Nullable
    private UUID playerInScope;

    public SinglePlayerQuestScope(@NotNull UUID questUUID) {
        this.questUUID = questUUID;
    }

    @NotNull
    @Override
    public NamespacedKey getScopeKey() {
        return SINGLE_PLAYER_SCOPE_KEY;
    }

    @NotNull
    @Override
    public UUID getQuestUUID() {
        return questUUID;
    }

    @Override
    public @NonNull Set<UUID> getCurrentPlayersInScope() {
        return playerInScope != null ? Set.of(playerInScope) : Set.of();
    }

    @Override
    public boolean isPlayerInScope(@NotNull UUID playerUUID) {
        return playerInScope != null && playerInScope.equals(playerUUID);
    }

    @Override
    public boolean isScopeValid() {
        return playerInScope != null;
    }

    @NotNull
    public Optional<UUID> getPlayerInScope() {
        return Optional.ofNullable(playerInScope);
    }

    public void setPlayerInScope(@NotNull UUID playerUUID) {
        if (isScopeValid()) {
            throw new QuestScopeInvalidStateException(this, String.format("Attempting to add player %s to scope but scope " +
                    "already has a player with UUID %s", playerUUID, playerInScope));
        }
        this.playerInScope = playerUUID;
    }

    @NotNull
    @Override
    public List<PreparedStatement> saveScope(@NotNull Connection connection) {
        if (!isScopeValid()) {
            throw new QuestScopeInvalidStateException(this, "Attempting to save scope but scope is not valid... expected to have a player UUID");
        }
        return SinglePlayerQuestScopeDAO.saveScope(connection, this);
    }

    @NotNull
    @Override
    public CompletableFuture<Void> loadScope() {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            setPlayerInScope(SinglePlayerQuestScopeDAO.getPlayerInScope(database.getConnection(), questUUID));
            completableFuture.complete(null);
        });
        return completableFuture;
    }
}
