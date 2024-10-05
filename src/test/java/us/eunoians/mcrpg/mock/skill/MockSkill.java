package us.eunoians.mcrpg.mock.skill;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Optional;

public class MockSkill extends Skill {

    private final int maxLevel;

    public MockSkill(@NotNull NamespacedKey skillKey) {
        super(skillKey);
        maxLevel = 1000;
    }

    public MockSkill(@NotNull NamespacedKey skillKey, int maxLevel) {
        super(skillKey);
        this.maxLevel = maxLevel;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Mocked Skill";
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }
}
