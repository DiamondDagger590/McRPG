package us.eunoians.mcrpg.quest.board.distribution;

/**
 * Controls how integer truncation remainders are distributed when splitting integral
 * reward amounts. For example, 10 diamonds split among 3 players yields 3 each
 * (9 total) with 1 remainder.
 */
public enum RemainderStrategy {

    /**
     * The remainder is lost (default). Simple, predictable.
     * 10 diamonds / 3 players = 3 each, 1 discarded.
     */
    DISCARD,

    /**
     * The remainder goes to the top contributor by contribution amount.
     * 10 diamonds / 3 players = top gets 4, others get 3.
     * If contributions are equal, the first by UUID natural ordering receives extra.
     */
    TOP_CONTRIBUTOR,

    /**
     * The remainder is distributed randomly among qualifying players.
     * 10 diamonds / 3 players = 3 each + 1 random player gets +1.
     */
    RANDOM
}
