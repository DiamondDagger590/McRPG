package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.listener.ability.OnAbilityActivateListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityCooldownExpireListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityPutOnCooldownListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityUnlockListener;
import us.eunoians.mcrpg.listener.ability.OnAttackAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnBleedActivateListener;
import us.eunoians.mcrpg.listener.ability.OnBlockBreakListener;
import us.eunoians.mcrpg.listener.ability.OnBlockDropItemListener;
import us.eunoians.mcrpg.listener.ability.OnExtraOreActivateListener;
import us.eunoians.mcrpg.listener.ability.OnFoodLevelChangeAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnInteractAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnPlayerMoveAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnSneakAbilityListener;
import us.eunoians.mcrpg.listener.entity.EntitySpawnListener;
import us.eunoians.mcrpg.listener.entity.holder.OnAbilityHolderReadyListener;
import us.eunoians.mcrpg.listener.entity.holder.OnAbilityHolderUnreadyListener;
import us.eunoians.mcrpg.listener.entity.player.CorePlayerLoadListener;
import us.eunoians.mcrpg.listener.entity.player.CorePlayerUnloadListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerJoinListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerLeaveListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerPickupItemListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerSafeZoneStateChangeListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerSettingChangeListener;
import us.eunoians.mcrpg.listener.quest.QuestCompleteListener;
import us.eunoians.mcrpg.listener.quest.QuestObjectiveCompleteListener;
import us.eunoians.mcrpg.listener.skill.OnAttackLevelListener;
import us.eunoians.mcrpg.listener.skill.OnBlockBreakLevelListener;
import us.eunoians.mcrpg.listener.skill.OnSkillLevelUpListener;
import us.eunoians.mcrpg.listener.world.FakeBlockBreakListener;

/**
 * This registrar is in charge of registering {@link org.bukkit.event.Listener}s
 * for McRPG.
 */
final class McRPGListenerRegistrar implements Registrar<McRPG> {
    
    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        McRPG plugin = context.plugin();
        // Player load/save
        if (context.startupProfile() == StartupProfile.PROD) {
            Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
            Bukkit.getPluginManager().registerEvents(new PlayerLeaveListener(), plugin);
            Bukkit.getPluginManager().registerEvents(new CorePlayerLoadListener(), plugin);
            Bukkit.getPluginManager().registerEvents(new CorePlayerUnloadListener(), plugin);
        }

        // Ability activation/ready listeners
        Bukkit.getPluginManager().registerEvents(new OnAttackAbilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnBleedActivateListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnInteractAbilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnSneakAbilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnBlockBreakListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnBlockDropItemListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnExtraOreActivateListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnFoodLevelChangeAbilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnPlayerMoveAbilityListener(), plugin);

        // Skill listeners
        Bukkit.getPluginManager().registerEvents(new OnSkillLevelUpListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnAttackLevelListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnBlockBreakLevelListener(), plugin);

        // Ability listeners
        Bukkit.getPluginManager().registerEvents(new OnAbilityHolderReadyListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnAbilityHolderUnreadyListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnAbilityUnlockListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnAbilityCooldownExpireListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OnAbilityPutOnCooldownListener(), plugin);

        // Quest Listeners
        Bukkit.getPluginManager().registerEvents(new QuestCompleteListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new QuestObjectiveCompleteListener(), plugin);

        // World listener
        Bukkit.getPluginManager().registerEvents(new FakeBlockBreakListener(), plugin);
        CustomBlockData.registerListener(plugin);

        // Entity Listeners
        Bukkit.getPluginManager().registerEvents(new EntitySpawnListener(), plugin);

        // Debug Listener
        Bukkit.getPluginManager().registerEvents(new OnAbilityActivateListener(), plugin);

        // Setting listener
        Bukkit.getPluginManager().registerEvents(new PlayerSettingChangeListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerPickupItemListener(), plugin);

        // Safe zones
        Bukkit.getPluginManager().registerEvents(new PlayerSafeZoneStateChangeListener(), plugin);
    }
}
