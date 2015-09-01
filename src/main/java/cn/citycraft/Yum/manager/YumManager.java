package cn.citycraft.Yum.manager;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.Yum.repository.PluginInfo;

/**
 * 自动更新类
 * 
 * @author 蒋天蓓
 *         2015年9月1日上午10:59:47
 */
public class YumManager {
	Plugin plugin;
	public static DownloadManager download;
	public static PluginsManager plugman;
	public static RepositoryManager repo;

	public YumManager(Plugin plugin) {
		this.plugin = plugin;
		plugman = new PluginsManager(plugin);
		download = new DownloadManager(plugin);
		repo = new RepositoryManager(plugin);
	}

	public static boolean install(CommandSender sender, String pluginname) {
		return install(sender, pluginname, null);
	}

	public static boolean install(CommandSender sender, String pluginname, String version) {
		PluginInfo pi = repo.getPlugin(pluginname);
		if (pi != null)
			if (download.run(sender, pi.getMavenUrl(version))) {
				return plugman.load(sender, pluginname);
			}
		return false;
	}

	public static boolean update(CommandSender sender, Plugin plugin) {
		return update(sender, plugin, null);
	}

	public static boolean update(CommandSender sender, Plugin plugin, String version) {
		PluginInfo pi = repo.getPlugin(plugin.getName());
		if (pi != null) {

			if (download.run(sender, pi.getMavenUrl(version), plugman.getPluginFile(plugin))) {
				return plugman.reload(sender, plugin);
			}
			return false;
		} else {
			sender.sendMessage("§6更新: §c仓库缓存中未找到插件 " + plugin.getName());
			return false;
		}
	}
}
