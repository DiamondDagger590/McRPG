package us.eunoians.mcrpg.listener.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestManager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

public class QuestProgressListenerWiringTest extends McRPGBaseTest {

    @Test
    @DisplayName("Given a QuestManager, when constructing AbilityActivateQuestProgressListener, then it does not throw")
    public void construct_abilityActivateListener_doesNotThrow() {
        QuestManager qm = mock(QuestManager.class);
        assertDoesNotThrow(() -> new AbilityActivateQuestProgressListener(qm));
    }

    @Test
    @DisplayName("Given a QuestManager, when constructing AbilityUnlockQuestProgressListener, then it does not throw")
    public void construct_abilityUnlockListener_doesNotThrow() {
        QuestManager qm = mock(QuestManager.class);
        assertDoesNotThrow(() -> new AbilityUnlockQuestProgressListener(qm));
    }

    @Test
    @DisplayName("Given a QuestManager, when constructing SkillLevelQuestProgressListener, then it does not throw")
    public void construct_skillLevelListener_doesNotThrow() {
        QuestManager qm = mock(QuestManager.class);
        assertDoesNotThrow(() -> new SkillLevelQuestProgressListener(qm));
    }

    @Test
    @DisplayName("Given a QuestManager, when constructing GuiOpenQuestProgressListener, then it does not throw")
    public void construct_guiOpenListener_doesNotThrow() {
        QuestManager qm = mock(QuestManager.class);
        assertDoesNotThrow(() -> new GuiOpenQuestProgressListener(qm));
    }

    @Test
    @DisplayName("Given a QuestManager, when constructing QuestBoardAcceptQuestProgressListener, then it does not throw")
    public void construct_questBoardAcceptListener_doesNotThrow() {
        QuestManager qm = mock(QuestManager.class);
        assertDoesNotThrow(() -> new QuestBoardAcceptQuestProgressListener(qm));
    }

    @Test
    @DisplayName("Given a QuestManager, when constructing LoadoutEquipQuestProgressListener, then it does not throw")
    public void construct_loadoutEquipListener_doesNotThrow() {
        QuestManager qm = mock(QuestManager.class);
        assertDoesNotThrow(() -> new LoadoutEquipQuestProgressListener(qm));
    }
}
