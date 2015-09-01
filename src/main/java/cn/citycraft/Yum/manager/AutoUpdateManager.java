package cn.citycraft.Yum.manager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * 自动更新类
 * 
 * @author 蒋天蓓
 *         2015年9月1日上午10:59:47
 */
public class AutoUpdateManager {
	Plugin plugin;
	DownloadManager download;
	PluginsManager plugman;

	public AutoUpdateManager(Plugin plugin) {
		this.plugin = plugin;
		plugman = new PluginsManager(plugin);
		download = new DownloadManager(plugin);
	}

	public boolean run(CommandSender sender) {
		if (download.run(sender, "下载地址", plugman.getPluginFile(plugin))) {
			plugman.reload(sender, plugin);
		}
		return false;
	}

	public boolean run() {
		return run(Bukkit.getConsoleSender());
	}
}
