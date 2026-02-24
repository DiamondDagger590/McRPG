package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MobKillQuestProgressListenerTest extends McRPGBaseTest {

    private QuestManager mockQuestManager;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        when(mockQuestManager.getActiveQuestsForPlayer(any(UUID.class))).thenReturn(List.of());
        server.getPluginManager().registerEvents(new MobKillQuestProgressListener(), mcRPG);
    }

    @DisplayName("Given an entity death with a player killer, when fired, then progressQuests queries active quests")
    @Test
    public void onEntityDeath_playerKiller_callsProgressQuests() {
        PlayerMock killer = new PlayerMock(server, "Slayer");
        server.addPlayer(killer);

        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getKiller()).thenReturn(killer);

        DamageSource damageSource = DamageSource.builder(DamageType.GENERIC).build();
        EntityDeathEvent event = new EntityDeathEvent(entity, damageSource, List.of());
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager).getActiveQuestsForPlayer(killer.getUniqueId());
    }

    @DisplayName("Given an entity death with no player killer, when fired, then progressQuests is not invoked")
    @Test
    public void onEntityDeath_nonPlayerKiller_doesNotProgress() {
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getKiller()).thenReturn(null);

        DamageSource damageSource = DamageSource.builder(DamageType.GENERIC).build();
        EntityDeathEvent event = new EntityDeathEvent(entity, damageSource, List.of());
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager, never()).getActiveQuestsForPlayer(any(UUID.class));
    }

    @DisplayName("Given a cancelled entity death event, when fired, then progressQuests is not invoked")
    @Test
    public void onEntityDeath_cancelledEvent_doesNotProgress() {
        PlayerMock killer = new PlayerMock(server, "Slayer");
        server.addPlayer(killer);

        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getKiller()).thenReturn(killer);

        DamageSource damageSource = DamageSource.builder(DamageType.GENERIC).build();
        EntityDeathEvent event = new EntityDeathEvent(entity, damageSource, List.of());
        event.setCancelled(true);
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager, never()).getActiveQuestsForPlayer(any(UUID.class));
    }
}
