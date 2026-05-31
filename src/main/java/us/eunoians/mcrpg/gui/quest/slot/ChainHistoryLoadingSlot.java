package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestChainHistoryDetailGui;
import us.eunoians.mcrpg.gui.quest.QuestHistoryGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Info slot placed at the center of {@link QuestChainHistoryDetailGui} or
 * {@link QuestHistoryGui} while an async data load is in progress. Replaced by
 * the appropriate empty or content slots once the load completes.
 * <p>
 * The display name is resolved from the provided locale {@link Route} so each
 * GUI can supply its own localization key.
 */
public class ChainHistoryLoadingSlot implements McRPGSlot {

    private final Route localizationRoute;

    /**
     * Creates a loading slot that resolves its display name from the given locale route.
     *
     * @param localizationRoute the locale route whose value becomes the slot display name
     */
    public ChainHistoryLoadingSlot(@NotNull Route localizationRoute) {
        this.localizationRoute = localizationRoute;
    }

    /**
     * Builds a clock item whose display name is the localized loading-state message.
     *
     * @param mcRPGPlayer the player viewing the slot; used for locale resolution
     * @return the item builder for the loading-state indicator
     */
    @Override
    @NotNull
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        String loadingMessage = localizationManager.getLocalizedMessage(mcRPGPlayer, localizationRoute);
        String resolvedName = localizationManager.resolvePaletteColors(loadingMessage);
        return ItemBuilder.from(new ItemStack(Material.CLOCK))
                .setDisplayName(resolvedName);
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @Override
    @NotNull
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestChainHistoryDetailGui.class, QuestHistoryGui.class);
    }
}
