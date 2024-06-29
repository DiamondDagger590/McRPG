package us.eunoians.mcrpg.entity.holder;

import com.diamonddagger590.mccore.task.core.CoreTask;
import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.api.event.ability.AbilityCooldownExpireEvent;
import us.eunoians.mcrpg.api.event.entity.AbilityHolderReadyEvent;
import us.eunoians.mcrpg.api.event.entity.AbilityHolderUnreadyEvent;
import us.eunoians.mcrpg.exception.ready.AbilityNotValidToReadyException;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A "holder" is a representation of an {@link org.bukkit.entity.Entity} for McRPG.
 * <p>
 * An AbilityHolder is the basic level of holders, where an entity that is this holder type
 * can have {@link Ability abilities} associated with them. This is designed to allow for non-player
 * entities to have abilities, which wasn't possible in the previous iteration of this plugin.
 * <p>
 * To take things further, there are the classes {@link LoadoutHolder} and {@link SkillHolder}. A {@link SkillHolder}
 * is one that has a levelable skill which usually can unlock more abilities. There is also {@link LoadoutHolder}
 * which restricts the abilities that an entity can use. By nature, an AbilityHolder can use any number of abilities
 * that they have. If an ability holder is also a loadout holder, then they are restricted to using only the abilities in
 * their loadout.
 */
public class AbilityHolder {

    private final UUID uuid;
    private final Set<NamespacedKey> availableAbilities;
    private final Map<NamespacedKey, AbilityData> abilityDataMap;
    private final Map<NamespacedKey, Integer> abilityCooldownExpireTasks;
    private final Set<NamespacedKey> activeAbilities;
    private int upgradePoints;
    private Optional<ReadyData> readiedAbility;
    private int readiedAbilityExpireTaskId;

    public AbilityHolder(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.availableAbilities = new HashSet<>();
        this.abilityDataMap = new HashMap<>();
        this.abilityCooldownExpireTasks = new HashMap<>();
        this.activeAbilities = new HashSet<>();
        this.upgradePoints = 0;
        this.readiedAbility = Optional.empty();
    }

    /**
     * Gets the {@link UUID} of this holder
     *
     * @return The {@link UUID} of this holder
     */
    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Adds the {@link Ability} as an available ability for this holder to use.
     *
     * @param ability The {@link Ability} to add as an available ability for this holder to use.
     */
    public void addAvailableAbility(@NotNull Ability ability) {
        addAvailableAbility(ability.getAbilityKey());
    }

    /**
     * Adds the {@link NamespacedKey} as an available ability for this holder to use
     *
     * @param abilityKey The {@link Ability} to add as an available ability for this holder to use.
     */
    public void addAvailableAbility(@NotNull NamespacedKey abilityKey) {
        if (validateAbilityExists(abilityKey)) {
            availableAbilities.add(abilityKey);
        }
    }

    /**
     * Gets an {@link ImmutableSet} of all available abilities that this holder
     * has access to.
     *
     * @return An {@link ImmutableSet} of all available abilities that this
     * holder has access to.
     */
    @NotNull
    public ImmutableSet<NamespacedKey> getAvailableAbilities() {
        return ImmutableSet.copyOf(availableAbilities);
    }

    /**
     * Removes the {@link Ability} so it is no longer an available ability for this holder to use.
     *
     * @param ability The {@link Ability} to remove so that it is no longer an available ability for this
     *                holder to use.
     */
    public void removeAvailableAbility(@NotNull Ability ability) {
        removeAvailableAbility(ability.getAbilityKey());
    }

    /**
     * Removes the {@link NamespacedKey} so it is no longer an available ability for this holder to use.
     *
     * @param abilityKey The {@link NamespacedKey} to remove so that it is no longer an available ability for this
     *                   holder to use.
     */
    public void removeAvailableAbility(@NotNull NamespacedKey abilityKey) {
        if (validateAbilityExists(abilityKey)) {
            availableAbilities.remove(abilityKey);
        }
    }

    /**
     * Checks to see if the provided {@link Ability} is available for this holder to use.
     *
     * @param ability The {@link Ability} to check
     * @return {@code true} if the provided {@link Ability} is available for this holder to use
     */
    public boolean isAbilityAvailable(@NotNull Ability ability) {
        return isAbilityAvailable(ability.getAbilityKey());
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} is available for this holder to use.
     *
     * @param abilityKey The {@link NamespacedKey} to check
     * @return {@code true} if the provided {@link NamespacedKey} is available for this holder to use
     */
    public boolean isAbilityAvailable(@NotNull NamespacedKey abilityKey) {
        return validateAbilityExists(abilityKey) && availableAbilities.contains(abilityKey);
    }

    /**
     * Checks to see if this ability holder has {@link AbilityData} that matches
     * the provided {@link Ability}
     *
     * @param ability The {@link Ability} to check for
     * @return {@code true} if the provided {@link Ability} has {@link AbilityData}
     * associated with it for this holder.
     */
    public boolean hasAbilityData(@NotNull Ability ability) {
        return hasAbilityData(ability.getAbilityKey());
    }

    /**
     * Checks to see if this ability holder has {@link AbilityData} that matches
     * the provided {@link NamespacedKey}
     *
     * @param abilityKey The {@link NamespacedKey} to check for
     * @return {@code true} if the provided {@link NamespacedKey} has {@link AbilityData}
     * associated with it for this holder.
     */
    public boolean hasAbilityData(@NotNull NamespacedKey abilityKey) {
        return validateAbilityExists(abilityKey) && abilityDataMap.containsKey(abilityKey);
    }

    /**
     * Gets the {@link AbilityData} associated with the provided {@link Ability}
     *
     * @param ability The {@link Ability} to get the associated {@link AbilityData} for
     * @return An {@link Optional} that will be empty if no match is found, or will contain
     * the {@link AbilityData} that is associated with the provided {@link Ability}
     */
    @NotNull
    public Optional<AbilityData> getAbilityData(@NotNull Ability ability) {
        return getAbilityData(ability.getAbilityKey());
    }

    /**
     * Gets the {@link AbilityData} associated with the provided {@link NamespacedKey}
     *
     * @param abilityKey The {@link NamespacedKey} to get the associated {@link AbilityData} for
     * @return An {@link Optional} that will be empty if no match is found, or will contain
     * the {@link AbilityData} that is associated with the provided {@link NamespacedKey}
     */
    @NotNull
    public Optional<AbilityData> getAbilityData(@NotNull NamespacedKey abilityKey) {
        if (validateAbilityExists(abilityKey)) {
            if (abilityDataMap.containsKey(abilityKey)) {
                return Optional.of(abilityDataMap.get(abilityKey));
            } else {
                // TODO This code adds all default attributes if none are loaded, but this may already be handled in the SkilLDAO so maybe this can be removed
                Set<NamespacedKey> abilityAttributes = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(abilityKey).getApplicableAttributes();
                AbilityAttributeManager abilityAttributeManager = McRPG.getInstance().getAbilityAttributeManager();
                AbilityData abilityData = new AbilityData(abilityKey);
                for (NamespacedKey abilityAttributeKey : abilityAttributes) {
                    Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityAttributeManager.getAttribute(abilityAttributeKey);
                    if (abilityAttributeOptional.isPresent()) {
                        AbilityAttribute<?> abilityAttribute = abilityAttributeOptional.get();
                        abilityData.addAttribute(abilityAttribute);
                    }
                }
                return Optional.of(abilityData);
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Gets a {@link Set} that contains all {@link AbilityData}s that belong to {@link Ability Abilities} under the
     * provided {@link Skill}.
     *
     * @param skill The {@link Skill} to get all of the {@link AbilityData} for.
     * @return A {@link Set} that contains all {@link AbilityData}s that belong to {@link Ability Abilities} under the
     * provided {@link Skill}.
     */
    @NotNull
    public Set<AbilityData> getAllAbilityDataForSkill(@NotNull Skill skill) {
        return getAllAbilityDataForSkill(skill.getSkillKey());
    }

    /**
     * Gets a {@link Set} that contains all {@link AbilityData}s that belong to {@link Ability Abilities} under the
     * {@link Skill} that matches the provided {@link NamespacedKey}.
     *
     * @param namespacedKey The {@link NamespacedKey} belonging to the {@link Skill} to get all of the {@link AbilityData} for.
     * @return A {@link Set} that contains all {@link AbilityData}s that belong to {@link Ability Abilities} under the
     * provided {@link NamespacedKey}.
     */
    @NotNull
    public Set<AbilityData> getAllAbilityDataForSkill(@NotNull NamespacedKey namespacedKey) {
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
        Set<AbilityData> returnSet = new HashSet<>();
        Set<NamespacedKey> abilityKeys = abilityRegistry.getAbilitiesBelongingToSkill(namespacedKey);
        abilityKeys.stream().map(this::getAbilityData).filter(Optional::isPresent).map(Optional::get).forEach(returnSet::add);
        return returnSet;
    }

    /**
     * Adds the provided {@link AbilityData} to this ability holder
     *
     * @param abilityData The {@link AbilityData} to add to this ability holder
     */
    public void addAbilityData(@NotNull AbilityData abilityData) {
        if (validateAbilityExists(abilityData.getAbilityKey())) {
            abilityDataMap.put(abilityData.getAbilityKey(), abilityData);
        }
    }

    /**
     * Removes the {@link AbilityData} for the associated {@link Ability} provided
     * that there is any stored in this holder.
     *
     * @param ability The {@link Ability} to remove the associated {@link AbilityData}
     *                for.
     */
    public void removeAbilityData(@NotNull Ability ability) {
        removeAbilityData(ability.getAbilityKey());
    }

    /**
     * Removes the {@link AbilityData} for the associated {@link NamespacedKey} provided
     * that there is any stored in this holder.
     *
     * @param abilityKey The {@link NamespacedKey} to remove the associated {@link AbilityData}
     *                   for.
     */
    public void removeAbilityData(@NotNull NamespacedKey abilityKey) {
        if (validateAbilityExists(abilityKey)) {
            abilityDataMap.remove(abilityKey);
        }
    }

    /**
     * Sets the provided {@link Ability} as "currently active" for this holder, allowing
     * effects to happen for a duration of time instead of just at the time of activation.
     * <p>
     * This method does not automatically remove the ability from being active, if that functionality is
     * needed, see {@link #addActiveAbility(Ability, int)}.
     *
     * @param ability The {@link Ability} to set as active.
     */
    public void addActiveAbility(@NotNull Ability ability) {
        addActiveAbility(ability.getAbilityKey());
    }

    /**
     * Sets the provided {@link NamespacedKey} as "currently active" for this holder, allowing
     * effects to happen for a duration of time instead of just at the time of activation.
     * <p>
     * This method does not automatically remove the ability from being active, if that functionality is
     * needed, see {@link #addActiveAbility(NamespacedKey, int)}.
     *
     * @param abilityKey The {@link NamespacedKey} to set as active.
     */
    public void addActiveAbility(@NotNull NamespacedKey abilityKey) {
        activeAbilities.add(abilityKey);
    }

    /**
     * Sets the provided {@link Ability} as "currently active" for this holder, allowing
     * effects to happen for a duration of time instead of just at the time of activation.
     * <p>
     * This method will automatically remove the ability from being active after the provided seconds
     * have elapsed.
     *
     * @param ability          The {@link Ability} to set as active.
     * @param secondsActiveFor The amount of seconds the ability should be active for before being
     *                         automatically set as no longer active.
     */
    public void addActiveAbility(@NotNull Ability ability, int secondsActiveFor) {
        addActiveAbility(ability.getAbilityKey(), secondsActiveFor);
    }

    /**
     * Sets the provided {@link NamespacedKey} as "currently active" for this holder, allowing
     * effects to happen for a duration of time instead of just at the time of activation.
     * <p>
     * This method will automatically remove the ability from being active after the provided seconds
     * have elapsed.
     *
     * @param abilityKey       The {@link NamespacedKey} to set as active.
     * @param secondsActiveFor The amount of seconds the ability should be active for before being
     *                         automatically set as no longer active.
     */
    public void addActiveAbility(@NotNull NamespacedKey abilityKey, int secondsActiveFor) {
        activeAbilities.add(abilityKey);
        new DelayableCoreTask(McRPG.getInstance(), secondsActiveFor) {
            @Override
            public void run() {
                removeActiveAbility(abilityKey);
            }
        }.runTask();
    }

    /**
     * Checks to see if the provided {@link Ability} is marked as active for this holder.
     *
     * @param ability The {@link Ability} to check
     * @return {@code true} if the provided {@link Ability} is marked as active.
     */
    public boolean isAbilityActive(@NotNull Ability ability) {
        return isAbilityActive(ability.getAbilityKey());
    }

    /**
     * Checks to see if the {@link Ability} associated with the provided {@link NamespacedKey} is marked
     * as active for this holder.
     *
     * @param abilityKey The {@link NamespacedKey} to check.
     * @return {@code true} of the {@link Ability} associated with the provided {@link NamespacedKey} is
     * marked as active for this holder.
     */
    public boolean isAbilityActive(@NotNull NamespacedKey abilityKey) {
        return activeAbilities.contains(abilityKey);
    }

    /**
     * Gets an immutable {@link Set} of all {@link NamespacedKey}s that belong to {@link Ability Abilities}
     * which are currently active for this holder.
     *
     * @return An immutable {@link Set} of all {@link NamespacedKey}s that belong to {@link Ability Abilities}
     * which are currently active for this holder.
     */
    @NotNull
    public Set<NamespacedKey> getCurrentlyActiveAbilities() {
        return ImmutableSet.copyOf(activeAbilities);
    }

    /**
     * Sets the provided {@link Ability} as no longer active.
     *
     * @param ability The {@link Ability} to set as no longer active.
     */
    public void removeActiveAbility(@NotNull Ability ability) {
        removeActiveAbility(ability.getAbilityKey());
    }

    /**
     * Sets the {@link Ability} associated with the provided {@link NamespacedKey} as
     * no longer active.
     *
     * @param abilityKey The {@link NamespacedKey} to set as no longer active.
     */
    public void removeActiveAbility(@NotNull NamespacedKey abilityKey) {
        activeAbilities.remove(abilityKey);
    }

    /**
     * Gets the amount of upgrade points that this holder currently has.
     *
     * @return The amount of upgrade points that this holder currently has.
     */
    public int getUpgradePoints() {
        return upgradePoints;
    }

    /**
     * Gives the provided amount of upgrade points.
     *
     * @param upgradePoints The amount of upgrade points to give.
     */
    public void giveUpgradePoints(int upgradePoints) {
        this.upgradePoints += Math.max(0, upgradePoints);
    }

    /**
     * Removes the provided amount of upgrade points.
     *
     * @param upgradePoints The amount of upgrade points to remove.
     */
    public void removeUpgradePoints(int upgradePoints) {
        this.upgradePoints -= Math.max(0, Math.min(this.upgradePoints, upgradePoints));
    }

    /**
     * Sets the amount of upgrade points for this holder.
     *
     * @param upgradePoints The amount of upgrade points to set.
     */
    public void setUpgradePoints(int upgradePoints) {
        this.upgradePoints = Math.max(0, upgradePoints);
    }

    /**
     * Gets an {@link Optional} containing {@link ReadyData} representing the holder's
     * ready state. The {@link Optional} will be empty if the holder is not currently ready.
     * <p>
     * The {@link ReadyData} is not tied to a specific ability, instead represents a possibly shared
     * ready state. An example would be two skills that use an axe as the ready tool, WoodCutting and Axes.
     * <p>
     * Since both use an axe to ready, they both need to share the same type of {@link ReadyData}. It is then up
     * to specific implementation (breaking a log or attacking an entity) to determine what ability is going to 'consume'
     * the ready status.
     *
     * @return An {@link Optional} containing {@link ReadyData} representing the holder's
     * ready state. The {@link Optional} will be empty if the holder is not currently ready.
     */
    @NotNull
    public Optional<ReadyData> getReadiedAbility() {
        return readiedAbility;
    }

    /**
     * Manually marks this holder as no longer ready
     */
    public void unreadyHolder() {
        unreadyHolder(false);
    }

    /**
     * Manually marks this holder as no longer ready.
     *
     * @param autoExpired {@code true} if the holder is no longer ready due to the ready status
     *                    auto expiring.
     */
    private void unreadyHolder(boolean autoExpired) {
        if (readiedAbility.isPresent()) {
            AbilityHolderUnreadyEvent abilityHolderUnreadyEvent = new AbilityHolderUnreadyEvent(this, readiedAbility.get(), autoExpired);
            Bukkit.getPluginManager().callEvent(abilityHolderUnreadyEvent);
            readiedAbility = Optional.empty();
            Bukkit.getServer().getScheduler().cancelTask(readiedAbilityExpireTaskId);
            readiedAbilityExpireTaskId = -1;
        }
    }

    /**
     * Marks the holder as ready using the provided {@link ReadyData}.
     * <p>
     * The {@link ReadyData} is not tied to a specific ability, instead represents a possibly shared
     * ready state. An example would be two skills that use an axe as the ready tool, WoodCutting and Axes.
     * <p>
     * Since both use an axe to ready, they both need to share the same type of {@link ReadyData}. It is then up
     * to specific implementation (breaking a log or attacking an entity) to determine what ability is going to 'consume'
     * the ready status.
     *
     * @param readyData The {@link ReadyData} to mark the holder as ready with.
     */
    public void readyHolder(@NotNull ReadyData readyData) {
        readiedAbility = Optional.of(readyData);
        AbilityHolderReadyEvent abilityHolderReadyEvent = new AbilityHolderReadyEvent(this, readiedAbility.get());
        Bukkit.getPluginManager().callEvent(abilityHolderReadyEvent);
        CoreTask autoExpireTask = new DelayableCoreTask(McRPG.getInstance(), 2) {
            @Override
            public void run() {
                // If the ability isn't already unreadied
                if (readiedAbility.isPresent()) {
                    unreadyHolder(true);
                }
            }
        };
        autoExpireTask.runTask();
        readiedAbilityExpireTaskId = autoExpireTask.getBukkitTaskId();
    }

    /**
     * Uses the {@link ReadyData} from the provided {@link Ability} to ready this holder.
     * <p>
     * The {@link ReadyData} is not tied to a specific ability, instead represents a possibly shared
     * ready state. An example would be two skills that use an axe as the ready tool, WoodCutting and Axes.
     * <p>
     * Since both use an axe to ready, they both need to share the same type of {@link ReadyData}. It is then up
     * to specific implementation (breaking a log or attacking an entity) to determine what ability is going to 'consume'
     * the ready status.
     *
     * @param ability The {@link Ability} to use to ready this holder.
     */
    public void readyAbility(@NotNull Ability ability) {
        readiedAbility = ability.getReadyData();
        if (readiedAbility.isEmpty()) {
            throw new AbilityNotValidToReadyException(this, ability);
        }
        AbilityHolderReadyEvent abilityHolderReadyEvent = new AbilityHolderReadyEvent(this, readiedAbility.get());
        Bukkit.getPluginManager().callEvent(abilityHolderReadyEvent);
        CoreTask autoExpireTask = new DelayableCoreTask(McRPG.getInstance(), 3) {
            @Override
            public void run() {
                // If the ability isn't already unreadied
                if (readiedAbility.isPresent()) {
                    unreadyHolder(true);
                }
            }
        };
        autoExpireTask.runTask();
        readiedAbilityExpireTaskId = autoExpireTask.getBukkitTaskId();
    }

    /**
     * Starts a timer that will fire a {@link AbilityCooldownExpireEvent} after the cooldown has expired for the provided
     * {@link Ability}.
     *
     * @param ability  The {@link Ability} to start a timer for
     * @param cooldown The amount of seconds the cooldown timer should run for
     */
    public void startCooldownExpireNotificationTimer(@NotNull Ability ability, long cooldown) {
        startCooldownExpireNotificationTimer(ability.getAbilityKey(), cooldown);
    }

    /**
     * Starts a timer that will fire a {@link AbilityCooldownExpireEvent} after the cooldown has expired for the provided
     * {@link NamespacedKey}.
     *
     * @param abilityKey The {@link NamespacedKey} to start a timer for
     * @param cooldown   The amount of seconds the cooldown timer should run for
     */
    public void startCooldownExpireNotificationTimer(@NotNull NamespacedKey abilityKey, long cooldown) {
        if (Bukkit.getEntity(uuid) instanceof Player player) {
            // Remove any existing cooldown timers
            removeCooldownExpireNotificationTimer(abilityKey);
            AbilityHolder abilityHolder = this;
            DelayableCoreTask delayableCoreTask = new DelayableCoreTask(McRPG.getInstance(), (int) cooldown) {
                @Override
                public void run() {
                    Ability ability = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(abilityKey);
                    AbilityCooldownExpireEvent abilityCooldownExpireEvent = new AbilityCooldownExpireEvent(abilityHolder, ability);
                    Bukkit.getPluginManager().callEvent(abilityCooldownExpireEvent);
                    removeCooldownExpireNotificationTimer(abilityKey);
                }
            };
            delayableCoreTask.runTask();
        }
    }

    /**
     * Manually stops the cooldown expire timer for the provided {@link Ability}.
     *
     * @param ability The {@link Ability} to stop the cooldown expire timer for.
     */
    public void removeCooldownExpireNotificationTimer(@NotNull Ability ability) {
        removeCooldownExpireNotificationTimer(ability.getAbilityKey());
    }

    /**
     * Manually stops the cooldown expire timer for the provided {@link NamespacedKey}.
     *
     * @param namespacedKey The {@link NamespacedKey} to stop the cooldown expire timer for.
     */
    public void removeCooldownExpireNotificationTimer(@NotNull NamespacedKey namespacedKey) {
        if (abilityCooldownExpireTasks.containsKey(namespacedKey)) {
            Bukkit.getServer().getScheduler().cancelTask(abilityCooldownExpireTasks.remove(namespacedKey));
        }
    }

    /**
     * Checks to see if there is an active {@link us.eunoians.mcrpg.quest.Quest} for upgrading the {@link Ability} associated
     * with the provided {@link NamespacedKey}.
     *
     * @param abilityKey The {@link NamespacedKey} to check
     * @return {@code true} if the provided {@link NamespacedKey} has an active upgrade {@link us.eunoians.mcrpg.quest.Quest}
     */
    public boolean hasActiveUpgradeQuest(@NotNull NamespacedKey abilityKey) {
        if (abilityDataMap.containsKey(abilityKey)) {
            AbilityData abilityData = abilityDataMap.get(abilityKey);
            var questOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE);
            return questOptional.isPresent() && questOptional.get() instanceof AbilityUpgradeQuestAttribute attribute && attribute.shouldContentBeSaved();
        }
        return false;
    }

    /**
     * Cleans up this holder from any ongoing tasks and resets it
     * to a "base" state.
     */
    public void cleanupHolder() {
        for (int taskId : abilityCooldownExpireTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        Bukkit.getScheduler().cancelTask(readiedAbilityExpireTaskId);
        activeAbilities.clear();
    }

    /**
     * Helper method to easily check if the provided {@link NamespacedKey} has an {@link Ability} that exists.
     *
     * @param abilityKey The {@link NamespacedKey} to check
     * @return {@code true} if the provided {@link NamespacedKey} has an {@link Ability} that exists.
     */
    private boolean validateAbilityExists(@NotNull NamespacedKey abilityKey) {
        return McRPG.getInstance().getAbilityRegistry().isAbilityRegistered(abilityKey);
    }
}
