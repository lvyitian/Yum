package pw.yumc.Yum.manager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.kit.PluginKit;
import pw.yumc.Yum.Yum;
import pw.yumc.injected.event.SetOpEvent;

/**
 *
 * @since 2016年3月31日 下午3:01:22
 * @author 喵♂呜
 */
public class SecurityManager implements Listener {
    public String warn = "§6[§bYum §a安全系统§6] §c插件 §e%s §c已设置玩家 §a%s §c为OP §4请注意服务器安全!";
    public String prevent = "§6[§bYum §a安全系统§6] §c黑名单插件 §e%s §c尝试设置玩家 §a%s §c为OP §a安全系统已成功拦截!";

    public SecurityManager(final Yum yum) {
        Bukkit.getPluginManager().registerEvents(this, yum);
    }

    @EventHandler
    public void setop(final SetOpEvent event) {
        final Plugin plugin = PluginKit.getOperatePlugin();
        if (plugin != null) {
            if (plugin.getName().equalsIgnoreCase("BukkitInjectedTools")) {
                Bukkit.getConsoleSender().sendMessage(String.format(prevent, plugin, event.getOfflinePlayer().getName()));
            } else {
                Bukkit.getConsoleSender().sendMessage(String.format(warn, plugin, event.getOfflinePlayer().getName()));
            }
        }
    }
}
