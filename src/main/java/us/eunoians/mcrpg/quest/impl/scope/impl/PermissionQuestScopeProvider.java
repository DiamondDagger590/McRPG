package us.eunoians.mcrpg.quest.impl.scope.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.database.table.quest.scope.PermissionQuestScopeDAO;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provider for {@link PermissionQuestScope} instances.
 * Creates scopes that encompass all players with a specific permission node.
 * <p>
 * The permission node must be provided via {@link #withPermissionNode(String)}
 * before calling {@link #createNewScope(UUID)}.
 */
public class PermissionQuestScopeProvider extends QuestScopeProvider<PermissionQuestScope> {

    private String permissionNode;

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return PermissionQuestScope.PERMISSION_SCOPE_KEY;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    /**
     * Configures the permission node to use when creating new scopes.
     *
     * @param permissionNode the permission node string
     * @return this provider for chaining
     */
    @NotNull
    public PermissionQuestScopeProvider withPermissionNode(@NotNull String permissionNode) {
        this.permissionNode = permissionNode;
        return this;
    }

    @NotNull
    @Override
    public PermissionQuestScope createNewScope(@NotNull UUID questUUID) {
        if (permissionNode == null) {
            throw new IllegalStateException("Permission node must be set via withPermissionNode() before creating a scope");
        }
        return new PermissionQuestScope(questUUID, permissionNode);
    }

    @NotNull
    @Override
    public List<UUID> resolveActiveQuestUUIDs(@NotNull UUID playerUUID, @NotNull Connection connection) {
        Map<UUID, String> activePermQuests = PermissionQuestScopeDAO.findAllActivePermissionQuests(connection);
        if (activePermQuests.isEmpty()) {
            return List.of();
        }

        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        List<UUID> result = new ArrayList<>();

        for (Map.Entry<UUID, String> entry : activePermQuests.entrySet()) {
            String node = entry.getValue();
            boolean hasPermission;
            if (onlinePlayer != null) {
                hasPermission = onlinePlayer.hasPermission(node);
            } else {
                hasPermission = checkOfflinePermission(playerUUID, node);
            }
            if (hasPermission) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Checks offline permission using Vault. Returns {@code false} if Vault is unavailable.
     */
    private boolean checkOfflinePermission(@NotNull UUID playerUUID, @NotNull String node) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        return rsp.getProvider().playerHas(null, offlinePlayer, node);
    }

    @NotNull
    @Override
    public CompletableFuture<PermissionQuestScope> loadScope(@NotNull UUID questUUID, @NotNull UUID scopeUUID) {
        CompletableFuture<PermissionQuestScope> future = new CompletableFuture<>();
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                String storedNode = PermissionQuestScopeDAO.getPermissionNode(connection, questUUID);
                future.complete(new PermissionQuestScope(questUUID, storedNode));
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
