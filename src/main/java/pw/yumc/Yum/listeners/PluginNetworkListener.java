package pw.yumc.Yum.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.kit.PluginKit;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.events.PluginNetworkEvent;
import pw.yumc.Yum.managers.ConfigManager;

public class PluginNetworkListener implements Listener {
    public String prefix = "§6[§bYum §a网络管理§6] ";

    public PluginNetworkListener(final Yum yum) {
        Bukkit.getPluginManager().registerEvents(this, yum);
    }

    @EventHandler
    public void onPluginNetworkConect(final PluginNetworkEvent e) {
        final Plugin plugin = e.getPlugin();
        final String urlinfo = e.getUrl().toString();
        final boolean isPrimaryThread = e.isPrimaryThread();
        final String str = prefix + (isPrimaryThread ? "§c插件 §6%s §c尝试在主线程访问 §e%s §4可能会导致服务器卡顿或无响应!" : "§c插件 §6%s §c尝试访问 §e%s §c请注意服务器网络安全!");
        if (plugin != null) {
            if (ConfigManager.i().getNetworkBlackList().contains(plugin.getName())) {
                PluginKit.sc(prefix + "§4已阻止黑名单插件 §b" + plugin.getName() + " §4访问网络!");
                PluginKit.sc(prefix + "§4地址: " + urlinfo);
                return;
            }
            PluginKit.sc(String.format(str, plugin.getName(), urlinfo));
            if (!ConfigManager.i().isAllowPrimaryThread() && isPrimaryThread) {
                PluginKit.sc(prefix + "§4已阻止插件 §b" + plugin.getName() + " §4在主线程访问网络!");
                e.setCancelled(true);
            }
        }
    }

}
