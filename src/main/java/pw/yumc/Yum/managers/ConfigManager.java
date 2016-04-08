package pw.yumc.Yum.managers;

import java.util.List;

import cn.citycraft.PluginHelper.config.FileConfig;

public class ConfigManager {
    private static boolean allowPrimaryThread;
    private static boolean networkDebug;
    private static List<String> blackList;
    private static List<String> ignoreList;

    public static List<String> getBlackList() {
        return blackList;
    }

    public static List<String> getIgnoreList() {
        return ignoreList;
    }

    public static void init(final FileConfig config) {
        allowPrimaryThread = config.getBoolean("AllowPrimaryThread", false);
        networkDebug = config.getBoolean("NetworkDebug", false);
    }

    public static boolean isAllowPrimaryThread() {
        return allowPrimaryThread;
    }

    public static boolean isNetworkDebug() {
        return networkDebug;
    }
}
