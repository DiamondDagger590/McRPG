package us.eunoians.mcrpg.skill;

import com.diamonddagger590.mccore.parser.Parser;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGMockExtension;
import us.eunoians.mcrpg.builder.item.skill.SkillItemBuilder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;

public class MockSkill implements Skill {

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
    public SkillItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
        throw new UnsupportedOperationException("Mock this behavior for your specific test");
    }

    @NotNull
    @Override
    public Parser getLevelUpEquation() {
        throw new UnsupportedOperationException("Mock this behavior for your specific test");
    }

    @Override
    public int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        throw new UnsupportedOperationException("Mock this behavior for your specific test");
    }

    @Override
    public boolean canEventLevelSkill(@NotNull Event event) {
        throw new UnsupportedOperationException("Mock this behavior for your specific test");
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }
}
