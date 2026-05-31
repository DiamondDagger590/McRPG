package us.eunoians.mcrpg.ability.unlock.builtin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionManager;
import us.eunoians.mcrpg.exception.UnlockConditionParseException;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Optional;

/**
 * Composite condition that is met when <i>any</i> child condition is met. Useful for nesting
 * an additional OR-group inside an outer composite; the top-level
 * {@link us.eunoians.mcrpg.ability.impl.type.UnlockableAbility#getUnlockConditions()} list is
 * already OR-combined.
 */
public final class AnyOfUnlockConditionType implements UnlockConditionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "any_of");

    private final List<UnlockConditionType> children;

    public AnyOfUnlockConditionType() {
        this(List.of());
    }

    public AnyOfUnlockConditionType(@NotNull List<UnlockConditionType> children) {
        this.children = List.copyOf(children);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public UnlockConditionType parseConfig(@NotNull Section section) {
        Section nested = section.getOptionalSection("conditions").orElseThrow(() ->
                new UnlockConditionParseException(KEY, "mcrpg:any_of requires a 'conditions' section"));
        UnlockConditionManager manager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.UNLOCK_CONDITION);
        List<UnlockConditionType> parsed = manager.parseSection(nested);
        if (parsed.isEmpty()) {
            throw new UnlockConditionParseException(KEY, "mcrpg:any_of requires at least one child");
        }
        return new AnyOfUnlockConditionType(parsed);
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        if (children.isEmpty()) {
            return false;
        }
        for (UnlockConditionType child : children) {
            if (child.isMet(holder)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getProgress(@NotNull AbilityHolder holder) {
        if (children.isEmpty()) {
            return 0.0;
        }
        double max = 0.0;
        for (UnlockConditionType child : children) {
            max = Math.max(max, child.getProgress(holder));
        }
        return max;
    }

    @NotNull
    @Override
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        McRPGLocalizationManager localization = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component header = localization.getLocalizedMessageAsComponent(player,
                LocalizationKey.UNLOCK_CONDITION_ANY_OF_HEADER);
        Component result = header;
        for (UnlockConditionType child : children) {
            Component bullet = localization.getLocalizedMessageAsComponent(player,
                    LocalizationKey.UNLOCK_CONDITION_BULLET);
            result = result.append(Component.newline())
                    .append(bullet)
                    .append(child.getDisplayDescription(player));
        }
        return result;
    }

    @NotNull
    public List<UnlockConditionType> getChildren() {
        return children;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
