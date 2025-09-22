package us.eunoians.mcrpg.gui.experiencebank.redeemable.skill;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.RedeemableType;
import us.eunoians.mcrpg.gui.skill.SkillGui;
import us.eunoians.mcrpg.skill.Skill;

import java.util.List;

/**
 * This gui is used to select what {@link Skill} to redeem either experience or levels into.
 */
public class RedeemableSkillSelectionGui extends SkillGui {

    private final RedeemableType redeemableType;

    public RedeemableSkillSelectionGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull RedeemableType redeemableType) {
        super(mcRPGPlayer);
        this.redeemableType = redeemableType;
    }

    @Override
    protected void paintSkills(int page) {
        List<Skill> sortedSkills = getSortedSkillsForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < sortedSkills.size()) {
                setSlot(i, new RedeemableSkillSelectionSlot(sortedSkills.get(i), redeemableType));
            } else {
                removeSlot(i);
            }
        }
    }
}
