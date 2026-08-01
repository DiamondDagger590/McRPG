package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatStateCodec")
class CombatStateCodecTest {

    private static final NamespacedKey KEY = new NamespacedKey("mcrpg", "combats_today");
    private static final UUID ENTITY_UUID = UUID.randomUUID();

    private CombatStateCodec codec;

    @BeforeEach
    void setUp() {
        codec = new CombatStateCodec(Logger.getLogger(CombatStateCodecTest.class.getName()));
    }

    /**
     * Builds a persistent state type with the given serializer/deserializer pair.
     *
     * @param serializer   The serializer to use.
     * @param deserializer The deserializer to use.
     * @return A persistent {@link CombatStateType}.
     */
    private CombatStateType<Integer> persistentType(Function<Integer, String> serializer,
                                                    Function<String, Integer> deserializer) {
        return CombatStateType.persistent(KEY, Integer.class, 0, serializer, deserializer, null);
    }

    @Nested
    @DisplayName("encode")
    class Encode {

        @Test
        @DisplayName("returns the serializer's output")
        void encode_returnsSerializerOutput() {
            CombatStateType<Integer> type = persistentType(String::valueOf, Integer::parseInt);

            assertEquals(Optional.of("7"), codec.encode(type, 7, ENTITY_UUID));
        }

        @Test
        @DisplayName("returns empty for a session-scoped type with no serializer")
        void encode_returnsEmpty_whenNoSerializer() {
            CombatStateType<Integer> type = CombatStateType.of(KEY, Integer.class, 0, null);

            assertTrue(codec.encode(type, 7, ENTITY_UUID).isEmpty());
        }

        @Test
        @DisplayName("returns empty when the serializer throws")
        void encode_returnsEmpty_whenSerializerThrows() {
            CombatStateType<Integer> type = persistentType(value -> {
                throw new IllegalStateException("cannot serialize");
            }, Integer::parseInt);

            assertTrue(assertDoesNotThrow(() -> codec.encode(type, 7, ENTITY_UUID)).isEmpty());
        }

        @Test
        @DisplayName("returns empty when the serializer returns null")
        void encode_returnsEmpty_whenSerializerReturnsNull() {
            // Distinct from "no serializer declared", which Optional.map would conflate it with —
            // and it matters, because a dropped entry silently leaves stale data in the database.
            CombatStateType<Integer> type = persistentType(value -> null, Integer::parseInt);

            assertTrue(codec.encode(type, 7, ENTITY_UUID).isEmpty());
        }
    }

    @Nested
    @DisplayName("decode")
    class Decode {

        @Test
        @DisplayName("returns the deserializer's output")
        void decode_returnsDeserializerOutput() {
            CombatStateType<Integer> type = persistentType(String::valueOf, Integer::parseInt);

            assertEquals(Optional.of(7), codec.decode(type, "7", ENTITY_UUID));
        }

        @Test
        @DisplayName("returns empty for a session-scoped type with no deserializer")
        void decode_returnsEmpty_whenNoDeserializer() {
            CombatStateType<Integer> type = CombatStateType.of(KEY, Integer.class, 0, null);

            assertTrue(codec.decode(type, "7", ENTITY_UUID).isEmpty());
        }

        @Test
        @DisplayName("returns empty when the deserializer throws on a corrupt value")
        void decode_returnsEmpty_whenDeserializerThrows() {
            CombatStateType<Integer> type = persistentType(String::valueOf, Integer::parseInt);

            assertTrue(assertDoesNotThrow(() -> codec.decode(type, "not-a-number", ENTITY_UUID)).isEmpty());
        }

        @Test
        @DisplayName("returns empty when the deserializer returns null")
        void decode_returnsEmpty_whenDeserializerReturnsNull() {
            // A null reaching the raw state store would later blow up Map.copyOf in the session
            // snapshot, throwing out of session end.
            CombatStateType<Integer> type = persistentType(String::valueOf, serialized -> null);

            assertTrue(codec.decode(type, "7", ENTITY_UUID).isEmpty());
        }

        @Test
        @DisplayName("returns empty when the deserializer returns a wrongly-typed value")
        void decode_returnsEmpty_whenDeserializerReturnsWrongType() {
            // Reachable from a raw-typed deserializer. This path writes straight into the raw state
            // store, bypassing CombatSession.setState's validation, so the check has to happen here.
            // Built raw so the bad deserializer's String return doesn't get inferred into the type.
            Function<String, Integer> wrongTypeDeserializer = misusedRawDeserializer();
            CombatStateType<Integer> rawTypedType = persistentType(String::valueOf, wrongTypeDeserializer);

            assertTrue(codec.decode(rawTypedType, "7", ENTITY_UUID).isEmpty());
        }

        /**
         * A deserializer typed as {@code Function<String, Integer>} that actually returns a String,
         * simulating a third party's raw-typed or unchecked deserializer sneaking a wrong type past
         * the compiler.
         *
         * @return A deserializer that yields a non-Integer at runtime.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private Function<String, Integer> misusedRawDeserializer() {
            return (Function) (Function<String, String>) serialized -> "not an integer";
        }

        @Test
        @DisplayName("accepts a boxed value for a primitive class token")
        void decode_acceptsBoxedValue_forPrimitiveToken() {
            // int.class.isInstance(5) is false, so a naive check would reject every stored value.
            CombatStateType<Integer> type = CombatStateType.persistent(
                    KEY, int.class, 0, String::valueOf, Integer::parseInt, null);

            assertEquals(Optional.of(7), codec.decode(type, "7", ENTITY_UUID));
        }
    }
}
