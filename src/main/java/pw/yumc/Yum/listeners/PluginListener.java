package pw.yumc.Yum.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.managers.MonitorManager;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.kit.PKit;

/**
 *
 * @since 2016年7月6日 下午6:44:16
 * @author 喵♂呜
 */
public class PluginListener implements Listener {

    public PluginListener() {
        Bukkit.getPluginManager().registerEvents(this, P.instance);
        Log.console("§a性能监控系统已启用...");
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent e) {
        YumAPI.uninject(e.getPlugin());
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent e) {
        if (ConfigManager.i().getMonitorIgnoreList().contains(e.getPlugin().getName())) {
            return;
        }
        MonitorManager.reset(e.getPlugin().getName());
        PKit.runTaskLater(new Runnable() {
            @Override
            public void run() {
                YumAPI.inject(e.getPlugin());
            }
        }, 2);
    }
}
