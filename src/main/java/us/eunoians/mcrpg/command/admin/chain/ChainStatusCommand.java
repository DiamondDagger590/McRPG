package us.eunoians.mcrpg.command.admin.chain;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerState;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Command: {@code /mcrpg quest admin chain status <player> <chain>}
 * <p>
 * Displays the target player's chain state including: current state, current step,
 * completion count, last completed timestamp, and step progress.
 */
public class ChainStatusCommand extends ChainAdminCommandBase {

    private static final Permission CHAIN_STATUS_PERMISSION = Permission.of("mcrpg.quest.admin.chain.status");

    private static final CloudKey<Player> PLAYER_KEY = CloudKey.of("player", Player.class);
    private static final CloudKey<QuestChainDefinition> CHAIN_KEY =
            CloudKey.of("chain", QuestChainDefinition.class);

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    /**
     * Registers the {@code /mcrpg quest admin chain status} command.
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
                .literal("status")
                .required(PLAYER_KEY, PlayerParser.playerParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Target player")))
                .required(CHAIN_KEY, ChainKeyParser.chainKeyParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Chain key (e.g. mcrpg:my_chain)")))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION,
                        CHAIN_ADMIN_BASE_PERMISSION, CHAIN_STATUS_PERMISSION))
                .handler(ctx -> {
                    Audience sender = ctx.sender().getSender();
                    Player target = ctx.get(PLAYER_KEY);
                    QuestChainDefinition chain = ctx.get(CHAIN_KEY);

                    QuestChainManager chainManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST_CHAIN);
                    Optional<QuestChainPlayerState> stateOpt =
                            chainManager.getChainStatus(target.getUniqueId(), chain.getChainKey());

                    if (stateOpt.isEmpty()) {
                        sender.sendMessage(mm.deserialize("<yellow>" + target.getName()
                                + " has no state for chain <white>" + chain.getChainKey() + "</white>."));
                        return;
                    }

                    QuestChainPlayerState state = stateOpt.get();
                    String currentStep = state.getCurrentQuestKey()
                            .map(NamespacedKey::toString)
                            .orElse("<none>");
                    String lastCompleted = state.getLastCompletedAt()
                            .map(ts -> TIMESTAMP_FORMAT.format(Instant.ofEpochMilli(ts)))
                            .orElse("never");
                    int totalSteps = chain.getSteps().size();

                    sender.sendMessage(mm.deserialize(
                            "<gold>Chain status for <white>" + target.getName()
                            + "</white> — <white>" + chain.getDisplayName() + "</white>"));
                    sender.sendMessage(mm.deserialize(
                            "<gray>State: <white>" + state.getState().name()));
                    sender.sendMessage(mm.deserialize(
                            "<gray>Current step: <white>" + currentStep));
                    sender.sendMessage(mm.deserialize(
                            "<gray>Completions: <white>" + state.getCompletionCount()));
                    sender.sendMessage(mm.deserialize(
                            "<gray>Last completed: <white>" + lastCompleted));
                    sender.sendMessage(mm.deserialize(
                            "<gray>Total steps: <white>" + totalSteps));
                }));
    }
}
