package pw.yumc.Yum.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import pw.yumc.Yum.Yum;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.kit.PKit;
import pw.yumc.injected.event.SetOpEvent;

/**
 *
 * @since 2016年3月31日 下午3:01:22
 * @author 喵♂呜
 */
public class SecurityListener implements Listener {
    private String prefix = "§6[§bYum §a安全系统§6] ";
    private String warn = "§c插件 §e%s §c已设置玩家 §a%s §c为OP §4请注意服务器安全!";
    private String prevent = "§c黑名单插件 §e%s §c尝试设置玩家 §a%s §c为OP §a安全系统已成功拦截!";

    public SecurityListener(Yum yum) {
        Bukkit.getPluginManager().registerEvents(this, yum);
    }

    @EventHandler
    public void setop(SetOpEvent e) {
        Plugin plugin = PKit.getOperatePlugin();
        if (plugin != null) {
            if (ConfigManager.i().getSetOpBlackList().contains(plugin.getName())) {
                Log.console(prefix + prevent, plugin, e.getOfflinePlayer().getName());
                e.setCancelled(true);
                return;
            }
            if (ConfigManager.i().getSetOpIgnoreList().contains(plugin.getName())) { return; }
            Bukkit.getConsoleSender().sendMessage(String.format(prefix + warn, plugin, e.getOfflinePlayer().getName()));
        }
    }
}
