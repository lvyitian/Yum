package pw.yumc.Yum.managers;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.kit.PKit;

public class ConfigManager {
    private final static String ENABLE = "Enable";
    private final static String BLACK = "Black";
    private final static String IGNORE = "Ignore";

    private final static ConfigManager i = new ConfigManager(PKit.i());

    public final FileConfig config;
    public final FileConfig setop;
    public final FileConfig network;

    public static ConfigManager i() {
        return i;
    }

    public ConfigManager(final JavaPlugin plugin) {
        config = new FileConfig(plugin);
        setop = new FileConfig(plugin, "setop.yml");
        network = new FileConfig(plugin, "network.yml");
    }

    public List<String> getBlackList() {
        return config.getStringList("blacklist");
    }

    public List<String> getIgnoreList() {
        return config.getStringList("ignorelist");
    }

    public List<String> getNetworkBlackList() {
        return network.getStringList(BLACK);
    }

    public List<String> getNetworkIgnoreList() {
        return network.getStringList(IGNORE);
    }

    public List<String> getSetOpBlackList() {
        return setop.getStringList(BLACK);
    }

    public List<String> getSetOpIgnoreList() {
        return setop.getStringList(IGNORE);
    }

    public boolean isAllowPrimaryThread() {
        return config.getBoolean("AllowPrimaryThread", false);
    }

    public boolean isNetworkDebug() {
        return config.getBoolean("NetworkDebug", false);
    }

    public boolean isNetworkEnable() {
        return network.getBoolean(ENABLE, true);
    }

    public boolean isSetOpEnable() {
        return setop.getBoolean(ENABLE, true);
    }

    public void reload() {
        setop.reload();
        network.reload();
    }
}
