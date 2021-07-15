package us.eunoians.mcrpg.api.manager;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.cooldown.LCCooldown;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.CooldownableAbility;
import us.eunoians.mcrpg.api.lunar.LunarClientHook;
import us.eunoians.mcrpg.player.McRPGPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * This class handles managing all cooldowns for {@link us.eunoians.mcrpg.ability.CooldownableAbility}s.
 * <p>
 * It contains multiple wrapper objects to help keep the code fairly clean with {@link CooldownWrapper} containing information
 * of all cooldowns for a specific player whilst a {@link SkillCooldownWrapper} contains info of cooldowns for all {@link us.eunoians.mcrpg.ability.CooldownableAbility}s for
 * a single {@link us.eunoians.mcrpg.skill.AbstractSkill}.
 *
 * @author DiamondDagger590
 */
public class CooldownManager {

    @NotNull
    private final Map<UUID, CooldownWrapper> cooldownWrappers;

    public CooldownManager() {
        this.cooldownWrappers = new HashMap<>();
    }

    /**
     * Gets the {@link CooldownWrapper} associated with the provided {@link UUID}.
     * <p>
     * The object being present means that at some point the entity had an ability on cooldown
     *
     * @param uuid The {@link UUID} of the {@link us.eunoians.mcrpg.api.AbilityHolder} to get the {@link CooldownWrapper} for
     * @return {@code null} if there is no valid {@link CooldownWrapper} for the provided {@link UUID} or the {@link CooldownWrapper} object
     */
    @NotNull
    public CooldownWrapper getCooldownWrapper(@NotNull UUID uuid) {
        if (!cooldownWrappers.containsKey(uuid)) {
            cooldownWrappers.put(uuid, new CooldownWrapper(uuid));
        }
        return cooldownWrappers.get(uuid);
    }

    /**
     * Removes the {@link CooldownWrapper} from storage.
     * <p>
     * This is mostly called whenever a {@link org.bukkit.entity.Player} logs out and needs their data
     * removed from memory.
     * <p>
     * This method also clears all lunar client cooldowns that were sent due to cooldowns from abilities
     * <p>
     * This method doesn't handle any sort of persistence and should be implemented in the {@link us.eunoians.mcrpg.api.AbilityHolder}.
     *
     * @param uuid The {@link UUID} of the entity who's cooldown is being removed
     */
    public void removeCooldownWrapper(@NotNull UUID uuid) {
        cooldownWrappers.remove(uuid).wipeLunar();
    }

    /**
     * This class holds information regarding a skill and contains the methods needed to handle cooldowns for a specific skill
     *
     * @author DiamondDagger590
     */
    public class CooldownWrapper {

        @NotNull
        private final Map<NamespacedKey, SkillCooldownWrapper> skillCooldowns;

        @NotNull
        private final UUID uuid;

        public CooldownWrapper(@NotNull UUID uuid) {
            this.skillCooldowns = new HashMap<>();
            this.uuid = uuid;
        }

        /**
         * Puts the provided {@link BaseAbility} on cooldown that will expire at the provided time.
         * <p>
         * This method requires input of a {@link BaseAbility} for methods needed for storage, but it does validate that the provided
         * ability is a {@link CooldownableAbility}, and if it is not, will return before doing anything.
         * <p>
         * This method also automatically displays a {@link LCCooldown} if the ability activator is a {@link Player} and
         * lunar is valid.
         *
         * @param ability      The {@link BaseAbility} that is being put on cooldown
         * @param expireTime   The time in millis that the ability expires from cooldown
         * @param displayLunar If false, then no {@link LCCooldown} will be displayed to the player even if it's valid
         */
        public void putAbilityOnCooldown(@NotNull BaseAbility ability, long expireTime, boolean displayLunar) {

            if (!(ability instanceof CooldownableAbility)) {
                return;
            }

            NamespacedKey skillKey = ability.getSkill();
            NamespacedKey abilityKey = Ability.getId(ability.getClass());

            if (skillCooldowns.containsKey(skillKey)) {
                SkillCooldownWrapper skillCooldownWrapper = getSkillCooldownWrapper(skillKey);
                skillCooldownWrapper.setCooldown(abilityKey, expireTime);
            }
            else {
                SkillCooldownWrapper skillCooldownWrapper = new SkillCooldownWrapper();
                skillCooldownWrapper.setCooldown(abilityKey, expireTime);

                skillCooldowns.put(skillKey, skillCooldownWrapper);
            }

            //Handle sending lunar cooldowns
            if (displayLunar && McRPG.getInstance().getLunarClientHook() != null && Bukkit.getPlayer(uuid) != null
                    && McRPG.getInstance().getLunarClientHook().getLunarClientAPI().isRunningLunarClient(uuid)) {

                LunarClientHook lunarClientHook = McRPG.getInstance().getLunarClientHook();
                LunarClientAPI lunarClientAPI = lunarClientHook.getLunarClientAPI();

                //We have to subtract current time from expire time in order to handle modifying of a cooldown before passing into this method
                LCCooldown lcCooldown = null;
                //TODO we need to update this when they update their api
                ///if(LunarClientAPICooldown.sendCooldown();)
                //= new LCCooldown(ability.getAbilityID().getKey(), (int) (expireTime - System.currentTimeMillis()), TimeUnit.MILLISECONDS, ability.getDisplayItem().getType());
                lcCooldown.send(Bukkit.getPlayer(uuid));
            }
        }

        /**
         * Checks to see if the provided {@link NamespacedKey} that maps to an {@link us.eunoians.mcrpg.skill.AbstractSkill} has any cooldowns
         *
         * @param namespacedKey The {@link NamespacedKey} that maps to an {@link us.eunoians.mcrpg.skill.AbstractSkill}
         * @return {@code true} if the provided {@link NamespacedKey} maps to an {@link us.eunoians.mcrpg.skill.AbstractSkill}
         */
        public boolean doesSkillHaveCooldown(@NotNull NamespacedKey namespacedKey) {
            return !getAbilitiesOnCooldown(namespacedKey).isEmpty();
        }

        /**
         * Gets the {@link SkillCooldownWrapper} that holds all cooldown information relating to the {@link us.eunoians.mcrpg.skill.AbstractSkill} that maps
         * to the provided {@link NamespacedKey}.
         *
         * @param namespacedKey The {@link NamespacedKey} that belongs to the {@link us.eunoians.mcrpg.skill.AbstractSkill}
         * @return The {@link SkillCooldownWrapper} that holds all cooldown information for the {@link us.eunoians.mcrpg.skill.AbstractSkill} that maps
         * to the provided {@link NamespacedKey}.
         */
        @NotNull
        public SkillCooldownWrapper getSkillCooldownWrapper(@NotNull NamespacedKey namespacedKey) {
            if (!skillCooldowns.containsKey(namespacedKey)) {
                skillCooldowns.put(namespacedKey, new SkillCooldownWrapper());
            }

            return skillCooldowns.get(namespacedKey);
        }

        /**
         * Gets all {@link us.eunoians.mcrpg.ability.CooldownableAbility}s that are currently on cooldown for the {@link NamespacedKey} provided that maps to a specific skill
         *
         * @param skillKey The {@link NamespacedKey} that maps to a specific {@link us.eunoians.mcrpg.skill.AbstractSkill}
         * @return A {@link Map} that is either empty if the provided {@link NamespacedKey} for the {@link us.eunoians.mcrpg.skill.AbstractSkill} is invalid or it will contain
         * a {@link NamespacedKey} key value for each ability on cooldown along with the EXPIRATION time of the ability in millis as opposed to the millis remaining.
         */
        @NotNull
        public Map<NamespacedKey, Long> getAbilitiesOnCooldown(@NotNull NamespacedKey skillKey) {

            //This map will be returned at the wend of the method
            Map<NamespacedKey, Long> abilitiesOnCooldown = new HashMap<>();

            //Checks to see if the skill is even valid
            if (skillCooldowns.containsKey(skillKey)) {

                SkillCooldownWrapper skillCooldownWrapper = skillCooldowns.get(skillKey);

                //Gets all abilities that are on cooldown. Reason we don't return a direct copy of the map is to validate cooldowns before returning
                Set<NamespacedKey> abilities = skillCooldownWrapper.getAbilitiesOnCooldown();

                //Loop through all abilities
                for (NamespacedKey abilityKey : abilities) {

                    //Validate cooldown before putting it in the map to be returned
                    if (skillCooldownWrapper.isOnCooldown(abilityKey)) {
                        abilitiesOnCooldown.put(abilityKey, skillCooldownWrapper.getCooldownExpireTime(abilityKey));
                    }
                }

                //We wait until we validate all abilities to remove the skillCooldownWrapper object from the stored values
                if (abilitiesOnCooldown.isEmpty()) {
                    skillCooldowns.remove(skillKey);
                    return abilitiesOnCooldown;
                }

            }

            return abilitiesOnCooldown;
        }

        /**
         * Wipe all lunar client cooldowns for the {@link UUID} of the {@link us.eunoians.mcrpg.api.AbilityHolder} represented by this object
         */
        public void wipeLunar() {
            if (McRPG.getInstance().getLunarClientHook() != null && Bukkit.getPlayer(uuid) != null &&
                    McRPG.getInstance().getLunarClientHook().getLunarClientAPI().isRunningLunarClient(uuid)) {

                Optional<McRPGPlayer> mcRPGPlayerOptional = McRPG.getInstance().getPlayerContainer().getPlayer(uuid);

                if (mcRPGPlayerOptional.isPresent()) {

                    McRPGPlayer mcRPGPlayer = mcRPGPlayerOptional.get();
                    LunarClientHook lunarClientHook = McRPG.getInstance().getLunarClientHook();
                    LunarClientAPI lunarClientAPI = lunarClientHook.getLunarClientAPI();

                    for (NamespacedKey namespacedKey : McRPG.getInstance().getAbilityRegistry().getAllRegisteredAbilityKeys()) {

                        Ability ability = mcRPGPlayer.getAbility(namespacedKey);

                        if (ability != null) {
                            //TODO update whenever lunar updates their API
//                            try {
//                                LunarClientAPICooldown.clearCooldown(mcRPGPlayer.getEntity(), );
//                                lunarClientAPI.clearCooldown(Objects.requireNonNull(mcRPGPlayer.getEntity()),
//                                        new LCCooldown("Ability Cooldown", 0, TimeUnit.SECONDS, ability.getDisplayItem().getType()));
//                            } catch (AbilityConfigurationNotFoundException e) {
//                                e.printStackTrace();
//                                return;
//                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This class holds information regarding abilities on cooldown that all belong to a single skill
     *
     * @author DiamondDagger590
     */
    public class SkillCooldownWrapper {

        /**
         * This contains a key of {@link NamespacedKey} that represents an ability on cooldown while the
         * {@link Long} value represents the time in millis that the ability gets off cooldown
         */
        @NotNull
        private final Map<NamespacedKey, Long> abilitiesCooldown;

        public SkillCooldownWrapper() {
            abilitiesCooldown = new HashMap<>();
        }

        /**
         * Checks to see if the {@link us.eunoians.mcrpg.ability.CooldownableAbility} mapped to the provided
         * {@link NamespacedKey}.
         *
         * @param abilityKey The {@link NamespacedKey} that maps to a {@link us.eunoians.mcrpg.ability.CooldownableAbility}
         * @return {@code true} if the {@link NamespacedKey} maps to an {@link us.eunoians.mcrpg.ability.CooldownableAbility}
         * that is currently on cooldown
         */
        public boolean isOnCooldown(@NotNull NamespacedKey abilityKey) {
            return getMilisLeftOnCooldown(abilityKey) > -1;
        }

        /**
         * Gets the amount of millis left on cooldown for the {@link NamespacedKey} provided
         *
         * @param abilityKey The {@link NamespacedKey} to check
         * @return The remaining milis of the cooldown or {@code -1} if there is no cooldown.
         */
        public long getMilisLeftOnCooldown(@NotNull NamespacedKey abilityKey) {

            long cooldownRemaining = abilitiesCooldown.getOrDefault(abilityKey, -1L);

            if (cooldownRemaining <= System.currentTimeMillis()) {
                abilitiesCooldown.remove(abilityKey);
                cooldownRemaining = -1;
            }

            return cooldownRemaining - System.currentTimeMillis();
        }

        /**
         * Sets the {@link us.eunoians.mcrpg.ability.CooldownableAbility} that maps to the provided {@link NamespacedKey}
         * on a cooldown for the duration provided
         *
         * @param abilityKey              The {@link NamespacedKey} that maps to a {@link us.eunoians.mcrpg.ability.CooldownableAbility} that should be put on cooldown
         * @param cooldownExpireTimeMilis The end time in miliseconds for when the desired {@link us.eunoians.mcrpg.ability.CooldownableAbility} should be taken off cooldonw
         */
        public void setCooldown(@NotNull NamespacedKey abilityKey, long cooldownExpireTimeMilis) {
            abilitiesCooldown.put(abilityKey, cooldownExpireTimeMilis);
        }

        /**
         * Removes the provided {@link NamespacedKey} from being on cooldown
         *
         * @param abilityKey The {@link NamespacedKey} that maps to an {@link us.eunoians.mcrpg.ability.ReadyableAbility} to be removed off cooldown
         */
        public void removeFromCooldown(@NotNull NamespacedKey abilityKey) {
            abilitiesCooldown.remove(abilityKey);
        }

        /**
         * Gets a {@link Set} of {@link NamespacedKey}s that are currently "on cooldown".
         * <p>
         * This method does not verify if any of these abilities are still on a valid cooldown
         * and this should be done on one's own depending on the use case.
         *
         * @return The {@link Set} of {@link NamespacedKey}s that are currently "on cooldown".
         */
        @NotNull
        public Set<NamespacedKey> getAbilitiesOnCooldown() {
            return abilitiesCooldown.keySet();
        }

        /**
         * Gets the expire time of the cooldown of the {@link us.eunoians.mcrpg.ability.CooldownableAbility} in millis.
         * <p>
         * Calling this method also will remove a no longer valid cooldown from the stored data and return {@code -1} in that case.
         *
         * @param abilityKey The {@link NamespacedKey} that maps to the {@link us.eunoians.mcrpg.ability.CooldownableAbility} that is being referenced
         * @return The expire time of the cooldown in millis or {@code -1} if there is not a valid cooldown.
         */
        public long getCooldownExpireTime(@NotNull NamespacedKey abilityKey) {
            long cooldownExpireTime = abilitiesCooldown.getOrDefault(abilityKey, -1L);

            if (cooldownExpireTime <= System.currentTimeMillis()) {
                abilitiesCooldown.remove(abilityKey);
                cooldownExpireTime = -1;
            }

            return cooldownExpireTime;
        }
    }

}
