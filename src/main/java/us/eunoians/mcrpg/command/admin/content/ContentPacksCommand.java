package us.eunoians.mcrpg.command.admin.content;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Optional;

/**
 * Command: {@code /mcrpg admin content packs <expansion>}
 * <p>
 * Lists all {@link McRPGContentPack}s registered for a specific {@link ContentExpansion},
 * identified by its {@link NamespacedKey}.
 */
public class ContentPacksCommand extends AdminBaseCommand {

    private static final Permission CONTENT_PERMISSION = Permission.of("mcrpg.admin.content");
    private static final CloudKey<String> EXPANSION_KEY = CloudKey.of("expansion", String.class);

    /**
     * Registers the {@code /mcrpg admin content packs <expansion>} command.
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
                .literal("packs")
                .required(EXPANSION_KEY, StringParser.stringParser(),
                        RichDescription.richDescription(
                                mm.deserialize("<gray>Expansion key (e.g. mcrpg:mcrpg-expansion)")))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, CONTENT_PERMISSION))
                .handler(ctx -> {
                    Audience sender = ctx.sender().getSender();
                    String expansionKeyStr = ctx.get(EXPANSION_KEY);
                    sendPackList(sender, mm, expansionKeyStr);
                }));
    }

    /**
     * Sends a formatted list of content packs for the given expansion key to the audience.
     *
     * @param sender          The audience to send the list to.
     * @param mm              The {@link MiniMessage} instance for parsing formatted text.
     * @param expansionKeyStr The raw string key for the expansion (e.g. {@code "mcrpg:mcrpg-expansion"}).
     */
    private static void sendPackList(@NotNull Audience sender, @NotNull MiniMessage mm,
                                     @NotNull String expansionKeyStr) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(expansionKeyStr);
        if (namespacedKey == null) {
            sender.sendMessage(mm.deserialize(
                    "<negative>Invalid key format: <body>" + expansionKeyStr
                            + " <negative>(expected namespace:key)"));
            return;
        }

        ContentExpansionManager manager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.CONTENT_EXPANSION);
        Optional<List<McRPGContentPack<?>>> packsOpt = manager.getContentPacks(namespacedKey);

        if (packsOpt.isEmpty()) {
            sender.sendMessage(mm.deserialize(
                    "<negative>No expansion found for key: <body>" + namespacedKey));
            return;
        }

        List<McRPGContentPack<?>> packs = packsOpt.get();
        if (packs.isEmpty()) {
            sender.sendMessage(mm.deserialize(
                    "<primary>Expansion <body>" + namespacedKey + " <primary>has no content packs."));
            return;
        }

        sender.sendMessage(mm.deserialize(
                "<primary>Content Packs for <body>" + namespacedKey + " <primary>(" + packs.size() + "):"));

        packs.stream()
                .sorted((a, b) -> stripContentPackSuffix(a.getClass().getSimpleName()).compareTo(stripContentPackSuffix(b.getClass().getSimpleName())))
                .forEach(pack -> {
                    String typeName = stripContentPackSuffix(pack.getClass().getSimpleName());
                    int contentCount = pack.getContent().size();
                    sender.sendMessage(mm.deserialize(
                            " <primary>- <body>" + typeName + " <primary>(" + contentCount + " entries)"));
                });
    }

}
