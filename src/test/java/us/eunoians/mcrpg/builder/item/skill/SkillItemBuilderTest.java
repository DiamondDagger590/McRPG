package us.eunoians.mcrpg.builder.item.skill;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.MockSkill;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("SkillItemBuilder")
class SkillItemBuilderTest extends McRPGBaseTest {

    private MockSkill mockSkill;

    @BeforeEach
    void setUp() {
        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);
        mockSkill = spy(MockSkill.class);
        when(mockSkill.getMaxLevel()).thenReturn(50);
        when(mockSkill.getLevelUpEquation()).thenReturn(new Parser("100"));
        skillRegistry.register(mockSkill);
    }

    @Nested
    @DisplayName("ItemStack constructor")
    class ItemStackConstructor {

        @Test
        @DisplayName("Given an ItemStack with skill holder data, populates all placeholders")
        void constructor_populatesAllPlaceholders_whenSkillDataPresent(@NotNull McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(mockSkill, 5);

            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
            SkillItemBuilder builder = new SkillItemBuilder(itemStack, mcRPGPlayer, mockSkill);

            assertNotNull(builder);
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.SKILL.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.LEVEL.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.CURRENT_EXPERIENCE.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.REQUIRED_EXPERIENCE_TO_LEVEL_UP.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.REMAINING_EXPERIENCE_TO_LEVEL_UP.getKey()));
        }

        @Test
        @DisplayName("Given an ItemStack with no skill holder data, placeholders default to zero")
        void constructor_defaultsToZero_whenNoSkillData(@NotNull McRPGPlayer mcRPGPlayer) {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
            SkillItemBuilder builder = new SkillItemBuilder(itemStack, mcRPGPlayer, mockSkill);

            assertNotNull(builder);
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.SKILL.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.LEVEL.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.CURRENT_EXPERIENCE.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.REQUIRED_EXPERIENCE_TO_LEVEL_UP.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.REMAINING_EXPERIENCE_TO_LEVEL_UP.getKey()));
        }
    }

    @Nested
    @DisplayName("String constructor")
    class StringConstructor {

        @Test
        @DisplayName("Given a material string, populates all placeholders")
        void constructor_populatesAllPlaceholders_whenUsingString(@NotNull McRPGPlayer mcRPGPlayer) {
            SkillItemBuilder builder = new SkillItemBuilder("DIAMOND_SWORD", mcRPGPlayer, mockSkill);

            assertNotNull(builder);
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.SKILL.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.LEVEL.getKey()));
        }
    }

    @Nested
    @DisplayName("from factory methods")
    class FromFactoryMethods {

        @Test
        @DisplayName("Given an existing SkillItemBuilder, creates new SkillItemBuilder with placeholders")
        void from_createsBuilderWithPlaceholders_whenGivenSkillItemBuilder(@NotNull McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(mockSkill, 10);

            SkillItemBuilder source = new SkillItemBuilder(new ItemStack(Material.IRON_PICKAXE), mcRPGPlayer, mockSkill);
            SkillItemBuilder builder = SkillItemBuilder.from(source, mcRPGPlayer, mockSkill);

            assertNotNull(builder);
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.SKILL.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.LEVEL.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.CURRENT_EXPERIENCE.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.REQUIRED_EXPERIENCE_TO_LEVEL_UP.getKey()));
            assertTrue(builder.hasPlaceholder(SkillItemPlaceholderKeys.REMAINING_EXPERIENCE_TO_LEVEL_UP.getKey()));
        }
    }
}
