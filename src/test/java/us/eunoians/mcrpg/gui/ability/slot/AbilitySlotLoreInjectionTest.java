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
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityToggledOffAttribute;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.localization.McRPGDisplayDecimalFormatter;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * Verifies that {@link AbilitySlot#getItem(McRPGPlayer)} injects the correct set of lore lines in the correct order.
 * <p>
 * Expected lore order (non-blank lines):
 * <ol>
 *   <li>Type tag (always present)</li>
 *   <li>Mana cost (only for {@link us.eunoians.mcrpg.ability.impl.type.ManaAbility} with positive cost)</li>
 *   <li>Status (only when ability has {@link AbilityToggledOffAttribute})</li>
 *   <li>Toggle hint (only when ability has {@link AbilityToggledOffAttribute})</li>
 *   <li>Configure hint (always present for non-Bedrock players)</li>
 * </ol>
 */
@ExtendWith(McRPGPlayerExtension.class)
public class AbilitySlotLoreInjectionTest extends McRPGBaseTest {

    /** Expected type text for stubs with toggle attributes (passive/innate). */
    static final String TYPE_INNATE = "Type: Innate";
    /** Expected type text for active (combo-activatable) stubs. */
    static final String TYPE_ACTIVE = "Type: Active";
    /** Expected mana cost text (with placeholder resolved to "30"). */
    static final String MANA_COST = "Mana Cost: 30";
    /** Expected status text when the ability is enabled. */
    static final String STATUS_ENABLED = "Status: Enabled";
    /** Expected status text when the ability is toggled off. */
    static final String STATUS_DISABLED = "Status: Disabled";
    /** Expected toggle hint text shown when ability is currently disabled. */
    static final String HINT_TOGGLE_ENABLE = "Left-click to enable";
    /** Expected toggle hint text shown when ability is currently enabled. */
    static final String HINT_TOGGLE_DISABLE = "Left-click to disable";
    /** Expected configure hint text (always shown for non-Bedrock). */
    static final String HINT_CONFIGURE = "Right-click to configure";

    private static final Map<Route, String> LOCALE_MESSAGES = Map.of(
            LocalizationKey.ABILITY_LORE_TYPE_ACTIVE, TYPE_ACTIVE,
            LocalizationKey.ABILITY_LORE_TYPE_PASSIVE, "Type: Passive",
            LocalizationKey.ABILITY_LORE_TYPE_INNATE, TYPE_INNATE,
            LocalizationKey.ABILITY_LORE_MANA_COST_LINE, "Mana Cost: <mana-cost>",
            LocalizationKey.ABILITY_LORE_STATUS_ENABLED, STATUS_ENABLED,
            LocalizationKey.ABILITY_LORE_STATUS_DISABLED, STATUS_DISABLED,
            LocalizationKey.ABILITY_LORE_HINT_TOGGLE_ENABLE, HINT_TOGGLE_ENABLE,
            LocalizationKey.ABILITY_LORE_HINT_TOGGLE_DISABLE, HINT_TOGGLE_DISABLE,
            LocalizationKey.ABILITY_LORE_HINT_CONFIGURE, HINT_CONFIGURE,
            LocalizationKey.ABILITY_LORE_HINT_CONFIGURE_BEDROCK, "Click to configure"
    );

    private AbilityRegistry abilityRegistry;

    /**
     * Sets up empty {@link AbilityRegistry}, {@link AbilityAttributeRegistry}, and {@link SkillRegistry}
     * and stubs the mock {@link McRPGLocalizationManager} so that all lore-related locale messages return
     * non-null strings identifiable by test assertions.
     */
    @BeforeEach
    void setUpSlotDependencies() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();
        abilityRegistry = new AbilityRegistry(mcRPG);
        registryAccess.register(abilityRegistry);
        registryAccess.register(new AbilityAttributeRegistry());
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
     * calls recorded on the given mock builder, preserving insertion order.
     *
     * @param mockBuilder The mock {@link AbilityItemBuilder} returned by the test ability stub.
     * @return Ordered list of plain-text serializations of every component passed to {@code addDisplayLoreComponent}.
     */
    @NotNull
    private List<String> capturedLoreTexts(@NotNull AbilityItemBuilder mockBuilder) {
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(mockBuilder, atLeastOnce()).addDisplayLoreComponent(captor.capture());
        return captor.getAllValues().stream()
                .map(c -> PlainTextComponentSerializer.plainText().serialize(c))
                .toList();
    }

    /**
     * Registers a test ability in the {@link AbilityRegistry} so that
     * {@link AbilityHolder#getAbilityData(us.eunoians.mcrpg.ability.Ability)} can look it up and auto-create
     * its {@link AbilityData} from its applicable attributes.
     *
     * @param ability     The ability to register.
     * @param mcRPGPlayer The player whose skill holder will gain the ability.
     */
    private void registerAndAddAbility(@NotNull LoreTestAbilityBase ability, @NotNull McRPGPlayer mcRPGPlayer) {
        abilityRegistry.register(ability);
        mcRPGPlayer.asSkillHolder().addAvailableAbility(ability);
    }

    /**
     * Forces the ability's toggle state by setting an {@link AbilityToggledOffAttribute} on the player's
     * {@link AbilityData} for this ability. Assumes {@link #registerAndAddAbility} was already called.
     *
     * @param ability     The ability to toggle.
     * @param mcRPGPlayer The player whose data should be updated.
     * @param toggledOff  {@code true} = disabled, {@code false} = enabled.
     */
    private void setToggleState(@NotNull LoreTestAbilityBase ability,
                                @NotNull McRPGPlayer mcRPGPlayer, boolean toggledOff) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        skillHolder.getAbilityData(ability).ifPresent(data ->
                data.addAttribute(new AbilityToggledOffAttribute(toggledOff)));
    }

    // ── Type ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Type line is the first non-blank lore entry")
    void typeLine_isFirstNonBlankLoreEntry(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new InnateAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        List<String> nonBlank = texts.stream().filter(t -> !t.isBlank()).toList();
        assertFalse(nonBlank.isEmpty(), "Expected at least one non-blank lore entry");
        assertEquals(TYPE_INNATE, nonBlank.get(0),
                "Expected type tag to be the first non-blank lore line. Got: " + texts);
    }

    // ── Mana Cost ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Mana cost line appears for a ManaAbility with positive cost")
    void manaCostLine_presentForManaAbility(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new ActiveManaAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(texts.stream().anyMatch(t -> t.contains("Mana Cost")),
                "Expected a mana cost lore line for a ManaAbility. Got: " + texts);
    }

    @Test
    @DisplayName("Mana cost line shows the resolved cost value")
    void manaCostLine_containsResolvedCost(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new ActiveManaAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(texts.stream().anyMatch(t -> t.equals(MANA_COST)),
                "Expected mana cost line '" + MANA_COST + "'. Got: " + texts);
    }

    @Test
    @DisplayName("Mana cost line is absent for a non-ManaAbility")
    void manaCostLine_absentForNonManaAbility(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new InnateAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        assertFalse(texts.stream().anyMatch(t -> t.contains("Mana Cost")),
                "Did not expect a mana cost line for a non-ManaAbility. Got: " + texts);
    }

    // ── Status ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Status line shows 'Enabled' when ability is not toggled off")
    void statusLine_showsEnabled_whenAbilityIsOn(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new ToggleableInnateStub(mcRPG);
        registerAndAddAbility(ability, mcRPGPlayer);
        setToggleState(ability, mcRPGPlayer, false); // enabled

        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(texts.stream().anyMatch(t -> t.equals(STATUS_ENABLED)),
                "Expected 'Status: Enabled' when toggle is off. Got: " + texts);
    }

    @Test
    @DisplayName("Status line shows 'Disabled' when ability is toggled off")
    void statusLine_showsDisabled_whenAbilityIsOff(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new ToggleableInnateStub(mcRPG);
        registerAndAddAbility(ability, mcRPGPlayer);
        setToggleState(ability, mcRPGPlayer, true); // disabled

        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(texts.stream().anyMatch(t -> t.equals(STATUS_DISABLED)),
                "Expected 'Status: Disabled' when toggle is on. Got: " + texts);
    }

    @Test
    @DisplayName("Status line is absent for an ability without AbilityToggledOffAttribute data")
    void statusLine_absent_whenAbilityDataHasNoToggleAttribute(@NotNull McRPGPlayer mcRPGPlayer) {
        // InnateAbilityStub is NOT registered → ability data is empty → no status line
        var ability = new InnateAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        assertFalse(texts.stream().anyMatch(t -> t.contains("Status")),
                "Did not expect a status line when ability data is absent. Got: " + texts);
    }

    // ── Click Hints ────────────────────────────────────────────────────

    @Test
    @DisplayName("Toggle hint shows 'enable' hint when ability is currently toggled off")
    void toggleHint_showsEnableHint_whenAbilityIsOff(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new ToggleableInnateStub(mcRPG);
        registerAndAddAbility(ability, mcRPGPlayer);
        setToggleState(ability, mcRPGPlayer, true); // disabled → hint should say enable

        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(texts.stream().anyMatch(t -> t.equals(HINT_TOGGLE_ENABLE)),
                "Expected 'Left-click to enable' hint when ability is disabled. Got: " + texts);
    }

    @Test
    @DisplayName("Toggle hint shows 'disable' hint when ability is currently enabled")
    void toggleHint_showsDisableHint_whenAbilityIsOn(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new ToggleableInnateStub(mcRPG);
        registerAndAddAbility(ability, mcRPGPlayer);
        setToggleState(ability, mcRPGPlayer, false); // enabled → hint should say disable

        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(texts.stream().anyMatch(t -> t.equals(HINT_TOGGLE_DISABLE)),
                "Expected 'Left-click to disable' hint when ability is enabled. Got: " + texts);
    }

    @Test
    @DisplayName("Configure hint is always present for non-Bedrock players")
    void configureHint_alwaysPresent(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new InnateAbilityStub(mcRPG);
        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        assertTrue(texts.stream().anyMatch(t -> t.equals(HINT_CONFIGURE)),
                "Expected 'Right-click to configure' hint to always be present. Got: " + texts);
    }

    // ── Lore Order ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Lore order is: blank → type → mana → status → toggle hint → configure hint")
    void loreOrder_fullInjectionOrder(@NotNull McRPGPlayer mcRPGPlayer) {
        var ability = new ToggleableActiveManaStub(mcRPG);
        registerAndAddAbility(ability, mcRPGPlayer);
        setToggleState(ability, mcRPGPlayer, false); // enabled

        new AbilitySlot(mcRPGPlayer, ability).getItem(mcRPGPlayer);

        List<String> texts = capturedLoreTexts(ability.mockBuilder);
        int idxType = firstIndexOf(texts, TYPE_ACTIVE);
        int idxMana = firstIndexOf(texts, MANA_COST);
        int idxStatus = firstIndexOf(texts, STATUS_ENABLED);
        int idxToggleHint = firstIndexOf(texts, HINT_TOGGLE_DISABLE); // ability is enabled → "disable" hint
        int idxConfigure = firstIndexOf(texts, HINT_CONFIGURE);

        assertTrue(idxType >= 0, "Expected type line in lore. Got: " + texts);
        assertTrue(idxMana >= 0, "Expected mana cost line in lore. Got: " + texts);
        assertTrue(idxStatus >= 0, "Expected status line in lore. Got: " + texts);
        assertTrue(idxToggleHint >= 0, "Expected toggle hint in lore. Got: " + texts);
        assertTrue(idxConfigure >= 0, "Expected configure hint in lore. Got: " + texts);

        assertTrue(idxType < idxMana, "Type must come before mana cost. Got: " + texts);
        assertTrue(idxMana < idxStatus, "Mana cost must come before status. Got: " + texts);
        assertTrue(idxStatus < idxToggleHint, "Status must come before toggle hint. Got: " + texts);
        assertTrue(idxToggleHint < idxConfigure, "Toggle hint must come before configure hint. Got: " + texts);
    }

    /**
     * Returns the first index in {@code list} where the element equals {@code target}, or -1 if not found.
     *
     * @param list   The list to search.
     * @param target The string to find.
     * @return First index of {@code target}, or -1.
     */
    private int firstIndexOf(@NotNull List<String> list, @NotNull String target) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(target)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Base class for all lore injection test stubs. Holds a public Mockito mock of {@link AbilityItemBuilder}
     * so that captured {@link AbilityItemBuilder#addDisplayLoreComponent(Component)} calls can be inspected.
     */
    abstract static class LoreTestAbilityBase extends BaseAbility {

        final AbilityItemBuilder mockBuilder = mock(AbilityItemBuilder.class);

        LoreTestAbilityBase(@NotNull us.eunoians.mcrpg.McRPG plugin, @NotNull String key) {
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
        public boolean isPassive() {
            return false;
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

    /**
     * Innate passive ability without {@link AbilityAttributeRegistry#ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY}.
     * Used to verify that neither status nor toggle hint are added when toggle data is absent.
     */
    static class InnateAbilityStub extends LoreTestAbilityBase implements PassiveAbility {

        InnateAbilityStub(@NotNull us.eunoians.mcrpg.McRPG plugin) {
            super(plugin, "test-lore-innate-no-toggle");
        }
    }

    /**
     * Innate passive ability that includes {@link AbilityAttributeRegistry#ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY}.
     * Suitable for status and toggle-hint lore injection tests.
     */
    static class ToggleableInnateStub extends LoreTestAbilityBase implements PassiveAbility {

        ToggleableInnateStub(@NotNull us.eunoians.mcrpg.McRPG plugin) {
            super(plugin, "test-lore-innate-toggleable");
        }

        @Override
        @NotNull
        public Set<NamespacedKey> getApplicableAttributes() {
            return Set.of(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
        }
    }

    /**
     * Active ({@link ComboActivatable}) ability with mana cost 30, no toggle attribute.
     * Used for mana-cost line tests.
     */
    static class ActiveManaAbilityStub extends LoreTestAbilityBase implements ComboActivatable {

        ActiveManaAbilityStub(@NotNull us.eunoians.mcrpg.McRPG plugin) {
            super(plugin, "test-lore-active-mana");
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

    /**
     * Active ({@link ComboActivatable}) ability with mana cost 30 and a
     * {@link AbilityAttributeRegistry#ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY} applicable attribute.
     * Used for the full lore-order test.
     */
    static class ToggleableActiveManaStub extends LoreTestAbilityBase implements ComboActivatable, PassiveAbility {

        ToggleableActiveManaStub(@NotNull us.eunoians.mcrpg.McRPG plugin) {
            super(plugin, "test-lore-active-mana-toggleable");
        }

        @Override
        public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
            return true;
        }

        @Override
        public int getManaCost(@NotNull AbilityHolder abilityHolder) {
            return 30;
        }

        @Override
        @NotNull
        public Set<NamespacedKey> getApplicableAttributes() {
            return Set.of(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
        }
    }
}
