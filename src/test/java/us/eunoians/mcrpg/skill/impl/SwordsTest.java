package us.eunoians.mcrpg.skill.impl;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.exception.skill.EventNotRegisteredForLevelingException;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class SwordsTest extends McRPGBaseTest {

    private Swords swords;
    private YamlDocument swordsConfig;
    private YamlDocument mainConfig;

    @BeforeEach
    public void setup() {
        SkillRegistry skillRegistry = new SkillRegistry(mcRPG);
        RegistryAccess.registryAccess().register(skillRegistry);
        swords = new Swords(mcRPG);
        skillRegistry.register(swords);

        swordsConfig = mock(YamlDocument.class);
        when(swordsConfig.getStringList(SwordsConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_SWORD"));
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        ExperienceModifierRegistry experienceModifierRegistry = new ExperienceModifierRegistry(mcRPG);
        RegistryAccess.registryAccess().register(experienceModifierRegistry);
        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);
        WorldManager worldManager = spy(new WorldManager(mcRPG));
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);
    }

    @DisplayName("Given Swords skill, when calling getPlugin, then it returns the McRPG instance")
    @Test
    public void getPlugin_returnsMcRPG_whenSwordsSkill() {
        assertEquals(mcRPG, swords.getPlugin());
    }

    @DisplayName("Given Swords skill, when requesting its YAML document, then it returns the Swords config")
    @Test
    public void getYamlDocument_returnsSwordsConfig_whenSwordsSkill() {
        assertEquals(swordsConfig, swords.getYamlDocument());
    }

    @DisplayName("Given Swords skill, when requesting display item route, then it returns SWORDS_DISPLAY_ITEM")
    @Test
    public void getDisplayItemRoute_returnsSwordsDisplayItem_whenSwordsSkill() {
        assertEquals(LocalizationKey.SWORDS_DISPLAY_ITEM, swords.getDisplayItemRoute());
    }

    @DisplayName("Given Swords skill, when requesting database name, then it returns \"swords\"")
    @Test
    public void getDatabaseName_returnsSwords_whenSwordsSkill() {
        assertEquals("swords", swords.getDatabaseName());
    }

    @DisplayName("Given a PlayerItemConsumeEvent, when checking canEventLevelSkill, then it returns false")
    @Test
    public void canEventLevelSkill_returnsFalse_whenPlayerConsumesItem(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertFalse(swords.canEventLevelSkill(playerItemConsumeEvent));
    }

    @DisplayName("Given a PlayerItemConsumeEvent, when calculating experience, then it throws EventNotRegisteredForLevelingException")
    @Test
    public void calculateExperienceToGive_throwsNotRegistered_whenPlayerConsumesItem(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertThrows(EventNotRegisteredForLevelingException.class, () -> swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), playerItemConsumeEvent));
    }

    @DisplayName("Given damage event with not-allowed target entity, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenTargetNotAllowedEntity(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(spawnEntity(Zombie.class));
        when(swordsConfig.getStringList(SwordsConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_SWORD"));
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        assertEquals(0, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @DisplayName("Given damage event and no sword in main hand, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenNoSwordInMainHand(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Skeleton skeleton = spawnEntity(Skeleton.class);
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.ENTITY_EXPERIENCE_HEADER, skeleton.getType().toString()));
        when(swordsConfig.getInt(eq(route), anyInt())).thenReturn(5);
        when(swordsConfig.contains(route)).thenReturn(true);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(skeleton);
        assertEquals(0, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @DisplayName("Given damage event and sword only in offhand, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenSwordInOffhand(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.getEquipment().setItemInOffHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(spawnEntity(Skeleton.class));
        assertEquals(0, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @DisplayName("Given damage event and disabled world, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenWorldIsDisabled(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Skeleton skeleton = spawnEntity(Skeleton.class);
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.ENTITY_EXPERIENCE_HEADER, skeleton.getType().toString()));
        when(swordsConfig.getInt(eq(route), anyInt())).thenReturn(5);
        when(swordsConfig.contains(route)).thenReturn(true);
        when(swordsConfig.getStringList(SwordsConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_SWORD"));
        when(mainConfig.getDouble(MainConfigFile.MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE)).thenReturn(3d);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of("world"));
        RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.RELOADABLE_CONTENT).reloadAllContent();
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(skeleton);
        when(entityDamageByEntityEvent.getFinalDamage()).thenReturn(3d);
        assertEquals(0, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @DisplayName("Given damage event and sword in main hand, when checking canEventLevelSkill, then it returns true")
    @Test
    public void canEventLevelSkill_returnsTrue_whenSwordInMainHand(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(spawnEntity(Skeleton.class));
        assertTrue(swords.canEventLevelSkill(entityDamageByEntityEvent));
    }

    @DisplayName("Given valid damage event at max-cap, when calculating experience, then it returns fifteen")
    @Test
    public void calculateExperienceToGive_returnsFifteen_whenValidDamageAtCap(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        World world = server.getWorld("world");
        Skeleton skeleton = spawnEntity(Skeleton.class);
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.ENTITY_EXPERIENCE_HEADER, skeleton.getType().toString()));
        when(swordsConfig.getInt(eq(route), anyInt())).thenReturn(5);
        when(swordsConfig.contains(route)).thenReturn(true);
        when(swordsConfig.getStringList(SwordsConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_SWORD"));
        when(mainConfig.getDouble(MainConfigFile.MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE)).thenReturn(3d);

        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(skeleton);
        when(entityDamageByEntityEvent.getFinalDamage()).thenReturn(3d);

        assertEquals(15, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @DisplayName("Given valid damage event above max-cap, when calculating experience, then it returns fifteen")
    @Test
    public void calculateExperienceToGive_returnsFifteen_whenValidDamageAboveCap(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        World world = server.getWorld("world");
        Skeleton skeleton = spawnEntity(Skeleton.class);
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.ENTITY_EXPERIENCE_HEADER, skeleton.getType().toString()));
        when(swordsConfig.getInt(eq(route), anyInt())).thenReturn(5);
        when(swordsConfig.contains(route)).thenReturn(true);
        when(swordsConfig.getStringList(SwordsConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_SWORD"));
        when(mainConfig.getDouble(MainConfigFile.MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE)).thenReturn(3d);

        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(skeleton);
        when(entityDamageByEntityEvent.getFinalDamage()).thenReturn(10d);

        assertEquals(15, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @DisplayName("Given a player in creative mode, when calculating experience, then it returns zero")
    @Test
    public void calculateExperienceToGive_returnsZero_whenPlayerInCreative(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        World world = server.getWorld("world");
        Skeleton skeleton = spawnEntity(Skeleton.class);
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.ENTITY_EXPERIENCE_HEADER, skeleton.getType().toString()));
        when(swordsConfig.getInt(eq(route), anyInt())).thenReturn(5);
        when(swordsConfig.contains(route)).thenReturn(true);
        when(swordsConfig.getStringList(SwordsConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_SWORD"));
        when(mainConfig.getDouble(MainConfigFile.MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE)).thenReturn(3d);

        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.setGameMode(GameMode.CREATIVE);
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(skeleton);
        when(entityDamageByEntityEvent.getFinalDamage()).thenReturn(10d);

        assertEquals(0, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @DisplayName("Given no configured material modifier, when getting held item bonus, then it returns zero")
    @Test
    public void getHeldItemBonus_returnsZero_whenNoConfiguredModifier() {
        ItemStack itemStack = ItemType.DIAMOND_SWORD.createItemStack();
        assertEquals(0, swords.getHeldItemBonus(itemStack));
    }

    @DisplayName("Given configured material modifier for item, when getting held item bonus, then it returns five")
    @Test
    public void getHeldItemBonus_returnsFive_whenConfiguredModifierPresent() {
        ItemStack itemStack = ItemType.DIAMOND_SWORD.createItemStack();
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.MATERIAL_MODIFIERS_HEADER, itemStack.getType().toString()));
        when(swordsConfig.getDouble(eq(route), anyDouble())).thenReturn(5d);
        assertEquals(5, swords.getHeldItemBonus(itemStack));
    }
}
