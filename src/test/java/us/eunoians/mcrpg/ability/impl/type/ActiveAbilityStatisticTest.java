package us.eunoians.mcrpg.ability.impl.type;

import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ActiveAbilityStatisticTest extends McRPGBaseTest {

    private MockActiveAbility ability;

    @BeforeEach
    public void setup() {
        ability = new MockActiveAbility(mcRPG);
    }

    @DisplayName("Given an ActiveAbility, when calling getActivationStatisticKey, then the key follows the expected format")
    @Test
    public void getActivationStatisticKey_returnsCorrectFormat() {
        assertEquals(new NamespacedKey(ability.getPlugin(), "mock_active_ability_activations"),
                ability.getActivationStatisticKey());
    }

    @DisplayName("Given an ActiveAbility, when getting default statistics, then it returns exactly one statistic")
    @Test
    public void getDefaultStatistics_returnsOneStatistic() {
        assertEquals(1, ability.getDefaultStatistics().size());
    }

    @DisplayName("Given an ActiveAbility, when getting default statistics, then the statistic is a LONG type")
    @Test
    public void getDefaultStatistics_statisticIsLongType() {
        Set<Statistic> stats = ability.getDefaultStatistics();
        Statistic stat = stats.iterator().next();
        assertEquals(StatisticType.LONG, stat.getStatisticType());
        assertEquals(0L, stat.getDefaultValue());
    }

    @DisplayName("Given an ActiveAbility, when getting default statistics, then the statistic key matches getActivationStatisticKey")
    @Test
    public void getDefaultStatistics_statisticKeyMatchesGetActivationStatisticKey() {
        Set<Statistic> stats = ability.getDefaultStatistics();
        assertTrue(stats.stream().anyMatch(s -> s.getStatisticKey().equals(ability.getActivationStatisticKey())));
    }

    @DisplayName("Given an ActiveAbility, when getting default statistics, then the display name contains the ability name")
    @Test
    public void getDefaultStatistics_displayNameContainsAbilityName() {
        Set<Statistic> stats = ability.getDefaultStatistics();
        Statistic stat = stats.iterator().next();
        assertTrue(stat.getDisplayName().contains("Mock Active Ability"),
                "Expected display name to contain 'Mock Active Ability' but was: " + stat.getDisplayName());
    }

    /**
     * Minimal concrete {@link ActiveAbility} used only within this test class.
     */
    private static class MockActiveAbility extends BaseAbility implements ActiveAbility {

        MockActiveAbility(@NotNull McRPG plugin) {
            super(plugin, new NamespacedKey(plugin, "mock_active_ability"));
        }

        @NotNull
        @Override
        public String getDatabaseName() {
            return "mock_active_ability";
        }

        @NotNull
        @Override
        public String getName(@NotNull McRPGPlayer player) {
            return "Mock Active Ability";
        }

        @NotNull
        @Override
        public String getName() {
            return "Mock Active Ability";
        }

        @NotNull
        @Override
        public Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("Mock Active Ability");
        }

        @NotNull
        @Override
        public Component getDisplayName() {
            return Component.text("Mock Active Ability");
        }

        @Override
        public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            // No-op for testing
            return true;
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @NotNull
        @Override
        public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            return mock(AbilityItemBuilder.class);
        }

        @NotNull
        @Override
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
