package pw.yumc.Yum.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import cn.citycraft.PluginHelper.kit.PKit;
import cn.citycraft.PluginHelper.kit.PluginKit;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.managers.MonitorManager;

/**
 *
 * @since 2016年7月6日 下午6:44:16
 * @author 喵♂呜
 */
public class PluginListener implements Listener {

    public PluginListener() {
        Bukkit.getPluginManager().registerEvents(this, PKit.i());
        PluginKit.scp("§a性能监控系统已启用...");
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent e) {
        YumAPI.uninject(e.getPlugin());
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent e) {
        MonitorManager.reset(e.getPlugin().getName());
        PluginKit.runTaskLater(new Runnable() {
            @Override
            public void run() {
                YumAPI.inject(e.getPlugin());
            }
        }, 60);
    }
}
