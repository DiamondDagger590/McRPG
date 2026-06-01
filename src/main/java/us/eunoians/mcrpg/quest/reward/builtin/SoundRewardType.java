package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reward type that plays a sound to the rewarded player.
 * <p>
 * YAML configuration:
 * <pre>
 * celebration_sound:
 *   type: mcrpg:sound
 *   sound: UI_TOAST_CHALLENGE_COMPLETE
 *   volume: 1.0    # optional, default 1.0
 *   pitch: 1.0     # optional, default 1.0
 * </pre>
 * <p>
 * This reward is invisible in GUI lore — {@link #describeForDisplay()} returns an empty string.
 */
public final class SoundRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "sound");

    private static final float DEFAULT_VOLUME = 1.0f;
    private static final float DEFAULT_PITCH = 1.0f;

    private final Sound sound;
    private final float volume;
    private final float pitch;

    /**
     * Creates an unconfigured base instance for registry registration.
     * Fields default to null/defaults; this instance must not be used to grant rewards.
     */
    public SoundRewardType() {
        this.sound = null;
        this.volume = DEFAULT_VOLUME;
        this.pitch = DEFAULT_PITCH;
    }

    private SoundRewardType(@NotNull Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Returns the unique key for this reward type.
     *
     * @return {@code mcrpg:sound}
     */
    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    /**
     * Parses sound reward configuration from a YAML section.
     *
     * @param section the section containing {@code sound}, optional {@code volume}, optional {@code pitch}
     * @return a configured instance, or a no-op instance if {@code sound} is invalid
     */
    @NotNull
    @Override
    public SoundRewardType parseConfig(@NotNull Section section) {
        String soundName = section.getString("sound");
        if (soundName == null) {
            McRPG.getInstance().getLogger().warning("[SoundRewardType] Missing 'sound' field in reward config");
            return new SoundRewardType();
        }
        Sound parsedSound;
        try {
            parsedSound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            McRPG.getInstance().getLogger().warning("[SoundRewardType] Unknown sound: " + soundName);
            return new SoundRewardType();
        }
        float vol = section.contains("volume") ? ((Number) section.get("volume")).floatValue() : DEFAULT_VOLUME;
        float pit = section.contains("pitch") ? ((Number) section.get("pitch")).floatValue() : DEFAULT_PITCH;
        return new SoundRewardType(parsedSound, vol, pit);
    }

    /**
     * Plays the configured sound to the player at the player's current location.
     * If this instance has no valid sound (e.g. constructed as base registry instance
     * or due to a bad config), this method is a no-op.
     *
     * @param player the player to play the sound to
     */
    @Override
    public void grant(@NotNull Player player) {
        if (sound == null) {
            return;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Serializes the sound reward config for pending-reward persistence.
     *
     * @return map with {@code sound}, {@code volume}, and {@code pitch} keys
     */
    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        if (sound != null) {
            map.put("sound", sound.name());
            map.put("volume", volume);
            map.put("pitch", pitch);
        }
        return map;
    }

    /**
     * Reconstructs a configured instance from a serialized config map.
     *
     * @param config the previously serialized map
     * @return a configured instance, or no-op instance on missing/invalid sound
     */
    @NotNull
    @Override
    public SoundRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        Object rawSound = config.get("sound");
        if (rawSound == null) {
            McRPG.getInstance().getLogger().warning("[SoundRewardType] Missing 'sound' in serialized config");
            return new SoundRewardType();
        }
        Sound parsedSound;
        try {
            parsedSound = Sound.valueOf(rawSound.toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            McRPG.getInstance().getLogger().warning("[SoundRewardType] Unknown sound in serialized config: " + rawSound);
            return new SoundRewardType();
        }
        float vol = config.containsKey("volume") ? ((Number) config.get("volume")).floatValue() : DEFAULT_VOLUME;
        float pit = config.containsKey("pitch") ? ((Number) config.get("pitch")).floatValue() : DEFAULT_PITCH;
        return new SoundRewardType(parsedSound, vol, pit);
    }

    /**
     * Sound rewards are invisible in GUI lore.
     *
     * @return empty string
     */
    @NotNull
    @Override
    public String describeForDisplay() {
        return "";
    }

    /**
     * Returns the expansion key for the McRPG built-in expansion.
     *
     * @return the McRPG expansion key
     */
    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
