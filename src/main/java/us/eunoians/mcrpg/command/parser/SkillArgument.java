//package us.eunoians.mcrpg.command.parser;
//
//import cloud.commandframework.ArgumentDescription;
//import cloud.commandframework.CommandManager;
//import cloud.commandframework.arguments.CommandArgument;
//import cloud.commandframework.arguments.parser.ArgumentParseResult;
//import cloud.commandframework.arguments.parser.ArgumentParser;
//import cloud.commandframework.arguments.parser.ParserRegistry;
//import cloud.commandframework.arguments.standard.StringArgument;
//import cloud.commandframework.arguments.standard.UUIDArgument;
//import cloud.commandframework.captions.Caption;
//import cloud.commandframework.captions.CaptionRegistry;
//import cloud.commandframework.captions.CaptionVariable;
//import cloud.commandframework.captions.FactoryDelegatingCaptionRegistry;
//import cloud.commandframework.context.CommandContext;
//import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
//import cloud.commandframework.exceptions.parsing.ParserException;
//import io.leangen.geantyref.TypeToken;
//import org.bukkit.NamespacedKey;
//import org.bukkit.command.CommandSender;
//import org.checkerframework.checker.nullness.qual.NonNull;
//import org.checkerframework.checker.nullness.qual.Nullable;
//import us.eunoians.mcrpg.McRPG;
//import us.eunoians.mcrpg.exception.skill.SkillNotRegisteredException;
//import us.eunoians.mcrpg.skill.Skill;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Queue;
//import java.util.UUID;
//import java.util.function.BiFunction;
//
//public class SkillArgument<C> extends CommandArgument<C, Skill> {
//
//
//    private static final Caption CAPTION = Caption.of("mcrpg.argument.parse.failure.skill");
//
//    private SkillArgument(boolean required, @NonNull String name, @NonNull String defaultValue, @Nullable BiFunction<@NonNull CommandContext<C>,
//            @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider, @NonNull ArgumentDescription defaultDescription) {
//        super(required, name, new SkillParser<>(), defaultValue, Skill.class, suggestionsProvider, defaultDescription);
//    }
//
//    public static void registerCaption() {
//        CommandManager<CommandSender> commandManager = McRPG.getInstance().getBukkitCommandManager();
//        CaptionRegistry<CommandSender> captionRegistry = commandManager.captionRegistry();
//        if (captionRegistry instanceof FactoryDelegatingCaptionRegistry<CommandSender> factoryRegistry) {
//            factoryRegistry.registerMessageFactory(
//                    CAPTION,
//                    (context, key) -> "'{input}' is not a valid skill."
//            );
//        }
//
//        ParserRegistry<CommandSender> parserRegistry = commandManager.parserRegistry();
//        parserRegistry.registerSuggestionProvider("skill_parser", ((commandContext, s) -> {
//            List<String> skills = new ArrayList<>();
//            for (Skill skill : McRPG.getInstance().getSkillRegistry().getRegisteredSkills()) {
//                skills.add(skill.getDisplayName().toLowerCase());
//            }
//            return skills;
//        }));
//
//    }
//
//    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
//        return new Builder<>(name);
//    }
//
//    public static <C> @NonNull CommandArgument<C, Skill> of(final @NonNull String name) {
//        return newBuilder(name).asRequired().build();
//    }
//
//    public static <C> @NonNull CommandArgument<C, Skill> optional(final @NonNull String name) {
//        return newBuilder(name).asOptional().build();
//    }
//
//    public static <C> @NonNull CommandArgument<C, Skill> optional(@NonNull String name, @NonNull Skill skill) {
//        return newBuilder(name).asOptionalWithDefault(skill.getDisplayName()).build();
//    }
//
//    public static final class Builder<C> extends CommandArgument.Builder<C, Skill> {
//
//        private Builder(final @NonNull String name) {
//            super(Skill.class, name);
//        }
//
//        /**
//         * Builder a new example component
//         *
//         * @return Constructed component
//         */
//        @Override
//        public @NonNull SkillArgument<C> build() {
//            return new SkillArgument<>(
//                    this.isRequired(),
//                    this.getName(),
//                    this.getDefaultValue(),
//                    this.getSuggestionsProvider(),
//                    this.getDefaultDescription()
//            );
//        }
//    }
//
//
//    public static class SkillParser<C> implements ArgumentParser<C, Skill> {
//
//        @Override
//        public @NonNull ArgumentParseResult<@NonNull Skill> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
//            final String input = inputQueue.peek();
//
//            if (input == null) {
//                return ArgumentParseResult.failure(new NoInputProvidedException(
//                        SkillParser.class,
//                        commandContext
//                ));
//            }
//
//            try {
//                Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(new NamespacedKey(McRPG.getInstance(), input));
//                inputQueue.remove();
//                return ArgumentParseResult.success(skill);
//            } catch (IllegalArgumentException | SkillNotRegisteredException e) {
//                return ArgumentParseResult.failure(new SkillParseException(input, commandContext));
//            }
//        }
//
//        @Override
//        public boolean isContextFree() {
//            return true;
//        }
//
//        public static final class SkillParseException extends ParserException {
//
//            private final String input;
//
//            public SkillParseException(
//                    final @NonNull String input,
//                    final @NonNull CommandContext<?> context
//            ) {
//                super(
//                        SkillParser.class,
//                        context,
//                        CAPTION,
//                        CaptionVariable.of("input", input)
//                );
//                this.input = input;
//            }
//        }
//
//    }
//}
