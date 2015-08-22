/**
 * 
 */
package cn.citycraft.Yum.api;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.Yum.utils.DownloadManager;
import cn.citycraft.Yum.utils.PluginsManager;

/**
 * Yum仓库插件API
 * 
 * @author 蒋天蓓
 *         2015年8月22日下午4:43:41
 */
public class YumApi {

	public static void install(Plugin yum, final String pluginname) {
		install(yum, Bukkit.getConsoleSender(), pluginname);
	}

	public static void install(Plugin yum, final CommandSender sender, final String pluginname) {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		final DownloadManager download = new DownloadManager(yum);
		if (plugin == null) {
			Bukkit.getScheduler().runTaskAsynchronously(yum, new Runnable() {
				@Override
				public void run() {
					if (download.run(sender, pluginname)) {
						sender.sendMessage(PluginsManager.load(pluginname));
					}
				}
			});
		} else {
			sender.sendMessage("§c插件已安装在服务器 需要更新请使用yum update " + pluginname + "!");
		}
	}

	public static void update(Plugin yum, final String pluginname) {
		update(yum, Bukkit.getConsoleSender(), pluginname);
	}

	public static void update(Plugin yum, final CommandSender sender, final String pluginname) {
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		final DownloadManager download = new DownloadManager(yum);
		sender.sendMessage("§a开始更新插件: " + pluginname);
		if (plugin != null) {
			Bukkit.getScheduler().runTaskAsynchronously(yum, new Runnable() {
				@Override
				public void run() {
					sender.sendMessage(PluginsManager.unload(plugin));
					PluginsManager.getPluginFile(plugin).delete();
					if (download.run(sender, pluginname)) {
						sender.sendMessage(PluginsManager.load(pluginname));
					}
				}
			});
		} else {
			sender.sendMessage("§c插件未安装或已卸载 需要安装请使用yum install " + pluginname + "!");
		}
	}
}
