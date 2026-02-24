package us.eunoians.mcrpg.quest.impl.scope.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.database.table.quest.scope.LandQuestScopeDAO;
import us.eunoians.mcrpg.external.lands.LandsHook;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A quest scope that encompasses all members (trusted players) of a specific
 * Lands plugin land. Membership is resolved dynamically via the Lands API --
 * if a player is added to or removed from the land, they automatically enter
 * or leave the quest scope.
 * <p>
 * The land is identified by its name for human-readable config and resolved
 * at runtime. The name is persisted to SQL.
 */
public class LandQuestScope extends QuestScope {

    public static final NamespacedKey LAND_SCOPE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "land_scope");

    private final UUID questUUID;
    @Nullable
    private String landName;

    /**
     * Creates a new land quest scope.
     *
     * @param questUUID the quest instance UUID
     * @param landName  the name of the land
     */
    public LandQuestScope(@NotNull UUID questUUID, @Nullable String landName) {
        this.questUUID = questUUID;
        this.landName = landName;
    }

    @NotNull
    @Override
    public NamespacedKey getScopeKey() {
        return LAND_SCOPE_KEY;
    }

    @NotNull
    @Override
    public UUID getQuestUUID() {
        return questUUID;
    }

    @NotNull
    @Override
    public Set<UUID> getCurrentPlayersInScope() {
        return resolveLand()
                .map(land -> Set.copyOf(land.getTrustedPlayers()))
                .orElse(Set.of());
    }

    @Override
    public boolean isPlayerInScope(@NotNull UUID playerUUID) {
        return resolveLand()
                .map(land -> land.isTrusted(playerUUID))
                .orElse(false);
    }

    @Override
    public boolean isScopeValid() {
        return landName != null && resolveLand().isPresent();
    }

    /**
     * Gets the name of the land this scope is tied to.
     *
     * @return the land name, or empty if not set
     */
    @NotNull
    public Optional<String> getLandName() {
        return Optional.ofNullable(landName);
    }

    /**
     * Sets the land name for this scope. Can only be set once.
     *
     * @param landName the land name
     */
    public void setLandName(@NotNull String landName) {
        if (this.landName != null) {
            throw new IllegalStateException("Land name already set for scope on quest " + questUUID);
        }
        this.landName = landName;
    }

    @NotNull
    @Override
    public List<PreparedStatement> saveScope(@NotNull Connection connection) {
        if (landName == null) {
            throw new IllegalStateException("Cannot save land scope without a land name for quest " + questUUID);
        }
        return LandQuestScopeDAO.saveScope(connection, this);
    }

    @NotNull
    @Override
    public CompletableFuture<Void> loadScope() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                this.landName = LandQuestScopeDAO.getLandName(connection, questUUID);
                future.complete(null);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Resolves the {@link Land} object from the Lands API using the stored land name.
     *
     * @return the land, or empty if the Lands plugin is not available or the land does not exist
     */
    @NotNull
    private Optional<Land> resolveLand() {
        if (landName == null) {
            return Optional.empty();
        }
        return RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                .pluginHook(McRPGPluginHookKey.LANDS)
                .map(hook -> ((LandsHook) hook).getLandsIntegration().getLandByName(landName));
    }
}
