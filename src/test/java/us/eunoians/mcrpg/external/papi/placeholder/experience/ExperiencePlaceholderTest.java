package us.eunoians.mcrpg.external.papi.placeholder.experience;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Experience PAPI Placeholders")
@ExtendWith(McRPGPlayerExtension.class)
class ExperiencePlaceholderTest extends McRPGBaseTest {

    private OfflinePlayer offlinePlayer(UUID uuid) {
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getUniqueId()).thenReturn(uuid);
        return offlinePlayer;
    }

    @Nested
    @DisplayName("BoostedExperiencePlaceholder")
    class BoostedExperiencePlaceholderTests {

        @Test
        @DisplayName("returns the boosted experience amount when the player is loaded")
        void parsePlaceholder_returnsBoostedExp_whenPlayerExists(McRPGPlayer mcRPGPlayer) {
            PlayerExperienceExtras extras = mcRPGPlayer.getExperienceExtras();
            extras.setBoostedExperience(750);

            BoostedExperiencePlaceholder placeholder = new BoostedExperiencePlaceholder();
            assertEquals("750", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("returns 0 when the player has no boosted experience")
        void parsePlaceholder_returnsZero_whenNoBoostedExp(McRPGPlayer mcRPGPlayer) {
            BoostedExperiencePlaceholder placeholder = new BoostedExperiencePlaceholder();
            assertEquals("0", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("returns null when the player is not loaded")
        void parsePlaceholder_returnsNull_whenPlayerNotLoaded() {
            BoostedExperiencePlaceholder placeholder = new BoostedExperiencePlaceholder();
            assertNull(placeholder.parsePlaceholder(offlinePlayer(UUID.randomUUID())));
        }

        @Test
        @DisplayName("identifier is boosted_experience")
        void getIdentifier_matchesExpected() {
            assertEquals("boosted_experience", new BoostedExperiencePlaceholder().getIdentifier());
        }
    }

    @Nested
    @DisplayName("RedeemableExperiencePlaceholder")
    class RedeemableExperiencePlaceholderTests {

        @Test
        @DisplayName("returns the redeemable experience amount when the player is loaded")
        void parsePlaceholder_returnsRedeemableExp_whenPlayerExists(McRPGPlayer mcRPGPlayer) {
            PlayerExperienceExtras extras = mcRPGPlayer.getExperienceExtras();
            extras.setRedeemableExperience(2000);

            RedeemableExperiencePlaceholder placeholder = new RedeemableExperiencePlaceholder();
            assertEquals("2000", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("returns 0 when the player has no redeemable experience")
        void parsePlaceholder_returnsZero_whenNoRedeemableExp(McRPGPlayer mcRPGPlayer) {
            RedeemableExperiencePlaceholder placeholder = new RedeemableExperiencePlaceholder();
            assertEquals("0", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("returns null when the player is not loaded")
        void parsePlaceholder_returnsNull_whenPlayerNotLoaded() {
            RedeemableExperiencePlaceholder placeholder = new RedeemableExperiencePlaceholder();
            assertNull(placeholder.parsePlaceholder(offlinePlayer(UUID.randomUUID())));
        }

        @Test
        @DisplayName("identifier is redeemable_experience")
        void getIdentifier_matchesExpected() {
            assertEquals("redeemable_experience", new RedeemableExperiencePlaceholder().getIdentifier());
        }
    }

    @Nested
    @DisplayName("RedeemableLevelsPlaceholder")
    class RedeemableLevelsPlaceholderTests {

        @Test
        @DisplayName("returns the redeemable levels amount when the player is loaded")
        void parsePlaceholder_returnsRedeemableLevels_whenPlayerExists(McRPGPlayer mcRPGPlayer) {
            PlayerExperienceExtras extras = mcRPGPlayer.getExperienceExtras();
            extras.setRedeemableLevels(5);

            RedeemableLevelsPlaceholder placeholder = new RedeemableLevelsPlaceholder();
            assertEquals("5", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("returns 0 when the player has no redeemable levels")
        void parsePlaceholder_returnsZero_whenNoRedeemableLevels(McRPGPlayer mcRPGPlayer) {
            RedeemableLevelsPlaceholder placeholder = new RedeemableLevelsPlaceholder();
            assertEquals("0", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("returns null when the player is not loaded")
        void parsePlaceholder_returnsNull_whenPlayerNotLoaded() {
            RedeemableLevelsPlaceholder placeholder = new RedeemableLevelsPlaceholder();
            assertNull(placeholder.parsePlaceholder(offlinePlayer(UUID.randomUUID())));
        }

        @Test
        @DisplayName("identifier is redeemable_levels")
        void getIdentifier_matchesExpected() {
            assertEquals("redeemable_levels", new RedeemableLevelsPlaceholder().getIdentifier());
        }
    }

    @Nested
    @DisplayName("RestedExperiencePlaceholder")
    class RestedExperiencePlaceholderTests {

        @Test
        @DisplayName("returns the formatted rested experience when the player is loaded")
        void parsePlaceholder_returnsRestedExp_whenPlayerExists(McRPGPlayer mcRPGPlayer) {
            PlayerExperienceExtras extras = mcRPGPlayer.getExperienceExtras();
            extras.setRestedExperience(3.5f);

            RestedExperiencePlaceholder placeholder = new RestedExperiencePlaceholder();
            String result = placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID()));
            assertEquals("3.5", result);
        }

        @Test
        @DisplayName("returns null when the player is not loaded")
        void parsePlaceholder_returnsNull_whenPlayerNotLoaded() {
            RestedExperiencePlaceholder placeholder = new RestedExperiencePlaceholder();
            assertNull(placeholder.parsePlaceholder(offlinePlayer(UUID.randomUUID())));
        }

        @Test
        @DisplayName("identifier is rested_experience")
        void getIdentifier_matchesExpected() {
            assertEquals("rested_experience", new RestedExperiencePlaceholder().getIdentifier());
        }
    }
}
