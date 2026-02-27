package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Evaluates to {@code true} if the player in the context has the specified Bukkit
 * permission node. Returns {@code false} when no player UUID is available (safe default).
 */
public final class PermissionCondition implements TemplateCondition {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "permission_check");

    private final String permission;

    public PermissionCondition(@NotNull String permission) {
        if (permission.isBlank()) {
            throw new IllegalArgumentException("Permission string must not be blank");
        }
        this.permission = permission;
    }

    @Override
    public boolean evaluate(@NotNull ConditionContext context) {
        if (context.playerUUID() == null) {
            return false;
        }
        Player player = Bukkit.getPlayer(context.playerUUID());
        if (player == null) {
            return false;
        }
        return player.hasPermission(permission);
    }

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
    public TemplateCondition fromConfig(@NotNull Section section) {
        String perm = section.getString("permission");
        if (perm == null || perm.isBlank()) {
            throw new IllegalArgumentException("Missing 'permission' in permission_check condition");
        }
        return new PermissionCondition(perm);
    }

    @NotNull
    public String getPermission() {
        return permission;
    }
}
