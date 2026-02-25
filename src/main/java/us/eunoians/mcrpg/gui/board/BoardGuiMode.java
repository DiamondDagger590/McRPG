package us.eunoians.mcrpg.gui.board;

/**
 * Determines which set of offerings the {@link QuestBoardGui} displays.
 */
public enum BoardGuiMode {
    /** Shared and personal offerings (default view). */
    SHARED_AND_PERSONAL,
    /** Scoped offerings across all member entities (displayed as "Group Quests" to players). */
    SCOPED
}
