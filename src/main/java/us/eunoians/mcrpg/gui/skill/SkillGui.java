package us.eunoians.mcrpg.gui.skill;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.FillerItemGui;
import us.eunoians.mcrpg.gui.skill.slot.SkillSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.List;
import java.util.Set;

/**
 * This gui is the main gui for players to view all their skill from.
 */
public class SkillGui extends PaginatedSortedSkillGui implements FillerItemGui {

    protected static final int NAVIGATION_ROW_START_INDEX = 45;
    protected static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    protected static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
    protected static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;

    public SkillGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(getPlayer(), 54,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.SKILL_GUI_TITLE));
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    @Override
    protected void paintNavigationBar(int page) {
        // Paint the nav bar with filler glass
        Slot<McRPGPlayer> fillerSlot = getFillerItemSlot();
        for (int i = 0; i < 9; i++) {
            setSlot(NAVIGATION_ROW_START_INDEX + i, fillerSlot);
        }
        // Set the sort slot
        setSlot(SORT_SLOT_INDEX, getSkillSortNode().getNodeValue().getSlot());
        // If the page is not the first page, then we need to put a previous arrow button
        if (page > 1) {
            setSlot(PREVIOUS_PAGE_SLOT_INDEX, getPreviousPageSlot());
        }
        // If the page is not the max page, then we need to put a next arrow button
        if (page < getMaximumPage()) {
            setSlot(NEXT_PAGE_SLOT_INDEX, getNextPageSlot());
        }
    }

    @Override
    protected void paintSkills(int page) {
        List<Skill> sortedSkills = getSortedSkillsForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < sortedSkills.size()) {
                setSlot(i, new SkillSlot(sortedSkills.get(i)));
            } else {
                removeSlot(i);
            }
        }
    }

    @Override
    @NotNull
    public Set<NamespacedKey> getUnsortedSkills() {
        return getCreatingPlayer().asSkillHolder().getSkills();
    }

    @Override
    public int getNavigationRowStartIndex() {
        return NAVIGATION_ROW_START_INDEX;
    }
}
