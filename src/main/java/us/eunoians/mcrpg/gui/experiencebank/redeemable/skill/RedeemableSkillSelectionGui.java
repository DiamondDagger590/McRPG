package us.eunoians.mcrpg.gui.experiencebank.redeemable.skill;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.skill.SkillGui;
import us.eunoians.mcrpg.skill.Skill;

import java.util.List;

public class RedeemableSkillSelectionGui extends SkillGui {

    private final boolean redeemableExperience;

    public RedeemableSkillSelectionGui(@NotNull McRPGPlayer mcRPGPlayer, boolean redeemableExperience) {
        super(mcRPGPlayer);
        this.redeemableExperience = redeemableExperience;
    }

    @Override
    protected void paintSkills(int page) {
        List<Skill> sortedSkills = getSortedSkillsForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < sortedSkills.size()) {
                setSlot(i, new RedeemableSkillSelectionSlot(sortedSkills.get(i)));
            } else {
                removeSlot(i);
            }
        }
    }
}
