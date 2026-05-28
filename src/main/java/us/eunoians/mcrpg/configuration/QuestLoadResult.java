package us.eunoians.mcrpg.configuration;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Carries the output of a single {@link QuestConfigLoader#loadQuestsFromDirectory} scan:
 * the parsed quest definitions and the paths of files that were flagged as chain files
 * (i.e., they contained {@code quest-chain-file: true} and were skipped by the quest loader
 * so the chain loader can process them in a second phase).
 *
 * @param definitions map of quest key to parsed definition, in load order
 * @param chainFiles  paths of files carrying {@code quest-chain-file: true}
 */
public record QuestLoadResult(
        @NotNull Map<NamespacedKey, QuestDefinition> definitions,
        @NotNull List<Path> chainFiles
) {
}
