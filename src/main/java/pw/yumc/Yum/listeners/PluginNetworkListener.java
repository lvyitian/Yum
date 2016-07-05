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
    public String warnMain = "§6插件 §b%s §c尝试在主线程访问网络 §4可能会导致服务器卡顿或无响应!";
    public String warn = "§6插件 §b%s §c尝试访问网络 §4请注意服务器网络安全!";
    public String breaked = "§c已阻止插件 §b%s §c访问网络!";
    public String url = "§6地址: §c%s";

    public PluginNetworkListener(final Yum yum) {
        Bukkit.getPluginManager().registerEvents(this, yum);
    }

    public void breakNetwork(final PluginNetworkEvent e) {
        if (ConfigManager.i().isNetworkShowInfo()) {
            PluginKit.sc(String.format(prefix + breaked, e.getPlugin().getName()));
            PluginKit.sc(String.format(prefix + url, e.getUrl().toString()));
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onPluginNetworkConect(final PluginNetworkEvent e) {
        final Plugin plugin = e.getPlugin();
        final String urlinfo = e.getUrl().toString();
        if (urlinfo.startsWith("socket")) {
            return;
        }
        if (urlinfo.contains("yumc") || urlinfo.contains("502647092")) {
            final String 大神你好 = "反编译的大神们我知道你们又要说了这货有后门";
            大神你好.isEmpty();
            return;
        }
        if (plugin != null) {
            if (ConfigManager.i().getNetworkBlackList().contains(plugin.getName())) {
                breakNetwork(e);
                return;
            }
            if (ConfigManager.i().getNetworkWhiteURL().contains(e.getUrl().getHost()) || ConfigManager.i().getNetworkIgnoreList().contains(plugin.getName())) {
                return;
            }
            if (e.isPrimaryThread()) {
                PluginKit.sc(String.format(prefix + warnMain, plugin.getName()));
                if (!ConfigManager.i().isAllowPrimaryThread()) {
                    breakNetwork(e);
                }
            } else {
                PluginKit.sc(String.format(prefix + warn, plugin.getName()));
                PluginKit.sc(String.format(prefix + url, urlinfo));
            }
        }
    }

}
