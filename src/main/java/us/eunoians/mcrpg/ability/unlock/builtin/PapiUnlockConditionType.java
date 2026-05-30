package us.eunoians.mcrpg.ability.unlock.builtin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.exception.UnlockConditionParseException;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;
import us.eunoians.mcrpg.util.compare.ComparisonOperator;

import java.util.Map;
import java.util.Optional;

/**
 * Unlock condition met when a PlaceholderAPI placeholder, resolved against the holder,
 * satisfies the configured comparison. Uses {@link ComparisonOperator} for the actual
 * comparison logic, which is shared with other scripting surfaces.
 * <p>
 * PAPI is a soft dependency: when PAPI is absent, {@link McRPGMethods#applyPapi} returns the
 * input unchanged, and an unchanged-still-contains-{@code %} string is treated as "not met"
 * rather than throwing.
 */
public final class PapiUnlockConditionType implements UnlockConditionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "papi");

    private final String placeholder;
    private final ComparisonOperator operator;
    private final String value;
    private final Route localeKey;
    private final String inlineText;

    public PapiUnlockConditionType() {
        this(null, null, null, null, null);
    }

    public PapiUnlockConditionType(@Nullable String placeholder, @Nullable ComparisonOperator operator,
                                   @Nullable String value, @Nullable Route localeKey,
                                   @Nullable String inlineText) {
        this.placeholder = placeholder;
        this.operator = operator;
        this.value = value;
        this.localeKey = localeKey;
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
        String placeholderRaw = section.getString("placeholder");
        if (placeholderRaw == null || placeholderRaw.isBlank()) {
            throw new UnlockConditionParseException("mcrpg:papi requires a 'placeholder' key");
        }
        String operatorRaw = section.getString("operator");
        if (operatorRaw == null || operatorRaw.isBlank()) {
            throw new UnlockConditionParseException("mcrpg:papi requires an 'operator' key");
        }
        if (!section.contains("value")) {
            throw new UnlockConditionParseException("mcrpg:papi requires a 'value' key");
        }
        ComparisonOperator parsedOperator = ComparisonOperator.fromSymbol(operatorRaw).orElseThrow(() ->
                new UnlockConditionParseException("Unknown PAPI operator '" + operatorRaw
                        + "'. Valid operators: >=, >, ==, <=, <, !="));
        String valueRaw = String.valueOf(section.get("value"));
        boolean hasKey = section.contains("locale-key");
        boolean hasText = section.contains("text");
        if (hasKey && hasText) {
            throw new UnlockConditionParseException(
                    "mcrpg:papi may set at most one of 'locale-key' or 'text'");
        }
        Route customKey = hasKey ? Route.fromString(section.getString("locale-key")) : null;
        String customText = hasText ? section.getString("text") : null;
        return new PapiUnlockConditionType(placeholderRaw, parsedOperator, valueRaw, customKey, customText);
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        if (placeholder == null || operator == null || value == null) {
            return false;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(holder.getUUID());
        String resolved = McRPGMethods.applyPapi(placeholder, offlinePlayer);
        if (resolved.equals(placeholder)) {
            return false;
        }
        return operator.compare(resolved, value);
    }

    @NotNull
    @Override
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        if (placeholder == null || operator == null || value == null) {
            return Component.empty();
        }
        McRPGLocalizationManager localization = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        String resolved = McRPGMethods.applyPapi(placeholder, Bukkit.getOfflinePlayer(player.getUUID()));
        Map<String, String> placeholders = Map.of(
                "placeholder", placeholder,
                "operator", operator.getSymbol(),
                "required", value,
                "current", resolved);
        if (localeKey != null) {
            return localization.getLocalizedMessageAsComponent(player, localeKey, placeholders);
        }
        if (inlineText != null) {
            String text = localization.getLocalizedMessage(
                    localization.resolvePaletteColors(inlineText), placeholders);
            return McRPG.getInstance().getMiniMessage().deserialize(text);
        }
        return localization.getLocalizedMessageAsComponent(player,
                LocalizationKey.UNLOCK_CONDITION_PAPI_DESCRIPTION, placeholders);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
