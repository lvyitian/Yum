package pw.yumc.Yum.managers;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.kit.PKit;

public class ConfigManager {
    public final static String ENABLE = "Enable";
    public final static String BLACK = "Black";
    public final static String IGNORE = "Ignore";

    private final static ConfigManager i = new ConfigManager(PKit.i());

    public final FileConfig config;
    public final FileConfig setop;
    public final FileConfig network;
    public final FileConfig thread;
    public final FileConfig monitor;

    public ConfigManager(final JavaPlugin plugin) {
        config = new FileConfig(plugin);
        setop = new FileConfig(plugin, "setop.yml");
        network = new FileConfig(plugin, "network.yml");
        thread = new FileConfig(plugin, "thread.yml");
        monitor = new FileConfig(plugin, "monitor.yml");
    }

    public static ConfigManager i() {
        return i;
    }

    public List<String> getBlackList() {
        return config.getStringList("blacklist");
    }

    public List<String> getIgnoreList() {
        return config.getStringList("ignorelist");
    }

    public List<String> getMonitorIgnoreList() {
        return monitor.getStringList(IGNORE);
    }

    public List<String> getNetworkBlackList() {
        return network.getStringList(BLACK);
    }

    public List<String> getNetworkIgnoreList() {
        return network.getStringList(IGNORE);
    }

    public List<String> getNetworkWhiteURL() {
        return network.getStringList("WhiteURL");
    }

    public List<String> getSetOpBlackList() {
        return setop.getStringList(BLACK);
    }

    public List<String> getSetOpIgnoreList() {
        return setop.getStringList(IGNORE);
    }

    public boolean isAllowPrimaryThread() {
        return network.getBoolean("AllowPrimaryThread", false);
    }

    public boolean isMainThreadCheck() {
        return thread.getBoolean("MainThreadCheck", true);
    }

    public boolean isMonitorDebug() {
        return monitor.getBoolean("Debug");
    }

    public boolean isMonitorEnable() {
        return monitor.getBoolean(ENABLE, true);
    }

    public boolean isNetworkDebug() {
        return network.getBoolean("NetworkDebug", false);
    }

    public boolean isNetworkEnable() {
        return network.getBoolean(ENABLE, true);
    }

    public boolean isNetworkShowInfo() {
        return network.getBoolean("ShowInfo", true);
    }

    public boolean isSetOpEnable() {
        return setop.getBoolean(ENABLE, true);
    }

    public boolean isThreadSafe() {
        return thread.getBoolean("ThreadSafe", true);
    }

    public void reload() {
        setop.reload();
        network.reload();
        thread.reload();
        monitor.reload();
    }
}
