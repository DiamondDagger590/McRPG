package us.eunoians.mcrpg.command.admin.content;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;
import us.eunoians.mcrpg.expansion.content.StatisticContent;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionType;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.trigger.ChainAutoStartTrigger;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.stat.PlayerStat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Command: {@code /mcrpg admin content keys <pack-type>}
 * <p>
 * Lists all registered content keys across all expansions for a given content pack type.
 * The pack type is identified by the simple class name with the "ContentPack" suffix stripped
 * (e.g. "Ability", "Skill", "Statistic", "QuestObjectiveType").
 */
public class ContentKeysCommand extends AdminBaseCommand {

    private static final Permission CONTENT_PERMISSION = Permission.of("mcrpg.admin.content");
    private static final CloudKey<String> PACK_TYPE_KEY = CloudKey.of("pack-type", String.class);

    /**
     * Registers the {@code /mcrpg admin content keys <pack-type>} command.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        McRPG plugin = McRPG.getInstance();
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        MiniMessage mm = plugin.getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("content")
                .literal("keys")
                .required(PACK_TYPE_KEY, StringParser.stringParser(),
                        org.incendo.cloud.minecraft.extras.RichDescription.richDescription(
                                mm.deserialize("<gray>Pack type (e.g. Ability, Skill, Statistic)")))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, CONTENT_PERMISSION))
                .handler(ctx -> {
                    Audience sender = ctx.sender().getSender();
                    String packType = ctx.get(PACK_TYPE_KEY);
                    sendKeyList(sender, mm, packType);
                }));
    }

    /**
     * Sends a formatted list of all registered content keys for the given pack type
     * across all expansions.
     *
     * @param sender   The audience to send the list to.
     * @param mm       The {@link MiniMessage} instance for parsing formatted text.
     * @param packType The pack type name (e.g. "Ability", "Skill").
     */
    private static void sendKeyList(@NotNull Audience sender, @NotNull MiniMessage mm,
                                    @NotNull String packType) {
        ContentExpansionManager manager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.CONTENT_EXPANSION);
        Collection<ContentExpansion> expansions = manager.getRegisteredExpansions();

        String targetClassName = packType + "ContentPack";
        List<McRPGContentPack<?>> matchingPacks = new ArrayList<>();

        for (ContentExpansion expansion : expansions) {
            for (McRPGContentPack<?> pack : expansion.getExpansionContent()) {
                if (pack.getClass().getSimpleName().equalsIgnoreCase(targetClassName)) {
                    matchingPacks.add(pack);
                }
            }
        }

        if (matchingPacks.isEmpty()) {
            Set<String> availableTypes = collectAvailablePackTypes(expansions);
            sender.sendMessage(mm.deserialize(
                    "<negative>No content packs found for type: <body>" + packType));
            if (!availableTypes.isEmpty()) {
                sender.sendMessage(mm.deserialize(
                        "<body>Available types: <primary>" + String.join(", ", availableTypes)));
            }
            return;
        }

        Set<String> keys = new TreeSet<>();
        for (McRPGContentPack<?> pack : matchingPacks) {
            for (McRPGContent content : pack.getContent()) {
                keys.add(describeContent(content));
            }
        }

        if (keys.isEmpty()) {
            sender.sendMessage(mm.deserialize(
                    "<primary>" + packType + " <body>packs are registered but contain no entries."));
            return;
        }

        sender.sendMessage(mm.deserialize(
                "<primary>" + packType + " Content Keys <body>(" + keys.size() + "):"));

        for (String key : keys) {
            sender.sendMessage(mm.deserialize(" <primary>- <body>" + key));
        }
    }

    /**
     * Collects all unique pack type names across all registered expansions.
     *
     * @param expansions The registered expansions to scan.
     * @return A sorted set of pack type names (with "ContentPack" suffix stripped).
     */
    @NotNull
    private static Set<String> collectAvailablePackTypes(@NotNull Collection<ContentExpansion> expansions) {
        Set<String> types = new TreeSet<>();
        for (ContentExpansion expansion : expansions) {
            for (McRPGContentPack<?> pack : expansion.getExpansionContent()) {
                types.add(stripContentPackSuffix(pack.getClass().getSimpleName()));
            }
        }
        return types;
    }

    /**
     * Extracts a human-readable key string from a {@link McRPGContent} item by dispatching
     * on the known content type interfaces to call the appropriate key accessor.
     *
     * @param content The content item to describe.
     * @return The {@link org.bukkit.NamespacedKey} string for the content, or the simple class name
     *         if no known key accessor is found.
     */
    @NotNull
    private static String describeContent(@NotNull McRPGContent content) {
        if (content instanceof Ability ability) {
            return ability.getAbilityKey().toString();
        } else if (content instanceof Skill skill) {
            return skill.getSkillKey().toString();
        } else if (content instanceof StatisticContent statisticContent) {
            return statisticContent.getStatistic().getStatisticKey().toString();
        } else if (content instanceof QuestDefinition questDefinition) {
            return questDefinition.getQuestKey().toString();
        } else if (content instanceof QuestObjectiveType objectiveType) {
            return objectiveType.getKey().toString();
        } else if (content instanceof QuestRewardType rewardType) {
            return rewardType.getKey().toString();
        } else if (content instanceof QuestSource questSource) {
            return questSource.getKey().toString();
        } else if (content instanceof QuestScopeProvider<?> scopeProvider) {
            return scopeProvider.getKey().toString();
        } else if (content instanceof PlayerStat playerStat) {
            return playerStat.getKey().toString();
        } else if (content instanceof McRPGSetting setting) {
            return setting.getSettingKey().toString();
        } else if (content instanceof TemplateCondition condition) {
            return condition.getKey().toString();
        } else if (content instanceof RewardDistributionType distributionType) {
            return distributionType.getKey().toString();
        } else if (content instanceof QuestRarity rarity) {
            return rarity.getKey().toString();
        } else if (content instanceof QuestTemplate template) {
            return template.getKey().toString();
        } else if (content instanceof QuestChainDefinition chainDefinition) {
            return chainDefinition.getChainKey().toString();
        } else if (content instanceof ChainAutoStartTrigger trigger) {
            return trigger.getKey().toString();
        } else if (content instanceof ScopedBoardAdapter adapter) {
            return adapter.getScopeProviderKey().toString();
        }
        return content.getClass().getSimpleName();
    }

    /**
     * Strips the "ContentPack" suffix from a simple class name to produce a human-readable
     * pack type label.
     *
     * @param simpleName The simple class name to strip.
     * @return The stripped name, or the original if the suffix was not present.
     */
    @NotNull
    private static String stripContentPackSuffix(@NotNull String simpleName) {
        if (simpleName.endsWith("ContentPack")) {
            return simpleName.substring(0, simpleName.length() - "ContentPack".length());
        }
        return simpleName;
    }
}
