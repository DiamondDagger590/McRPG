package us.eunoians.mcrpg.registry.manager;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import us.eunoians.mcrpg.ability.impl.swords.bleed.BleedManager;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.gui.McRPGGuiManager;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager;
import us.eunoians.mcrpg.world.WorldManager;
import us.eunoians.mcrpg.world.safezone.SafeZoneManager;

import static com.diamonddagger590.mccore.registry.manager.ManagerKeyImpl.create;

/**
 * A soft enum of all {@link ManagerKey}s supported by McRPG.
 * <p>
 * To use these, you will need access to the {@link com.diamonddagger590.mccore.registry.manager.ManagerRegistry}
 * via {@link com.diamonddagger590.mccore.registry.RegistryAccess#registry(RegistryKey)} while passing in
 * {@link RegistryKey#MANAGER}.
 */
public interface McRPGManagerKey<M> extends ManagerKey<M> {

    ManagerKey<McRPGPlayerManager> PLAYER = create(McRPGPlayerManager.class);
    ManagerKey<McRPGLocalizationManager> LOCALIZATION = create(McRPGLocalizationManager.class);
    ManagerKey<FileManager> FILE = create(FileManager.class);
    ManagerKey<McRPGGuiManager> GUI = create(McRPGGuiManager.class);
    ManagerKey<EntityManager> ENTITY = create(EntityManager.class);
    ManagerKey<DisplayManager> DISPLAY = create(DisplayManager.class);
    ManagerKey<SafeZoneManager> SAFE_ZONE = create(SafeZoneManager.class);
    ManagerKey<QuestManager> QUEST = create(QuestManager.class);
    ManagerKey<ContentExpansionManager> CONTENT_EXPANSION = create(ContentExpansionManager.class);
    ManagerKey<BleedManager> BLEED = create(BleedManager.class);
    ManagerKey<WorldManager> WORLD = create(WorldManager.class);
    ManagerKey<RestedExperienceManager> RESTED_EXPERIENCE = create(RestedExperienceManager.class);
}
