package us.eunoians.mcrpg.skill;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGMockExtension;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;

public abstract class MockSkill implements Skill {

    @NotNull
    @Override
    public McRPG getPlugin() {
        return McRPGMockExtension.mcRPG;
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return new NamespacedKey(getPlugin(), "mock-skill");
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "mock-skill";
    }

    @NotNull
    @Override
    public String getName(@NotNull McRPGPlayer player) {
        return "mock-skill";
    }

    @NotNull
    @Override
    public String getName() {
        return "mock-skill";
    }

    @NotNull
    @Override
    public Component getDisplayName(@NotNull McRPGPlayer player) {
        return getPlugin().getMiniMessage().deserialize("mock-skill");
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return getPlugin().getMiniMessage().deserialize("mock-skill");
    }

    @Override
    public int getMaxLevel() {
        return 1000;
    }

    @Override
    public boolean isSkillEnabled() {
        return true;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }
}
