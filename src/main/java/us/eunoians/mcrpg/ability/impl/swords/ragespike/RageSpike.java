package us.eunoians.mcrpg.ability.impl.swords.ragespike;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.ActiveAbility;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.CooldownableAbility;
import us.eunoians.mcrpg.ability.PlayerAbility;
import us.eunoians.mcrpg.ability.ReadyableAbility;
import us.eunoians.mcrpg.ability.TierableAbility;
import us.eunoians.mcrpg.ability.ToggleableAbility;
import us.eunoians.mcrpg.ability.UnlockableAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.swords.ragespike.RageSpikeBeginChargeEvent;
import us.eunoians.mcrpg.api.event.ability.swords.ragespike.RageSpikeDamageEvent;
import us.eunoians.mcrpg.api.event.ability.swords.ragespike.RageSpikeLaunchEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class RageSpike extends BaseAbility implements UnlockableAbility, ToggleableAbility,
        TierableAbility, ReadyableAbility, ActiveAbility, PlayerAbility, CooldownableAbility {

    private final static Set<Material> ACTIVATION_MATERIALS = new HashSet<>();

    static {
        for (Material material : Material.values()) {
            if (material.toString().contains("_SWORD")) {
                ACTIVATION_MATERIALS.add(material);
            }
        }
    }

    private int tier = 0;
    private boolean toggled = false;
    private boolean unlocked = false;
    private boolean ready = false;
    private @Nullable BukkitTask chargingTask;
    private @Nullable BukkitTask flyingTask;

    /**
     * This assumes that the required extension of {@link AbilityCreationData}. Implementations of this will need
     * to sanitize the input.
     *
     * @param abilityCreationData The {@link AbilityCreationData} that is used to create this {@link Ability}
     */
    public RageSpike(@NotNull AbilityCreationData abilityCreationData) {
        super(abilityCreationData);

        if (abilityCreationData instanceof RageSpikeCreationData) {
            RageSpikeCreationData rageSpikeCreationData = (RageSpikeCreationData) abilityCreationData;

            this.tier = rageSpikeCreationData.getTier();
            this.toggled = rageSpikeCreationData.isToggled();
            this.unlocked = rageSpikeCreationData.isUnlocked();
        }
    }

    /**
     * Gets the {@link NamespacedKey} that this {@link Ability} belongs to
     *
     * @return The {@link NamespacedKey} that this {@link Ability} belongs to
     */
    @Override
    public @NotNull NamespacedKey getSkill() {
        return McRPG.getNamespacedKey("swords");
    }

    /**
     * @param activator    The {@link AbilityHolder} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     */
    @Override
    public void activate(AbilityHolder activator, Object... optionalData) {

        ConfigurationSection configurationSection = getTierConfigSection(getTier());

        //TODO pull from config
        double chargeSeconds = 5;
        Player player = Bukkit.getPlayer(getPlayer().getUniqueId());

        RageSpikeBeginChargeEvent rageSpikeBeginChargeEvent = new RageSpikeBeginChargeEvent(getAbilityHolder(), this, chargeSeconds);
        Bukkit.getPluginManager().callEvent(rageSpikeBeginChargeEvent);

        RageSpike rageSpikeReference = this;

        if (!rageSpikeBeginChargeEvent.isCancelled()) {

            //This task deals with players still crouching and if they uncrouch then this should be cancelled
            BukkitTask chargeTask = new BukkitRunnable() {
                @Override
                public void run() {

                    //Set the task to null so outside checks no longer can see it
                    chargingTask = null;

                    //We don't know that the player is online at this point so we need to validate this
                    if (player != null && player.isOnline()) {

                        RageSpikeLaunchEvent rageSpikeLaunchEvent = new RageSpikeLaunchEvent(getAbilityHolder(), rageSpikeReference, 5.0, 2, 2, -4.3, getCooldownDuration()); //TODO make configurable
                        Bukkit.getPluginManager().callEvent(rageSpikeLaunchEvent);

                        if (!rageSpikeLaunchEvent.isCancelled()) {

                            //Deal with launching the player
                            Vector unitVector = new Vector(player.getLocation().getDirection().getX(), 0, player.getLocation().getDirection().getZ());

                            player.setVelocity(unitVector.multiply(rageSpikeLaunchEvent.getVectorMultiplier()));

                            //Audio and visual feedback
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.6f, 1.0f);
                            player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 1);

                            AtomicInteger atomicInteger = new AtomicInteger(0);

                            //Pull out our event variables
                            double radius = rageSpikeLaunchEvent.getDamageRadius();
                            double damage = rageSpikeLaunchEvent.getDamage();
                            double enemyVelocity = rageSpikeLaunchEvent.getTargetVectorMultiplier();

                            //Cache entities we have already damaged so we don't deal duplicate damage
                            Set<UUID> attackedEntities = new HashSet<>();

                            /*Store the task that updates as the player flies. We check every tick for 20 ticks around the flying player for any entities to damage.
                             * While the player may fly for more than a second, we only want to apply damage for a second. This can easily be adjusted by changing the == statement below*/
                            BukkitTask flyTask = new BukkitRunnable() {
                                @Override
                                public void run() {

                                    //It's a repeating task so we need exit conditions
                                    if (player == null || !player.isOnline() || atomicInteger.get() == 21) {
                                        flyingTask = null;
                                        cancel();
                                        return;
                                    }

                                    if (atomicInteger.get() % 3 == 0) {
                                        player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 1);
                                    }

                                    for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {

                                        if (!attackedEntities.contains(entity.getUniqueId()) && entity instanceof LivingEntity) {
                                            LivingEntity livingEntity = (LivingEntity) entity;

                                            RageSpikeDamageEvent rageSpikeDamageEvent = new RageSpikeDamageEvent(getAbilityHolder(), rageSpikeReference, livingEntity, damage, enemyVelocity);
                                            Bukkit.getPluginManager().callEvent(rageSpikeDamageEvent);

                                            if (!rageSpikeDamageEvent.isCancelled()) {

                                                //Damage and launch the target
                                                livingEntity.damage(rageSpikeDamageEvent.getDamage());

                                                Vector targetVector = new Vector(livingEntity.getLocation().getDirection().getX(), 0, livingEntity.getLocation().getZ());
                                                livingEntity.setVelocity(targetVector.multiply(rageSpikeDamageEvent.getTargetVectorMultiplier()));

                                                attackedEntities.add(entity.getUniqueId());
                                            }
                                        }
                                    }
                                }
                            }.runTaskTimer(McRPG.getInstance(), 0, 1);

                            flyingTask = flyTask;
                        }
                    }
                }
            }.runTaskLater(McRPG.getInstance(), (long) (chargeSeconds * 20));

            chargingTask = chargeTask;
        }
    }

    /**
     * Abstract method that can be used to create listeners for this specific ability.
     * Note: This should only return a {@link List} of {@link Listener} objects. These shouldn't be registered yet!
     * This will be done automatically.
     *
     * @return a list of listeners for this {@link Ability}
     */
    @Override
    protected List<Listener> createListeners() {
        return Collections.singletonList(new RageSpikeListener());
    }

    /**
     * Gets the tier of this {@link Ability}.
     * <p>
     * A tier of 0 represents an ability that is currently not unlocked but this should be checked by
     * {@link UnlockableAbility#isUnlocked()}.
     *
     * @return A positive zero inclusive number representing the current tier of this {@link TierableAbility}.
     */
    @Override
    public int getTier() {
        return this.tier;
    }

    /**
     * Sets the tier of this {@link TierableAbility}.
     * <p>
     * This should only accept positive zero inclusive numbers and should sanitize for them.
     *
     * @param tier A positive zero inclusive number representing the new tier of this {@link TierableAbility}
     */
    @Override
    public void setTier(int tier) {
        this.tier = tier;
    }

    /**
     * Gets the {@link ConfigurationSection} that belongs to this ability
     *
     * @param tier The tier at which to get the {@link ConfigurationSection} for
     * @return Either the {@link ConfigurationSection} mapped to the provided tier or {@code null} if invalid
     */
    @Override
    public @Nullable ConfigurationSection getTierConfigSection(int tier) {
        return null;
    }

    /**
     * This method checks to see if the {@link ToggleableAbility} is currently toggled on
     *
     * @return True if the {@link ToggleableAbility} is currently toggled on
     */
    @Override
    public boolean isToggled() {
        return this.toggled;
    }

    /**
     * This method inverts the current toggled state of the ability and returns the result.
     * <p>
     * This is more of a lazy way of calling {@link #setToggled(boolean)} without also needing to call
     * {@link #isToggled()} to invert
     *
     * @return The stored result of the inverted version of {@link #isToggled()}
     */
    @Override
    public boolean toggle() {
        this.toggled = !this.toggled;
        return this.toggled;
    }

    /**
     * This method sets the toggled status of the ability
     *
     * @param toggled True if the ability should be toggled on
     */
    @Override
    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    /**
     * Checks to see if the {@link UnlockableAbility} is currently unlocked or not.
     *
     * @return {@code true} if this {@link UnlockableAbility} is currently unlocked.
     */
    @Override
    public boolean isUnlocked() {
        return this.unlocked;
    }

    /**
     * Sets if this {@link UnlockableAbility} is currently unlocked or not.
     *
     * @param unlocked If this {@link UnlockableAbility} is currently unlocked or not.
     */
    @Override
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    /**
     * Checks to see if this ability is currently in a ready status
     *
     * @return {@code true} if this ability is currently in a ready status
     */
    @Override
    public boolean isReady() {
        return this.ready;
    }

    /**
     * Sets if this ability is currently in a ready status or not
     *
     * @param ready If this ability should be in a ready state or note
     */
    @Override
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Handles parsing an {@link Event} to see if this ability should enter "ready" status.
     * <p>
     *
     * @param event The {@link Event} that needs to be parsed
     * @return {@code true} if the {@link ReadyableAbility} should enter "ready" status from this method call
     */
    @Override
    public boolean handleReadyAttempt(Event event) {

        if (!isReady() && event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getItem() != null &&
                getActivatableMaterials().contains(((PlayerInteractEvent) event).getItem().getType())) {
            return true;
        }

        return false;
    }

    /**
     * Gets a {@link Set} of all {@link Material}s that can activate this {@link ReadyableAbility}
     *
     * @return A {@link Set} of all {@link Material}s that can activate this {@link ReadyableAbility}
     */
    @Override
    public Set<Material> getActivatableMaterials() {
        return ACTIVATION_MATERIALS;
    }

    /**
     * Checks to see if this {@link ReadyableAbility} can be set to a ready status by interacting with a block.
     * <p>
     * If this returns {@code false}, then {@link #isValidReadyableBlock(Block)} will not be called.
     *
     * @return {@code true} if this ability can be ready'd from interacting with a {@link org.bukkit.block.Block}
     */
    @Override
    public boolean readyFromBlock() {
        return true;
    }

    /**
     * Checks to see if this {@link ReadyableAbility} can be set to a ready status by interacting with an entity.
     * <p>
     * If this returns {@code false}, then {@link #isValidReadyableEntity(Entity)}  will not be called.
     *
     * @return {@code true} if this ability can be ready'd from interacting with a {@link Entity}
     */
    @Override
    public boolean readyFromEntity() {
        return true;
    }

    /**
     * Gets the amount of seconds that the "ready" status should last for this ability
     *
     * @return The amount of seconds that the "ready" status should last for this ability
     */
    @Override
    public int getReadyDurationSeconds() {
        return 5;
    }

    /**
     * Gets the {@link McRPGPlayer} that this {@link Ability} belongs to.
     *
     * @return The {@link McRPGPlayer} that this {@link Ability} belongs to
     */
    @Override
    public @NotNull McRPGPlayer getPlayer() {
        return getAbilityHolder();
    }

    /**
     * Gets the {@link AbilityHolder} that owns this {@link Ability}
     *
     * @return THe {@link AbilityHolder} that owns this {@link Ability}
     */
    @Override
    public @NotNull McRPGPlayer getAbilityHolder() {
        return (McRPGPlayer) super.getAbilityHolder();
    }

    /**
     * Gets the task of the {@link #getPlayer()} who is currently charging.
     *
     * @return The {@link BukkitTask} of the {@link #getPlayer()} who is currently charging
     * or {@code null} if the {@link #getPlayer()} is not charging
     */
    @Nullable
    public BukkitTask getChargingTask() {
        return chargingTask;
    }

    /**
     * Gets the task of the {@link #getPlayer()} who is currently "flying" and dealing damage.
     *
     * @return The {@link BukkitTask} of the {@link #getPlayer()} who is currently "flying"
     * or {@code null} if the {@link #getPlayer()} is not charging
     */
    @Nullable
    public BukkitTask getFlyingTask() {
        return flyingTask;
    }

    /**
     * Cancels the {@link #getChargingTask()} if there is one valid
     */
    public void cancelChargingTask() {
        if (getChargingTask() != null) {

            Entity entity = getPlayer().getEntity();

            if (entity != null) {
                Player player = (Player) entity;
                McRPG.getInstance().getMessageSender().sendMessage(player, ChatColor.RED + "You cancelled the charge of Rage Spike", false);
            }
            getChargingTask().cancel();
        }

        this.chargingTask = null;
    }

    /**
     * Cancels the {@link #getFlyingTask()} if there is one valid
     */
    public void cancelFlyingTask() {
        if (getFlyingTask() != null) {
            getFlyingTask().cancel();
        }

        this.flyingTask = null;
    }

    /**
     * Gets the amount of time in seconds that this {@link CooldownableAbility} should be on cooldown for after activation
     *
     * @return The postivie zero exclusive amount of time in seconds this {@link CooldownableAbility} should be on cooldown for after activation.
     */
    @Override
    public int getCooldownDuration() {
        return 180; //TODO make configable
    }
}
