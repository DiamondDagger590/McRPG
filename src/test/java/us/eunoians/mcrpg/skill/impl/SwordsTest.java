package us.eunoians.mcrpg.skill.impl;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
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
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class SwordsTest extends McRPGBaseTest {

    private Swords swords;
    private YamlDocument swordsConfig;

    @BeforeEach
    public void setup() {
        SkillRegistry skillRegistry = new SkillRegistry(mcRPG);
        RegistryAccess.registryAccess().register(skillRegistry);
        swords = new Swords(mcRPG);
        skillRegistry.register(swords);

        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        ExperienceModifierRegistry experienceModifierRegistry = new ExperienceModifierRegistry(mcRPG);
        RegistryAccess.registryAccess().register(experienceModifierRegistry);
    }

    @Test
    public void getPlugin_returnsMcRPGInstance_forSwordsSkill() {
        assertEquals(mcRPG, swords.getPlugin());
    }

    @Test
    public void getYamlDocument_returnsSwordsConfig_forSwordsSkill() {
        assertEquals(swordsConfig, swords.getYamlDocument());
    }

    @Test
    public void getDisplayItemRoute_returnsSwordsDisplayItemRoute_forSwordsSkill() {
        assertEquals(LocalizationKey.SWORDS_DISPLAY_ITEM, swords.getDisplayItemRoute());
    }

    @Test
    public void getDatabaseName_returnsSwords_forSwordsSkill() {
        assertEquals("swords", swords.getDatabaseName());
    }

    @Test
    public void canEventLevelSkill_returnsFalse_forPlayerConsumeEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertFalse(swords.canEventLevelSkill(playerItemConsumeEvent));
    }

    @Test
    public void calculateExperienceToGive_throwsEventNotRegisteredForLevelingException_forPlayerConsumeEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertThrows(EventNotRegisteredForLevelingException.class, () -> swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), playerItemConsumeEvent));
    }

    @Test
    public void calculateExperienceToGive_returnsZero_whenDamageEventIsNotLivingEntity(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(mock(Entity.class));
        assertEquals(0, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @Test
    public void calculateExperienceToGive_returnsZero_whenPlayerNotHoldingSword(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(mock(LivingEntity.class));
        assertEquals(0, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @Test
    public void calculateExperienceToGive_returnsZero_whenPlayerHoldingSwordInOffhand(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.getEquipment().setItemInOffHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(mock(LivingEntity.class));
        assertEquals(0, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @Test
    public void canEventLevelSkill_returnsTrue_whenPlayerHoldingSwordInMainHand(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(mock(LivingEntity.class));
        assertTrue(swords.canEventLevelSkill(entityDamageByEntityEvent));
    }

    @Test
    public void calculateExperienceToGive_returnsZero_whenDamageEventInvalid(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(mock(LivingEntity.class));
        assertEquals(0, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @Test
    public void calculateExperienceToGive_returnsFifteen_whenDamageEventIsValid(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        World world = server.getWorld("world");
        Skeleton skeleton = world.spawn(new Location(world, 0, 0, 0), Skeleton.class);
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.ENTITY_EXPERIENCE_HEADER, skeleton.getType().toString()));
        when(swordsConfig.getInt(eq(route), anyInt())).thenReturn(5);
        YamlDocument mainConfig = mock(YamlDocument.class);
        when(mainConfig.getDouble(MainConfigFile.MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE)).thenReturn(3d);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(skeleton);
        when(entityDamageByEntityEvent.getFinalDamage()).thenReturn(3d);

        assertEquals(15, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @Test
    public void calculateExperienceToGive_returnsFifteen_whenDamageEventIsValidAndDamageIsAboveMax(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        World world = server.getWorld("world");
        Skeleton skeleton = world.spawn(new Location(world, 0, 0, 0), Skeleton.class);
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.ENTITY_EXPERIENCE_HEADER, skeleton.getType().toString()));
        when(swordsConfig.getInt(eq(route), anyInt())).thenReturn(5);
        YamlDocument mainConfig = mock(YamlDocument.class);
        when(mainConfig.getDouble(MainConfigFile.MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE)).thenReturn(3d);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().get();
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(skeleton);
        when(entityDamageByEntityEvent.getFinalDamage()).thenReturn(10d);

        assertEquals(15, swords.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), entityDamageByEntityEvent));
    }

    @Test
    public void getHeldItemBonus_returnsZero_forNoValidItem(@NotNull McRPGPlayer mcRPGPlayer) {
        ItemStack itemStack = ItemType.DIAMOND_SWORD.createItemStack();
        assertEquals(0, swords.getHeldItemBonus(itemStack));
    }

    @Test
    public void getHeldItemBonus_returnsFive_forValidItem(@NotNull McRPGPlayer mcRPGPlayer) {
        ItemStack itemStack = ItemType.DIAMOND_SWORD.createItemStack();
        Route route = Route.fromString(toRoutePath(SwordsConfigFile.MATERIAL_MODIFIERS_HEADER, itemStack.getType().toString()));
        when(swordsConfig.getDouble(eq(route), anyDouble())).thenReturn(5d);
        assertEquals(5, swords.getHeldItemBonus(itemStack));
    }

}
