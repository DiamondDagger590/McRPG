package us.eunoians.mcrpg.command.admin.chain;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Command: {@code /mcrpg quest admin chain reset <player> <chain>}
 * <p>
 * Hard-wipes all chain state and completion log for the target player. The player will
 * experience the chain as if for the first time. Delegates to
 * {@link QuestChainManager#resetChain(java.util.UUID, org.bukkit.NamespacedKey)}.
 */
public class ChainResetCommand extends ChainAdminCommandBase {

    private static final Permission CHAIN_RESET_PERMISSION = Permission.of("mcrpg.quest.admin.chain.reset");

    private static final CloudKey<Player> PLAYER_KEY = CloudKey.of("player", Player.class);
    private static final CloudKey<QuestChainDefinition> CHAIN_KEY =
            CloudKey.of("chain", QuestChainDefinition.class);

    /**
     * Registers the {@code /mcrpg quest admin chain reset} command.
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
                .literal("reset")
                .required(PLAYER_KEY, PlayerParser.playerParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Target player")))
                .required(CHAIN_KEY, ChainKeyParser.chainKeyParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Chain key")))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION,
                        CHAIN_ADMIN_BASE_PERMISSION, CHAIN_RESET_PERMISSION))
                .handler(ctx -> {
                    Audience sender = ctx.sender().getSender();
                    Player target = ctx.get(PLAYER_KEY);
                    QuestChainDefinition chain = ctx.get(CHAIN_KEY);

                    QuestChainManager chainManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST_CHAIN);

                    boolean success = chainManager.resetChain(target.getUniqueId(), chain.getChainKey());
                    if (success) {
                        sender.sendMessage(mm.deserialize(
                                "<green>Reset chain <white>" + chain.getChainKey()
                                + "</white> for <white>" + target.getName() + "</white>."));
                    } else {
                        sender.sendMessage(mm.deserialize(
                                "<red>Could not reset chain <white>" + chain.getChainKey()
                                + "</white> for <white>" + target.getName()
                                + "</white>. Player may be offline or the chain is currently active."));
                    }
                }));
    }
}
