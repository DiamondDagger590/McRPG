package us.eunoians.mcrpg.command.loadout;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.loadout.LoadoutDisplay;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static us.eunoians.mcrpg.command.CommandPlaceholders.LOADOUT_SLOT;
import static us.eunoians.mcrpg.command.CommandPlaceholders.SKIPPED_ABILITIES;
import static us.eunoians.mcrpg.command.CommandPlaceholders.TARGET;

/**
 * Permission-gated command that copies the executing player's current {@link Loadout} into a
 * target player's loadout slot, including display settings and all abilities the target player
 * has access to.
 * <p>
 * Usage: {@code /mcrpg loadout copy <player> [slot]}
 * <ul>
 *   <li>{@code player} — the online player whose loadout slot will be overwritten.</li>
 *   <li>{@code slot} — the target loadout slot to overwrite (1-indexed). Defaults to the
 *       target's currently active slot when omitted.</li>
 * </ul>
 * <p>
 * Abilities in the executor's loadout that the target player does not have available <em>and</em>
 * unlocked are silently excluded from the copied loadout. The executor receives a summary message
 * indicating how many abilities (if any) were skipped.
 */
public class LoadoutCopyCommand extends McRPGCommandBase {

    private static final Permission LOADOUT_COPY_PERMISSION = Permission.of("mcrpg.loadout.copy");

    /**
     * Registers the {@code /mcrpg loadout copy} command with the Cloud command manager.
     */
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("loadout")
                .literal("copy")
                .required("player", PlayerParser.playerParser(),
                        RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(
                                LocalizationKey.COMMAND_DESCRIPTION_LOADOUT_COPY_PLAYER)))
                .optional("slot", IntegerParser.integerParser(1),
                        RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(
                                LocalizationKey.COMMAND_DESCRIPTION_LOADOUT_COPY_SLOT)))
                .permission(Permission.anyOf(ROOT_PERMISSION, LOADOUT_COPY_PERMISSION))
                .handler(commandContext -> {
                    CommandSender sender = commandContext.sender().getSender();
                    McRPGLocalizationManager lm = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

                    if (!(sender instanceof Player executorPlayer)) {
                        sender.sendMessage(lm.getLocalizedMessageAsComponent(
                                sender, LocalizationKey.LOADOUT_COPY_COMMAND_PLAYER_ONLY_MESSAGE));
                        return;
                    }

                    var playerManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);

                    var executorMcRPGOptional = playerManager.getPlayer(executorPlayer.getUniqueId());
                    if (executorMcRPGOptional.isEmpty()) {
                        return;
                    }
                    LoadoutHolder executorHolder = executorMcRPGOptional.get().asSkillHolder();
                    Loadout executorLoadout = executorHolder.getLoadout();

                    Player targetBukkitPlayer = commandContext.get("player");
                    var targetMcRPGOptional = playerManager.getPlayer(targetBukkitPlayer.getUniqueId());
                    if (targetMcRPGOptional.isEmpty()) {
                        return;
                    }
                    LoadoutHolder targetHolder = targetMcRPGOptional.get().asSkillHolder();

                    int targetSlot = commandContext.<Integer>optional("slot")
                            .orElse(targetHolder.getCurrentLoadoutSlot());

                    if (!targetHolder.hasLoadout(targetSlot)) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put(TARGET.getPlaceholder(), targetBukkitPlayer.getName());
                        placeholders.put(LOADOUT_SLOT.getPlaceholder(), Integer.toString(targetSlot));
                        executorPlayer.sendMessage(lm.getLocalizedMessageAsComponent(
                                executorPlayer, LocalizationKey.LOADOUT_COPY_COMMAND_INVALID_SLOT_MESSAGE, placeholders));
                        return;
                    }

                    Set<NamespacedKey> filteredAbilities = filterAccessibleAbilities(executorLoadout, targetHolder);
                    int skipped = executorLoadout.getAbilities().size() - filteredAbilities.size();

                    LoadoutDisplay copiedDisplay = copyDisplay(executorLoadout.getDisplay());
                    Loadout newLoadout = new Loadout(targetHolder.getUUID(), targetSlot, filteredAbilities, copiedDisplay);
                    targetHolder.setLoadout(newLoadout);

                    sendExecutorFeedback(executorPlayer, targetBukkitPlayer, targetSlot, skipped, lm);
                    sendTargetNotification(targetBukkitPlayer, targetSlot, lm);
                }));
    }

    /**
     * Filters the ability keys in {@code source} down to only those the target player has
     * both available (registered from skill progression) and unlocked (upgrade point spent).
     *
     * @param source       The loadout whose abilities are the copy source.
     * @param targetHolder The holder whose access is being checked.
     * @return A mutable set of ability keys eligible to be placed into the target's loadout.
     */
    @NotNull
    static Set<NamespacedKey> filterAccessibleAbilities(@NotNull Loadout source, @NotNull LoadoutHolder targetHolder) {
        var abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);
        Set<NamespacedKey> result = new HashSet<>();
        for (NamespacedKey abilityKey : source.getAbilities()) {
            if (!targetHolder.isAbilityAvailable(abilityKey)) {
                continue;
            }
            Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
            if (ability instanceof UnlockableAbility unlockable && !unlockable.isAbilityUnlocked(targetHolder)) {
                continue;
            }
            result.add(abilityKey);
        }
        return result;
    }

    /**
     * Creates a defensive copy of the given {@link LoadoutDisplay} so that subsequent
     * mutations to the executor's display do not affect the target's copied loadout.
     *
     * @param source The display to copy.
     * @return A new {@link LoadoutDisplay} with the same item and name as {@code source}.
     */
    @NotNull
    private static LoadoutDisplay copyDisplay(@NotNull LoadoutDisplay source) {
        return new LoadoutDisplay(source.getDisplayItem(), source.getDisplayName().orElse(null));
    }

    /**
     * Sends the appropriate success message to the executor, choosing between the full-success
     * and partial-success variant depending on whether any abilities were skipped.
     *
     * @param executorPlayer      The player who ran the command.
     * @param targetBukkitPlayer  The target whose loadout was updated.
     * @param targetSlot          The slot that was overwritten.
     * @param skipped             The number of abilities that could not be copied.
     * @param lm                  The localization manager.
     */
    private static void sendExecutorFeedback(
            @NotNull Player executorPlayer,
            @NotNull Player targetBukkitPlayer,
            int targetSlot,
            int skipped,
            @NotNull McRPGLocalizationManager lm) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(TARGET.getPlaceholder(), targetBukkitPlayer.getName());
        placeholders.put(LOADOUT_SLOT.getPlaceholder(), Integer.toString(targetSlot));
        if (skipped == 0) {
            executorPlayer.sendMessage(lm.getLocalizedMessageAsComponent(
                    executorPlayer, LocalizationKey.LOADOUT_COPY_COMMAND_EXECUTOR_SUCCESS_MESSAGE, placeholders));
        } else {
            placeholders.put(SKIPPED_ABILITIES.getPlaceholder(), Integer.toString(skipped));
            executorPlayer.sendMessage(lm.getLocalizedMessageAsComponent(
                    executorPlayer, LocalizationKey.LOADOUT_COPY_COMMAND_EXECUTOR_PARTIAL_SUCCESS_MESSAGE, placeholders));
        }
    }

    /**
     * Sends a notification to the target player informing them that their loadout slot
     * was updated.
     *
     * @param targetBukkitPlayer The target player.
     * @param targetSlot         The slot that was overwritten.
     * @param lm                 The localization manager.
     */
    private static void sendTargetNotification(
            @NotNull Player targetBukkitPlayer,
            int targetSlot,
            @NotNull McRPGLocalizationManager lm) {
        Map<String, String> placeholders = Map.of(LOADOUT_SLOT.getPlaceholder(), Integer.toString(targetSlot));
        targetBukkitPlayer.sendMessage(lm.getLocalizedMessageAsComponent(
                targetBukkitPlayer, LocalizationKey.LOADOUT_COPY_COMMAND_TARGET_NOTIFICATION_MESSAGE, placeholders));
    }
}
