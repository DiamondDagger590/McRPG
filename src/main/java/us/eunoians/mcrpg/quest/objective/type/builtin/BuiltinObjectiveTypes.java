package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;

/**
 * Central reference for all built-in {@link us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType} implementations.
 * <p>
 * Call {@link #registerAll(QuestObjectiveTypeRegistry)} during bootstrap to register all built-in types.
 */
public final class BuiltinObjectiveTypes {

    public static final BlockBreakObjectiveType BLOCK_BREAK = new BlockBreakObjectiveType();
    public static final MobKillObjectiveType MOB_KILL = new MobKillObjectiveType();

    private BuiltinObjectiveTypes() {
    }

    /**
     * Registers all built-in objective types with the given registry.
     *
     * @param registry the registry to populate
     */
    public static void registerAll(@NotNull QuestObjectiveTypeRegistry registry) {
        registry.register(BLOCK_BREAK);
        registry.register(MOB_KILL);
    }
}
