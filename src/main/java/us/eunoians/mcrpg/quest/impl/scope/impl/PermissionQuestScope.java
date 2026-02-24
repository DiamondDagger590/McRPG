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
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.database.table.quest.scope.PermissionQuestScopeDAO;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A quest scope that encompasses all players with a specific permission node.
 * Membership is fully dynamic -- checked live via the Bukkit permission API.
 * <p>
 * For online players, uses {@link Player#hasPermission(String)}. For offline
 * players, gracefully degrades: if Vault is present, uses
 * {@link Permission#playerHas(String, OfflinePlayer, String)} which delegates
 * to the server's permission plugin (LuckPerms, CMI, etc.). If Vault is not
 * present, offline players return {@code false} from scope checks (they'll
 * receive queued rewards via the pending reward system instead).
 */
public class PermissionQuestScope extends QuestScope {

    public static final NamespacedKey PERMISSION_SCOPE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "permission_scope");

    private final UUID questUUID;
    @Nullable
    private String permissionNode;

    /**
     * Creates a new permission quest scope.
     *
     * @param questUUID      the quest instance UUID
     * @param permissionNode the permission node to check
     */
    public PermissionQuestScope(@NotNull UUID questUUID, @Nullable String permissionNode) {
        this.questUUID = questUUID;
        this.permissionNode = permissionNode;
    }

    @NotNull
    @Override
    public NamespacedKey getScopeKey() {
        return PERMISSION_SCOPE_KEY;
    }

    @NotNull
    @Override
    public UUID getQuestUUID() {
        return questUUID;
    }

    @NotNull
    @Override
    public Set<UUID> getCurrentPlayersInScope() {
        if (permissionNode == null) {
            return Set.of();
        }
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission(permissionNode))
                .map(Player::getUniqueId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isPlayerInScope(@NotNull UUID playerUUID) {
        if (permissionNode == null) {
            return false;
        }
        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null) {
            return onlinePlayer.hasPermission(permissionNode);
        }
        return checkOfflinePermission(playerUUID);
    }

    @Override
    public boolean isScopeValid() {
        return permissionNode != null;
    }

    /**
     * Gets the permission node this scope checks against.
     *
     * @return the permission node, or empty if not set
     */
    @NotNull
    public Optional<String> getPermissionNode() {
        return Optional.ofNullable(permissionNode);
    }

    /**
     * Sets the permission node for this scope. Can only be set once.
     *
     * @param permissionNode the permission node
     */
    public void setPermissionNode(@NotNull String permissionNode) {
        if (this.permissionNode != null) {
            throw new IllegalStateException("Permission node already set for scope on quest " + questUUID);
        }
        this.permissionNode = permissionNode;
    }

    @NotNull
    @Override
    public List<PreparedStatement> saveScope(@NotNull Connection connection) {
        if (permissionNode == null) {
            throw new IllegalStateException("Cannot save permission scope without a permission node for quest " + questUUID);
        }
        return PermissionQuestScopeDAO.saveScope(connection, this);
    }

    @NotNull
    @Override
    public CompletableFuture<Void> loadScope() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                this.permissionNode = PermissionQuestScopeDAO.getPermissionNode(connection, questUUID);
                future.complete(null);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Attempts to check permission for an offline player using Vault.
     * Returns {@code false} if Vault is not available.
     *
     * @param playerUUID the UUID of the offline player
     * @return {@code true} if Vault confirms the player has the permission
     */
    private boolean checkOfflinePermission(@NotNull UUID playerUUID) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        return rsp.getProvider().playerHas(null, offlinePlayer, permissionNode);
    }
}
