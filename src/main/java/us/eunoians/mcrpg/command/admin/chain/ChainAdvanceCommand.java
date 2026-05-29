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
 * Command: {@code /mcrpg quest admin chain advance <player> <chain>}
 * <p>
 * Force-completes the player's current chain step and starts the next one. If the player
 * is on the last step the chain is completed. Delegates to
 * {@link QuestChainManager#forceAdvanceChain(java.util.UUID, org.bukkit.NamespacedKey)}.
 */
public class ChainAdvanceCommand extends ChainAdminCommandBase {

    private static final Permission CHAIN_ADVANCE_PERMISSION = Permission.of("mcrpg.quest.admin.chain.advance");

    private static final CloudKey<Player> PLAYER_KEY = CloudKey.of("player", Player.class);
    private static final CloudKey<QuestChainDefinition> CHAIN_KEY =
            CloudKey.of("chain", QuestChainDefinition.class);

    /**
     * Registers the {@code /mcrpg quest admin chain advance} command.
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
                .literal("advance")
                .required(PLAYER_KEY, PlayerParser.playerParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Target player")))
                .required(CHAIN_KEY, ChainKeyParser.chainKeyParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Chain key")))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION,
                        CHAIN_ADMIN_BASE_PERMISSION, CHAIN_ADVANCE_PERMISSION))
                .handler(ctx -> {
                    Audience sender = ctx.sender().getSender();
                    Player target = ctx.get(PLAYER_KEY);
                    QuestChainDefinition chain = ctx.get(CHAIN_KEY);

                    QuestChainManager chainManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST_CHAIN);

                    boolean success = chainManager.forceAdvanceChain(target.getUniqueId(), chain.getChainKey());
                    if (success) {
                        sender.sendMessage(mm.deserialize(
                                "<green>Advanced chain <white>" + chain.getChainKey()
                                + "</white> for <white>" + target.getName() + "</white>."));
                    } else {
                        sender.sendMessage(mm.deserialize(
                                "<red>Could not advance chain <white>" + chain.getChainKey()
                                + "</white> for <white>" + target.getName()
                                + "</white>. Player may have no active state or chain is in a terminal state."));
                    }
                }));
    }
}
