package us.eunoians.mcrpg.task.quest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestSaveTaskTest extends McRPGBaseTest {

    private QuestDefinition definition;

    @BeforeEach
    public void setup() {
        definition = QuestTestHelper.singlePhaseQuest("save_test");
    }

    @DisplayName("Given a freshly created quest, when checking dirty, then it returns false")
    @Test
    public void isDirty_returnsFalse_onFreshInstance() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        assertFalse(instance.isDirty());
    }

    @DisplayName("Given a quest, when markDirty is called, then isDirty returns true")
    @Test
    public void markDirty_setsFlag() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        instance.markDirty();
        assertTrue(instance.isDirty());
    }

    @DisplayName("Given a dirty quest, when clearDirty is called, then isDirty returns false")
    @Test
    public void clearDirty_resetsFlag() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        instance.markDirty();
        instance.clearDirty();
        assertFalse(instance.isDirty());
    }

    @DisplayName("Given a quest with progress applied, when checking dirty, then it returns true")
    @Test
    public void progressApplied_setsDirty() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        assertFalse(instance.isDirty());
        instance.markDirty();
        assertTrue(instance.isDirty());
    }

    @DisplayName("Given a dirty quest, when clearDirty then markDirty again, then it cycles correctly")
    @Test
    public void dirtyFlagLifecycle_cyclesCorrectly() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        assertFalse(instance.isDirty());
        instance.markDirty();
        assertTrue(instance.isDirty());
        instance.clearDirty();
        assertFalse(instance.isDirty());
        instance.markDirty();
        assertTrue(instance.isDirty());
    }
}
