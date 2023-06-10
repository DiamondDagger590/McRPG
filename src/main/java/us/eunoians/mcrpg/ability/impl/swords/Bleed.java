package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.BleedDamageEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Bleed is an ability that does a DOT for enemies whenever
 * an entity attacks with a sword.
 * <p>
 * This is designed to do armor piercing damage so that it can be viable even against opponents
 * with maxed out armor. As a trade-off, this will not damage an opponent past a certain
 * health threshold in order to remain balanced.
 * <p>
 * After an enemy is done bleeding, they are put on a short bleed immunity in order to
 * allow them a chance to regenerate health since constantly bleeding would cause fights to
 * easily swing in one direction.
 */
public class Bleed extends Ability {

    public static final NamespacedKey BLEED_KEY = new NamespacedKey(McRPG.getInstance(), "bleed");
    private static final BleedManager BLEED_MANAGER = new BleedManager();

    public Bleed() {
        super(BLEED_KEY);
        addActivatableComponent(BleedComponents.BLEED_ON_ATTACK_COMPONENT, EntityDamageByEntityEvent.class, 0);
        addActivatableComponent(BleedComponents.BLEED_ON_TARGET_PLAYER_COMPONENT, EntityDamageByEntityEvent.class, 1);
    }

    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @Override
    public Optional<String> getLegacyName() {
        return Optional.of("Bleed");
    }

    @Override
    public Optional<String> getDatabaseName() {
        return Optional.empty();
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        //This is the only event that can activate this ability, so this should be a safe cast
        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
        Entity entity = entityDamageByEntityEvent.getEntity();

        if (entity instanceof LivingEntity livingEntity && BLEED_MANAGER.canEntityStartBleeding(livingEntity)) {

            BleedActivateEvent bleedActivateEvent = new BleedActivateEvent(abilityHolder, livingEntity, 3);
            Bukkit.getPluginManager().callEvent(bleedActivateEvent);

            if(!bleedActivateEvent.isCancelled()) {
                BLEED_MANAGER.startBleeding(abilityHolder, livingEntity, bleedActivateEvent.getBleedCycles());
            }
        }
    }

    /**
     * Gets the {@link BleedManager} that handles all the specific mechanics required for bleed to work.
     *
     * @return The {@link BleedManager} that handles all the specific mechanics required for bleed to work.
     */
    @NotNull
    public static BleedManager getBleedManager() {
        return BLEED_MANAGER;
    }

    /**
     * This class is used to handle all the specific mechanics required for bleed to work.
     */
    private static class BleedManager {
        private final Map<UUID, Optional<AbilityHolder>> ENTITIES_BLEEDING = new HashMap<>();
        private final Set<UUID> BLEED_IMMUNE_ENTITIES = new HashSet<>();

        /**
         * Checks to see if the provided {@link LivingEntity} is currently bleeding.
         *
         * @param entity The {@link LivingEntity} to check
         * @return {@code true} if the provided {@link LivingEntity} is currently bleeding
         */
        public boolean isEntityBleeding(@NotNull LivingEntity entity) {
            return isEntityBleeding(entity.getUniqueId());
        }

        /**
         * Checks to see if the provided {@link UUID} is currently bleeding.
         *
         * @param uuid The {@link UUID} to check
         * @return {@code true} if the provided {@link UUID} is currently bleeding.
         */
        public boolean isEntityBleeding(@NotNull UUID uuid) {
            return ENTITIES_BLEEDING.containsKey(uuid);
        }

        /**
         * Stops the provided {@link LivingEntity} from bleeding. If they have an active task
         * that is causing them to bleed, it will be cancelled on the next iteration after
         * this method is called.
         *
         * @param entity The {@link LivingEntity} to stop the bleeding for.
         */
        public void stopEntityBleeding(@NotNull LivingEntity entity) {
            stopEntityBleeding(entity.getUniqueId());
        }

        /**
         * Stops the provided {@link UUID} from bleeding. If they have an active task
         * that is causing them to bleed, it will be cancelled on the next iteration after
         * this method is called.
         *
         * @param uuid The {@link UUID} to stop the bleeding for.
         */
        public void stopEntityBleeding(@NotNull UUID uuid) {
            ENTITIES_BLEEDING.remove(uuid);
        }

        /**
         * Starts the bleeding process for the provided {@link LivingEntity}.
         * <p>
         * This method does a failsafe check to {@link #canEntityStartBleeding(LivingEntity)} to ensure
         * it is safe to start the bleeding process.
         *
         * @param entity The {@link LivingEntity} to start the bleeding process for
         */
        public void startBleeding(@NotNull LivingEntity entity) {
            startBleeding(null, entity, 3);
        }

        public void startBleeding(@NotNull LivingEntity entity, int bleedCycles) {
            startBleeding(null, entity, bleedCycles);
        }

        public void startBleeding(@Nullable AbilityHolder abilityHolder, @NotNull LivingEntity entity) {
            startBleeding(abilityHolder, entity, 3);
        }

        public void startBleeding(@Nullable AbilityHolder abilityHolder, @NotNull LivingEntity entity, int bleedCycles) {
            if (canEntityStartBleeding(entity)) {

                ENTITIES_BLEEDING.put(entity.getUniqueId(), Optional.ofNullable(abilityHolder));

                ExpireableCoreTask expireableCoreTask = new ExpireableCoreTask(McRPG.getInstance(), 0.5, 0.5, 3) {
                    @Override
                    protected void onTaskExpire() {
                        stopEntityBleeding(entity);
                        startBleedImmunity(entity);
                    }

                    @Override
                    protected void onCancel() {
                        stopEntityBleeding(entity);
                        startBleedImmunity(entity);
                    }

                    @Override
                    protected void onDelayComplete() {
                    }

                    @Override
                    protected void onIntervalStart() {
                    }

                    @Override
                    protected void onIntervalComplete() {
                        if (isEntityBleeding(entity) && !entity.isDead()) { //Ensure entity is bleeding and is not dead
                            if (entity instanceof Player player && !player.isOnline()) { //If the entity is a player and offline, skip this interval
                                return;
                            }

                            if (entity.getHealth() > 4) {

                                BleedDamageEvent bleedDamageEvent = new BleedDamageEvent(ENTITIES_BLEEDING.get(entity.getUniqueId()), entity, 4, true);
                                Bukkit.getPluginManager().callEvent(bleedDamageEvent);

                                if(bleedDamageEvent.isCancelled()) { //If bleed damage is cancelled, skip this interval
                                    return;
                                }

                                FakeBleedDamageEvent fakeBleedDamageEvent = new FakeBleedDamageEvent(entity, EntityDamageEvent.DamageCause.CUSTOM, bleedDamageEvent.getDamage()); //Call a fake event to check for region protections
                                Bukkit.getPluginManager().callEvent(fakeBleedDamageEvent);

                                if (!fakeBleedDamageEvent.isCancelled()) {
                                    entity.setHealth(Math.max(4, entity.getHealth() - fakeBleedDamageEvent.getFinalDamage())); //Respects damage modifiers since those are applied after the bleed event
                                    entity.damage(0.01); //for damage effect
                                }
                            }
                        } else {
                            cancelTask();
                        }
                    }

                    @Override
                    protected void onIntervalPause() {
                    }

                    @Override
                    protected void onIntervalResume() {
                    }
                };
                expireableCoreTask.runTask();
            }

        }

        /**
         * Checks to see if the provided {@link LivingEntity} can start bleeding.
         * <p>
         * This checks two things:
         * 1) That the entity does not have an active bleed immunity
         * 2) That the entity is not actively bleeding.
         *
         * @param entity The {@link LivingEntity} to check
         * @return {@code true} if the provided {@link LivingEntity} can start bleeding
         */
        public boolean canEntityStartBleeding(@NotNull LivingEntity entity) {
            return canEntityStartBleeding(entity.getUniqueId());
        }

        /**
         * Checks to see if the provided {@link UUID} can start bleeding.
         * <p>
         * This checks two things:
         * 1) That the entity does not have an active bleed immunity
         * 2) That the entity is not actively bleeding.
         *
         * @param uuid The {@link UUID} to check
         * @return {@code true} if the provided {@link UUID} can start bleeding
         */
        public boolean canEntityStartBleeding(@NotNull UUID uuid) {
            return !isEntityBleedImmune(uuid) && !isEntityBleeding(uuid);
        }

        /**
         * Checks to see if the provided {@link LivingEntity} currently has a bleed immunity,
         * which prevents it from being afflicted with bleed again.
         *
         * @param entity The {@link LivingEntity} to check
         * @return {@code true} if the provided {@link LivingEntity} has an active
         * bleed immunity.
         */
        public boolean isEntityBleedImmune(@NotNull LivingEntity entity) {
            return isEntityBleedImmune(entity.getUniqueId());
        }

        /**
         * Checks to see if the provided {@link UUID} currently has a bleed immunity,
         * which prevents it from being afflicted with bleed again.
         *
         * @param uuid The {@link UUID} to check
         * @return {@code true} if the provided {@link UUID} has an active
         * bleed immunity.
         */
        public boolean isEntityBleedImmune(@NotNull UUID uuid) {
            return BLEED_IMMUNE_ENTITIES.contains(uuid);
        }

        /**
         * Removes the provided {@link LivingEntity} from having a bleed immunity,
         * allowing it to again be afflicted with bleed.
         *
         * @param entity The {@link LivingEntity} to end the bleed immunity for.
         */
        public void endEntityBleedImmunity(@NotNull LivingEntity entity) {
            endEntityBleedImmunity(entity.getUniqueId());
        }

        /**
         * Removes the provided {@link UUID} from having a bleed immunity, allowing
         * it to again be afflicted with bleed.
         *
         * @param uuid The {@link UUID} to end the bleed immunity for.
         */
        public void endEntityBleedImmunity(@NotNull UUID uuid) {
            BLEED_IMMUNE_ENTITIES.remove(uuid);
        }

        /**
         * Starts a bleed immunity for the provided {@link LivingEntity}.
         * <p>
         * This means that while the bleed immunity is active, the entity can not be
         * afflicted with bleed again.
         *
         * @param entity The {@link LivingEntity} to start a bleed immunity for.
         */
        public void startBleedImmunity(@NotNull LivingEntity entity) {
            startBleedImmunity(entity.getUniqueId());
        }

        /**
         * Starts a bleed immunity for the provided {@link UUID}
         * <p>
         * This means that while the bleed immunity is active, the entity can not be
         * afflicted with bleed again.
         *
         * @param uuid The {@link UUID} to start a bleed immunity for.
         */
        public void startBleedImmunity(@NotNull UUID uuid) {
            BLEED_IMMUNE_ENTITIES.add(uuid);
            new DelayableCoreTask(McRPG.getInstance(), 5) {
                @Override
                public void run() {
                    BLEED_IMMUNE_ENTITIES.remove(uuid);
                }
            }.runTask();
        }
    }

    /**
     * This event is used to check bleed damage since it is a DOT and we want to respect plugins that have safe zones
     */
    private static class FakeBleedDamageEvent extends EntityDamageEvent {

        public FakeBleedDamageEvent(@NotNull Entity damagee, @NotNull DamageCause cause, double damage) {
            super(damagee, cause, damage);

            if (getDamage(DamageModifier.ARMOR) > 0) { //Will throw a fit for things like zombies if not ><
                setDamage(DamageModifier.ARMOR, 0);
            }
        }
    }

}
