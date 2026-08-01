package us.eunoians.mcrpg.registry.manager;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import us.eunoians.mcrpg.ability.combo.ComboManager;
import us.eunoians.mcrpg.ability.impl.swords.bleed.BleedManager;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.log.CombatLogManager;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.external.glowing.GlowingManager;
import us.eunoians.mcrpg.gui.McRPGGuiManager;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager;
import us.eunoians.mcrpg.statistic.McRPGStatisticCacheManager;
import us.eunoians.mcrpg.world.WorldManager;

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
    ManagerKey<QuestManager> QUEST = create(QuestManager.class);
    ManagerKey<ContentExpansionManager> CONTENT_EXPANSION = create(ContentExpansionManager.class);
    ManagerKey<BleedManager> BLEED = create(BleedManager.class);
    ManagerKey<WorldManager> WORLD = create(WorldManager.class);
    ManagerKey<RestedExperienceManager> RESTED_EXPERIENCE = create(RestedExperienceManager.class);
    ManagerKey<McRPGDatabaseManager> DATABASE = create(McRPGDatabaseManager.class);
    ManagerKey<GlowingManager> GLOWING = create(GlowingManager.class);
    ManagerKey<QuestBoardManager> QUEST_BOARD = create(QuestBoardManager.class);
    ManagerKey<ComboManager> COMBO = create(ComboManager.class);
    /** Retrieves the {@link McRPGStatisticCacheManager} used to cache offline player statistic lookups. */
    ManagerKey<McRPGStatisticCacheManager> STATISTIC_CACHE = create(McRPGStatisticCacheManager.class);
    /**
     * Retrieves the {@link QuestChainManager} responsible for the full quest chain lifecycle:
     * starting, advancing, completing, abandoning, failing, restarting, and resetting chains.
     * <p>
     * All state-mutating operations run on the main Bukkit thread; DAO reads run on the database
     * executor thread and deliver results back to the main thread via the Bukkit scheduler.
     */
    ManagerKey<QuestChainManager> QUEST_CHAIN = create(QuestChainManager.class);
    /**
     * Retrieves the {@link CombatTrackerManager}, which owns active combat sessions and the public
     * combat API. All of its mutating methods must be called from the main server thread. To register
     * a combat condition at runtime, register it in the {@link McRPGRegistryKey#COMBAT_CONDITION}
     * registry and then call {@link CombatTrackerManager#startConditionTask} so it is polled.
     */
    ManagerKey<CombatTrackerManager> COMBAT_TRACKER = create(CombatTrackerManager.class);
    /**
     * Retrieves the {@link CombatLogManager}, which evaluates combat log detection, applies
     * punishments, and owns the shared {@link com.diamonddagger590.mccore.configuration.ReloadableContent}
     * for the combat log mode.
     */
    ManagerKey<CombatLogManager> COMBAT_LOG = create(CombatLogManager.class);
}
