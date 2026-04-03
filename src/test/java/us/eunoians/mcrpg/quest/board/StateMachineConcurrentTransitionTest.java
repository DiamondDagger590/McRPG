package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Regression tests for the TOCTOU race on {@link BoardOffering#accept(long, UUID)}.
 * <p>
 * Background: {@link us.eunoians.mcrpg.util.StateMachine} has no internal synchronization.
 * {@code canTransitionTo()} and {@code transitionTo()} are two separate operations. Without
 * an external lock, two threads can both read {@code VISIBLE} before either advances the
 * state, and both submit DB queries (in production) believing they are permitted to accept.
 * The state machine catches the second {@code transitionTo()} call, but only after the DB
 * query already ran — meaning duplicate acceptance attempts reach the database.
 * <p>
 * The fix wraps the guard ({@code canTransitionTo}) and the mutation ({@code accept}) together
 * inside a per-offering {@code synchronized} block, using the same {@code offeringLocks} map
 * pattern already in place for personal offerings in {@code acceptOffering()}.
 */
public class StateMachineConcurrentTransitionTest {

    private static BoardOffering freshVisibleOffering() {
        return new BoardOffering(
                UUID.randomUUID(), UUID.randomUUID(),
                new NamespacedKey("mcrpg", "shared_daily"), 0,
                new NamespacedKey("mcrpg", "test_quest"),
                new NamespacedKey("mcrpg", "common"),
                "land_entity_1", Duration.ofHours(24));
    }

    /**
     * Demonstrates the TOCTOU window: both threads call {@code canTransitionTo(ACCEPTED)}
     * before either calls {@code accept()}. The {@link CyclicBarrier} forces both threads
     * into the gap between check and mutate simultaneously, reproducing the production
     * scenario where two concurrent HTTP requests both pass the guard before the DB query
     * for {@code acceptScopedOffering()} completes.
     * <p>
     * Both threads observe {@code canTransitionTo() == true}. The second {@code accept()} call
     * throws {@link IllegalStateException} from the state machine (which is the only thing
     * preventing silent double-acceptance at this layer), but crucially <em>both DB queries
     * were already submitted</em> in the production code path.
     */
    @Test
    @DisplayName("without lock: both threads observe canTransitionTo=true before either accepts — the TOCTOU window")
    void withoutLock_bothThreadsObserveCanTransitionToTrue_demonstratingTocTouWindow() throws Exception {
        BoardOffering offering = freshVisibleOffering();
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger trueCheckCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        // Both threads check before either accepts — models the race between main-thread guard
        // and DB-executor accept() in acceptScopedOffering().
        Runnable raceyAccept = () -> {
            try {
                boolean canAccept = offering.canTransitionTo(BoardOffering.State.ACCEPTED);
                if (canAccept) {
                    trueCheckCount.incrementAndGet();
                }
                // Both pause here — now both have checked but neither has mutated
                barrier.await(5, TimeUnit.SECONDS);
                if (canAccept) {
                    try {
                        offering.accept(System.currentTimeMillis(), UUID.randomUUID());
                        successCount.incrementAndGet();
                    } catch (IllegalStateException ignored) {
                        // Second thread: the state machine catches the duplicate transitionTo call.
                        // In production, the DB query ran before this line — that's the real bug.
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<?>> futures = List.of(executor.submit(raceyAccept), executor.submit(raceyAccept));
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertEquals(2, trueCheckCount.get(),
                "Both threads must have observed canTransitionTo=true — the TOCTOU window is open");
        assertEquals(1, successCount.get(),
                "Only one accept() succeeds at the state-machine level, but both DB paths were triggered");
    }

    /**
     * Verifies the fixed pattern: wrapping both {@code canTransitionTo} and {@code accept}
     * inside a {@code synchronized} block keyed by offering ID ensures that at most one
     * thread can complete an acceptance. The second thread acquires the lock, re-checks the
     * state (now {@code ACCEPTED}), and exits without calling {@code accept()}.
     * <p>
     * Run repeatedly to exercise the scheduler across many interleavings.
     */
    @RepeatedTest(50)
    @DisplayName("with offeringLocks pattern: exactly one concurrent acceptance succeeds")
    void withOfferingLock_exactlyOneAcceptanceSucceeds_underConcurrentLoad() throws Exception {
        BoardOffering offering = freshVisibleOffering();
        ConcurrentHashMap<UUID, Object> offeringLocks = new ConcurrentHashMap<>();
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger successCount = new AtomicInteger(0);

        // Mirrors the fixed acceptScopedOffering() pattern: synchronized check + accept
        Runnable lockedAccept = () -> {
            try {
                barrier.await(5, TimeUnit.SECONDS); // arrive together
                Object lock = offeringLocks.computeIfAbsent(offering.getOfferingId(), k -> new Object());
                synchronized (lock) {
                    if (offering.canTransitionTo(BoardOffering.State.ACCEPTED)) {
                        offering.accept(System.currentTimeMillis(), UUID.randomUUID());
                        successCount.incrementAndGet();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<?>> futures = List.of(executor.submit(lockedAccept), executor.submit(lockedAccept));
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertEquals(1, successCount.get(),
                "Exactly one acceptance must succeed with the per-offering lock");
        assertEquals(BoardOffering.State.ACCEPTED, offering.getState(),
                "Offering must be in ACCEPTED state after one successful acceptance");
        assertNotNull(offering.getQuestInstanceUUID().orElse(null),
                "questInstanceUUID must be set by the accepting thread");
    }

    /**
     * Verifies that the per-offering lock correctly rejects acceptance of an already-accepted
     * offering, even when the second caller arrives long after the first has completed.
     * This is the steady-state guard that prevents re-acceptance.
     */
    @Test
    @DisplayName("with offeringLocks pattern: second acceptance attempt on ACCEPTED offering returns false")
    void withOfferingLock_secondAcceptanceAttempt_isRejected() {
        BoardOffering offering = freshVisibleOffering();
        ConcurrentHashMap<UUID, Object> offeringLocks = new ConcurrentHashMap<>();
        AtomicInteger successCount = new AtomicInteger(0);

        // Simulate sequentially — first acceptance
        Object lock = offeringLocks.computeIfAbsent(offering.getOfferingId(), k -> new Object());
        synchronized (lock) {
            if (offering.canTransitionTo(BoardOffering.State.ACCEPTED)) {
                offering.accept(System.currentTimeMillis(), UUID.randomUUID());
                successCount.incrementAndGet();
            }
        }

        // Second attempt (e.g., player clicks again or second player tries)
        Object lock2 = offeringLocks.computeIfAbsent(offering.getOfferingId(), k -> new Object());
        synchronized (lock2) {
            if (offering.canTransitionTo(BoardOffering.State.ACCEPTED)) {
                offering.accept(System.currentTimeMillis(), UUID.randomUUID());
                successCount.incrementAndGet();
            }
        }

        assertEquals(1, successCount.get(),
                "Second acceptance attempt must be rejected because the offering is already ACCEPTED");
    }

    /**
     * Verifies the pattern holds under a higher thread count — 5 threads concurrently
     * attempting to accept the same offering. Only one must succeed.
     */
    @RepeatedTest(20)
    @DisplayName("with offeringLocks pattern: exactly one acceptance across 5 concurrent threads")
    void withOfferingLock_exactlyOneAcceptance_underHighConcurrency() throws Exception {
        int threadCount = 5;
        BoardOffering offering = freshVisibleOffering();
        ConcurrentHashMap<UUID, Object> offeringLocks = new ConcurrentHashMap<>();
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                    Object lock = offeringLocks.computeIfAbsent(offering.getOfferingId(), k -> new Object());
                    synchronized (lock) {
                        if (offering.canTransitionTo(BoardOffering.State.ACCEPTED)) {
                            offering.accept(System.currentTimeMillis(), UUID.randomUUID());
                            successCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertEquals(1, successCount.get(),
                "Exactly one acceptance must succeed across " + threadCount + " concurrent threads");
    }
}
