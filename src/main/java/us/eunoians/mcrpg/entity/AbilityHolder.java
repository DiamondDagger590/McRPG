package us.eunoians.mcrpg.entity;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AbilityHolder {

    private final UUID uuid;

    public AbilityHolder(@NotNull UUID uuid){
        this.uuid = uuid;
    }
}
