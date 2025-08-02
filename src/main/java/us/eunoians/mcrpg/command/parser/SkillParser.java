package us.eunoians.mcrpg.command.parser;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.Optional;

public class SkillParser<C> implements ArgumentParser<C, Skill>, BlockingSuggestionProvider.Strings<C> {

    public static <C> @NonNull ParserDescriptor<C, Skill> skillParser() {
        return ParserDescriptor.of(new SkillParser<>(), Skill.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Skill> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
        String input = commandInput.peekString();
        SkillRegistry skillRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL);
        NamespacedKey skillKey = new NamespacedKey(McRPG.getInstance(), input);

        if (skillRegistry.registered(skillKey)) {
            commandInput.readString();
            return ArgumentParseResult.success(skillRegistry.getRegisteredSkill(skillKey));
        }
        return ArgumentParseResult.failure(new SkillParseException(input, commandContext));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<C> commandContext, @NonNull CommandInput input) {
        Optional<McRPGPlayer> playerOptional = commandContext.sender() instanceof Player player ?
                RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId()) : Optional.empty();
        return McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkills().stream()
                .map(skill -> playerOptional.isPresent() ? skill.getName(playerOptional.get()) : skill.getName())
                .map(String::toLowerCase)
                .toList();
    }

    private static class SkillParseException extends ParserException {

        private final String input;

        /**
         * Construct a new UUID parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public SkillParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    SkillParser.class,
                    context,
                    Caption.of("argument.parse.failure.skill"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Returns the supplied input.
         *
         * @return string value
         */
        public String input() {
            return this.input;
        }
    }
}
