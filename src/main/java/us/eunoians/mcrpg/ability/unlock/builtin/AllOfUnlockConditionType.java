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
 * Composite condition that is met only when <i>every</i> child condition is met.
 * Children are parsed once at config-load time via the
 * {@link UnlockConditionManager#parseSection(Section)} recursion and held immutably.
 */
public final class AllOfUnlockConditionType implements UnlockConditionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "all_of");

    private final List<UnlockConditionType> children;

    public AllOfUnlockConditionType() {
        this(List.of());
    }

    public AllOfUnlockConditionType(@NotNull List<UnlockConditionType> children) {
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
                new UnlockConditionParseException("mcrpg:all_of requires a 'conditions' section"));
        UnlockConditionManager manager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.UNLOCK_CONDITION);
        List<UnlockConditionType> parsed = manager.parseSection(nested);
        if (parsed.isEmpty()) {
            throw new UnlockConditionParseException("mcrpg:all_of requires at least one child");
        }
        return new AllOfUnlockConditionType(parsed);
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        if (children.isEmpty()) {
            return false;
        }
        for (UnlockConditionType child : children) {
            if (!child.isMet(holder)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double getProgress(@NotNull AbilityHolder holder) {
        if (children.isEmpty()) {
            return 0.0;
        }
        double min = 1.0;
        for (UnlockConditionType child : children) {
            min = Math.min(min, child.getProgress(holder));
        }
        return min;
    }

    @NotNull
    @Override
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        McRPGLocalizationManager localization = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component header = localization.getLocalizedMessageAsComponent(player,
                LocalizationKey.UNLOCK_CONDITION_ALL_OF_HEADER);
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

    /**
     * The parsed children of this composite, in config order.
     *
     * @return immutable list of children
     */
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
