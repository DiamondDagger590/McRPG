package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
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
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
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

import java.util.ArrayList;
import java.util.List;
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
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
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
        Optional<QuestDefinition> definitionOpt = boardManager.resolveDefinitionForOffering(offering);
        if (definitionOpt.isEmpty()) return;
        QuestDefinition definition = definitionOpt.get();

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
        McRPGLocalizationManager localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        String rarityDisplay = offering.getRarityKey().getKey().replace('_', ' ');
        String categoryDisplay = offering.getCategoryKey().getKey().replace('_', ' ');
        QuestRarityRegistry rarityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_RARITY);
        Optional<QuestRarity> rarityOpt = rarityRegistry.get(offering.getRarityKey());
        String nameColor = rarityOpt.flatMap(QuestRarity::getNameColor).orElse("<white>");

        // Build base item (material + name) from localized section
        ItemBuilder builder = ItemBuilder.from(localization.getLocalizedSection(mcRPGPlayer,
                LocalizationKey.QUEST_BOARD_OFFERING_SLOT_DISPLAY_ITEM));
        builder.applyTagReplacements(localization.getPaletteReplacements());
        if (rarityOpt.isPresent()) {
            builder = rarityOpt.get().configureIcon(builder);
        }
        // Re-set name after configureIcon() since it unconditionally overwrites the display name
        builder.setDisplayName(nameColor + boardManager.getOfferingDisplayName(mcRPGPlayer, offering));

        Optional<QuestDefinition> definitionOpt = boardManager.resolveDefinitionForOffering(offering);
        List<Component> lore = new ArrayList<>();

        lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer,
                LocalizationKey.QUEST_BOARD_RARITY_LINE,
                Map.of("rarity_color", nameColor, "rarity", rarityDisplay)));
        lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer,
                LocalizationKey.QUEST_BOARD_OFFERING_CATEGORY,
                Map.of("category", categoryDisplay)));

        lore.add(Component.empty());
        String durationText = definitionOpt
                .flatMap(def -> def.getExpiration().map(d -> McRPGMethods.formatDuration(d.toMillis())))
                .orElse(localization.getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_NONE));
        lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer,
                LocalizationKey.QUEST_BOARD_DURATION_LINE,
                Map.of("duration", durationText)));

        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            int active = boardManager.getActiveBoardQuestCount(mcRPGPlayer.getUUID());
            int max = boardManager.getEffectiveMaxQuests(player, board);
            int remaining = max - active;
            lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer,
                    LocalizationKey.QUEST_BOARD_BOARD_QUESTS_LINE,
                    Map.of("count_color", remaining > 0 ? "<green>" : "<red>",
                            "board_quests", String.valueOf(active),
                            "max_quests", String.valueOf(max))));
        });

        lore.add(Component.empty());
        lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.QUEST_BOARD_CLICK_TO_ACCEPT));
        lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.QUEST_BOARD_RIGHT_CLICK_PREVIEW));

        builder.addDisplayLoreComponent(lore);
        return builder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestBoardGui.class);
    }
}
