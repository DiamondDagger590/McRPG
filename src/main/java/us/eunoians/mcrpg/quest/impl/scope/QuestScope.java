package us.eunoians.mcrpg.quest.impl.scope;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class QuestScope {

    @NotNull
    public abstract NamespacedKey getScopeKey();

    @NotNull
    public abstract UUID getQuestUUID();

    @NotNull
    public abstract Set<UUID> getCurrentPlayersInScope();

    public abstract boolean isPlayerInScope(@NotNull UUID playerUUID);

    public abstract boolean isScopeValid();

    @NotNull
    public abstract List<PreparedStatement> saveScope(@NotNull Connection connection);

    @NotNull
    public abstract CompletableFuture<Void> loadScope();
}
