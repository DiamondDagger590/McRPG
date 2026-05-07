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

    @DisplayName("addAllowedItem makes item allowed; removeAllowedItem reverts it")
    @Test
    void addAndRemoveStaticItem() {
        CustomItemWrapper sword = new CustomItemWrapper(new ItemStack(Material.DIAMOND_SWORD));

        assertFalse(comboManager.isAllowedHeldItem(new ItemStack(Material.DIAMOND_SWORD)));

        comboManager.addAllowedItem(sword);
        assertTrue(comboManager.isAllowedHeldItem(new ItemStack(Material.DIAMOND_SWORD)));

        comboManager.removeAllowedItem(sword);
        assertFalse(comboManager.isAllowedHeldItem(new ItemStack(Material.DIAMOND_SWORD)));
    }

    @DisplayName("registerAllowedItemSet contributes its items to isAllowedHeldItem")
    @Test
    @SuppressWarnings("unchecked")
    void registerAllowedItemSet_contributesItems() {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        CustomItemWrapper axeWrapper = new CustomItemWrapper(axe);
        ReloadableSet<CustomItemWrapper> contributed = mock(ReloadableSet.class);
        when(contributed.getContent()).thenReturn(Set.of(axeWrapper));

        assertFalse(comboManager.isAllowedHeldItem(axe));

        comboManager.registerAllowedItemSet(contributed);
        assertTrue(comboManager.isAllowedHeldItem(axe));
    }

    @DisplayName("unregisterAllowedItemSet removes contributed items from isAllowedHeldItem")
    @Test
    @SuppressWarnings("unchecked")
    void unregisterAllowedItemSet_removesItems() {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        CustomItemWrapper axeWrapper = new CustomItemWrapper(axe);
        ReloadableSet<CustomItemWrapper> contributed = mock(ReloadableSet.class);
        when(contributed.getContent()).thenReturn(Set.of(axeWrapper));

        comboManager.registerAllowedItemSet(contributed);
        assertTrue(comboManager.isAllowedHeldItem(axe));

        comboManager.unregisterAllowedItemSet(contributed);
        assertFalse(comboManager.isAllowedHeldItem(axe));
    }

    @DisplayName("getAllowedItems returns merged snapshot including static and contributed items")
    @Test
    @SuppressWarnings("unchecked")
    void getAllowedItems_returnsMergedSnapshot() {
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

    @DisplayName("AIR is always allowed regardless of item lists")
    @Test
    void airIsAlwaysAllowed() {
        assertTrue(comboManager.isAllowedHeldItem(new ItemStack(Material.AIR)));
    }
}
