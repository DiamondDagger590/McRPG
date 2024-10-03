package us.eunoians.mcrpg.util;

import org.jetbrains.annotations.NotNull;

public class McRPGMethods {

    private static final String MCRPG_NAMESPACED_KEY = "mcrpg";

    @NotNull
    public static String getMcRPGNamespace() {
        return MCRPG_NAMESPACED_KEY;
    }
}
