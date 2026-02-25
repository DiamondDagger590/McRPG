package us.eunoians.mcrpg.external.lands;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.enums.FlagTarget;
import me.angeschossen.lands.api.flags.enums.RoleFlagCategory;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.role.Role;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.impl.scope.impl.LandQuestScope;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@link ScopedBoardAdapter} implementation for the Lands plugin.
 * <p>
 * Delegates all operations to the Lands API via {@link LandsIntegration}. Registers a
 * custom Lands role flag ({@code manage_board_quests}) so land owners can optionally
 * delegate quest management to other roles.
 * <p>
 * <b>Custom flag and Lands allowlisting:</b>
 * McRPG registers the {@code manage_board_quests} flag programmatically via the Lands API.
 * However, Lands requires the server owner to <b>allowlist</b> custom flags in Lands'
 * configuration before they appear in the in-game role editor. Until the flag is
 * allowlisted, {@code hasRoleFlag()} returns {@code false} for all roles.
 * <p>
 * <b>Default behavior (flag NOT allowlisted):</b>
 * <ul>
 *   <li>Land owner: always permitted (hardcoded UUID bypass)</li>
 *   <li>All other roles: cannot manage board quests</li>
 * </ul>
 */
public final class LandScopedBoardAdapter implements ScopedBoardAdapter {

    private final LandsIntegration landsIntegration;
    private final RoleFlag manageBoardQuestsFlag;

    public LandScopedBoardAdapter(@NotNull LandsIntegration landsIntegration) {
        this.landsIntegration = landsIntegration;
        this.manageBoardQuestsFlag = RoleFlag.of(landsIntegration, FlagTarget.PLAYER, RoleFlagCategory.ACTION, "manage_board_quests");
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    @NotNull
    @Override
    public NamespacedKey getScopeProviderKey() {
        return LandQuestScope.LAND_SCOPE_KEY;
    }

    @NotNull
    @Override
    public Set<String> getAllActiveEntities() {
        return landsIntegration.getLands().stream()
                .map(Land::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    @NotNull
    @Override
    public Set<String> getMemberEntities(@NotNull UUID playerUUID) {
        return landsIntegration.getLands().stream()
                .filter(land -> land.isTrusted(playerUUID))
                .map(Land::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    @NotNull
    @Override
    public Set<String> getManageableEntities(@NotNull UUID playerUUID) {
        return landsIntegration.getLands().stream()
                .filter(land -> canManageQuests(playerUUID, land.getName()))
                .map(Land::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean canManageQuests(@NotNull UUID playerUUID, @NotNull String entityId) {
        Land land = landsIntegration.getLandByName(entityId);
        if (land == null) return false;
        if (!land.isTrusted(playerUUID)) return false;
        if (land.getOwnerUID().equals(playerUUID)) return true;
        Role role = land.getDefaultArea().getRole(playerUUID);
        return role != null && role.hasFlag(manageBoardQuestsFlag);
    }

    @NotNull
    @Override
    public Optional<String> getEntityDisplayName(@NotNull String entityId) {
        Land land = landsIntegration.getLandByName(entityId);
        return Optional.ofNullable(land).map(Land::getName);
    }
}
