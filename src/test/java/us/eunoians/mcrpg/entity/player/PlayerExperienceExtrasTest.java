package us.eunoians.mcrpg.entity.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerExperienceExtrasTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("All fields initialize to zero")
        void allFieldsInitializeToZero() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras();
            assertEquals(0, extras.getRedeemableExperience());
            assertEquals(0, extras.getRedeemableLevels());
            assertEquals(0, extras.getBoostedExperience());
            assertEquals(0f, extras.getRestedExperience());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Stores provided values")
        void storesProvidedValues() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(100, 5, 200, 1.5f);
            assertEquals(100, extras.getRedeemableExperience());
            assertEquals(5, extras.getRedeemableLevels());
            assertEquals(200, extras.getBoostedExperience());
            assertEquals(1.5f, extras.getRestedExperience());
        }
    }

    @Nested
    @DisplayName("RedeemableExperience")
    class RedeemableExperience {

        @Test
        @DisplayName("setRedeemableExperience stores positive value")
        void set_storesPositiveValue() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras();
            extras.setRedeemableExperience(500);
            assertEquals(500, extras.getRedeemableExperience());
        }

        @Test
        @DisplayName("setRedeemableExperience clamps negative to zero")
        void set_clampsNegativeToZero() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras();
            extras.setRedeemableExperience(-10);
            assertEquals(0, extras.getRedeemableExperience());
        }

        @Test
        @DisplayName("modifyRedeemableExperience adds positive delta")
        void modify_addsPositiveDelta() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(100, 0, 0, 0);
            extras.modifyRedeemableExperience(50);
            assertEquals(150, extras.getRedeemableExperience());
        }

        @Test
        @DisplayName("modifyRedeemableExperience subtracts negative delta")
        void modify_subtractsNegativeDelta() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(100, 0, 0, 0);
            extras.modifyRedeemableExperience(-30);
            assertEquals(70, extras.getRedeemableExperience());
        }

        @Test
        @DisplayName("modifyRedeemableExperience clamps result to zero")
        void modify_clampsResultToZero() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(50, 0, 0, 0);
            extras.modifyRedeemableExperience(-100);
            assertEquals(0, extras.getRedeemableExperience());
        }
    }

    @Nested
    @DisplayName("RedeemableLevels")
    class RedeemableLevels {

        @Test
        @DisplayName("setRedeemableLevels stores positive value")
        void set_storesPositiveValue() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras();
            extras.setRedeemableLevels(10);
            assertEquals(10, extras.getRedeemableLevels());
        }

        @Test
        @DisplayName("setRedeemableLevels clamps negative to zero")
        void set_clampsNegativeToZero() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras();
            extras.setRedeemableLevels(-5);
            assertEquals(0, extras.getRedeemableLevels());
        }

        @Test
        @DisplayName("modifyRedeemableLevels adds positive delta")
        void modify_addsPositiveDelta() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(0, 5, 0, 0);
            extras.modifyRedeemableLevels(3);
            assertEquals(8, extras.getRedeemableLevels());
        }

        @Test
        @DisplayName("modifyRedeemableLevels clamps result to zero")
        void modify_clampsResultToZero() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(0, 2, 0, 0);
            extras.modifyRedeemableLevels(-10);
            assertEquals(0, extras.getRedeemableLevels());
        }
    }

    @Nested
    @DisplayName("BoostedExperience")
    class BoostedExperience {

        @Test
        @DisplayName("setBoostedExperience stores positive value")
        void set_storesPositiveValue() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras();
            extras.setBoostedExperience(1000);
            assertEquals(1000, extras.getBoostedExperience());
        }

        @Test
        @DisplayName("setBoostedExperience clamps negative to zero")
        void set_clampsNegativeToZero() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras();
            extras.setBoostedExperience(-1);
            assertEquals(0, extras.getBoostedExperience());
        }

        @Test
        @DisplayName("modifyBoostedExperience adds positive delta")
        void modify_addsPositiveDelta() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(0, 0, 500, 0);
            extras.modifyBoostedExperience(200);
            assertEquals(700, extras.getBoostedExperience());
        }

        @Test
        @DisplayName("modifyBoostedExperience clamps result to zero")
        void modify_clampsResultToZero() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(0, 0, 100, 0);
            extras.modifyBoostedExperience(-500);
            assertEquals(0, extras.getBoostedExperience());
        }
    }

    @Nested
    @DisplayName("RestedExperience")
    class RestedExperience {

        @Test
        @DisplayName("setRestedExperience stores positive value")
        void set_storesPositiveValue() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras();
            extras.setRestedExperience(2.5f);
            assertEquals(2.5f, extras.getRestedExperience());
        }

        @Test
        @DisplayName("setRestedExperience clamps negative to zero")
        void set_clampsNegativeToZero() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras();
            extras.setRestedExperience(-0.5f);
            assertEquals(0f, extras.getRestedExperience());
        }

        @Test
        @DisplayName("modifyRestedExperience adds positive delta")
        void modify_addsPositiveDelta() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(0, 0, 0, 1.0f);
            extras.modifyRestedExperience(0.5f);
            assertEquals(1.5f, extras.getRestedExperience());
        }

        @Test
        @DisplayName("modifyRestedExperience clamps result to zero")
        void modify_clampsResultToZero() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(0, 0, 0, 0.3f);
            extras.modifyRestedExperience(-1.0f);
            assertEquals(0f, extras.getRestedExperience());
        }
    }

    @Nested
    @DisplayName("reset")
    class Reset {

        @Test
        @DisplayName("Zeroes all fields")
        void zeroesAllFields() {
            PlayerExperienceExtras extras = new PlayerExperienceExtras(100, 5, 200, 1.5f);
            extras.reset();
            assertEquals(0, extras.getRedeemableExperience());
            assertEquals(0, extras.getRedeemableLevels());
            assertEquals(0, extras.getBoostedExperience());
            assertEquals(0f, extras.getRestedExperience());
        }
    }

    @Nested
    @DisplayName("copyExtras")
    class CopyExtras {

        @Test
        @DisplayName("Copies all values from source")
        void copiesAllValuesFromSource() {
            PlayerExperienceExtras source = new PlayerExperienceExtras(100, 5, 200, 1.5f);
            PlayerExperienceExtras target = new PlayerExperienceExtras();
            target.copyExtras(source);
            assertEquals(100, target.getRedeemableExperience());
            assertEquals(5, target.getRedeemableLevels());
            assertEquals(200, target.getBoostedExperience());
            assertEquals(1.5f, target.getRestedExperience());
        }

        @Test
        @DisplayName("Overwrites existing values")
        void overwritesExistingValues() {
            PlayerExperienceExtras source = new PlayerExperienceExtras(10, 1, 20, 0.5f);
            PlayerExperienceExtras target = new PlayerExperienceExtras(999, 999, 999, 999f);
            target.copyExtras(source);
            assertEquals(10, target.getRedeemableExperience());
            assertEquals(1, target.getRedeemableLevels());
            assertEquals(20, target.getBoostedExperience());
            assertEquals(0.5f, target.getRestedExperience());
        }

        @Test
        @DisplayName("Source is not modified")
        void sourceIsNotModified() {
            PlayerExperienceExtras source = new PlayerExperienceExtras(100, 5, 200, 1.5f);
            PlayerExperienceExtras target = new PlayerExperienceExtras();
            target.copyExtras(source);
            target.setRedeemableExperience(0);
            assertEquals(100, source.getRedeemableExperience());
        }
    }
}
