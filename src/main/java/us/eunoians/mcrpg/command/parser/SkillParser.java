package us.eunoians.mcrpg.command.parser;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.Optional;

public class SkillParser implements ArgumentParser<CommandSourceStack, Skill>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    public static @NotNull ParserDescriptor<CommandSourceStack, Skill> skillParser() {
        return ParserDescriptor.of(new SkillParser(), Skill.class);
    }

    @Override
    public @NotNull ArgumentParseResult<Skill> parse(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull CommandInput commandInput) {
        String input = commandInput.peekString();
        SkillRegistry skillRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL);
        Optional<McRPGPlayer> playerOptional = commandContext.sender().getSender() instanceof Player player ?
                RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId()) : Optional.empty();
        if (playerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = playerOptional.get();
            for(Skill skill : skillRegistry.getRegisteredSkills()) {
                if (skill.getName(mcRPGPlayer).equalsIgnoreCase(input)) {
                    commandInput.readString();
                    return ArgumentParseResult.success(skill);
                }
            }
        }
        return ArgumentParseResult.failure(new SkillParseException(input, commandContext));
    }

    @Override
    public @NotNull Iterable<String> stringSuggestions(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull CommandInput input) {
        Optional<McRPGPlayer> playerOptional = commandContext.sender().getSender() instanceof Player player ?
                RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId()) : Optional.empty();
        return McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkills().stream()
                .map(skill -> playerOptional.map(skill::getName).orElseGet(skill::getName))
                .map(String::toLowerCase)
                .toList();
    }

    private static class SkillParseException extends ParserException {

        /**
         * Construct a new Skill parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public SkillParseException(final @NotNull String input, final @NotNull CommandContext<?> context) {
            super(
                    SkillParser.class,
                    context,
                    Caption.of("argument.parse.failure.skill"),
                    CaptionVariable.of("input", input)
            );
        }
    }
}
