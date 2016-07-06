package pw.yumc.Yum.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import cn.citycraft.PluginHelper.kit.PKit;
import pw.yumc.Yum.api.YumAPI;

/**
 *
 * @since 2016年7月6日 下午6:44:16
 * @author 喵♂呜
 */
public class PluginListener implements Listener {

    public PluginListener() {
        Bukkit.getPluginManager().registerEvents(this, PKit.i());
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent e) {
        YumAPI.uninject(e.getPlugin());
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent e) {
        YumAPI.inject(e.getPlugin());
    }
}
