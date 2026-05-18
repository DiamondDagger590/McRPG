package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.BoardGuiMode;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
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
import java.util.concurrent.CompletableFuture;

/**
 * GUI slot for a scoped board offering. Displays the offering with its entity context
 * (shown as "Group: {name}" to players). Permission-gated: managers can accept,
 * non-managers see an informational message.
 */
public class ScopedOfferingSlot implements McRPGSlot {

    private final BoardOffering offering;
    private final String entityId;
    private final NamespacedKey scopeProviderKey;
    private final String entityDisplayName;
    private final boolean canManage;

    public ScopedOfferingSlot(@NotNull BoardOffering offering,
                              @NotNull String entityId,
                              @NotNull NamespacedKey scopeProviderKey,
                              @NotNull String entityDisplayName,
                              boolean canManage) {
        this.offering = offering;
        this.entityId = entityId;
        this.scopeProviderKey = scopeProviderKey;
        this.entityDisplayName = entityDisplayName;
        this.canManage = canManage;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        if (!canManage) {
            mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.QUEST_BOARD_GROUP_NO_PERMISSION));
            });
            return true;
        }

        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestBoardManager boardManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.QUEST_BOARD);

            CompletableFuture<Boolean> future = boardManager.acceptScopedOffering(
                    player, offering.getOfferingId(), entityId, scopeProviderKey);

            future.thenAccept(success -> Bukkit.getScheduler().runTask(McRPG.getInstance(), () -> {
                if (success) {
                    mcRPGPlayer.getAsBukkitPlayer().ifPresent(p -> {
                        QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer,
                                BoardGuiMode.SCOPED);
                        RegistryAccess.registryAccess()
                                .registry(RegistryKey.MANAGER)
                                .manager(McRPGManagerKey.GUI)
                                .trackPlayerGui(p, gui);
                        p.openInventory(gui.getInventory());
                    });
                } else {
                    mcRPGPlayer.getAsBukkitPlayer().ifPresent(p -> {
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        p.sendMessage(RegistryAccess.registryAccess()
                                .registry(RegistryKey.MANAGER)
                                .manager(McRPGManagerKey.LOCALIZATION)
                                .getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.QUEST_BOARD_GROUP_SLOTS_FULL));
                    });
                }
            }));
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);
        McRPGLocalizationManager localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        String rarityDisplay = offering.getRarityKey().getKey().replace('_', ' ');
        String categoryDisplay = offering.getCategoryKey().getKey().replace('_', ' ');
        QuestRarityRegistry rarityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_RARITY);
        Optional<QuestRarity> rarityOpt = rarityRegistry.get(offering.getRarityKey());
        String nameColor = rarityOpt.flatMap(QuestRarity::getNameColor).orElse("<white>");

        ItemBuilder builder = ItemBuilder.from(localization.getLocalizedSection(mcRPGPlayer,
                LocalizationKey.QUEST_BOARD_SCOPED_OFFERING_SLOT_DISPLAY_ITEM));
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
                LocalizationKey.QUEST_BOARD_GROUP_ENTITY_LORE,
                Map.of("entity_name", entityDisplayName)));
        lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer,
                LocalizationKey.QUEST_BOARD_OFFERING_CATEGORY,
                Map.of("category", categoryDisplay)));

        definitionOpt.ifPresent(definition -> {
            // Distribution tier preview (structure only — no live contributions since quest is not yet active)
            definition.getRewardDistribution().ifPresent(distConfig -> {
                lore.add(Component.empty());
                lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer,
                        LocalizationKey.QUEST_BOARD_DISTRIBUTION_HEADER));
                for (DistributionTierConfig tier : distConfig.getTiers()) {
                    lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer,
                            LocalizationKey.QUEST_BOARD_DISTRIBUTION_TIER,
                            Map.of("tier", tier.getTierKey())));
                }
            });
        });

        lore.add(Component.empty());
        String durationText = definitionOpt
                .flatMap(def -> def.getExpiration().map(d -> McRPGMethods.formatDuration(d.toMillis())))
                .orElse(localization.getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_NONE));
        lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer,
                LocalizationKey.QUEST_BOARD_DURATION_LINE,
                Map.of("duration", durationText)));

        lore.add(Component.empty());
        if (canManage) {
            lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.QUEST_BOARD_GROUP_ACCEPT));
        } else {
            lore.add(localization.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.QUEST_BOARD_GROUP_NO_PERMISSION));
        }

        builder.addDisplayLoreComponent(lore);
        return builder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestBoardGui.class);
    }
}
