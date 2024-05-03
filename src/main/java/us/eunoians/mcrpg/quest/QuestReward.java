package us.eunoians.mcrpg.quest;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This function runs after a {@link Quest} is completed for every single user
 * who was a part of the quest.
 */
public interface QuestReward {

    /**
     * Gives a reward to the user associated with the provided {@link UUID} after the provided
     * {@link Quest} has completed.
     *
     * @param uuid  The {@link UUID} associated with the user to be rewarded
     * @param quest The {@link Quest} that was completed
     */
    public void giveReward(@NotNull UUID uuid, @NotNull Quest quest);
}
