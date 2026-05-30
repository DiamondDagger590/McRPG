package us.eunoians.mcrpg.ability.unlock.builtin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionParseException;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Display-only unlock condition: never met by McRPG, used to advertise an externally-driven
 * unlock path (skill books, crate plugins, achievements, etc.). The external system flips
 * {@link us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute} directly when it
 * grants the unlock.
 * <p>
 * Configured via exactly one of:
 * <ul>
 *   <li>{@code locale-key} — translatable through the player's locale chain. Preferred for
 *       bundled / multi-language hint text.</li>
 *   <li>{@code text} — inline MiniMessage, single-language, palette-resolved. For
 *       server-owner-specific advertising like "Epic Crates!".</li>
 * </ul>
 */
public final class DisplayHintUnlockConditionType implements UnlockConditionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "display_hint");

    private final Route localeKey;
    private final String inlineText;

    public DisplayHintUnlockConditionType() {
        this.localeKey = null;
        this.inlineText = null;
    }

    public DisplayHintUnlockConditionType(@NotNull Route localeKey) {
        this.localeKey = localeKey;
        this.inlineText = null;
    }

    public DisplayHintUnlockConditionType(@NotNull String inlineText) {
        this.localeKey = null;
        this.inlineText = inlineText;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public UnlockConditionType parseConfig(@NotNull Section section) {
        boolean hasKey = section.contains("locale-key");
        boolean hasText = section.contains("text");
        if (hasKey == hasText) {
            throw new UnlockConditionParseException(
                    "mcrpg:display_hint requires exactly one of 'locale-key' or 'text'");
        }
        if (hasKey) {
            String raw = section.getString("locale-key");
            if (raw == null || raw.isBlank()) {
                throw new UnlockConditionParseException(
                        "mcrpg:display_hint 'locale-key' must not be blank");
            }
            return new DisplayHintUnlockConditionType(Route.fromString(raw));
        }
        String text = section.getString("text");
        if (text == null || text.isBlank()) {
            throw new UnlockConditionParseException(
                    "mcrpg:display_hint 'text' must not be blank");
        }
        return new DisplayHintUnlockConditionType(text);
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        return false;
    }

    @Override
    public boolean isDisplayOnly() {
        return true;
    }

    @NotNull
    @Override
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        McRPGLocalizationManager localization = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        if (localeKey != null) {
            return localization.getLocalizedMessageAsComponent(player, localeKey);
        }
        if (inlineText != null) {
            String resolved = localization.resolvePaletteColors(inlineText);
            return McRPG.getInstance().getMiniMessage().deserialize(resolved);
        }
        return Component.empty();
    }

    @Nullable
    public Route getLocaleKey() {
        return localeKey;
    }

    @Nullable
    public String getInlineText() {
        return inlineText;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
