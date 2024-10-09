package us.eunoians.mcrpg.ability.impl.swords.bleed;

import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.ability.swords.BleedDamageEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * This class is used to handle all the specific mechanics required for bleed to work.
 */
public final class BleedManager {

    private final Map<UUID, Optional<AbilityHolder>> ENTITIES_BLEEDING = new HashMap<>();
    private final Set<UUID> BLEED_IMMUNE_ENTITIES = new HashSet<>();
    private final McRPG mcRPG;

    public BleedManager(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
    }

    public McRPG getPlugin() {
        return mcRPG;
    }

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
        YamlDocument swordsConfig = getPlugin().getFileManager().getFile(FileType.SWORDS_CONFIG);
        startBleeding(null, entity, swordsConfig.getInt(SwordsConfigFile.BLEED_BASE_CYCLES), swordsConfig.getDouble(SwordsConfigFile.BLEED_BASE_DAMAGE));
    }

    public void startBleeding(@NotNull LivingEntity entity, int bleedCycles, double bleedDamage) {
        startBleeding(null, entity, bleedCycles, bleedDamage);
    }

    public void startBleeding(@Nullable AbilityHolder abilityHolder, @NotNull LivingEntity entity) {
        YamlDocument swordsConfig = getPlugin().getFileManager().getFile(FileType.SWORDS_CONFIG);
        startBleeding(abilityHolder, entity, swordsConfig.getInt(SwordsConfigFile.BLEED_BASE_CYCLES), swordsConfig.getDouble(SwordsConfigFile.BLEED_BASE_DAMAGE));
    }

    public void startBleeding(@Nullable AbilityHolder abilityHolder, @NotNull LivingEntity entity, int bleedCycles, double bleedDamage) {
        if (canEntityStartBleeding(entity)) {

            ENTITIES_BLEEDING.put(entity.getUniqueId(), Optional.ofNullable(abilityHolder));
            McRPG mcRPG = getPlugin();

            ExpireableCoreTask expireableCoreTask = new ExpireableCoreTask(mcRPG, 0.5, getPlugin().getFileManager().getFile(FileType.SWORDS_CONFIG).getDouble(SwordsConfigFile.BLEED_BASE_FREQUENCY), bleedCycles) {
                @Override
                protected void onTaskExpire() {
                    stopEntityBleeding(entity);
                    if (mcRPG.getFileManager().getFile(FileType.SWORDS_CONFIG).getBoolean(SwordsConfigFile.BLEED_GRANT_IMMUNITY_AFTER_EXPIRE)) {
                        startBleedImmunity(entity);
                    }
                }

                @Override
                protected void onCancel() {
                    stopEntityBleeding(entity);
                    if (mcRPG.getFileManager().getFile(FileType.SWORDS_CONFIG).getBoolean(SwordsConfigFile.BLEED_GRANT_IMMUNITY_AFTER_EXPIRE)) {
                        startBleedImmunity(entity);
                    }
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

                        int minimumHealthAllowed = mcRPG.getFileManager().getFile(FileType.SWORDS_CONFIG).getInt(SwordsConfigFile.BLEED_MINIMUM_HEALTH_ALLOWED);
                        if (entity.getHealth() > minimumHealthAllowed) {

                            BleedDamageEvent bleedDamageEvent = new BleedDamageEvent(ENTITIES_BLEEDING.get(entity.getUniqueId()), entity, bleedDamage, mcRPG.getFileManager().getFile(FileType.SWORDS_CONFIG).getBoolean(SwordsConfigFile.BLEED_DAMAGE_PIERCE_ARMOR));
                            Bukkit.getPluginManager().callEvent(bleedDamageEvent);

                            if(bleedDamageEvent.isCancelled()) { //If bleed damage is cancelled, skip this interval
                                return;
                            }

                            FakeBleedDamageEvent fakeBleedDamageEvent = new FakeBleedDamageEvent(entity, EntityDamageEvent.DamageCause.CUSTOM, bleedDamageEvent.getDamage()); //Call a fake event to check for region protections
                            Bukkit.getPluginManager().callEvent(fakeBleedDamageEvent);

                            if (!fakeBleedDamageEvent.isCancelled()) {
                                if  (bleedDamageEvent.isDamageIgnoringArmor()) {
                                    entity.setHealth(Math.max(minimumHealthAllowed, entity.getHealth() - fakeBleedDamageEvent.getFinalDamage())); //Respects damage modifiers since those are applied after the bleed event
                                    entity.damage(0.01); //for damage effect
                                }
                                else {
                                    entity.damage(fakeBleedDamageEvent.getFinalDamage());
                                }
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
        new DelayableCoreTask(getPlugin(), getPlugin().getFileManager().getFile(FileType.SWORDS_CONFIG).getInt(SwordsConfigFile.BLEED_IMMUNITY_DURATION)) {
            @Override
            public void run() {
                BLEED_IMMUNE_ENTITIES.remove(uuid);
            }
        }.runTask();
    }
}