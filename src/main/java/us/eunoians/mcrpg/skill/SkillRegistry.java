package us.eunoians.mcrpg.skill;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.skill.SkillRegisterEvent;
import us.eunoians.mcrpg.api.event.skill.SkillUnregisterEvent;

import java.util.HashMap;
import java.util.Map;

public class SkillRegistry {

    private final McRPG mcRPG;
    private final Map<NamespacedKey, Skill> skills;

    public SkillRegistry(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        this.skills = new HashMap<>();
    }

    public void registerSkill(@NotNull Skill skill) {
        skills.put(skill.getSkillKey(), skill);
        Bukkit.getPluginManager().callEvent(new SkillRegisterEvent(skill));
    }

    public boolean isSkillRegistered(@NotNull Skill skill) {
        return isSkillRegistered(skill.getSkillKey());
    }

    public boolean isSkillRegistered(@NotNull NamespacedKey skillKey) {
        return skills.containsKey(skillKey);
    }

    public void unregisterSkill(@NotNull Skill skill) {
        unregisterSkill(skill.getSkillKey());
    }

    public void unregisterSkill(@NotNull NamespacedKey skillKey) {
        Skill skill = skills.remove(skillKey);
        if (skill != null) {
            Bukkit.getPluginManager().callEvent(new SkillUnregisterEvent(skill));
        }
    }

}
