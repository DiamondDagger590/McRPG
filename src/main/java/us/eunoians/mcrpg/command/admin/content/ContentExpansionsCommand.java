package us.eunoians.mcrpg.command.admin.content;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Collection;

/**
 * Command: {@code /mcrpg admin content expansions}
 * <p>
 * Lists all registered {@link ContentExpansion} instances with their keys and content pack counts.
 */
public class ContentExpansionsCommand extends AdminBaseCommand {

    private static final Permission CONTENT_PERMISSION = Permission.of("mcrpg.admin.content");

    /**
     * Registers the {@code /mcrpg admin content expansions} command.
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
                .literal("expansions")
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, CONTENT_PERMISSION))
                .handler(ctx -> {
                    Audience sender = ctx.sender().getSender();
                    sendExpansionList(sender, mm);
                }));
    }

    /**
     * Sends a formatted list of all registered content expansions to the given audience.
     *
     * @param sender The audience to send the list to.
     * @param mm     The {@link MiniMessage} instance for parsing formatted text.
     */
    private static void sendExpansionList(@NotNull Audience sender, @NotNull MiniMessage mm) {
        ContentExpansionManager manager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.CONTENT_EXPANSION);
        Collection<ContentExpansion> expansions = manager.getRegisteredExpansions();

        if (expansions.isEmpty()) {
            sender.sendMessage(mm.deserialize("<negative>No content expansions are registered."));
            return;
        }

        sender.sendMessage(mm.deserialize("<primary>Registered Content Expansions <body>(" + expansions.size() + "):"));

        expansions.stream()
                .sorted((a, b) -> a.getExpansionKey().toString().compareTo(b.getExpansionKey().toString()))
                .forEach(expansion -> {
                    String key = expansion.getExpansionKey().toString();
                    int packCount = expansion.getExpansionContent().size();
                    sender.sendMessage(mm.deserialize(
                            " <primary>- <body>" + key + " <primary>(" + packCount + " packs)"));
                });
    }
}
