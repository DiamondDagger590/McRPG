package us.eunoians.mcrpg.ability.component.activatable;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

@DisplayName("EventActivatableComponentAttribute")
public class EventActivatableComponentAttributeTest {

    @Nested
    @DisplayName("constructor and accessors")
    class ConstructorAndAccessors {

        @DisplayName("stores and returns the ability component")
        @Test
        public void abilityComponent_returnsStoredComponent() {
            EventActivatableComponent component = mock(EventActivatableComponent.class);
            var attribute = new EventActivatableComponentAttribute(component, EntityDamageByEntityEvent.class, 0);
            assertSame(component, attribute.abilityComponent());
        }

        @DisplayName("stores and returns the event class")
        @Test
        public void clazz_returnsStoredEventClass() {
            EventActivatableComponent component = mock(EventActivatableComponent.class);
            var attribute = new EventActivatableComponentAttribute(component, BlockBreakEvent.class, 1);
            assertEquals(BlockBreakEvent.class, attribute.clazz());
        }

        @DisplayName("stores and returns the priority")
        @Test
        public void priority_returnsStoredPriority() {
            EventActivatableComponent component = mock(EventActivatableComponent.class);
            var attribute = new EventActivatableComponentAttribute(component, Event.class, 42);
            assertEquals(42, attribute.priority());
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @DisplayName("equals returns true for identical attributes")
        @Test
        public void equals_returnsTrue_whenIdentical() {
            EventActivatableComponent component = mock(EventActivatableComponent.class);
            var attr1 = new EventActivatableComponentAttribute(component, EntityDamageByEntityEvent.class, 5);
            var attr2 = new EventActivatableComponentAttribute(component, EntityDamageByEntityEvent.class, 5);
            assertEquals(attr1, attr2);
        }

        @DisplayName("equals returns false when priority differs")
        @Test
        public void equals_returnsFalse_whenPriorityDiffers() {
            EventActivatableComponent component = mock(EventActivatableComponent.class);
            var attr1 = new EventActivatableComponentAttribute(component, EntityDamageByEntityEvent.class, 0);
            var attr2 = new EventActivatableComponentAttribute(component, EntityDamageByEntityEvent.class, 1);
            assertNotEquals(attr1, attr2);
        }

        @DisplayName("equals returns false when event class differs")
        @Test
        public void equals_returnsFalse_whenEventClassDiffers() {
            EventActivatableComponent component = mock(EventActivatableComponent.class);
            var attr1 = new EventActivatableComponentAttribute(component, EntityDamageByEntityEvent.class, 0);
            var attr2 = new EventActivatableComponentAttribute(component, BlockBreakEvent.class, 0);
            assertNotEquals(attr1, attr2);
        }

        @DisplayName("hashCode is consistent for equal attributes")
        @Test
        public void hashCode_isConsistent_whenEqual() {
            EventActivatableComponent component = mock(EventActivatableComponent.class);
            var attr1 = new EventActivatableComponentAttribute(component, EntityDamageByEntityEvent.class, 5);
            var attr2 = new EventActivatableComponentAttribute(component, EntityDamageByEntityEvent.class, 5);
            assertEquals(attr1.hashCode(), attr2.hashCode());
        }
    }
}
