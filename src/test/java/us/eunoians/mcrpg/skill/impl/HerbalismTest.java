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
import org.bukkit.block.data.Ageable;
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
import org.mockbukkit.mockbukkit.block.data.AgeableDataMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.exception.skill.EventNotRegisteredForLevelingException;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
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
public class HerbalismTest extends McRPGBaseTest {
    
    private Herbalism herbalism;
    private YamlDocument herbalismConfig;
    private YamlDocument mainConfig;

    @BeforeEach
    public void setup() {
        SkillRegistry skillRegistry = new SkillRegistry(mcRPG);
        RegistryAccess.registryAccess().register(skillRegistry);
        herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

        herbalismConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);

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

    @DisplayName("Given the Herbalism skill, when getting the YAML document, then it returns the Herbalism config")
    @Test
    public void getYamlDocument_returnsHerbalismConfig_forHerbalismSkill() {
        assertEquals(herbalismConfig, herbalism.getYamlDocument());
    }

    @DisplayName("Given the Herbalism skill, when getting the display item route, then it returns the Herbalism display item")
    @Test
    public void getDisplayItemRoute_returnsHerbalismDisplayItem_forHerbalismSkill() {
        assertEquals(LocalizationKey.HERBALISM_DISPLAY_ITEM, herbalism.getDisplayItemRoute());
    }

    @DisplayName("Given the Herbalism skill, when getting the plugin, then it returns McRPG")
    @Test
    public void getPlugin_returnsMcRPG_forHerbalismSkill() {
        assertEquals(mcRPG, herbalism.getPlugin());
    }

    @DisplayName("Given the Herbalism skill, when getting the database name, then it returns 'herbalism'")
    @Test
    public void getDatabaseName_returnsHerbalism_forHerbalismSkill() {
        assertEquals("herbalism", herbalism.getDatabaseName());
    }

    @DisplayName("Given a PlayerItemConsumeEvent, when checking canEventLevelSkill, then it returns false")
    @Test
    public void canEventLevelSkill_returnsFalse_forPlayerConsumeEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertFalse(herbalism.canEventLevelSkill(playerItemConsumeEvent));
    }

    @DisplayName("Given a PlayerItemConsumeEvent, when calculating experience, then it throws EventNotRegisteredForLevelingException")
    @Test
    public void calculateExperienceToGive_throwsEventNotRegisteredForLevelingException_forPlayerConsumeEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertThrows(EventNotRegisteredForLevelingException.class, () -> herbalism.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), playerItemConsumeEvent));
    }

    @DisplayName("Given an invalid block, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenBlockNotValid(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        block.setType(Material.DIRT);
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SHOVEL.createItemStack());
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(0, herbalism.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
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
        block.setType(Material.MELON);

        Route route = Route.fromString(toRoutePath(HerbalismConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(herbalismConfig.getInt(eq(route), anyInt())).thenReturn(5);
        when(herbalismConfig.contains(route)).thenReturn(true);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(0, herbalism.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given a fully grown crop, when checking canEventLevelSkill for block break, then it returns true")
    @Test
    public void canEventLevelSkill_returnsTrue_whenCropFullyGrown(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.CARROTS);
        Ageable ageable = (Ageable) block.getBlockData();
        ageable.setAge(ageable.getMaximumAge());
        block.setBlockData(ageable);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertTrue(herbalism.canEventLevelSkill(blockBreakEvent));
    }

    @DisplayName("Given a non fully grown crop, when checking canEventLevelSkill for block break, then it returns false")
    @Test
    public void canEventLevelSkill_returnsFalse_whenCropNotFullyGrown(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.CARROTS);
        Ageable ageable = (Ageable) block.getBlockData();
        ageable.setAge(0);
        block.setBlockData(ageable);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertTrue(herbalism.canEventLevelSkill(blockBreakEvent));
    }

    @DisplayName("Given a valid block with configured experience, when calculating experience, then it returns two")
    @Test
    public void calculateExperienceToGive_returnsTwo_whenValidEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.CARROTS);
        AgeableDataMock ageable = (AgeableDataMock) block.getBlockData();
        ageable.setAge(ageable.getMaximumAge());
        block.setBlockData(ageable);

        Route route = Route.fromString(toRoutePath(HerbalismConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(herbalismConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(herbalismConfig.contains(route)).thenReturn(true);

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(2, herbalism.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given a player in creative mode, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenPlayerInCreative(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.setGameMode(GameMode.CREATIVE);
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.CARROTS);
        Ageable ageable = (Ageable) block.getBlockData();
        ageable.setAge(ageable.getMaximumAge());
        block.setBlockData(ageable);
        Route route = Route.fromString(toRoutePath(HerbalismConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(herbalismConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(herbalismConfig.contains(route)).thenReturn(true);

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(0, herbalism.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given a non-natural block, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenBlockIsNotNatural(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.CARROTS);
        Ageable ageable = (Ageable) block.getBlockData();
        ageable.setAge(ageable.getMaximumAge());
        block.setBlockData(ageable);
        WorldManager worldManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
        doReturn(false).when(worldManager).isBlockNatural(block);

        Route route = Route.fromString(toRoutePath(HerbalismConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(herbalismConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(herbalismConfig.contains(route)).thenReturn(true);

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(0, herbalism.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @DisplayName("Given no configured material modifier, when getting held item bonus, then it returns zero")
    @Test
    public void getHeldItemBonus_returnsZero_whenNoConfiguredModifier() {
        ItemStack itemStack = ItemType.DIAMOND_HOE.createItemStack();
        assertEquals(0, herbalism.getHeldItemBonus(itemStack));
    }

    @DisplayName("Given a configured material modifier, when getting held item bonus, then it returns five")
    @Test
    public void getHeldItemBonus_returnsFive_whenConfiguredModifierPresent() {
        ItemStack itemStack = ItemType.DIAMOND_HOE.createItemStack();
        Route route = Route.fromString(toRoutePath(HerbalismConfigFile.MATERIAL_MODIFIERS_HEADER, itemStack.getType().toString()));
        when(herbalismConfig.getDouble(eq(route), anyDouble())).thenReturn(5d);
        assertEquals(5, herbalism .getHeldItemBonus(itemStack));
    }

    @DisplayName("Given a valid block with configured experience, when calculating experience, then it returns two")
    @Test
    public void calculateExperienceToGive_returnsSix_whenMultipleNaturalBlocks(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        // Make a four block tall cactus, we expect to only give experience for 3 tall.
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.CACTUS);
        setBlockToMaxAge(block);
        Block block2 = world.getBlockAt(0, 1, 0);
        block2.setType(Material.CACTUS);
        setBlockToMaxAge(block2);
        Block block3 = world.getBlockAt(0, 2, 0);
        block3.setType(Material.CACTUS);
        setBlockToMaxAge(block3);
        Block block4 = world.getBlockAt(0, 3, 0);
        block4.setType(Material.CACTUS);
        setBlockToMaxAge(block4);

        Route route = Route.fromString(toRoutePath(HerbalismConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(herbalismConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(herbalismConfig.contains(route)).thenReturn(true);

        WorldManager worldManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
        worldManager.setBlockPlacedState(block, false);
        worldManager.setBlockPlacedState(block2, false);
        worldManager.setBlockPlacedState(block3, false);
        worldManager.setBlockPlacedState(block4, false);

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        // Expect only rewarding experience for three cacti
        assertEquals(6, herbalism.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    @Test
    public void calculateExperienceToGive_returnsFour_whenOneBlockIsUnnatural(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        World world = server.getWorld("world");
        Block block = world.getBlockAt(0, 0, 0);
        block.setType(Material.CACTUS);
        setBlockToMaxAge(block);
        Block block2 = world.getBlockAt(0, 1, 0);
        block2.setType(Material.CACTUS);
        setBlockToMaxAge(block2);
        Block block3 = world.getBlockAt(0, 2, 0);
        block3.setType(Material.CACTUS);
        setBlockToMaxAge(block3);

        WorldManager worldManager = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
        worldManager.setBlockPlacedState(block3, true);

        Route route = Route.fromString(toRoutePath(HerbalismConfigFile.BLOCK_EXPERIENCE_HEADER, block.getType().toString()));
        when(herbalismConfig.getInt(eq(route), anyInt())).thenReturn(2);
        when(herbalismConfig.contains(route)).thenReturn(true);

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        assertEquals(4, herbalism.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), blockBreakEvent));
    }

    private void setBlockToMaxAge(@NotNull Block block) {
        Ageable ageable = (Ageable) block.getBlockData();
        ageable.setAge(ageable.getMaximumAge());
        block.setBlockData(ageable);
    }
}
