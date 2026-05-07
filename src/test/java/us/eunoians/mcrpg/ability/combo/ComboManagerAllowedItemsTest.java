package us.eunoians.mcrpg.ability.combo;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.configuration.collection.ReloadableSet;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the third-party allowed-item extension API on {@link ComboManager}:
 * {@link ComboManager#addAllowedItem}, {@link ComboManager#removeAllowedItem},
 * {@link ComboManager#registerAllowedItemSet}, {@link ComboManager#unregisterAllowedItemSet},
 * and the merged snapshot returned by {@link ComboManager#getAllowedItems()}.
 */
class ComboManagerAllowedItemsTest extends McRPGBaseTest {

    private ComboManager comboManager;

    @BeforeEach
    void setUp() {
        // ReloadableContentManager is required by ComboManager's constructor to track the built-in set.
        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        // Return an empty list for the built-in allowed-items config key so the built-in list starts empty.
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        YamlDocument mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getStringList(MainConfigFile.COMBO_ALLOWED_ITEMS)).thenReturn(List.of());

        comboManager = new ComboManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(comboManager);
    }

    @Test
    @DisplayName("Given an item added via addAllowedItem, when isAllowedHeldItem is checked, then it returns true; after removeAllowedItem it returns false")
    void addAllowedItem_makesItemAllowed_andRemoveReverts() {
        CustomItemWrapper sword = new CustomItemWrapper(new ItemStack(Material.DIAMOND_SWORD));

        assertFalse(comboManager.isAllowedHeldItem(new ItemStack(Material.DIAMOND_SWORD)));

        comboManager.addAllowedItem(sword);
        assertTrue(comboManager.isAllowedHeldItem(new ItemStack(Material.DIAMOND_SWORD)));

        comboManager.removeAllowedItem(sword);
        assertFalse(comboManager.isAllowedHeldItem(new ItemStack(Material.DIAMOND_SWORD)));
    }

    @Test
    @DisplayName("Given a registered item set, when isAllowedHeldItem is checked for a set item, then it returns true")
    @SuppressWarnings("unchecked")
    void registerAllowedItemSet_contributesItemsToAllowedCheck() {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        CustomItemWrapper axeWrapper = new CustomItemWrapper(axe);
        ReloadableSet<CustomItemWrapper> contributed = mock(ReloadableSet.class);
        when(contributed.getContent()).thenReturn(Set.of(axeWrapper));

        assertFalse(comboManager.isAllowedHeldItem(axe));

        comboManager.registerAllowedItemSet(contributed);
        assertTrue(comboManager.isAllowedHeldItem(axe));
    }

    @Test
    @DisplayName("Given a registered then unregistered item set, when isAllowedHeldItem is checked for a set item, then it returns false")
    @SuppressWarnings("unchecked")
    void unregisterAllowedItemSet_removesItemsFromAllowedCheck() {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        CustomItemWrapper axeWrapper = new CustomItemWrapper(axe);
        ReloadableSet<CustomItemWrapper> contributed = mock(ReloadableSet.class);
        when(contributed.getContent()).thenReturn(Set.of(axeWrapper));

        comboManager.registerAllowedItemSet(contributed);
        assertTrue(comboManager.isAllowedHeldItem(axe));

        comboManager.unregisterAllowedItemSet(contributed);
        assertFalse(comboManager.isAllowedHeldItem(axe));
    }

    @Test
    @DisplayName("Given static and contributed items, when getAllowedItems is called, then it returns a merged snapshot of both")
    @SuppressWarnings("unchecked")
    void getAllowedItems_returnsMergedView_ofStaticAndContributedItems() {
        CustomItemWrapper sword = new CustomItemWrapper(new ItemStack(Material.DIAMOND_SWORD));
        CustomItemWrapper axe = new CustomItemWrapper(new ItemStack(Material.DIAMOND_AXE));

        ReloadableSet<CustomItemWrapper> contributed = mock(ReloadableSet.class);
        when(contributed.getContent()).thenReturn(Set.of(axe));

        comboManager.addAllowedItem(sword);
        comboManager.registerAllowedItemSet(contributed);

        Set<CustomItemWrapper> all = comboManager.getAllowedItems();
        assertTrue(all.contains(sword));
        assertTrue(all.contains(axe));
    }

    @Test
    @DisplayName("When isAllowedHeldItem is called with AIR, then it always returns true")
    void isAllowedHeldItem_returnsTrue_forAir() {
        assertTrue(comboManager.isAllowedHeldItem(new ItemStack(Material.AIR)));
    }
}
