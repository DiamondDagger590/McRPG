package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.loadout.LoadoutAbilityChangeEvent;
import us.eunoians.mcrpg.event.loadout.LoadoutAbilityChangeEvent.ChangeReason;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.LoadoutEquipQuestContext;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

/**
 * Listens for {@link LoadoutAbilityChangeEvent} and drives quest objective progress
 * for loadout equip objectives. Only credits progress when the change affects the player's
 * currently active loadout, so editing a non-active preset does not satisfy the objective.
 * Fires for both {@link ChangeReason#EQUIP} and {@link ChangeReason#SWAP} since both result
 * in a new ability entering the loadout.
 */
public class LoadoutEquipQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Constructs a new {@link LoadoutEquipQuestProgressListener}.
     *
     * @param questManager the {@link QuestManager} used to drive quest progress
     */
    public LoadoutEquipQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles {@link LoadoutAbilityChangeEvent} to progress any active loadout equip quest
     * objectives for the player. Only fires for equip and swap on the player's currently
     * active loadout — unequip events and changes to non-active loadout presets are ignored.
     *
     * @param event the loadout change event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLoadoutChange(@NotNull LoadoutAbilityChangeEvent event) {
        if (event.getReason() == ChangeReason.UNEQUIP) {
            return;
        }
        Optional<McRPGPlayer> playerOpt = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(event.getPlayerUUID());
        if (playerOpt.isEmpty() || playerOpt.get().asSkillHolder().getCurrentLoadoutSlot() != event.getLoadoutSlot()) {
            return;
        }
        progressQuests(questManager, event.getPlayerUUID(), new LoadoutEquipQuestContext(event));
    }
}
