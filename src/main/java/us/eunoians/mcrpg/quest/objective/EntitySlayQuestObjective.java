package us.eunoians.mcrpg.quest.objective;

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.quest.Quest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntitySlayQuestObjective extends QuestObjective {

    private final Set<EntityType> allowedEntities;
    private final Set<EntityType> bannedEntities;

    public EntitySlayQuestObjective(@NotNull Quest quest, int requiredProgression) {
        super(quest, requiredProgression);
        this.allowedEntities = new HashSet<>();
        this.bannedEntities = new HashSet<>();
    }

    public EntitySlayQuestObjective(@NotNull Quest quest, int requiredProgression, int currentProgression) {
        super(quest, requiredProgression, currentProgression);
        this.allowedEntities = new HashSet<>();
        this.bannedEntities = new HashSet<>();
    }

    /**
     * Adds the provided {@link EntityType}s to the allow list.
     *
     * @param entityTypes The {@link EntityType}s to add to the allow list
     */
    public void addAllowedEntities(@NotNull EntityType... entityTypes) {
        allowedEntities.addAll(List.of(entityTypes));
    }

    /**
     * Adds the provided {@link EntityType}s to the ban list.
     *
     * @param entityTypes The {@link EntityType}s to add to the ban list.
     */
    public void addBannedEntities(@NotNull EntityType... entityTypes) {
        bannedEntities.addAll(List.of(entityTypes));
    }

    /**
     * Gets a copy of the {@link Set} of all allowed entities.
     *
     * @return A copy of the {@link Set} of all allowed entities.
     */
    @NotNull
    public Set<EntityType> getAllowedEntities() {
        return ImmutableSet.copyOf(allowedEntities);
    }

    /**
     * Gets a copy of the {@link Set} of all banned entities.
     *
     * @return A copy of the {@link Set} of all banned entities.
     */
    @NotNull
    public Set<EntityType> getBannedEntities() {
        return ImmutableSet.copyOf(bannedEntities);
    }

    @Override
    public boolean canProcessEvent(@NotNull QuestHolder questHolder, @NotNull Event event) {
        AllowMode allowMode = AllowMode.getAllowMode(this);
        // TODO track natural block placement
        return event instanceof EntityDeathEvent entityDeathEvent &&
                !entityDeathEvent.isCancelled() &&
                !isObjectiveCompleted() &&
                entityDeathEvent.getEntity().getKiller() != null &&
                questHolder.getUUID().equals(entityDeathEvent.getEntity().getKiller().getUniqueId()) &&
                questHolder.isQuestActive(getQuest()) &&
                allowMode.isEntityAllowed(this, entityDeathEvent.getEntity().getType()) &&
                entityDeathEvent.getEntity().getKiller().getGameMode() == GameMode.SURVIVAL;
    }

    @Override
    public void processEvent(@NotNull QuestHolder questHolder, @NotNull Event event) {
        // Since only one block can be broken per event, increment progression by 1
        progressObjective(1);
    }

    @Override
    public void startListeningForProgression() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void stopListeningForProgression() {
        EntityDeathEvent.getHandlerList().unregister(this);
    }

    @Override
    public Component getObjectiveTitle() {
        return McRPG.getInstance().getMiniMessage().deserialize("<gold>Entity Slay Objective</gold>");
    }

    @Override
    public List<Component> getObjectiveInfoText() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        List<Component> info = new ArrayList<>();
        AllowMode allowMode = AllowMode.getAllowMode(this);
        switch (allowMode) {
            case ALLOWED ->
                    info.add(miniMessage.deserialize("<gray>Break these blocks to progress this objective</gray>"));
            case BANNED ->
                    info.add(miniMessage.deserialize("<gray>Break any block besides these to progress this objective</gray>"));
            case ALL -> info.add(miniMessage.deserialize("<gray>Break any block to progress this objective</gray>"));
        }
        if (allowMode == AllowMode.ALLOWED) {
            for (EntityType entityType : allowedEntities) {
                info.add(miniMessage.deserialize("  <gray>- <gold>" + entityType + "</gold></gray>"));
            }
        } else if (allowMode == AllowMode.BANNED) {
            for (EntityType entityType : bannedEntities) {
                info.add(miniMessage.deserialize("  <gray>- <gold>" + entityType + "</gold></gray>"));
            }
        }
        return info;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
        Player player = entityDeathEvent.getEntity().getKiller();
        if (player != null) {
            var questHolderOptional = McRPG.getInstance().getEntityManager().getQuestHolder(player.getUniqueId());
            if (questHolderOptional.isPresent()) {
                var questHolder = questHolderOptional.get();
                if (questHolder.isQuestActive(getQuest()) && canProcessEvent(questHolder, entityDeathEvent)) {
                    processEvent(questHolder, entityDeathEvent);
                }
            }
        }
    }

    /**
     * An enum that represents the mode for validating what {@link EntityType}s can progress the objective.
     */
    private enum AllowMode {
        ALL,
        ALLOWED,
        BANNED;

        /**
         * Checks to see if the provided {@link EntityType} can progress the objective based on the allow or ban settings
         *
         * @param entitySlayQuestObjective The {@link EntitySlayQuestObjective} to check the lists of
         * @param entityType                 The {@link EntityType} to check
         * @return {@code true} if the provided {@link EntityType} is valid for this allow mode.
         */
        public boolean isEntityAllowed(@NotNull EntitySlayQuestObjective entitySlayQuestObjective, @NotNull EntityType entityType) {
            return switch (this) {
                case ALLOWED -> entitySlayQuestObjective.getAllowedEntities().contains(entityType);
                case BANNED -> !entitySlayQuestObjective.getBannedEntities().contains(entityType);
                case ALL -> true;
            };
        }

        /**
         * Gets the allow mode for the given {@link EntitySlayQuestObjective} based on the state of the allow/ban list.
         *
         * @param entitySlayQuestObjective The {@link EntitySlayQuestObjective} to get the allow mode for.
         * @return If both {@link EntitySlayQuestObjective#getAllowedEntities()} and {@link EntitySlayQuestObjective#getBannedEntities()}
         * return an empty {@link Set}, then {@link AllowMode#ALL} is returned. If both the allow and ban list are non-empty, then {@link AllowMode#ALLOWED}
         * is returned. Otherwise, it will return the allow mode corresponding to the list that has contents.
         */
        public static AllowMode getAllowMode(@NotNull EntitySlayQuestObjective entitySlayQuestObjective) {
            if (entitySlayQuestObjective.getAllowedEntities().isEmpty()) {
                if (entitySlayQuestObjective.getBannedEntities().isEmpty()) {
                    return ALL;
                } else {
                    return BANNED;
                }
            } else {
                return ALLOWED;
            }
        }
    }
}
