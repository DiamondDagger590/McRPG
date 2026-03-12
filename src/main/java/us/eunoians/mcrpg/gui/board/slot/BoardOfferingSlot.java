package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.McRPGGuiManager;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.OfferingAcceptResult;
import us.eunoians.mcrpg.quest.board.QuestBoard;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * GUI slot displaying a single board offering. Clicking triggers quest acceptance.
 */
public class BoardOfferingSlot implements McRPGSlot {

    private final BoardOffering offering;

    public BoardOfferingSlot(@NotNull BoardOffering offering) {
        this.offering = offering;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
                openOfferingPreview(mcRPGPlayer, player);
            } else {
                acceptAndRefresh(mcRPGPlayer, player);
            }
        });
        return true;
    }

    private void acceptAndRefresh(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Player player) {
        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);
        OfferingAcceptResult result = boardManager.acceptOffering(player, offering.getOfferingId());
        if (result.isAccepted()) {
            McRPGGuiManager guiManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI);
            Optional<Gui<McRPGPlayer>> openGui = guiManager.getOpenedGui(player);
            openGui.ifPresent(gui -> {
                if (gui instanceof PaginatedGui<?> paginated) {
                    paginated.refreshGUI();
                }
            });
        } else if (result == OfferingAcceptResult.SLOTS_FULL) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.QUEST_BOARD_SLOT_FULL));
        }
    }

    private void openOfferingPreview(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Player player) {
        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);
        QuestDefinition definition = boardManager.resolveDefinitionForOffering(offering);
        if (definition == null) return;

        QuestDetailGui detailGui = QuestDetailGui.forBoardPreview(mcRPGPlayer, definition, offering);
        McRPGGuiManager guiManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.GUI);
        guiManager.trackPlayerGui(player, detailGui);
        player.openInventory(detailGui.getInventory());
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);
        QuestBoard board = boardManager.getDefaultBoard();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("quest_name", boardManager.getOfferingDisplayName(mcRPGPlayer, offering));
        placeholders.put("rarity", offering.getRarityKey().getKey().replace('_', ' '));
        placeholders.put("category", offering.getCategoryKey().getKey().replace('_', ' '));

        QuestRarityRegistry rarityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_RARITY);
        Optional<QuestRarity> rarityOpt = rarityRegistry.get(offering.getRarityKey());
        String nameColor = rarityOpt.flatMap(QuestRarity::getNameColor).orElse("<white>");
        placeholders.put("rarity_color", nameColor);

        QuestDefinition definition = boardManager.resolveDefinitionForOffering(offering);
        String questDuration = definition != null
                ? definition.getExpiration()
                .map(expiration -> McRPGMethods.formatDuration(expiration.toMillis()))
                .orElse(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_NONE))
                : RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_NONE);
        placeholders.put("quest_duration", questDuration);

        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            int active = boardManager.getActiveBoardQuestCount(mcRPGPlayer.getUUID());
            int max = boardManager.getEffectiveMaxQuests(player, board);
            int remaining = max - active;
            placeholders.put("board_quests", String.valueOf(active));
            placeholders.put("max_quests", String.valueOf(max));
            placeholders.put("count_color", remaining > 0 ? "<green>" : "<red>");
        });

        ItemBuilder builder = ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_BOARD_OFFERING_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
        if (rarityOpt.isPresent() && rarityOpt.get().hasGlint()) {
            builder.setEnchantGlint(true);
        }

        return builder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestBoardGui.class);
    }
}
