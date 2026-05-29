package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.quest.ChainCompletionRun;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestChainHistoryDetailGui;
import us.eunoians.mcrpg.gui.quest.QuestHistoryGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A slot in {@link QuestHistoryGui} representing a single completed chain run.
 * Displays the chain name, completion date, and step count. Clicking opens
 * {@link QuestChainHistoryDetailGui} for the specific run.
 */
public class QuestChainHistorySlot implements McRPGSlot {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

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

    @Override
    @NotNull
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);

        Optional<QuestChainDefinition> definitionOpt = chainRegistry.get(run.chainKey());
        String chainName = definitionOpt.map(QuestChainDefinition::getDisplayName)
                .orElse(run.chainKey().getKey());
        String completedDate = DATE_FORMAT.format(new Date(run.completedAt()));

        var paletteReplacements = localizationManager.getPaletteReplacements();

        ItemBuilder builder = ItemBuilder.from(new ItemStack(Material.BOOK))
                .setDisplayName("<primary>" + chainName)
                .addDisplayLore(List.of(
                        "<body>Completed: <primary>" + completedDate,
                        "<body>Steps: <primary>" + run.stepCount() + "/" + run.stepCount(),
                        "",
                        "<hint>Click <body>to view details."
                ))
                .applyTagReplacements(paletteReplacements);
        return builder;
    }

    @Override
    @NotNull
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestHistoryGui.class);
    }
}
