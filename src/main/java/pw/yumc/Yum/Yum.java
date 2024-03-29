/**
 *
 */
package pw.yumc.Yum;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.commands.FileCommand;
import pw.yumc.Yum.commands.MonitorCommand;
import pw.yumc.Yum.commands.NetCommand;
import pw.yumc.Yum.commands.YumCommand;
import pw.yumc.Yum.listeners.PluginListener;
import pw.yumc.Yum.listeners.PluginNetworkListener;
import pw.yumc.Yum.listeners.SecurityListener;
import pw.yumc.Yum.listeners.ThreadSafetyListener;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.managers.MonitorManager;
import pw.yumc.Yum.managers.NetworkManager;
import pw.yumc.Yum.runnables.MainThreadCheckTask;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.reflect.Reflect;
import pw.yumc.YumCore.statistic.Statistics;
import pw.yumc.YumCore.update.SubscribeTask;

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
    }

    @Override
    public void onEnable() {
        if (Bukkit.isPrimaryThread()) {
            mainThread = Thread.currentThread();
        } else {
            mainThread = getMainThread();
        }
        new YumAPI();
        initCommands();
        initListeners();
        initRunnable();
        MonitorManager.init();
        new Statistics();
        new SubscribeTask();
        YumAPI.updateRepo(Bukkit.getConsoleSender());
    }

    @Override
    public void onLoad() {
        // 初始化配置
        ConfigManager.i();
        // 启用网络注入
        NetworkManager.register(this);
    }

    /**
     * @return 主线程
     */
    private Thread getMainThread() {
        Object console = Reflect.on(Bukkit.getServer()).get("console");
        return Reflect.on(console).get("primaryThread");
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

    /**
     * 初始化监听
     */
    private void initListeners() {
        if (ConfigManager.i().isSetOpEnable()) {
            try {
                ClassLoader cl = Class.forName("pw.yumc.injected.event.SetOpEvent").getClassLoader();
                try {
                    cl.getClass().getDeclaredField("plugin");
                    throw new ClassNotFoundException();
                } catch (NoSuchFieldException | SecurityException e) {
                    new SecurityListener(this);
                    Log.console("§a安全管理系统已启用...");
                }
            } catch (ClassNotFoundException e) {
                Log.console("§c服务端未注入安全拦截器 关闭功能...");
            }
        }
        if (ConfigManager.i().isNetworkEnable()) {
            new PluginNetworkListener(this);
            Log.console("§a网络管理系统已启用...");
        }
        if (ConfigManager.i().isThreadSafe()) {
            new ThreadSafetyListener(this);
            Log.console("§a线程管理系统已启用...");
        }
        if (ConfigManager.i().isMonitorEnable()) {
            new PluginListener();
        }
    }

    /**
     * 初始化任务
     */
    private void initRunnable() {
        // 需要在主线程注册任务
        if (ConfigManager.i().isMainThreadCheck() && mainThread != null) {
            Log.console("§aI O 管理系统已启用...");
            if (tt != null) {
                tt.cancel();
            }
            task.scheduleAtFixedRate(tt = new MainThreadCheckTask(mainThread), 0, 500);
        }
    }
}
