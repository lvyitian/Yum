package pw.yumc.Yum.listeners;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.kit.ExceptionKit;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.events.PluginNetworkEvent;
import pw.yumc.Yum.managers.ConfigManager;

public class PluginNetworkListener implements Listener {

    public PluginNetworkListener(final Yum yum) {
        Bukkit.getPluginManager().registerEvents(this, yum);
    }

    public void onPluginNetworkConect(final PluginNetworkEvent e) {
        final Plugin plugin = e.getPlugin();
        final String urlinfo = e.getUrl().toString();
        final boolean isPrimaryThread = e.isPrimaryThread();
        final String str = isPrimaryThread ? "§6[§bYum §a网络管理§6] §c插件 §6%s §c尝试在主线程访问 §e%s §4可能会导致服务器卡顿或无响应!" : "§6[§bYum §a网络监控§6] §c插件 §6%s §c尝试访问 §e%s §c请注意服务器网络安全!";
        if (plugin != null) {
            Bukkit.getConsoleSender().sendMessage(String.format(str, plugin.getName(), urlinfo));
            if (!ConfigManager.isAllowPrimaryThread() && isPrimaryThread) {
                Bukkit.getConsoleSender().sendMessage("§6[§bYum §a网络管理§6] §4已阻止插件 §b" + plugin.getName() + " §4在主线程访问网络!");
                ExceptionKit.throwException(new IOException("[Yum 网络防护] 已开启网络防护 不允许在主线程访问网络!"));
            }
        }
    }

}
