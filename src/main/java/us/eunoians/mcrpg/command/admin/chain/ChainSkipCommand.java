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
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.CommandPlaceholders;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerData;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerState;
import us.eunoians.mcrpg.quest.chain.QuestChainState;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;

/**
 * Command: {@code /mcrpg quest chain skip <player> <chain>}
 * <p>
 * Force-completes all remaining steps in the player's active chain, granting rewards
 * for each step and setting the chain to {@link QuestChainState#COMPLETED}.
 * Requires the {@code mcrpg.quest.chain.skip} permission.
 * <p>
 * Each step is advanced via
 * {@link QuestChainManager#forceAdvanceChain(java.util.UUID, org.bukkit.NamespacedKey)}
 * in a loop until the chain leaves the {@link QuestChainState#ACTIVE} state. Rewards
 * and the {@link us.eunoians.mcrpg.event.quest.chain.QuestChainCompleteEvent} fire through
 * the normal chain-completion path.
 */
public class ChainSkipCommand extends ChainAdminCommandBase {

    /**
     * Sentinel value returned by {@link #skipChain} when the player has no chain state.
     */
    public static final int SKIP_ERROR_NO_STATE = -1;

    /**
     * Sentinel value returned by {@link #skipChain} when the chain is already terminal.
     */
    public static final int SKIP_ERROR_TERMINAL = -2;

    private static final Permission CHAIN_SKIP_PERMISSION = Permission.of("mcrpg.quest.chain.skip");

    private static final CloudKey<Player> PLAYER_KEY = CloudKey.of("player", Player.class);
    private static final CloudKey<QuestChainDefinition> CHAIN_KEY =
            CloudKey.of("chain", QuestChainDefinition.class);

    /**
     * Registers the {@code /mcrpg quest chain skip} command.
     */
    public static void registerCommand() {
        McRPG plugin = McRPG.getInstance();
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        MiniMessage mm = plugin.getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("chain")
                .literal("skip")
                .required(PLAYER_KEY, PlayerParser.playerParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Target player")))
                .required(CHAIN_KEY, ChainKeyParser.chainKeyParser(),
                        RichDescription.richDescription(mm.deserialize("<gray>Chain key")))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION,
                        CHAIN_ADMIN_BASE_PERMISSION, CHAIN_SKIP_PERMISSION))
                .handler(ctx -> {
                    Audience sender = ctx.sender().getSender();
                    Player target = ctx.get(PLAYER_KEY);
                    QuestChainDefinition chain = ctx.get(CHAIN_KEY);
                    McRPGLocalizationManager lm = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

                    Optional<McRPGPlayer> mcRPGPlayerOpt = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.PLAYER)
                            .getPlayer(target.getUniqueId());
                    if (mcRPGPlayerOpt.isEmpty()) {
                        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.CHAIN_ADMIN_NO_STATE,
                                Map.of(
                                        CommandPlaceholders.TARGET.getPlaceholder(), target.getName(),
                                        CommandPlaceholders.CHAIN_DISPLAY_NAME.getPlaceholder(), chain.getDisplayName(),
                                        CommandPlaceholders.CHAIN_KEY.getPlaceholder(), chain.getChainKey().toString())));
                        return;
                    }

                    McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
                    int result = skipChain(mcRPGPlayer, chain);

                    if (result == SKIP_ERROR_NO_STATE) {
                        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_CHAIN_ADMIN_SKIP_ERROR_NO_STATE,
                                Map.of(
                                        CommandPlaceholders.TARGET.getPlaceholder(), target.getName(),
                                        CommandPlaceholders.CHAIN_DISPLAY_NAME.getPlaceholder(), chain.getDisplayName(),
                                        CommandPlaceholders.CHAIN_KEY.getPlaceholder(), chain.getChainKey().toString())));
                    } else if (result == SKIP_ERROR_TERMINAL) {
                        Optional<QuestChainPlayerState> stateOpt =
                                mcRPGPlayer.getChainData().getChainState(chain.getChainKey());
                        String stateName = stateOpt.map(s -> s.getState().name()).orElse("UNKNOWN");
                        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_CHAIN_ADMIN_SKIP_ERROR_TERMINAL,
                                Map.of(
                                        CommandPlaceholders.TARGET.getPlaceholder(), target.getName(),
                                        CommandPlaceholders.CHAIN_DISPLAY_NAME.getPlaceholder(), chain.getDisplayName(),
                                        CommandPlaceholders.CHAIN_KEY.getPlaceholder(), chain.getChainKey().toString(),
                                        CommandPlaceholders.CHAIN_STATE.getPlaceholder(), stateName)));
                    } else {
                        sender.sendMessage(lm.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.QUEST_CHAIN_ADMIN_SKIP_SUCCESS,
                                Map.of(
                                        CommandPlaceholders.TARGET.getPlaceholder(), target.getName(),
                                        CommandPlaceholders.CHAIN_DISPLAY_NAME.getPlaceholder(), chain.getDisplayName(),
                                        CommandPlaceholders.CHAIN_KEY.getPlaceholder(), chain.getChainKey().toString(),
                                        CommandPlaceholders.COUNT.getPlaceholder(), String.valueOf(result))));
                    }
                }));
    }

    /**
     * Force-completes all remaining steps in the given chain for the player. Each step is
     * advanced via {@link QuestChainManager#forceAdvanceChain} until the chain transitions
     * out of {@link QuestChainState#ACTIVE}.
     * <p>
     * Rewards and the {@link us.eunoians.mcrpg.event.quest.chain.QuestChainCompleteEvent} fire
     * through the normal chain-completion path inside {@code forceAdvanceChain}.
     *
     * @param mcRPGPlayer the player whose chain will be skipped
     * @param chain       the chain definition to skip
     * @return the number of steps skipped on success, {@link #SKIP_ERROR_NO_STATE} if the
     *         player has no state for the chain, or {@link #SKIP_ERROR_TERMINAL} if the
     *         chain is already in a terminal state
     */
    public static int skipChain(@NotNull McRPGPlayer mcRPGPlayer, @NotNull QuestChainDefinition chain) {
        QuestChainPlayerData chainData = mcRPGPlayer.getChainData();
        Optional<QuestChainPlayerState> stateOpt = chainData.getChainState(chain.getChainKey());
        if (stateOpt.isEmpty()) {
            return SKIP_ERROR_NO_STATE;
        }
        if (stateOpt.get().getState().isTerminal()) {
            return SKIP_ERROR_TERMINAL;
        }

        QuestChainManager chainManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST_CHAIN);
        int stepsSkipped = 0;
        int maxIterations = chain.getSteps().size() + 1;

        while (stepsSkipped < maxIterations) {
            Optional<QuestChainPlayerState> currentState = chainData.getChainState(chain.getChainKey());
            if (currentState.isEmpty() || currentState.get().getState() != QuestChainState.ACTIVE) {
                break;
            }
            boolean advanced = chainManager.forceAdvanceChain(mcRPGPlayer.getUUID(), chain.getChainKey());
            if (!advanced) {
                break;
            }
            stepsSkipped++;
        }
        return stepsSkipped;
    }
}
