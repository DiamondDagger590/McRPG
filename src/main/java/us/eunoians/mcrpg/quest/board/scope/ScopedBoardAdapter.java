package us.eunoians.mcrpg.quest.board.scope;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Defines the board-specific operations that a scoped group plugin must provide.
 * <p>
 * Each adapter is keyed by a {@link us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider}
 * key (e.g., {@code mcrpg:land_scope}). The {@link us.eunoians.mcrpg.quest.board.QuestBoardManager}
 * uses this interface exclusively and never references Lands, Factions, or any specific
 * plugin directly.
 * <p>
 * A third-party plugin registering a {@code QuestScopeProvider} for its group system
 * simply also registers a {@code ScopedBoardAdapter} to get full board integration.
 */
public interface ScopedBoardAdapter extends McRPGContent {

    /**
     * The scope provider key this adapter handles (e.g., {@code mcrpg:land_scope}).
     * Must match a registered {@link us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider} key.
     *
     * @return the scope provider key
     */
    @NotNull
    NamespacedKey getScopeProviderKey();

    /**
     * Returns identifiers for all active entities of this scope type (e.g., all
     * active land names). Used during rotation to generate scoped offerings.
     *
     * @return set of entity identifiers
     */
    @NotNull
    Set<String> getAllActiveEntities();

    /**
     * Returns the entities the player is a member of (e.g., lands they are
     * trusted in). Used to determine which scoped offerings to show on the board.
     *
     * @param playerUUID the player
     * @return set of entity identifiers
     */
    @NotNull
    Set<String> getMemberEntities(@NotNull UUID playerUUID);

    /**
     * Returns the entities the player has management permissions for (e.g.,
     * lands where they are owner/admin). Used to determine which scoped
     * quests the player can accept or abandon.
     *
     * @param playerUUID the player
     * @return set of entity identifiers
     */
    @NotNull
    Set<String> getManageableEntities(@NotNull UUID playerUUID);

    /**
     * Checks whether a specific player can manage (accept/abandon) scoped
     * quests for a specific entity.
     *
     * @param playerUUID the player
     * @param entityId   the scope entity identifier
     * @return true if the player can manage quests for this entity
     */
    boolean canManageQuests(@NotNull UUID playerUUID, @NotNull String entityId);

    /**
     * Returns a human-readable display name for the entity (e.g., "Kingdom of Elara").
     * Used in the GUI and localization.
     *
     * @param entityId the scope entity identifier
     * @return the display name, or empty if the entity is no longer valid
     */
    @NotNull
    Optional<String> getEntityDisplayName(@NotNull String entityId);
}
