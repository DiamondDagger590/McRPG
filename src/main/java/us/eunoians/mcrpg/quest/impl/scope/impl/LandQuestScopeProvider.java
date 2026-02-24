package us.eunoians.mcrpg.quest.impl.scope.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.database.table.quest.scope.LandQuestScopeDAO;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.external.lands.LandsHook;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provider for {@link LandQuestScope} instances.
 * Creates scopes that encompass all members of a specific Lands plugin land.
 * <p>
 * The land name must be provided via {@link #withLandName(String)} before calling
 * {@link #createNewScope(UUID)}.
 */
public class LandQuestScopeProvider extends QuestScopeProvider<LandQuestScope> {

    private String landName;

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return LandQuestScope.LAND_SCOPE_KEY;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    /**
     * Configures the land name to use when creating new scopes.
     *
     * @param landName the name of the Lands land
     * @return this provider for chaining
     */
    @NotNull
    public LandQuestScopeProvider withLandName(@NotNull String landName) {
        this.landName = landName;
        return this;
    }

    @NotNull
    @Override
    public LandQuestScope createNewScope(@NotNull UUID questUUID) {
        if (landName == null) {
            throw new IllegalStateException("Land name must be set via withLandName() before creating a scope");
        }
        return new LandQuestScope(questUUID, landName);
    }

    @NotNull
    @Override
    public List<UUID> resolveActiveQuestUUIDs(@NotNull UUID playerUUID, @NotNull Connection connection) {
        Map<UUID, String> activeLandQuests = LandQuestScopeDAO.findAllActiveLandQuests(connection);
        if (activeLandQuests.isEmpty()) {
            return List.of();
        }

        Optional<LandsHook> landsHook = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                .pluginHook(McRPGPluginHookKey.LANDS)
                .map(hook -> (LandsHook) hook);

        if (landsHook.isEmpty()) {
            return List.of();
        }

        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : activeLandQuests.entrySet()) {
            Land land = landsHook.get().getLandsIntegration().getLandByName(entry.getValue());
            if (land != null && land.isTrusted(playerUUID)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    @NotNull
    @Override
    public CompletableFuture<LandQuestScope> loadScope(@NotNull UUID questUUID, @NotNull UUID scopeUUID) {
        CompletableFuture<LandQuestScope> future = new CompletableFuture<>();
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                String storedLandName = LandQuestScopeDAO.getLandName(connection, questUUID);
                future.complete(new LandQuestScope(questUUID, storedLandName));
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
