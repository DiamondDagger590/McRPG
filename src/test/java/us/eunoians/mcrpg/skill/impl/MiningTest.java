package us.eunoians.mcrpg.skill.impl;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.exception.skill.EventNotRegisteredForLevelingException;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class MiningTest extends McRPGBaseTest {

    private Mining mining;
    private YamlDocument miningConfig;

    @BeforeEach
    public void setup() {
        SkillRegistry skillRegistry = new SkillRegistry(mcRPG);
        RegistryAccess.registryAccess().register(skillRegistry);
        mining = new Mining(mcRPG);
        skillRegistry.register(mining);

        miningConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(miningConfig);

        ExperienceModifierRegistry experienceModifierRegistry = new ExperienceModifierRegistry(mcRPG);
        RegistryAccess.registryAccess().register(experienceModifierRegistry);
    }

    @Test
    public void getYamlDocument_returnsMiningConfig_forMiningSkill() {
        assertEquals(miningConfig, mining.getYamlDocument());
    }

    @Test
    public void getDisplayItemRoute_returnsMiningDisplayItem_forMiningSkill() {
        assertEquals(LocalizationKey.MINING_DISPLAY_ITEM, mining.getDisplayItemRoute());
    }

    @Test
    public void getPlugin_returnsMcRPG_forMiningSkill() {
        assertEquals(mcRPG, mining.getPlugin());
    }

    @Test
    public void getDatabaseName_returnsMining_forMiningSkill() {
        assertEquals("mining", mining.getDatabaseName());
    }

    @Test
    public void canEventLevelSkill_returnsFalse_forPlayerConsumeEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertFalse(mining.canEventLevelSkill(playerItemConsumeEvent));
    }


    @Test
    public void calculateExperienceToGive_throwsEventNotRegisteredForLevelingException_forPlayerConsumeEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        PlayerItemConsumeEvent playerItemConsumeEvent = new PlayerItemConsumeEvent(mcRPGPlayer.getAsBukkitPlayer().get(),
                ItemType.BEEF.createItemStack(), EquipmentSlot.HAND);
        assertThrows(EventNotRegisteredForLevelingException.class, () -> mining.calculateExperienceToGive(mcRPGPlayer.asSkillHolder(), playerItemConsumeEvent));
    }


}
