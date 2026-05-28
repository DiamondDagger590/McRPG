package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.message.QuestMessageDeliverer;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Built-in reward type that sends informational messages to players.
 * <p>
 * Supports either a locale route key (resolved per-player through
 * {@link us.eunoians.mcrpg.localization.McRPGLocalizationManager}) or
 * a list of inline MiniMessage strings. Locale key takes priority when both
 * are present.
 * <p>
 * Config format:
 * <pre>
 * # Route-based (preferred for translatable text)
 * type: mcrpg:message
 * key: tutorial.welcome-message
 *
 * # Inline fallback
 * type: mcrpg:message
 * messages:
 *   - "&lt;primary&gt;Welcome!&lt;/primary&gt; &lt;body&gt;Your adventure begins."
 * </pre>
 */
public class MessageRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "message");

    @Nullable
    private final String localeKey;
    @NotNull
    private final List<String> inlineMessages;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public MessageRewardType() {
        this.localeKey = null;
        this.inlineMessages = List.of();
    }

    private MessageRewardType(@Nullable String localeKey, @NotNull List<String> inlineMessages) {
        this.localeKey = localeKey;
        this.inlineMessages = inlineMessages;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public MessageRewardType parseConfig(@NotNull Section section) {
        String key = section.contains("key") ? section.getString("key") : null;
        List<String> messages = section.contains("messages") ? section.getStringList("messages") : List.of();
        if (key == null && messages.isEmpty()) {
            McRPG.getInstance().getLogger().warning(
                    "Reward type '" + KEY + "' configured with no 'key' or 'messages' — reward will do nothing");
        }
        return new MessageRewardType(key, messages);
    }

    @Override
    public void grant(@NotNull Player player) {
        Optional<McRPGPlayer> mcRPGPlayerOpt = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());
        McRPGLocalizationManager locManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        QuestMessageDeliverer deliverer = new QuestMessageDeliverer(locManager, McRPG.getInstance().getMiniMessage(), McRPG.getInstance().getLogger());
        deliverer.deliver(player, mcRPGPlayerOpt.orElse(null), localeKey, inlineMessages);
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        if (localeKey != null && !localeKey.isEmpty()) {
            map.put("key", localeKey);
        }
        if (!inlineMessages.isEmpty()) {
            map.put("messages", inlineMessages);
        }
        return map;
    }

    @NotNull
    @Override
    public MessageRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String key = config.containsKey("key") ? config.get("key").toString() : null;
        List<String> messages = new ArrayList<>();
        if (config.containsKey("messages")) {
            Object raw = config.get("messages");
            if (raw instanceof List<?> rawList) {
                for (Object item : rawList) {
                    messages.add(item.toString());
                }
            }
        }
        if ((key == null || key.isEmpty()) && messages.isEmpty()) {
            McRPG.getInstance().getLogger().warning(
                    "Deserialized reward type '" + KEY + "' has no 'key' or 'messages' — reward will do nothing");
        }
        return new MessageRewardType(key, messages);
    }

    /**
     * Returns this instance unchanged — messages are not numerically scalable.
     *
     * @param multiplier ignored
     * @return this instance
     */
    @NotNull
    @Override
    public MessageRewardType withAmountMultiplier(double multiplier) {
        return this;
    }

    /**
     * Returns empty — message rewards have no numeric amount.
     *
     * @return empty
     */
    @NotNull
    @Override
    public OptionalLong getNumericAmount() {
        return OptionalLong.empty();
    }

    @NotNull
    @Override
    public String describeForDisplay() {
        return localeKey != null && !localeKey.isEmpty() ? "Message (" + localeKey + ")" : "Message";
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
