package us.eunoians.mcrpg.gui.ability.slot;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.localization.McRPGDisplayDecimalFormatter;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests that {@link AbilitySlot#getItem(McRPGPlayer)} injects the correct ability type tag
 * lore line based on the ability's interface hierarchy and applicable attributes.
 * <p>
 * Classification rules under test:
 * <ul>
 *   <li>{@link ComboActivatable} → "Active" type tag</li>
 *   <li>{@link PassiveAbility} + {@link AbilityAttributeRegistry#ABILITY_UNLOCKED_ATTRIBUTE} in applicable attributes → "Passive"</li>
 *   <li>{@link PassiveAbility} without {@link AbilityAttributeRegistry#ABILITY_UNLOCKED_ATTRIBUTE} → "Innate"</li>
 *   <li>Neither {@link ComboActivatable} nor {@link PassiveAbility} → "Innate"</li>
 *   <li>{@link ComboActivatable} + {@link PassiveAbility} → "Active" (ComboActivatable takes priority)</li>
 * </ul>
 * <p>
 * Each test creates a mock {@link AbilityItemBuilder} stored on the ability stub so that
 * {@link AbilityItemBuilder#addDisplayLoreComponent(Component)} calls can be captured
 * and inspected via Mockito's {@link ArgumentCaptor}.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class AbilitySlotTypeResolutionTest extends McRPGBaseTest {

    /** Locale message map keyed by route for the localization manager mock. */
    private static final Map<Route, String> LOCALE_MESSAGES = Map.of(
            LocalizationKey.ABILITY_LORE_TYPE_ACTIVE, "Type: Active",
            LocalizationKey.ABILITY_LORE_TYPE_PASSIVE, "Type: Passive",
            LocalizationKey.ABILITY_LORE_TYPE_INNATE, "Type: Innate",
            LocalizationKey.ABILITY_LORE_MANA_COST_LINE, "Mana Cost: <mana-cost>",
            LocalizationKey.ABILITY_LORE_STATUS_ENABLED, "Status: Enabled",
            LocalizationKey.ABILITY_LORE_STATUS_DISABLED, "Status: Disabled",
            LocalizationKey.ABILITY_LORE_HINT_TOGGLE_ENABLE, "Left-click to enable",
            LocalizationKey.ABILITY_LORE_HINT_TOGGLE_DISABLE, "Left-click to disable",
            LocalizationKey.ABILITY_LORE_HINT_CONFIGURE, "Right-click to configure",
            LocalizationKey.ABILITY_LORE_HINT_CONFIGURE_BEDROCK, "Click to configure"
    );

    /**
     * Registers the minimal registries required by {@link AbilitySlot#getItem(McRPGPlayer)} and stubs
     * the mocked {@link McRPGLocalizationManager} to return non-null strings for every route used by the slot.
     */
    @BeforeEach
    void setUpSlotDependencies() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();
        registryAccess.register(new AbilityRegistry(mcRPG));
        registryAccess.register(new SkillRegistry());

        McRPGLocalizationManager locMgr = registryAccess
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        lenient().when(locMgr.getLocalizedMessage(any(McRPGPlayer.class), any(Route.class)))
                .thenAnswer(inv -> LOCALE_MESSAGES.getOrDefault(inv.getArgument(1, Route.class), ""));
        lenient().when(locMgr.getLocalizedMessages(any(McRPGPlayer.class), any(Route.class)))
                .thenReturn(List.of());

        McRPGDisplayDecimalFormatter formatter = locMgr.getDisplayDecimalFormatter();
        lenient().when(formatter.formatDisplayDecimal(any(McRPGPlayer.class), anyFloat(), anyInt(), anyInt()))
                .thenAnswer(inv -> String.valueOf(((Float) inv.getArgument(1)).intValue()));
        lenient().when(formatter.formatDisplayDecimal(any(McRPGPlayer.class), anyDouble(), anyInt(), anyInt()))
                .thenAnswer(inv -> String.valueOf(((Double) inv.getArgument(1)).intValue()));
    }

    /**
     * Collects all plain-text strings from {@link AbilityItemBuilder#addDisplayLoreComponent(Component)}
     * calls recorded on the given mock builder.
     *
     * @param mockBuilder The mock {@link AbilityItemBuilder} that was used during {@code getItem()}.
     * @return List of plain-text serializations of every component passed to {@code addDisplayLoreComponent}.
     */
    @NotNull
    private List<String> capturedLoreTexts(@NotNull AbilityItemBuilder mockBuilder) {
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockBuilder, atLeastOnce()).addDisplayLoreComponent(captor.capture());
        return captor.getAllValues().stream()
                .map(c -> PlainTextComponentSerializer.plainText().serialize(c))
                .toList();
    }

    @Test
    @DisplayName("ComboActivatable ability produces 'Active' type tag in lore")
    void comboActivatable_producesActiveTypeTag(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new ActiveAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> loreTexts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(loreTexts.stream().anyMatch(t -> t.contains("Active")),
                "Expected lore line containing 'Active' for a ComboActivatable ability. Got: " + loreTexts);
    }

    @Test
    @DisplayName("PassiveAbility with ABILITY_UNLOCKED_ATTRIBUTE produces 'Passive' type tag in lore")
    void passiveAbilityWithUnlockAttribute_producesPassiveTypeTag(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new PassiveAbilityWithUnlockStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> loreTexts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(loreTexts.stream().anyMatch(t -> t.contains("Passive")),
                "Expected lore line containing 'Passive' for a PassiveAbility with ABILITY_UNLOCKED_ATTRIBUTE. Got: " + loreTexts);
    }

    @Test
    @DisplayName("PassiveAbility without ABILITY_UNLOCKED_ATTRIBUTE produces 'Innate' type tag in lore")
    void passiveAbilityWithoutUnlockAttribute_producesInnateTypeTag(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new InnatePassiveAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> loreTexts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(loreTexts.stream().anyMatch(t -> t.contains("Innate")),
                "Expected lore line containing 'Innate' for a PassiveAbility without ABILITY_UNLOCKED_ATTRIBUTE. Got: " + loreTexts);
    }

    @Test
    @DisplayName("Ability that is neither ComboActivatable nor PassiveAbility produces 'Innate' type tag in lore")
    void neitherComboNorPassive_producesInnateTypeTag(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new PlainAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> loreTexts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(loreTexts.stream().anyMatch(t -> t.contains("Innate")),
                "Expected lore line containing 'Innate' for a plain (non-PassiveAbility, non-ComboActivatable) ability. Got: " + loreTexts);
    }

    @Test
    @DisplayName("Ability implementing both ComboActivatable and PassiveAbility resolves as Active")
    void comboAndPassive_resolvesAsActive(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new ComboAndPassiveAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> loreTexts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(loreTexts.stream().anyMatch(t -> t.contains("Active")),
                "Expected ComboActivatable to take priority and produce 'Active' type tag. Got: " + loreTexts);
    }

    /**
     * Base class for all type-resolution test stubs. Stores a public Mockito mock
     * {@link AbilityItemBuilder} so tests can inspect which {@code addDisplayLoreComponent}
     * calls were made during {@link AbilitySlot#getItem(McRPGPlayer)}.
     */
    abstract static class TypeTestAbilityBase extends BaseAbility {

        final AbilityItemBuilder mockBuilder = mock(AbilityItemBuilder.class);

        TypeTestAbilityBase(@NotNull us.eunoians.mcrpg.McRPG plugin, @NotNull String key) {
            super(plugin, new NamespacedKey(McRPGMethods.getMcRPGNamespace(), key));
        }

        @Override
        @NotNull
        public String getDatabaseName() {
            return getAbilityKey().getKey();
        }

        @Override
        @NotNull
        public String getName(@NotNull McRPGPlayer player) {
            return getAbilityKey().getKey();
        }

        @Override
        @NotNull
        public String getName() {
            return getAbilityKey().getKey();
        }

        @Override
        @NotNull
        public Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text(getAbilityKey().getKey());
        }

        @Override
        @NotNull
        public Component getDisplayName() {
            return Component.text(getAbilityKey().getKey());
        }

        @Override
        public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return true;
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @Override
        @NotNull
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }

        @Override
        @NotNull
        public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            return mockBuilder;
        }
    }

    /** Active ability: implements {@link ComboActivatable}. */
    static class ActiveAbilityStub extends TypeTestAbilityBase implements ComboActivatable {

        ActiveAbilityStub(@NotNull us.eunoians.mcrpg.McRPG plugin) {
            super(plugin, "test-active-ability");
        }

        @Override
        public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
            return true;
        }

        @Override
        public int getManaCost(@NotNull AbilityHolder abilityHolder) {
            return 30;
        }
    }

    /** Passive ability with {@link AbilityAttributeRegistry#ABILITY_UNLOCKED_ATTRIBUTE} — classifies as Passive. */
    static class PassiveAbilityWithUnlockStub extends TypeTestAbilityBase implements PassiveAbility {

        PassiveAbilityWithUnlockStub(@NotNull us.eunoians.mcrpg.McRPG plugin) {
            super(plugin, "test-passive-ability");
        }

        @Override
        @NotNull
        public Set<NamespacedKey> getApplicableAttributes() {
            return Set.of(
                    AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                    AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE
            );
        }
    }

    /** Passive ability without {@link AbilityAttributeRegistry#ABILITY_UNLOCKED_ATTRIBUTE} — classifies as Innate. */
    static class InnatePassiveAbilityStub extends TypeTestAbilityBase implements PassiveAbility {

        InnatePassiveAbilityStub(@NotNull us.eunoians.mcrpg.McRPG plugin) {
            super(plugin, "test-innate-ability");
        }

        @Override
        @NotNull
        public Set<NamespacedKey> getApplicableAttributes() {
            return Set.of(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
        }
    }

    /** Neither {@link ComboActivatable} nor {@link PassiveAbility} — classifies as Innate. */
    static class PlainAbilityStub extends TypeTestAbilityBase {

        PlainAbilityStub(@NotNull us.eunoians.mcrpg.McRPG plugin) {
            super(plugin, "test-plain-ability");
        }
    }

    /** Implements both {@link ComboActivatable} and {@link PassiveAbility} — ComboActivatable wins. */
    static class ComboAndPassiveAbilityStub extends TypeTestAbilityBase implements ComboActivatable, PassiveAbility {

        ComboAndPassiveAbilityStub(@NotNull us.eunoians.mcrpg.McRPG plugin) {
            super(plugin, "test-combo-and-passive-ability");
        }

        @Override
        public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
            return true;
        }

        @Override
        public int getManaCost(@NotNull AbilityHolder abilityHolder) {
            return 25;
        }

        @Override
        @NotNull
        public Set<NamespacedKey> getApplicableAttributes() {
            return Set.of(
                    AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                    AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE
            );
        }
    }
}
