/**
 *
 */
package pw.yumc.Yum;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.CommonData.UpdatePlugin;
import cn.citycraft.PluginHelper.kit.PluginKit;
import cn.citycraft.PluginHelper.utils.VersionChecker;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.commands.FileCommand;
import pw.yumc.Yum.commands.MonitorCommand;
import pw.yumc.Yum.commands.NetCommand;
import pw.yumc.Yum.commands.YumCommand;
import pw.yumc.Yum.listeners.PluginNetworkListener;
import pw.yumc.Yum.listeners.SecurityListener;
import pw.yumc.Yum.listeners.ThreadSafetyListener;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.managers.NetworkManager;
import pw.yumc.Yum.runnables.MainThreadCheckTask;

/**
 * MC插件仓库
 *
 * @author 喵♂呜
 * @since 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
    public static Thread mainThread = null;
    public static Timer task = new Timer();
    public static TimerTask tt;

    @Override
    public FileConfiguration getConfig() {
        return ConfigManager.i().config;
    }

    @Override
    public void onDisable() {
        NetworkManager.unregister();
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            YumAPI.uninject(plugin);
        }
    }

    @Override
    public void onEnable() {
        if (Bukkit.isPrimaryThread()) {
            mainThread = Thread.currentThread();
        }
        new YumAPI();
        initCommands();
        initListeners();
        initRunnable();
        initInject();
        new VersionChecker(this);
        YumAPI.updaterepo(Bukkit.getConsoleSender());
        YumAPI.updatecheck(Bukkit.getConsoleSender());
    }

    @Override
    public void onLoad() {
        // 初始化配置
        ConfigManager.i();
        // 初始化更新列
        UpdatePlugin.getUpdateList();
        // 启用网络注入
        NetworkManager.register(this);
    }

    /**
     * 初始化命令
     */
    private void initCommands() {
        new YumCommand(this);
        new NetCommand(this);
        new FileCommand(this);
        new MonitorCommand(this);
    }

    private void initInject() {
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            YumAPI.inject(plugin);
        }
        PluginKit.scp("§a性能监控系统已启用...");
    }

    /**
     * 初始化监听
     */
    private void initListeners() {
        if (ConfigManager.i().isSetOpEnable()) {
            try {
                final ClassLoader cl = Class.forName("pw.yumc.injected.event.SetOpEvent").getClassLoader();
                try {
                    cl.getClass().getDeclaredField("plugin");
                    throw new ClassNotFoundException();
                } catch (final NoSuchFieldException | SecurityException e) {
                    new SecurityListener(this);
                    PluginKit.scp("§a安全管理系统已启用...");
                }
            } catch (final ClassNotFoundException e) {
                PluginKit.scp("§c服务端未注入安全拦截器 关闭功能...");
            }
        }
        if (ConfigManager.i().isNetworkEnable()) {
            new PluginNetworkListener(this);
            PluginKit.scp("§a网络管理系统已启用...");
        }
        if (ConfigManager.i().isThreadSafe()) {
            new ThreadSafetyListener(this);
            PluginKit.scp("§a线程管理系统已启用...");
        }
    }

    /**
     * 初始化任务
     */
    private void initRunnable() {
        // 需要在主线程注册任务
        if (ConfigManager.i().isMainThreadCheck() && mainThread != null) {
            PluginKit.scp("§aIO管理系统已启用...");
            if (tt != null) {
                tt.cancel();
            }
            task.scheduleAtFixedRate(tt = new MainThreadCheckTask(mainThread), 0, 5000);
        }
    }
}
