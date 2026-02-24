package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;

/**
 * The output of {@link QuestTemplateEngine#generate}: a fully materialized quest definition,
 * the originating template key, and a JSON-serialized snapshot of the definition for database
 * persistence.
 *
 * @param definition           the fully materialized quest definition ready for
 *                             {@code QuestManager.startQuest()}
 * @param templateKey          which template produced this definition (for traceability and display)
 * @param serializedDefinition JSON snapshot produced by {@link GeneratedQuestDefinitionSerializer#serialize}.
 *                             Persisted in the {@code generated_definition} column of
 *                             {@code mcrpg_board_offering} so the definition can be deserialized
 *                             on future board loads without re-running the template engine.
 */
public record GeneratedQuestResult(
        @NotNull QuestDefinition definition,
        @NotNull NamespacedKey templateKey,
        @NotNull String serializedDefinition
) {}
