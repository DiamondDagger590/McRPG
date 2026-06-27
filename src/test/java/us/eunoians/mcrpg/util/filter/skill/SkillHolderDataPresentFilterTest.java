package us.eunoians.mcrpg.util.filter.skill;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("SkillHolderDataPresentFilter")
class SkillHolderDataPresentFilterTest extends McRPGBaseTest {

    private SkillHolderDataPresentFilter filter;
    private McRPGPlayer mcRPGPlayer;
    private SkillRegistry skillRegistry;
    private Skill skillA;
    private Skill skillB;

    @BeforeEach
    void setUp(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.filter = new SkillHolderDataPresentFilter();
        addPlayerToServer(mcRPGPlayer);

        skillA = createMockSkill(new NamespacedKey("test", "skill_a"));
        skillB = createMockSkill(new NamespacedKey("test", "skill_b"));

        skillRegistry = mock(SkillRegistry.class);
        when(skillRegistry.getRegisteredSkills()).thenReturn(Set.of(skillA, skillB));
        RegistryAccess.registryAccess().register(skillRegistry);
    }

    private Skill createMockSkill(NamespacedKey key) {
        Skill skill = mock(Skill.class);
        when(skill.getSkillKey()).thenReturn(key);
        Parser parser = new Parser("100");
        when(skill.getLevelUpEquation()).thenReturn(parser);
        when(skill.getMaxLevel()).thenReturn(100);
        return skill;
    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        @DisplayName("returns empty when player has no skill data")
        void filter_noSkillData_returnsEmpty() {
            Collection<Skill> result = filter.filter(mcRPGPlayer, Set.of(skillA, skillB));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns skill when player has data for it")
        void filter_playerHasSkillData_returnsSkill() {
            mcRPGPlayer.asSkillHolder().addSkillHolderData(skillA);

            Collection<Skill> result = filter.filter(mcRPGPlayer, Set.of(skillA, skillB));

            assertEquals(1, result.size());
            assertTrue(result.contains(skillA));
        }

        @Test
        @DisplayName("returns all skills when player has data for all")
        void filter_playerHasAllSkillData_returnsAll() {
            mcRPGPlayer.asSkillHolder().addSkillHolderData(skillA);
            mcRPGPlayer.asSkillHolder().addSkillHolderData(skillB);

            Collection<Skill> result = filter.filter(mcRPGPlayer, Set.of(skillA, skillB));

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("returns subset of skills player has data for")
        void filter_playerHasPartialSkillData_returnsSubset() {
            mcRPGPlayer.asSkillHolder().addSkillHolderData(skillA);

            Collection<Skill> result = filter.filter(mcRPGPlayer, Set.of(skillA, skillB));

            assertEquals(1, result.size());
            assertTrue(result.contains(skillA));
        }

        @Test
        @DisplayName("ignores input collection and uses registry skills")
        void filter_ignoresInputCollection_usesRegistrySkills() {
            mcRPGPlayer.asSkillHolder().addSkillHolderData(skillA);

            Collection<Skill> result = filter.filter(mcRPGPlayer, List.of());

            assertEquals(1, result.size());
            assertTrue(result.contains(skillA));
        }

        @Test
        @DisplayName("skill data at specific level is still included")
        void filter_skillDataAtLevel_included() {
            mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skillA, 50);

            Collection<Skill> result = filter.filter(mcRPGPlayer, Set.of(skillA, skillB));

            assertTrue(result.contains(skillA));
        }
    }
}
