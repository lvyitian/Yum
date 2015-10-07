package cn.citycraft.Yum.manager;

import java.io.File;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * 自动更新类
 *
 * @author 蒋天蓓
 *         2015年9月1日上午10:59:47
 */
public class YumManager {
	public static DownloadManager download;
	public static PluginsManager plugman;
	public static RepositoryManager repo;

	Plugin plugin;

	public YumManager(final Plugin plugin) {
		this.plugin = plugin;
		plugman = new PluginsManager(plugin);
		download = new DownloadManager(plugin);
		repo = new RepositoryManager(plugin);
	}

	public static boolean install(final CommandSender sender, final String pluginname) {
		return install(sender, pluginname, null);
	}

	public static boolean install(final CommandSender sender, final String pluginname, final String version) {
		final PluginInfo pi = repo.getPlugin(pluginname);
		if (pi != null) {
			if (download.run(sender, pi.getMavenUrl(version))) {
				return plugman.load(sender, pluginname);
			}
		}
		return false;
	}

	public static boolean update(final CommandSender sender, final Plugin plugin) {
		return update(sender, plugin, null);
	}

	public static boolean update(final CommandSender sender, final Plugin plugin, final String version) {
		final PluginInfo pi = repo.getPlugin(plugin.getName());
		if (pi != null) {
			if (download.run(sender, pi.getMavenUrl(version), new File(Bukkit.getUpdateFolderFile(), plugman.getPluginFile(plugin).getName()))) {
				sender.sendMessage("§6更新: §a已下载插件 " + plugin.getName() + " 到update文件夹 重启后自动更新(或使用upgrade直接升级)!");
				return true;
			}
		} else {
			sender.sendMessage("§6更新: §c仓库缓存中未找到插件 " + plugin.getName());
		}
		return false;
	}

	public static boolean update(final CommandSender sender, final Plugin plugin, final URL url, final String version) {
		if (download.run(sender, url, new File(Bukkit.getUpdateFolderFile(), plugman.getPluginFile(plugin).getName()))) {
			sender.sendMessage("§6更新: §a已下载插件 " + plugin.getName() + " 到update文件夹 重启后自动更新(或使用upgrade直接升级)!");
			return true;
		}
		return false;
	}
}
