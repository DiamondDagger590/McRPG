package us.eunoians.mcrpg.event.ability.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BleedDamageEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(new Bleed(mcRPG));
    }

    @Test
    @DisplayName("Constructor without holder sets bleedUser to empty")
    void getBleedUser_returnsEmpty_whenConstructedWithoutHolder() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
        assertTrue(event.getBleedUser().isEmpty());
    }

    @Test
    @DisplayName("Constructor with holder wraps holder in Optional")
    void getBleedUser_returnsHolder_whenConstructedWithHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(holder, entity, 5.0, false);
        assertTrue(event.getBleedUser().isPresent());
        assertSame(holder, event.getBleedUser().get());
    }

    @Test
    @DisplayName("Constructor with Optional passes it through")
    void getBleedUser_returnsOptional_whenConstructedWithOptional() {
        AbilityHolder holder = mock(AbilityHolder.class);
        Entity entity = mock(Entity.class);
        Optional<AbilityHolder> optional = Optional.of(holder);
        BleedDamageEvent event = new BleedDamageEvent(optional, entity, 5.0, false);
        assertTrue(event.getBleedUser().isPresent());
        assertSame(holder, event.getBleedUser().get());
    }

    @Test
    @DisplayName("Constructor with empty Optional keeps bleedUser empty")
    void getBleedUser_returnsEmpty_whenConstructedWithEmptyOptional() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(Optional.empty(), entity, 5.0, false);
        assertTrue(event.getBleedUser().isEmpty());
    }

    @Test
    @DisplayName("Negative damage clamped to 0 at construction")
    void getDamage_returnsZero_whenConstructedWithNegativeDamage() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, -3.0, false);
        assertEquals(0.0, event.getDamage());
    }

    @Test
    @DisplayName("Positive damage preserved at construction")
    void getDamage_returnsValue_whenConstructedWithPositiveDamage() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 7.5, false);
        assertEquals(7.5, event.getDamage());
    }

    @Test
    @DisplayName("setDamage clamps negative to 0")
    void setDamage_clampsToZero_whenGivenNegativeValue() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
        event.setDamage(-1.0);
        assertEquals(0.0, event.getDamage());
    }

    @Test
    @DisplayName("setDamage preserves positive value")
    void setDamage_preservesValue_whenGivenPositiveValue() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
        event.setDamage(10.0);
        assertEquals(10.0, event.getDamage());
    }

    @Test
    @DisplayName("ignoreArmor flag preserved from constructor")
    void isDamageIgnoringArmor_returnsConstructorValue() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, true);
        assertTrue(event.isDamageIgnoringArmor());
    }

    @Test
    @DisplayName("setDamageIgnoreArmor updates the flag")
    void setDamageIgnoreArmor_updatesFlag() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
        assertFalse(event.isDamageIgnoringArmor());
        event.setDamageIgnoreArmor(true);
        assertTrue(event.isDamageIgnoringArmor());
    }

    @Test
    @DisplayName("getDamagedEntity returns the entity passed at construction")
    void getDamagedEntity_returnsConstructorEntity() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
        assertSame(entity, event.getDamagedEntity());
    }

    @Test
    @DisplayName("getAbility returns Bleed instance")
    void getAbility_returnsBleedInstance() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
        assertInstanceOf(Bleed.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        Entity entity = mock(Entity.class);
        BleedDamageEvent event = new BleedDamageEvent(entity, 5.0, false);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }
}
