package us.eunoians.mcrpg.command.admin.chain;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

/**
 * Command: {@code /mcrpg quest admin chain restart <player> <chain> [force]}
 * <p>
 * Restarts a player's chain from step 1. Without {@code force}, completed steps are skipped.
 * With {@code force}, all steps are replayed from the beginning. Delegates to
 * {@link QuestChainManager#restartChain(java.util.UUID, org.bukkit.NamespacedKey, boolean, java.util.function.Consumer)}.
 */
public class ChainRestartCommand extends ChainAdminCommandBase {

    private static final Permission CHAIN_RESTART_PERMISSION = Permission.of("mcrpg.quest.admin.chain.restart");

    private static final CloudKey<Player> PLAYER_KEY = CloudKey.of("player", Player.class);
    private static final CloudKey<QuestChainDefinition> CHAIN_KEY =
            CloudKey.of("chain", QuestChainDefinition.class);
    private static final CloudKey<String> FORCE_KEY = CloudKey.of("force", String.class);

    /**
     * Registers the {@code /mcrpg quest admin chain restart} command.
     */
    public static void registerCommand() {
        McRPG plugin = McRPG.getInstance();
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        MiniMessage mm = plugin.getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("admin")
                .literal("chain")
                .literal("restart")
                .required(PLAYER_KEY, PlayerParser.playerParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Target player")))
                .required(CHAIN_KEY, ChainKeyParser.chainKeyParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Chain key")))
                .optional(FORCE_KEY, StringParser.stringParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Type 'force' to replay all steps")))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION,
                        CHAIN_ADMIN_BASE_PERMISSION, CHAIN_RESTART_PERMISSION))
                .handler(ctx -> {
                    Audience sender = ctx.sender().getSender();
                    Player target = ctx.get(PLAYER_KEY);
                    QuestChainDefinition chain = ctx.get(CHAIN_KEY);
                    boolean force = isForceFlag(ctx);

                    QuestChainManager chainManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST_CHAIN);

                    chainManager.restartChain(target.getUniqueId(), chain.getChainKey(), force, success -> {
                        if (success) {
                            sender.sendMessage(mm.deserialize(
                                    "<green>Restarted chain <white>" + chain.getChainKey()
                                    + "</white> for <white>" + target.getName()
                                    + "</white>" + (force ? " (force)" : "") + "."));
                        } else {
                            sender.sendMessage(mm.deserialize(
                                    "<red>Could not restart chain <white>" + chain.getChainKey()
                                    + "</white> for <white>" + target.getName()
                                    + "</white>. Player may have no state for this chain."));
                        }
                    });
                }));
    }

    /**
     * Returns {@code true} if the sender included {@code force} as the optional argument.
     *
     * @param ctx the command context
     * @return whether the force flag was provided
     */
    private static boolean isForceFlag(@NotNull CommandContext<CommandSourceStack> ctx) {
        Optional<String> forceArg = ctx.optional(FORCE_KEY);
        return forceArg.map(s -> s.equalsIgnoreCase("force")).orElse(false);
    }
}
