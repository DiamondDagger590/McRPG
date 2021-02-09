package us.eunoians.mcrpg.api.lunar;

import com.lunarclient.bukkitapi.LunarClientAPI;
import org.jetbrains.annotations.NotNull;

/**
 * Handles allowing McRPG to utilize {@link LunarClientAPI} features
 *
 * @author DiamondDagger590
 */
public class LunarClientHook {

    @NotNull
    private final LunarClientAPI lunarClientAPI;

    public LunarClientHook() {
        lunarClientAPI = LunarClientAPI.getInstance();
    }

    /**
     * Gets the instance of {@link LunarClientAPI}
     *
     * @return The instance of {@link LunarClientAPI}
     */
    @NotNull
    public LunarClientAPI getLunarClientAPI() {
        return lunarClientAPI;
    }
}
