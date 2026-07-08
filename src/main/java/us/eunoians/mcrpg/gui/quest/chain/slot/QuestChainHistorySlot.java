package us.eunoians.mcrpg.gui.quest.chain.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.quest.chain.ChainCompletionRun;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.chain.QuestChainHistoryDetailGui;
import us.eunoians.mcrpg.gui.quest.QuestHistoryGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A slot in {@link QuestHistoryGui} representing a single completed chain run.
 * Displays the chain name, completion date, and step count. Clicking opens
 * {@link QuestChainHistoryDetailGui} for the specific run.
 */
public class QuestChainHistorySlot implements McRPGSlot {

    private final ChainCompletionRun run;

    public QuestChainHistorySlot(@NotNull ChainCompletionRun run) {
        this.run = run;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestChainHistoryDetailGui detailGui =
                    new QuestChainHistoryDetailGui(mcRPGPlayer, run.chainKey(), run.completionNumber());
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI).trackPlayerGui(player, detailGui);
            player.openInventory(detailGui.getInventory());
        });
        return true;
    }

    /**
     * Builds the display item for this chain run entry.
     *
     * <p>Placeholders resolved:
     * <ul>
     *   <li>{@code chain_name} — chain display name, or the raw key if the definition is no longer registered</li>
     *   <li>{@code completed_date} — locale-aware formatted date of the last step completion</li>
     *   <li>{@code step_count} — number of steps recorded in this run</li>
     *   <li>{@code total_steps} — total steps defined in the chain definition; falls back to {@code step_count}
     *       if the definition is no longer registered</li>
     *   <li>{@code completion_number} — 1-based run index for this chain (e.g. "1" for first completion)</li>
     * </ul>
     *
     * @param mcRPGPlayer the player viewing the slot; used for locale resolution
     * @return the item builder populated with all placeholders and palette replacements applied
     */
    @Override
    @NotNull
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);

        Optional<QuestChainDefinition> definitionOpt = chainRegistry.get(run.chainKey());
        String chainName = definitionOpt.map(QuestChainDefinition::getDisplayName)
                .orElse(run.chainKey().getKey());
        int totalSteps = definitionOpt.map(d -> d.getSteps().size()).orElse(run.stepCount());

        String completedDate = localizationManager.formatDisplayDate(mcRPGPlayer, run.completedAt());

        ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(
                mcRPGPlayer, LocalizationKey.QUEST_CHAIN_HISTORY_GUI_CHAIN_SLOT_DISPLAY_ITEM))
                .addPlaceholders(Map.of(
                        "chain_name", chainName,
                        "completed_date", completedDate,
                        "step_count", String.valueOf(run.stepCount()),
                        "total_steps", String.valueOf(totalSteps),
                        "completion_number", String.valueOf(run.completionNumber())
                ));
        itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
        return itemBuilder;
    }

    @Override
    @NotNull
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestHistoryGui.class);
    }
}
