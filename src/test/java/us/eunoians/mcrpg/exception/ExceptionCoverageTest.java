package us.eunoians.mcrpg.exception;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.exception.ability.AbilityActivatedWithWrongEventException;
import us.eunoians.mcrpg.exception.ability.AbilityNotRegisteredException;
import us.eunoians.mcrpg.exception.ability.EventNotRegisteredForActivationException;
import us.eunoians.mcrpg.exception.database.AbilityDatabaseNameException;
import us.eunoians.mcrpg.exception.entity.SkillHolderMissingSkillException;
import us.eunoians.mcrpg.exception.expansion.ContentPackFailedProcessingException;
import us.eunoians.mcrpg.exception.external.worldguard.WorldGuardFlagRegisterException;
import us.eunoians.mcrpg.exception.loadout.InvalidAbilityForLoadoutException;
import us.eunoians.mcrpg.exception.loadout.LoadoutMaxSizeExceededException;
import us.eunoians.mcrpg.exception.loadout.SelectedLoadoutAboveMaxException;
import us.eunoians.mcrpg.exception.localization.LocaleParseException;
import us.eunoians.mcrpg.exception.localization.NoLocalizationContainsMessageException;
import us.eunoians.mcrpg.exception.quest.QuestScopeInvalidStateException;
import us.eunoians.mcrpg.exception.skill.EventNotRegisteredForLevelingException;
import us.eunoians.mcrpg.exception.skill.SkillNotRegisteredException;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExceptionCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("AbilityActivatedWithWrongEventException")
    class AbilityActivatedWithWrongEventExceptionTests {

        @Test
        @DisplayName("getAbility returns constructor value")
        void getAbility_returnsConstructorValue() {
            Ability ability = mock(Ability.class);
            var exception = new AbilityActivatedWithWrongEventException(ability);
            assertSame(ability, exception.getAbility());
        }

        @Test
        @DisplayName("extends RuntimeException")
        void extendsRuntimeException() {
            Ability ability = mock(Ability.class);
            var exception = new AbilityActivatedWithWrongEventException(ability);
            assertTrue(exception instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("AbilityNotRegisteredException")
    class AbilityNotRegisteredExceptionTests {

        @Test
        @DisplayName("getAbilityKey returns constructor value")
        void getAbilityKey_returnsConstructorValue() {
            NamespacedKey key = new NamespacedKey("mcrpg", "test_ability");
            var exception = new AbilityNotRegisteredException(key);
            assertEquals(key, exception.getAbilityKey());
        }

        @Test
        @DisplayName("getMessage contains ability key")
        void getMessage_containsAbilityKey() {
            NamespacedKey key = new NamespacedKey("mcrpg", "test_ability");
            var exception = new AbilityNotRegisteredException(key);
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("test_ability"));
        }
    }

    @Nested
    @DisplayName("EventNotRegisteredForActivationException")
    class EventNotRegisteredForActivationExceptionTests {

        @Test
        @DisplayName("getFailedEvent returns constructor value")
        void getFailedEvent_returnsConstructorValue() {
            Event event = mock(Event.class);
            Ability ability = mock(Ability.class);
            var exception = new EventNotRegisteredForActivationException(event, ability);
            assertSame(event, exception.getFailedEvent());
        }

        @Test
        @DisplayName("getAbility returns constructor value")
        void getAbility_returnsConstructorValue() {
            Event event = mock(Event.class);
            Ability ability = mock(Ability.class);
            var exception = new EventNotRegisteredForActivationException(event, ability);
            assertSame(ability, exception.getAbility());
        }
    }

    @Nested
    @DisplayName("AbilityDatabaseNameException")
    class AbilityDatabaseNameExceptionTests {

        @Test
        @DisplayName("getAbility returns constructor value")
        void getAbility_returnsConstructorValue() {
            Ability mockAbility = mock(Ability.class);
            NamespacedKey key = new NamespacedKey("mcrpg", "test_ability");
            when(mockAbility.getDatabaseName()).thenReturn("");
            when(mockAbility.getAbilityKey()).thenReturn(key);
            var exception = new AbilityDatabaseNameException(mockAbility);
            assertSame(mockAbility, exception.getAbility());
        }

        @Test
        @DisplayName("getMessage contains ability key")
        void getMessage_containsAbilityKey() {
            Ability mockAbility = mock(Ability.class);
            NamespacedKey key = new NamespacedKey("mcrpg", "test_ability");
            when(mockAbility.getDatabaseName()).thenReturn("");
            when(mockAbility.getAbilityKey()).thenReturn(key);
            var exception = new AbilityDatabaseNameException(mockAbility);
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("test_ability"));
            assertTrue(exception.getMessage().contains("missing a database name"));
        }
    }

    @Nested
    @DisplayName("SkillHolderMissingSkillException")
    class SkillHolderMissingSkillExceptionTests {

        @Test
        @DisplayName("getSkillHolder returns constructor value")
        void getSkillHolder_returnsConstructorValue() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            NamespacedKey key = new NamespacedKey("mcrpg", "swords");
            var exception = new SkillHolderMissingSkillException(skillHolder, key);
            assertSame(skillHolder, exception.getSkillHolder());
        }

        @Test
        @DisplayName("getSkillKey returns constructor value")
        void getSkillKey_returnsConstructorValue() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            NamespacedKey key = new NamespacedKey("mcrpg", "swords");
            var exception = new SkillHolderMissingSkillException(skillHolder, key);
            assertEquals(key, exception.getSkillKey());
        }

        @Test
        @DisplayName("message constructor passes message to super")
        void messageConstructor_passesMessageToSuper() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            NamespacedKey key = new NamespacedKey("mcrpg", "swords");
            var exception = new SkillHolderMissingSkillException(skillHolder, key, "custom message");
            assertEquals("custom message", exception.getMessage());
            assertSame(skillHolder, exception.getSkillHolder());
            assertEquals(key, exception.getSkillKey());
        }
    }

    @Nested
    @DisplayName("ContentPackFailedProcessingException")
    class ContentPackFailedProcessingExceptionTests {

        @Test
        @DisplayName("getContentPack returns constructor value")
        void getContentPack_returnsConstructorValue() {
            @SuppressWarnings("unchecked")
            McRPGContentPack<McRPGContent> contentPack = mock(McRPGContentPack.class);
            var exception = new ContentPackFailedProcessingException(contentPack);
            assertSame(contentPack, exception.getContentPack());
        }

        @Test
        @DisplayName("getMessage contains class name and expansion key")
        void getMessage_containsClassNameAndExpansionKey() {
            @SuppressWarnings("unchecked")
            McRPGContentPack<McRPGContent> contentPack = mock(McRPGContentPack.class);
            var mockExpansion = mock(us.eunoians.mcrpg.expansion.ContentExpansion.class);
            NamespacedKey expansionKey = new NamespacedKey("mcrpg", "test_expansion");
            when(contentPack.getContentExpansion()).thenReturn(mockExpansion);
            when(mockExpansion.getExpansionKey()).thenReturn(expansionKey);
            var exception = new ContentPackFailedProcessingException(contentPack);
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("Content Pack"));
            assertTrue(exception.getMessage().contains(expansionKey.toString()));
        }
    }

    @Nested
    @DisplayName("InvalidAbilityForLoadoutException")
    class InvalidAbilityForLoadoutExceptionTests {

        @Test
        @DisplayName("getLoadout returns constructor value")
        void getLoadout_returnsConstructorValue() {
            Loadout loadout = mock(Loadout.class);
            NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
            var exception = new InvalidAbilityForLoadoutException(loadout, key);
            assertSame(loadout, exception.getLoadout());
        }

        @Test
        @DisplayName("getAbilityKey returns constructor value")
        void getAbilityKey_returnsConstructorValue() {
            Loadout loadout = mock(Loadout.class);
            NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
            var exception = new InvalidAbilityForLoadoutException(loadout, key);
            assertEquals(key, exception.getAbilityKey());
        }

        @Test
        @DisplayName("message constructor passes message to super")
        void messageConstructor_passesMessageToSuper() {
            Loadout loadout = mock(Loadout.class);
            NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
            var exception = new InvalidAbilityForLoadoutException(loadout, key, "not valid");
            assertEquals("not valid", exception.getMessage());
            assertSame(loadout, exception.getLoadout());
            assertEquals(key, exception.getAbilityKey());
        }
    }

    @Nested
    @DisplayName("LoadoutMaxSizeExceededException")
    class LoadoutMaxSizeExceededExceptionTests {

        @Test
        @DisplayName("getLoadout returns constructor value")
        void getLoadout_returnsConstructorValue() {
            Loadout loadout = mock(Loadout.class);
            var exception = new LoadoutMaxSizeExceededException(loadout);
            assertSame(loadout, exception.getLoadout());
        }

        @Test
        @DisplayName("message constructor passes message to super")
        void messageConstructor_passesMessageToSuper() {
            Loadout loadout = mock(Loadout.class);
            var exception = new LoadoutMaxSizeExceededException(loadout, "too many abilities");
            assertEquals("too many abilities", exception.getMessage());
            assertSame(loadout, exception.getLoadout());
        }

        @Test
        @DisplayName("default constructor has null message")
        void defaultConstructor_hasNullMessage() {
            Loadout loadout = mock(Loadout.class);
            var exception = new LoadoutMaxSizeExceededException(loadout);
            assertEquals(null, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("SelectedLoadoutAboveMaxException")
    class SelectedLoadoutAboveMaxExceptionTests {

        @Test
        @DisplayName("getLoadoutHolder returns constructor value")
        void getLoadoutHolder_returnsConstructorValue() {
            LoadoutHolder holder = mock(LoadoutHolder.class);
            var exception = new SelectedLoadoutAboveMaxException(holder, 5);
            assertSame(holder, exception.getLoadoutHolder());
        }

        @Test
        @DisplayName("getLoadoutSlot returns constructor value")
        void getLoadoutSlot_returnsConstructorValue() {
            LoadoutHolder holder = mock(LoadoutHolder.class);
            var exception = new SelectedLoadoutAboveMaxException(holder, 5);
            assertEquals(5, exception.getLoadoutSlot());
        }
    }

    @Nested
    @DisplayName("LocaleParseException")
    class LocaleParseExceptionTests {

        @Test
        @DisplayName("getParsedLocale returns constructor value")
        void getParsedLocale_returnsConstructorValue() {
            var exception = new LocaleParseException("xyz_invalid");
            assertEquals("xyz_invalid", exception.getParsedLocale());
        }

        @Test
        @DisplayName("default message contains locale string")
        void defaultMessage_containsLocaleString() {
            var exception = new LocaleParseException("xyz_invalid");
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("xyz_invalid"));
        }

        @Test
        @DisplayName("custom message constructor")
        void customMessage_passedToSuper() {
            var exception = new LocaleParseException("xyz_invalid", "custom parse error");
            assertEquals("custom parse error", exception.getMessage());
            assertEquals("xyz_invalid", exception.getParsedLocale());
        }
    }

    @Nested
    @DisplayName("NoLocalizationContainsMessageException")
    class NoLocalizationContainsMessageExceptionTests {

        @Test
        @DisplayName("getRoute returns constructor value")
        void getRoute_returnsConstructorValue() {
            Route route = Route.fromString("test.key");
            var exception = new NoLocalizationContainsMessageException(route, Set.of(Locale.ENGLISH));
            assertEquals(route, exception.getRoute());
        }

        @Test
        @DisplayName("getCheckedLocales returns defensive copy")
        void getCheckedLocales_returnsDefensiveCopy() {
            Route route = Route.fromString("test.key");
            java.util.HashSet<Locale> mutableLocales = new java.util.HashSet<>(Set.of(Locale.ENGLISH, Locale.FRENCH));
            var exception = new NoLocalizationContainsMessageException(route, mutableLocales);
            Set<Locale> returned = exception.getCheckedLocales();
            assertEquals(mutableLocales, returned);
            assertNotSame(mutableLocales, returned);
        }

        @Test
        @DisplayName("getMessage contains route and locale names")
        void getMessage_containsRouteAndLocaleNames() {
            Route route = Route.fromString("test.missing.key");
            var exception = new NoLocalizationContainsMessageException(route, Set.of(Locale.ENGLISH));
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("English"));
        }
    }

    @Nested
    @DisplayName("QuestScopeInvalidStateException")
    class QuestScopeInvalidStateExceptionTests {

        @Test
        @DisplayName("getQuestScope returns constructor value")
        void getQuestScope_returnsConstructorValue() {
            QuestScope scope = mock(QuestScope.class);
            var exception = new QuestScopeInvalidStateException(scope);
            assertSame(scope, exception.getQuestScope());
        }

        @Test
        @DisplayName("message constructor passes message to super")
        void messageConstructor_passesMessageToSuper() {
            QuestScope scope = mock(QuestScope.class);
            var exception = new QuestScopeInvalidStateException(scope, "invalid state");
            assertEquals("invalid state", exception.getMessage());
            assertSame(scope, exception.getQuestScope());
        }
    }

    @Nested
    @DisplayName("EventNotRegisteredForLevelingException")
    class EventNotRegisteredForLevelingExceptionTests {

        @Test
        @DisplayName("getFailedEvent returns constructor value")
        void getFailedEvent_returnsConstructorValue() {
            Event event = mock(Event.class);
            Skill skill = mock(Skill.class);
            var exception = new EventNotRegisteredForLevelingException(event, skill);
            assertSame(event, exception.getFailedEvent());
        }

        @Test
        @DisplayName("getSkill returns constructor value")
        void getSkill_returnsConstructorValue() {
            Event event = mock(Event.class);
            Skill skill = mock(Skill.class);
            var exception = new EventNotRegisteredForLevelingException(event, skill);
            assertSame(skill, exception.getSkill());
        }
    }

    @Nested
    @DisplayName("SkillNotRegisteredException")
    class SkillNotRegisteredExceptionTests {

        @Test
        @DisplayName("getSkillKey returns constructor value")
        void getSkillKey_returnsConstructorValue() {
            NamespacedKey key = new NamespacedKey("mcrpg", "mining");
            var exception = new SkillNotRegisteredException(key);
            assertEquals(key, exception.getSkillKey());
        }

        @Test
        @DisplayName("default constructor has null message")
        void defaultConstructor_hasNullMessage() {
            NamespacedKey key = new NamespacedKey("mcrpg", "mining");
            var exception = new SkillNotRegisteredException(key);
            assertEquals(null, exception.getMessage());
        }

        @Test
        @DisplayName("message constructor passes message to super")
        void messageConstructor_passesMessageToSuper() {
            NamespacedKey key = new NamespacedKey("mcrpg", "mining");
            var exception = new SkillNotRegisteredException(key, "not registered");
            assertEquals("not registered", exception.getMessage());
            assertEquals(key, exception.getSkillKey());
        }
    }

    @Nested
    @DisplayName("WorldGuardFlagRegisterException")
    class WorldGuardFlagRegisterExceptionTests {

        @Test
        @DisplayName("getStateFlagKey returns constructor value")
        void getStateFlagKey_returnsConstructorValue() {
            var exception = new WorldGuardFlagRegisterException("mcrpg-pvp");
            assertEquals("mcrpg-pvp", exception.getStateFlagKey());
        }

        @Test
        @DisplayName("default message contains flag key")
        void defaultMessage_containsFlagKey() {
            var exception = new WorldGuardFlagRegisterException("mcrpg-pvp");
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("mcrpg-pvp"));
        }

        @Test
        @DisplayName("custom message constructor")
        void customMessage_passedToSuper() {
            var exception = new WorldGuardFlagRegisterException("mcrpg-pvp", "custom conflict");
            assertEquals("custom conflict", exception.getMessage());
            assertEquals("mcrpg-pvp", exception.getStateFlagKey());
        }
    }
}
