package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DistanceTraveledObjectiveTypeTest extends McRPGBaseTest {

    private DistanceTraveledObjectiveType type;

    @BeforeEach
    void setUp() {
        type = new DistanceTraveledObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns distance_traveled key")
        void getKey_returnsDistanceTraveledKey() {
            assertEquals(DistanceTraveledObjectiveType.KEY, type.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for DistanceTraveledQuestContext")
        void canProcess_returnsTrue_forDistanceTraveledContext() {
            PlayerMoveEvent mockEvent = mock(PlayerMoveEvent.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(mockEvent, 5L);
            assertTrue(type.canProcess(context));
        }

        @Test
        @DisplayName("returns false for non-matching context")
        void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("parses empty modes list")
        void parseConfig_emptyModes_acceptsAnyMode() {
            Section section = mock(Section.class);
            when(section.contains("modes")).thenReturn(false);

            DistanceTraveledObjectiveType configured = type.parseConfig(section);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            Player player = mock(Player.class);
            when(moveEvent.getPlayer()).thenReturn(player);
            when(player.isGliding()).thenReturn(false);
            when(player.getVehicle()).thenReturn(null);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 10L);
            assertEquals(10L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("parses single mode from config")
        void parseConfig_singleMode_restrictsToThatMode() {
            Section section = mock(Section.class);
            when(section.contains("modes")).thenReturn(true);
            when(section.getStringList("modes")).thenReturn(List.of("FOOT"));

            DistanceTraveledObjectiveType configured = type.parseConfig(section);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            Player player = mock(Player.class);
            when(moveEvent.getPlayer()).thenReturn(player);
            when(player.isGliding()).thenReturn(false);
            when(player.getVehicle()).thenReturn(null);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 7L);
            assertEquals(7L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("parses multiple modes from config")
        void parseConfig_multipleModes_acceptsAllListed() {
            Section section = mock(Section.class);
            when(section.contains("modes")).thenReturn(true);
            when(section.getStringList("modes")).thenReturn(List.of("FOOT", "ELYTRA"));

            DistanceTraveledObjectiveType configured = type.parseConfig(section);

            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(true);
            when(player.getVehicle()).thenReturn(null);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 20L);
            assertEquals(20L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("mode parsing is case-insensitive")
        void parseConfig_caseInsensitive_parsesLowercaseModes() {
            Section section = mock(Section.class);
            when(section.contains("modes")).thenReturn(true);
            when(section.getStringList("modes")).thenReturn(List.of("horse"));

            DistanceTraveledObjectiveType configured = type.parseConfig(section);

            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(false);
            Horse horse = mock(Horse.class);
            when(player.getVehicle()).thenReturn(horse);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 15L);
            assertEquals(15L, configured.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("returns 0 for wrong context type")
        void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0L, type.processProgress(instance, wrongContext));
        }

        @Test
        @DisplayName("unconfigured type returns block distance for any mode")
        void processProgress_returnsBlockDistance_whenUnconfigured() {
            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            Player player = mock(Player.class);
            when(moveEvent.getPlayer()).thenReturn(player);
            when(player.isGliding()).thenReturn(false);
            when(player.getVehicle()).thenReturn(null);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 42L);
            assertEquals(42L, type.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when mode does not match configured modes")
        void processProgress_returnsZero_whenModeDoesNotMatch() {
            Section section = mock(Section.class);
            when(section.contains("modes")).thenReturn(true);
            when(section.getStringList("modes")).thenReturn(List.of("ELYTRA"));
            DistanceTraveledObjectiveType configured = type.parseConfig(section);

            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(false);
            when(player.getVehicle()).thenReturn(null);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 5L);
            assertEquals(0L, configured.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("TravelMode detection")
    class TravelModeDetection {

        private DistanceTraveledObjectiveType allModesType;

        @BeforeEach
        void setUp() {
            Section section = mock(Section.class);
            when(section.contains("modes")).thenReturn(true);
            when(section.getStringList("modes")).thenReturn(
                    List.of("FOOT", "HORSE", "BOAT", "MINECART", "ELYTRA", "PIG"));
            allModesType = type.parseConfig(section);
        }

        @Test
        @DisplayName("detects ELYTRA when player is gliding")
        void detectsElytra_whenGliding() {
            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(true);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 100L);
            assertEquals(100L, allModesType.processProgress(instance, context));
        }

        @Test
        @DisplayName("detects HORSE when riding a horse")
        void detectsHorse_whenRidingHorse() {
            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(false);
            Horse horse = mock(Horse.class);
            when(player.getVehicle()).thenReturn(horse);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 50L);
            assertEquals(50L, allModesType.processProgress(instance, context));
        }

        @Test
        @DisplayName("detects BOAT when riding a boat")
        void detectsBoat_whenInBoat() {
            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(false);
            Boat boat = mock(Boat.class);
            when(player.getVehicle()).thenReturn(boat);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 30L);
            assertEquals(30L, allModesType.processProgress(instance, context));
        }

        @Test
        @DisplayName("detects MINECART when in a minecart")
        void detectsMinecart_whenInMinecart() {
            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(false);
            Minecart minecart = mock(Minecart.class);
            when(player.getVehicle()).thenReturn(minecart);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 25L);
            assertEquals(25L, allModesType.processProgress(instance, context));
        }

        @Test
        @DisplayName("detects PIG when riding a pig")
        void detectsPig_whenRidingPig() {
            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(false);
            Pig pig = mock(Pig.class);
            when(player.getVehicle()).thenReturn(pig);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 12L);
            assertEquals(12L, allModesType.processProgress(instance, context));
        }

        @Test
        @DisplayName("detects FOOT when not gliding and no vehicle")
        void detectsFoot_whenOnFoot() {
            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(false);
            when(player.getVehicle()).thenReturn(null);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 3L);
            assertEquals(3L, allModesType.processProgress(instance, context));
        }

        @Test
        @DisplayName("ELYTRA takes priority over vehicle")
        void elytraPriority_overVehicle() {
            Section section = mock(Section.class);
            when(section.contains("modes")).thenReturn(true);
            when(section.getStringList("modes")).thenReturn(List.of("ELYTRA"));
            DistanceTraveledObjectiveType elytraOnly = type.parseConfig(section);

            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(true);
            Horse horse = mock(Horse.class);
            when(player.getVehicle()).thenReturn(horse);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 99L);
            assertEquals(99L, elytraOnly.processProgress(instance, context));
        }

        @Test
        @DisplayName("FOOT-only rejects horse travel")
        void footOnly_rejectsHorse() {
            Section section = mock(Section.class);
            when(section.contains("modes")).thenReturn(true);
            when(section.getStringList("modes")).thenReturn(List.of("FOOT"));
            DistanceTraveledObjectiveType footOnly = type.parseConfig(section);

            Player player = mock(Player.class);
            when(player.isGliding()).thenReturn(false);
            Horse horse = mock(Horse.class);
            when(player.getVehicle()).thenReturn(horse);

            PlayerMoveEvent moveEvent = mock(PlayerMoveEvent.class);
            when(moveEvent.getPlayer()).thenReturn(player);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(moveEvent, 10L);
            assertEquals(0L, footOnly.processProgress(instance, context));
        }
    }
}
