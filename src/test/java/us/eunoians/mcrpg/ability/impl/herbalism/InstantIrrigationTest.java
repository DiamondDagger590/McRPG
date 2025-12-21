package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.ability.herbalism.InstantIrrigationActivateEvent;
import us.eunoians.mcrpg.listener.ability.OnBlockBreakListener;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.world.WorldManager;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class InstantIrrigationTest extends McRPGBaseTest {

    private Herbalism herbalism;
    private InstantIrrigation instantIrrigation;
    private YamlDocument herbalismConfig;
    private YamlDocument mainConfig;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        SkillRegistry skillRegistry = new SkillRegistry(mcRPG);
        RegistryAccess.registryAccess().register(skillRegistry);
        herbalism = new Herbalism(mcRPG);
        skillRegistry.register(herbalism);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        instantIrrigation = new InstantIrrigation(mcRPG);
        abilityRegistry.register(instantIrrigation);

        herbalismConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        when(herbalismConfig.getString(HerbalismConfigFile.LEVEL_UP_EQUATION)).thenReturn("5");

        mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_AMOUNT)).thenReturn(5);
        when(mainConfig.getInt(MainConfigFile.MAX_LOADOUT_SIZE)).thenReturn(5);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);
        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of(""));
        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);

        server.getPluginManager().registerEvents(new OnBlockBreakListener(), mcRPG);
    }

    @Test
    public void getAbilityKey_returnsHerbalismKey() {
        assertEquals(instantIrrigation.getSkillKey(), herbalism.getSkillKey());
    }

    @Test
    public void abilityActivation_succeeds_forValidSetup(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_HOE.createItemStack());

        Instant instant = Instant.now();
        TimeProvider timeProvider = mcRPG.getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);
        skillHolder.addSkillHolderData(herbalism, 1);
        skillHolder.addAvailableAbility(instantIrrigation);

        Loadout loadout = new Loadout(mcRPGPlayer.getUUID(), 1, Set.of(instantIrrigation.getAbilityKey()));
        skillHolder.setLoadout(loadout);

        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component message = mcRPG.getMiniMessage().deserialize("You activated the ability.");
        when(localizationManager.getLocalizedMessageAsComponent(eq(mcRPGPlayer), eq(LocalizationKey.INSTANT_IRRIGATION_ACTIVATION_NOTIFICATION) , any())).thenReturn(message);

        when(herbalismConfig.getBoolean(HerbalismConfigFile.INSTANT_IRRIGATION_ENABLED)).thenReturn(true);
        when(herbalismConfig.getBoolean(HerbalismConfigFile.SKILL_ENABLED)).thenReturn(true);
        when(herbalismConfig.getString(HerbalismConfigFile.INSTANT_IRRIGATION_COOLDOWN)).thenReturn("120-(herbalism_level/10)");

        Block block = server.getWorld("world").getBlockAt(0, 0, 0);
        block.setType(Material.GRASS_BLOCK);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, mcRPGPlayer.getAsBukkitPlayer().get());
        server.getPluginManager().callEvent(blockBreakEvent);

        assertEquals(Material.WATER, block.getType());
        assertEquals(message, player.nextComponentMessage());
        server.getPluginManager().assertEventFired(InstantIrrigationActivateEvent.class);
        assertEquals(instant.toEpochMilli() + (119 * 1000), ((long) skillHolder.getAbilityData(instantIrrigation).get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY).get().getContent()));
    }

    @Test
    public void abilityActivation_hasCorrectCooldown_forHigherSkillLevel(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_HOE.createItemStack());

        Instant instant = Instant.now();
        TimeProvider timeProvider = mcRPG.getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);
        skillHolder.addSkillHolderData(herbalism, 100);
        skillHolder.addAvailableAbility(instantIrrigation);

        Loadout loadout = new Loadout(mcRPGPlayer.getUUID(), 1, Set.of(instantIrrigation.getAbilityKey()));
        skillHolder.setLoadout(loadout);

        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component message = mcRPG.getMiniMessage().deserialize("You activated the ability.");
        when(localizationManager.getLocalizedMessageAsComponent(eq(mcRPGPlayer), eq(LocalizationKey.INSTANT_IRRIGATION_ACTIVATION_NOTIFICATION) , any())).thenReturn(message);

        when(herbalismConfig.getBoolean(HerbalismConfigFile.INSTANT_IRRIGATION_ENABLED)).thenReturn(true);
        when(herbalismConfig.getBoolean(HerbalismConfigFile.SKILL_ENABLED)).thenReturn(true);
        when(herbalismConfig.getString(HerbalismConfigFile.INSTANT_IRRIGATION_COOLDOWN)).thenReturn("120-(herbalism_level/10)");

        Block block = server.getWorld("world").getBlockAt(0, 0, 0);
        block.setType(Material.GRASS_BLOCK);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, mcRPGPlayer.getAsBukkitPlayer().get());
        server.getPluginManager().callEvent(blockBreakEvent);

        assertEquals(Material.WATER, block.getType());
        assertEquals(message, player.nextComponentMessage());
        server.getPluginManager().assertEventFired(InstantIrrigationActivateEvent.class);
        assertEquals(instant.toEpochMilli() + (110 * 1000), ((long) skillHolder.getAbilityData(instantIrrigation).get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY).get().getContent()));
    }

    @Test
    public void abilityActivation_fails_whileNotHoldingHoe(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);
        skillHolder.addSkillHolderData(herbalism, 1);
        skillHolder.addAvailableAbility(instantIrrigation);

        Loadout loadout = new Loadout(mcRPGPlayer.getUUID(), 1, Set.of(instantIrrigation.getAbilityKey()));
        skillHolder.setLoadout(loadout);

        when(herbalismConfig.getBoolean(HerbalismConfigFile.INSTANT_IRRIGATION_ENABLED)).thenReturn(true);
        when(herbalismConfig.getBoolean(HerbalismConfigFile.SKILL_ENABLED)).thenReturn(true);
        when(herbalismConfig.getString(HerbalismConfigFile.INSTANT_IRRIGATION_COOLDOWN)).thenReturn("120-(level/10)");

        Block block = server.getWorld("world").getBlockAt(0, 0, 0);
        block.setType(Material.GRASS_BLOCK);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, mcRPGPlayer.getAsBukkitPlayer().get());
        server.getPluginManager().callEvent(blockBreakEvent);

        assertEquals(Material.GRASS_BLOCK, block.getType());
        server.getPluginManager().assertEventNotFired(InstantIrrigationActivateEvent.class);
        assertNull(player.nextComponentMessage());
        assertEquals(0, ((long) skillHolder.getAbilityData(instantIrrigation).get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY).get().getContent()));
    }

    @Test
    public void abilityActivation_fails_forDisabledAbility(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_HOE.createItemStack());

        Instant instant = Instant.now();
        TimeProvider timeProvider = mcRPG.getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);
        skillHolder.addSkillHolderData(herbalism, 1);
        skillHolder.addAvailableAbility(instantIrrigation);

        Loadout loadout = new Loadout(mcRPGPlayer.getUUID(), 1, Set.of(instantIrrigation.getAbilityKey()));
        skillHolder.setLoadout(loadout);

        when(herbalismConfig.getBoolean(HerbalismConfigFile.INSTANT_IRRIGATION_ENABLED)).thenReturn(false);
        when(herbalismConfig.getBoolean(HerbalismConfigFile.SKILL_ENABLED)).thenReturn(true);
        when(herbalismConfig.getString(HerbalismConfigFile.INSTANT_IRRIGATION_COOLDOWN)).thenReturn("120-(level/10)");

        Block block = server.getWorld("world").getBlockAt(0, 0, 0);
        block.setType(Material.GRASS_BLOCK);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, mcRPGPlayer.getAsBukkitPlayer().get());
        server.getPluginManager().callEvent(blockBreakEvent);

        assertEquals(Material.GRASS_BLOCK, block.getType());
        server.getPluginManager().assertEventNotFired(InstantIrrigationActivateEvent.class);
        assertNull(player.nextComponentMessage());
        assertEquals(0, ((long) skillHolder.getAbilityData(instantIrrigation).get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY).get().getContent()));
    }

    @Test
    public void abilityActivation_fails_forDisabledSkill(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_HOE.createItemStack());

        Instant instant = Instant.now();
        TimeProvider timeProvider = mcRPG.getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);
        skillHolder.addSkillHolderData(herbalism, 1);
        skillHolder.addAvailableAbility(instantIrrigation);

        Loadout loadout = new Loadout(mcRPGPlayer.getUUID(), 1, Set.of(instantIrrigation.getAbilityKey()));
        skillHolder.setLoadout(loadout);

        when(herbalismConfig.getBoolean(HerbalismConfigFile.INSTANT_IRRIGATION_ENABLED)).thenReturn(true);
        when(herbalismConfig.getBoolean(HerbalismConfigFile.SKILL_ENABLED)).thenReturn(false);
        when(herbalismConfig.getString(HerbalismConfigFile.INSTANT_IRRIGATION_COOLDOWN)).thenReturn("120-(level/10)");

        Block block = server.getWorld("world").getBlockAt(0, 0, 0);
        block.setType(Material.GRASS_BLOCK);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, mcRPGPlayer.getAsBukkitPlayer().get());
        server.getPluginManager().callEvent(blockBreakEvent);

        assertEquals(Material.GRASS_BLOCK, block.getType());
        server.getPluginManager().assertEventNotFired(InstantIrrigationActivateEvent.class);
        assertNull(player.nextComponentMessage());
        assertEquals(0, ((long) skillHolder.getAbilityData(instantIrrigation).get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY).get().getContent()));
    }

    @Test
    public void abilityActivation_fails_forDisabledWorld(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_HOE.createItemStack());

        Instant instant = Instant.now();
        TimeProvider timeProvider = mcRPG.getTimeProvider();
        when(timeProvider.now()).thenReturn(instant);

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);
        skillHolder.addSkillHolderData(herbalism, 1);
        skillHolder.addAvailableAbility(instantIrrigation);

        Loadout loadout = new Loadout(mcRPGPlayer.getUUID(), 1, Set.of(instantIrrigation.getAbilityKey()));
        skillHolder.setLoadout(loadout);

        when(herbalismConfig.getBoolean(HerbalismConfigFile.INSTANT_IRRIGATION_ENABLED)).thenReturn(true);
        when(herbalismConfig.getBoolean(HerbalismConfigFile.SKILL_ENABLED)).thenReturn(true);
        when(herbalismConfig.getString(HerbalismConfigFile.INSTANT_IRRIGATION_COOLDOWN)).thenReturn("120-(level/10)");

        WorldManager worldManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
        when(worldManager.isWorldDisabled(server.getWorld("world"))).thenReturn(true);
        Block block = server.getWorld("world").getBlockAt(0, 0, 0);
        block.setType(Material.GRASS_BLOCK);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, mcRPGPlayer.getAsBukkitPlayer().get());
        server.getPluginManager().callEvent(blockBreakEvent);

        assertEquals(Material.GRASS_BLOCK, block.getType());
        server.getPluginManager().assertEventNotFired(InstantIrrigationActivateEvent.class);
        assertNull(player.nextComponentMessage());
        assertEquals(0, ((long) skillHolder.getAbilityData(instantIrrigation).get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY).get().getContent()));
    }


}
