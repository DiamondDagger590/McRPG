package us.eunoians.mcrpg.skill.impl;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.exception.skill.EventNotRegisteredForLevelingException;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.component.block.MultiBlockType;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.List;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class WoodCuttingTest extends McRPGBaseTest {

    private WoodCutting woodcutting;
    private YamlDocument woodCuttingConfig;
    private YamlDocument mainConfig;

    @BeforeEach
    public void setup() {
        SkillRegistry skillRegistry = new SkillRegistry(mcRPG);
        RegistryAccess.registryAccess().register(skillRegistry);
        woodcutting = new WoodCutting(mcRPG);
        skillRegistry.register(woodcutting);

        woodCuttingConfig = mock(YamlDocument.class);
        when(woodCuttingConfig.getStringList(WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_AXE"));
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.WOODCUTTING_CONFIG)).thenReturn(woodCuttingConfig);

        mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        ExperienceModifierRegistry experienceModifierRegistry = new ExperienceModifierRegistry(mcRPG);
        RegistryAccess.registryAccess().register(experienceModifierRegistry);
        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));
        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    @DisplayName("Given the Woodcutting skill, when getting the YAML document, then it returns the Woodcutting config")
    @Test
    public void getYamlDocument_returnsWoodCuttingConfig_forWoodCuttingSkill() {
        assertEquals(woodCuttingConfig, woodcutting.getYamlDocument());
    }

    @DisplayName("Given the Woodcutting skill, when getting the display item route, then it returns the Woodcutting display item")
    @Test
    public void getDisplayItemRoute_returnsWoodCuttingDisplayItem_forWoodCuttingSkill() {
        assertEquals(LocalizationKey.WOODCUTTING_DISPLAY_ITEM, woodcutting.getDisplayItemRoute());
    }

    @DisplayName("Given the Woodcutting skill, when getting the plugin, then it returns McRPG")
    @Test
    public void getPlugin_returnsMcRPG_forWoodCuttingSkill() {
        assertEquals(mcRPG, woodcutting.getPlugin());
    }

    @DisplayName("Given the Woodcutting skill, when getting the database name, then it returns 'woodcutting'")
    @Test
    public void getDatabaseName_returnsWoodCutting_forWoodCuttingSkill() {
        assertEquals("woodcutting", woodcutting.getDatabaseName());
    }

    @DisplayName("Given a PlayerItemConsumeEvent, when checking canEventLevelSkill, then it returns false")
    @Test
    public void canEventLevelSkill_returnsFalse_forPlayerConsumeEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertFalse(woodcutting.canEventLevelSkill(playerItemConsumeEvent));
    }

    @DisplayName("Given a PlayerItemConsumeEvent, when calculating experience to give, then it throws EventNotRegisteredForLevelingException")
    @Test
    public void calculateExperienceToGive_throwsEventNotRegisteredForLevelingException_forPlayerConsumeEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertThrows(EventNotRegisteredForLevelingException.class, () -> woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), playerItemConsumeEvent));
    }

    @DisplayName("Given an invalid block and non-axe, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenBlockNotValid(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        block.setType(Material.ACACIA_LEAVES);
        when(woodCuttingConfig.getStringList(WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_AXE"));
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(0, woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given a valid wood block and no axe in main hand, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenNoAxeInMainHand(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.OAK_LOG);

        Route route = Route.fromString(toRoutePath(WoodcuttingConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(woodCuttingConfig.getInt(eq(route), anyInt())).thenReturn(5);
        when(woodCuttingConfig.contains(route)).thenReturn(true);

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, mcRPGPlayer.getAsBukkitPlayer().get());
        assertEquals(0, woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given an axe in offhand, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenAxeInOffhand(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.STONE);

        Route route = Route.fromString(toRoutePath(WoodcuttingConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(woodCuttingConfig.getInt(eq(route), anyInt())).thenReturn(5);
        when(woodCuttingConfig.contains(route)).thenReturn(true);
        when(woodCuttingConfig.getStringList(WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_AXE"));

        player.getEquipment().setItemInOffHand(ItemType.DIAMOND_AXE.createItemStack());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(0, woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given a disabled world, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenWorldIsDisabled(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of("world"));
        RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.RELOADABLE_CONTENT).reloadAllContent();
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.STONE);

        Route route = Route.fromString(toRoutePath(WoodcuttingConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(woodCuttingConfig.getInt(eq(route), anyInt())).thenReturn(5);
        when(woodCuttingConfig.contains(route)).thenReturn(true);
        when(woodCuttingConfig.getStringList(WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_AXE"));

        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_AXE.createItemStack());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(0, woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given an axe in main hand, when checking canEventLevelSkill for block break, then it returns true")
    @Test
    public void canEventLevelSkill_returnsTrue_whenAxeInMainHand(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertTrue(woodcutting.canEventLevelSkill(blockBreakEvent));
    }

    @DisplayName("Given a valid block with configured experience, when calculating experience, then it returns two")
    @Test
    public void calculateExperienceToGive_returnsTwo_whenValidEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.addSimpleWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.STONE);

        Route route = Route.fromString(toRoutePath(WoodcuttingConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(woodCuttingConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(woodCuttingConfig.contains(route)).thenReturn(true);
        when(woodCuttingConfig.getStringList(WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_AXE"));

        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_AXE.createItemStack());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(2, woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given a player in creative mode, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenPlayerInCreative(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.setGameMode(GameMode.CREATIVE);
        World world = server.addSimpleWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.STONE);

        Route route = Route.fromString(toRoutePath(WoodcuttingConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(woodCuttingConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(woodCuttingConfig.contains(route)).thenReturn(true);
        when(woodCuttingConfig.getStringList(WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_AXE"));

        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_AXE.createItemStack());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(0, woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given bamboo below the max height, when calculating experience across multi-blocks, then it returns six")
    @Test
    public void calculateExperienceToGive_returnsSix_whenMultiBlocksLessThanMax(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.addSimpleWorld("small_bamboo");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.BAMBOO);

        for (int i = 1; i <= 2; i++) {
            Block targetBlock = world.getBlockAt(block.getX(), block.getY() + i, block.getZ());
            targetBlock.setType(Material.BAMBOO);
        }

        Route route = Route.fromString(toRoutePath(WoodcuttingConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(woodCuttingConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(woodCuttingConfig.contains(route)).thenReturn(true);
        when(woodCuttingConfig.getStringList(WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_AXE"));

        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_AXE.createItemStack());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(6, woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given bamboo above the max height, when calculating experience across multi-blocks, then it returns thirty-two")
    @Test
    public void calculateExperienceToGive_returnsThirtyTwo_whenMultiBlocksMoreThanMax(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.addSimpleWorld("big_bamboo");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.BAMBOO);

        for (int i = 1; i <= MultiBlockType.BAMBOO.getMaxHeight() + 1; i++) {
            Block targetBlock = world.getBlockAt(block.getX(), block.getY() + i, block.getZ());
            targetBlock.setType(Material.BAMBOO);
        }

        Route route = Route.fromString(toRoutePath(WoodcuttingConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(woodCuttingConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(woodCuttingConfig.contains(route)).thenReturn(true);
        when(woodCuttingConfig.getStringList(WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_AXE"));

        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_AXE.createItemStack());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(32, woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given a non-natural block, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenBlockIsNotNatural(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.STONE);
        WorldManager worldManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
        doReturn(false).when(worldManager).isBlockNatural(block);

        Route route = Route.fromString(toRoutePath(WoodcuttingConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(woodCuttingConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(woodCuttingConfig.contains(route)).thenReturn(true);
        when(woodCuttingConfig.getStringList(WoodcuttingConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_AXE"));

        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_AXE.createItemStack());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(0, woodcutting.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given no configured material modifier, when getting held item bonus, then it returns zero")
    @Test
    public void getHeldItemBonus_returnsZero_whenNoConfiguredModifier() {
        ItemStack itemStack = ItemType.DIAMOND_AXE.createItemStack();
        assertEquals(0, woodcutting.getHeldItemBonus(itemStack));
    }

    @DisplayName("Given a configured material modifier, when getting held item bonus, then it returns five")
    @Test
    public void getHeldItemBonus_returnsFive_whenConfiguredModifierPresent() {
        ItemStack itemStack = ItemType.DIAMOND_AXE.createItemStack();
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.MATERIAL_MODIFIERS_HEADER, itemStack.getType().toString()));
        when(woodCuttingConfig.getDouble(eq(route), anyDouble())).thenReturn(5d);
        assertEquals(5, woodcutting.getHeldItemBonus(itemStack));
    }
}
