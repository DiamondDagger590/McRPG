package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.PreQuestStartEvent;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.source.builtin.TutorialQuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.DisableTutorialSetting;

import java.util.Optional;

/**
 * Cancels {@link PreQuestStartEvent} for tutorial-sourced quests when:
 * <ul>
 *   <li>The player's {@link DisableTutorialSetting} is {@code DISABLED}</li>
 *   <li>The player has the {@code mcrpg.tutorial.bypass} permission</li>
 *   <li>The server-wide {@code tutorial.enabled} config toggle is {@code false}</li>
 * </ul>
 * <p>
 * When cancelling a mid-chain start (the tutorial chain is already ACTIVE for this
 * player), schedules a next-tick task to call
 * {@link QuestChainManager#abandonChain(java.util.UUID, NamespacedKey)} to transition the
 * chain to ABANDONED state. The next-tick scheduling avoids mutating chain state
 * during event dispatch within {@code startQuest()}.
 */
public class TutorialPreQuestStartListener implements Listener {

    private static final String BYPASS_PERMISSION = "mcrpg.tutorial.bypass";

    private final McRPG mcRPG;

    /**
     * Creates a new tutorial pre-quest-start listener.
     *
     * @param mcRPG the McRPG plugin instance
     */
    public TutorialPreQuestStartListener(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
    }

    /**
     * Cancels the quest start when the source is {@link TutorialQuestSource} and
     * tutorials are disabled for the player through any gate. Schedules a next-tick
     * chain abandonment to cleanly transition chain state outside the event dispatch stack.
     *
     * @param event the pre-quest-start event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPreQuestStart(@NotNull PreQuestStartEvent event) {
        if (!(event.getSource() instanceof TutorialQuestSource)) {
            return;
        }

        Player player = event.getPlayer();
        if (!isTutorialDisabledForPlayer(player)) {
            return;
        }

        event.setCancelled(true);

        // Schedule chain abandonment on next tick to avoid mutating state
        // during the event dispatch stack of startQuest()
        Bukkit.getScheduler().runTask(mcRPG, () -> {
            QuestChainManager chainManager = mcRPG.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.QUEST_CHAIN);
            chainManager.abandonChain(player.getUniqueId(), TutorialQuestSource.TUTORIAL_CHAIN_KEY);
        });
    }

    /**
     * Checks whether tutorials are disabled for the given player through any mechanism:
     * server-wide config toggle, bypass permission, or the player's own setting.
     *
     * @param player the player to check
     * @return {@code true} if tutorial quests should be blocked for this player
     */
    private boolean isTutorialDisabledForPlayer(@NotNull Player player) {
        YamlDocument config = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG);
        if (!config.getBoolean(MainConfigFile.TUTORIAL_ENABLED)) {
            return true;
        }

        if (player.hasPermission(BYPASS_PERMISSION)) {
            return true;
        }

        Optional<McRPGPlayer> mcRPGPlayerOpt = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isPresent()) {
            McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
            var settingOpt = mcRPGPlayer.getPlayerSetting(DisableTutorialSetting.SETTING_KEY);
            if (settingOpt.isPresent()
                    && settingOpt.get() instanceof DisableTutorialSetting tutorialSetting
                    && tutorialSetting.isDisabled()) {
                return true;
            }
        }

        return false;
    }
}
