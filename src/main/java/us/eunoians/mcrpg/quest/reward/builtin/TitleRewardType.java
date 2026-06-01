package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.diamonddagger590.mccore.registry.RegistryKey;

/**
 * Reward type that sends a title and/or subtitle to the rewarded player.
 * <p>
 * YAML configuration:
 * <pre>
 * graduation_title:
 *   type: mcrpg:title_message
 *   title: "&lt;primary&gt;Tutorial Complete!"     # optional (empty if omitted)
 *   subtitle: "&lt;body&gt;You're ready to explore McRPG."  # optional
 *   fade-in: 10    # ticks, default 10
 *   stay: 70       # ticks, default 70
 *   fade-out: 20   # ticks, default 20
 * </pre>
 * <p>
 * Both {@code title} and {@code subtitle} pass through palette replacement
 * and MiniMessage parsing before display.
 * <p>
 * This reward is invisible in GUI lore — {@link #describeForDisplay()} returns an empty string.
 */
public final class TitleRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "title_message");

    private static final long MILLIS_PER_TICK = 50L;
    private static final int DEFAULT_FADE_IN = 10;
    private static final int DEFAULT_STAY = 70;
    private static final int DEFAULT_FADE_OUT = 20;

    private final String titleStr;
    private final String subtitleStr;
    private final int fadeInTicks;
    private final int stayTicks;
    private final int fadeOutTicks;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public TitleRewardType() {
        this.titleStr = "";
        this.subtitleStr = "";
        this.fadeInTicks = DEFAULT_FADE_IN;
        this.stayTicks = DEFAULT_STAY;
        this.fadeOutTicks = DEFAULT_FADE_OUT;
    }

    private TitleRewardType(@NotNull String titleStr, @NotNull String subtitleStr,
                            int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.titleStr = titleStr;
        this.subtitleStr = subtitleStr;
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
    }

    /**
     * Returns the unique key for this reward type.
     *
     * @return {@code mcrpg:title_message}
     */
    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    /**
     * Parses title reward configuration from a YAML section.
     *
     * @param section the section containing optional {@code title}, {@code subtitle},
     *                {@code fade-in}, {@code stay}, and {@code fade-out} fields
     * @return a configured instance
     */
    @NotNull
    @Override
    public TitleRewardType parseConfig(@NotNull Section section) {
        String title = section.contains("title") ? section.getString("title") : "";
        String subtitle = section.contains("subtitle") ? section.getString("subtitle") : "";
        int fadeIn = section.contains("fade-in") ? ((Number) section.get("fade-in")).intValue() : DEFAULT_FADE_IN;
        int stay = section.contains("stay") ? ((Number) section.get("stay")).intValue() : DEFAULT_STAY;
        int fadeOut = section.contains("fade-out") ? ((Number) section.get("fade-out")).intValue() : DEFAULT_FADE_OUT;
        return new TitleRewardType(title != null ? title : "", subtitle != null ? subtitle : "",
                fadeIn, stay, fadeOut);
    }

    /**
     * Sends the configured title and subtitle to the player after applying palette
     * replacements and MiniMessage parsing.
     *
     * @param player the player to show the title to
     */
    @Override
    public void grant(@NotNull Player player) {
        McRPGLocalizationManager locManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Map<String, String> paletteReplacements = locManager.getPaletteReplacements();

        String resolvedTitle = applyPalette(titleStr, paletteReplacements);
        String resolvedSubtitle = applyPalette(subtitleStr, paletteReplacements);

        Component titleComponent = McRPG.getInstance().getMiniMessage().deserialize(resolvedTitle);
        Component subtitleComponent = McRPG.getInstance().getMiniMessage().deserialize(resolvedSubtitle);

        player.showTitle(Title.title(titleComponent, subtitleComponent,
                Title.Times.times(
                        Duration.ofMillis(fadeInTicks * MILLIS_PER_TICK),
                        Duration.ofMillis(stayTicks * MILLIS_PER_TICK),
                        Duration.ofMillis(fadeOutTicks * MILLIS_PER_TICK))));
    }

    /**
     * Serializes the title reward config for pending-reward persistence.
     *
     * @return map with {@code title}, {@code subtitle}, {@code fade-in}, {@code stay},
     *         and {@code fade-out} keys
     */
    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", titleStr);
        map.put("subtitle", subtitleStr);
        map.put("fade-in", fadeInTicks);
        map.put("stay", stayTicks);
        map.put("fade-out", fadeOutTicks);
        return map;
    }

    /**
     * Reconstructs a configured instance from a serialized config map.
     *
     * @param config the previously serialized map
     * @return a configured instance
     */
    @NotNull
    @Override
    public TitleRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String title = config.containsKey("title") ? config.get("title").toString() : "";
        String subtitle = config.containsKey("subtitle") ? config.get("subtitle").toString() : "";
        int fadeIn = config.containsKey("fade-in") ? ((Number) config.get("fade-in")).intValue() : DEFAULT_FADE_IN;
        int stay = config.containsKey("stay") ? ((Number) config.get("stay")).intValue() : DEFAULT_STAY;
        int fadeOut = config.containsKey("fade-out") ? ((Number) config.get("fade-out")).intValue() : DEFAULT_FADE_OUT;
        return new TitleRewardType(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Title rewards are invisible in GUI lore.
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

    /**
     * Applies palette tag replacements to a MiniMessage string before parsing.
     *
     * @param input   the raw MiniMessage string that may contain palette placeholders
     * @param palette the map of palette placeholder names to their MiniMessage values
     * @return the string with all palette placeholders substituted
     */
    @NotNull
    private String applyPalette(@NotNull String input, @NotNull Map<String, String> palette) {
        String result = input;
        for (Map.Entry<String, String> entry : palette.entrySet()) {
            result = result.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return result;
    }
}
