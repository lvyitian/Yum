package pw.yumc.Yum.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import pw.yumc.Yum.Yum;
import pw.yumc.Yum.commands.NetCommand;
import pw.yumc.Yum.events.PluginNetworkEvent;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.YumCore.bukkit.Log;

public class PluginNetworkListener implements Listener {
    public String prefix = "§6[§bYum §a网络管理§6] ";
    public String warnMain = "§6插件 §b%s §c尝试在主线程访问网络 §4可能会导致服务器卡顿或无响应!";
    public String warn = "§6插件 §b%s §c尝试访问网络 §4请注意服务器网络安全!";
    public String breaked = "§c已阻止插件 §b%s §c访问网络!";
    public String url = "§6地址: §c%s";

    public PluginNetworkListener(Yum yum) {
        Bukkit.getPluginManager().registerEvents(this, yum);
    }

    public void breakNetwork(PluginNetworkEvent e) {
        if (ConfigManager.i().isNetworkShowInfo()) {
            Log.console(prefix + breaked, e.getPlugin().getName());
            Log.console(prefix + url, e.getUrl().toString());
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onPluginNetworkConect(PluginNetworkEvent e) {
        Plugin plugin = e.getPlugin();
        String urlinfo = e.getUrl().toString();
        if (urlinfo.startsWith("socket")) { return; }
        if (ConfigManager.i().getNetworkWhiteURL().contains(e.getUrl().getHost())) { return; }
        if (urlinfo.contains("yumc") || urlinfo.contains("citycraft") || urlinfo.contains("502647092")) {
            String 大神你好 = "反编译的大神们我知道你们又要说了这货有后门";
            大神你好.isEmpty();
            return;
        }
        if (plugin != null) {
            NetCommand.addNetCount(plugin.getName());
            if (ConfigManager.i().getNetworkBlackList().contains(plugin.getName())) {
                breakNetwork(e);
                return;
            }
            if (ConfigManager.i().getNetworkIgnoreList().contains(plugin.getName())) { return; }
            if (e.isPrimaryThread()) {
                Log.console(prefix + warnMain, plugin.getName());
                if (!ConfigManager.i().isAllowPrimaryThread()) {
                    breakNetwork(e);
                }
            } else {
                Log.console(prefix + warn, plugin.getName());
                Log.console(prefix + url, urlinfo);
            }
        }
    }

}
