package us.eunoians.mcrpg.skill;

import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import us.eunoians.mcrpg.BaseTest;

public class SkillRegistryTest extends BaseTest {

    //@Test
    public void skillRegistryTest () {
        TestSkill skill = getPlugin().getSkillRegistry().registerSkill(new NamespacedKey("unit-test", "test"), TestSkill::new);
        Validate.notNull(skill, "Failed to register skill!");

        TestSkill testSkill = (TestSkill) getPlugin().getSkillRegistry().getSkill(new NamespacedKey("unit-test", "test")).orElse(null);
        Validate.notNull(testSkill, "Failed to get skill from skill registry! (returned empty optional!)");
    }

    /**
     * Test skill used in unit testing
     *
     * @author OxKitsune
     */
    public static class TestSkill extends AbstractSkill {

        /**
         * Construct a new {@link AbstractSkill}.
         *
         * @param id the id of the skill
         */
        public TestSkill(NamespacedKey id) {
            super(id);
        }
    }

}
