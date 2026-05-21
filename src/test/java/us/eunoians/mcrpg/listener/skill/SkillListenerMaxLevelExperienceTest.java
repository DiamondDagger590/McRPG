package us.eunoians.mcrpg.listener.skill;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import us.eunoians.mcrpg.configuration.file.skill.SkillConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistryExtension;
import us.eunoians.mcrpg.skill.experience.modifier.BoostedExperienceModifier;
import us.eunoians.mcrpg.skill.experience.modifier.RestedExperienceModifier;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.List;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Validates {@link SkillListener#levelSkill} behavior when a skill is already at maximum level.
 * Rested and boosted experience must not be consumed for silent overflow accumulation.
 */
@ExtendWith(ExperienceModifierRegistryExtension.class)
@ExtendWith(McRPGPlayerExtension.class)
class SkillListenerMaxLevelExperienceTest extends McRPGBaseTest {

    private static final int MAX_SKILL_LEVEL = 5;
    private static final float INITIAL_RESTED_EXPERIENCE = 0.5f;
    private static final int INITIAL_BOOSTED_EXPERIENCE = 500;

    private Swords swords;
    private YamlDocument swordsConfig;
    private YamlDocument mainConfig;
    private OnAttackLevelListener onAttackLevelListener;

    @BeforeEach
    void setUp() {
        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);
        swords = new Swords(mcRPG);
        skillRegistry.register(swords);

        swordsConfig = mock(YamlDocument.class);
        when(swordsConfig.getStringList(SwordsConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_SWORD"));
        when(swordsConfig.getInt(SkillConfigFile.MAXIMUM_SKILL_LEVEL)).thenReturn(MAX_SKILL_LEVEL);
        when(swordsConfig.getString(SkillConfigFile.LEVEL_UP_EQUATION)).thenReturn("100");

        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getStringList(MainConfigFile.DISABLED_WORLDS)).thenReturn(List.of());
        when(mainConfig.getDouble(MainConfigFile.MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE)).thenReturn(3d);
        when(mainConfig.getDouble(MainConfigFile.RESTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);
        when(mainConfig.getDouble(MainConfigFile.BOOSTED_EXPERIENCE_USAGE_RATE)).thenReturn(3.0);
        when(mainConfig.getDouble(MainConfigFile.EXPERIENCE_MULTIPLIER_LIMIT)).thenReturn(100d);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);
        WorldManager worldManager = spy(new WorldManager(mcRPG));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(worldManager);

        ExperienceModifierRegistry experienceModifierRegistry =
                RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        experienceModifierRegistry.register(new RestedExperienceModifier(mcRPG));
        experienceModifierRegistry.register(new BoostedExperienceModifier(mcRPG));

        EntityManager entityManager = new EntityManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(entityManager);

        onAttackLevelListener = new OnAttackLevelListener();
    }

    @Test
    @DisplayName("Given a skill at max level with rested and boosted XP, when leveling via attack, then bonus banks are not consumed")
    void levelSkill_atMaxLevel_doesNotConsumeRestedOrBoostedExperience(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);
        skillHolder.addSkillHolderDataAtLevel(swords, MAX_SKILL_LEVEL);

        mcRPGPlayer.getExperienceExtras().setRestedExperience(INITIAL_RESTED_EXPERIENCE);
        mcRPGPlayer.getExperienceExtras().setBoostedExperience(INITIAL_BOOSTED_EXPERIENCE);

        SkillHolder.SkillHolderData skillData = skillHolder.getSkillHolderData(swords).orElseThrow();
        int totalExperienceBefore = skillData.getTotalExperience();

        EntityDamageByEntityEvent damageEvent = buildValidSwordsDamageEvent(mcRPGPlayer);
        onAttackLevelListener.levelSkill(mcRPGPlayer.getUUID(), damageEvent);

        assertEquals(INITIAL_RESTED_EXPERIENCE, mcRPGPlayer.getExperienceExtras().getRestedExperience());
        assertEquals(INITIAL_BOOSTED_EXPERIENCE, mcRPGPlayer.getExperienceExtras().getBoostedExperience());
        assertEquals(MAX_SKILL_LEVEL, skillData.getCurrentLevel());
        assertEquals(totalExperienceBefore + 15, skillData.getTotalExperience());
    }

    @Test
    @DisplayName("Given a skill below max level with rested XP, when leveling via attack, then rested experience is consumed")
    void levelSkill_belowMaxLevel_consumesRestedExperience(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackAbilityHolder(skillHolder);
        skillHolder.addSkillHolderDataAtLevel(swords, 1);

        mcRPGPlayer.getExperienceExtras().setRestedExperience(INITIAL_RESTED_EXPERIENCE);
        mcRPGPlayer.getExperienceExtras().setBoostedExperience(INITIAL_BOOSTED_EXPERIENCE);

        EntityDamageByEntityEvent damageEvent = buildValidSwordsDamageEvent(mcRPGPlayer);
        onAttackLevelListener.levelSkill(mcRPGPlayer.getUUID(), damageEvent);

        assertEquals(0f, mcRPGPlayer.getExperienceExtras().getRestedExperience());
    }

    @NotNull
    private EntityDamageByEntityEvent buildValidSwordsDamageEvent(@NotNull McRPGPlayer mcRPGPlayer) {
        Skeleton skeleton = spawnEntity(Skeleton.class);
        Route entityRoute = Route.fromString(toRoutePath(SwordsConfigFile.ENTITY_EXPERIENCE_HEADER, skeleton.getType().toString()));
        when(swordsConfig.getInt(eq(entityRoute), anyInt())).thenReturn(5);
        when(swordsConfig.contains(entityRoute)).thenReturn(true);

        EntityDamageByEntityEvent entityDamageByEntityEvent = mock(EntityDamageByEntityEvent.class);
        Player player = mcRPGPlayer.getAsBukkitPlayer().orElseThrow();
        player.getEquipment().setItemInMainHand(ItemType.DIAMOND_SWORD.createItemStack());
        when(entityDamageByEntityEvent.getDamager()).thenReturn(player);
        when(entityDamageByEntityEvent.getEntity()).thenReturn(skeleton);
        when(entityDamageByEntityEvent.getFinalDamage()).thenReturn(3d);
        return entityDamageByEntityEvent;
    }
}
