package pw.yumc.Yum.managers;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.config.FileConfig;
import pw.yumc.YumCore.sql.DataBase;

public class ConfigManager {
    public static String ENABLE = "Enable";
    public static String BLACK = "Black";
    public static String IGNORE = "Ignore";

    private static ConfigManager i = new ConfigManager(P.instance);

    public FileConfig config;
    public FileConfig setop;
    public FileConfig network;
    public FileConfig thread;
    public FileConfig monitor;

    public ConfigManager(JavaPlugin plugin) {
        config = new FileConfig();
        setop = new FileConfig("setop.yml");
        network = new FileConfig("network.yml");
        thread = new FileConfig("thread.yml");
        monitor = new FileConfig("monitor.yml");
    }

    public static ConfigManager i() {
        return i;
    }

    public List<String> getBlackList() {
        return config.getStringList("blacklist");
    }

    public DataBase getDataBase() {
        return DataBase.create(P.instance, config.getConfigurationSection(""));
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

    public boolean isLogToFile() {
        return monitor.getBoolean("LogToFile");
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

    public List<String> getNetWorkDebug() {
        return network.getStringList("Debug");
    }

    public void reload() {
        setop.reload();
        network.reload();
        thread.reload();
        monitor.reload();
    }
}
