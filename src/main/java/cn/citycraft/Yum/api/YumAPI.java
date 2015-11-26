package cn.citycraft.Yum.api;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.CommonData.UpdatePlugin;
import cn.citycraft.Yum.manager.DownloadManager;
import cn.citycraft.Yum.manager.PluginInfo;
import cn.citycraft.Yum.manager.PluginsManager;
import cn.citycraft.Yum.manager.RepositoryManager;

/**
 * Yum管理中心
 *
 * @author 蒋天蓓
 *         2015年9月1日上午10:59:47
 */
public class YumAPI {
	private static DownloadManager download;

	private static PluginsManager plugman;

	private static RepositoryManager repo;

	protected static Plugin main;

	public YumAPI(final Plugin plugin) {
		YumAPI.main = plugin;
		plugman = new PluginsManager(main);
		download = new DownloadManager(main);
		repo = new RepositoryManager(main);
	}

	/**
	 * 删除插件
	 *
	 * @param plugin
	 *            - 插件实体
	 */
	public static void delete(final Plugin plugin) {
		plugman.deletePlugin(plugin);
	}

	/**
	 * 获得下载管理器
	 *
	 * @return {@link DownloadManager}
	 */
	public static DownloadManager getDownload() {
		return download;
	}

	/**
	 * 获得插件管理器
	 *
	 * @return {@link PluginsManager}
	 */
	public static PluginsManager getPlugman() {
		return plugman;
	}

	/**
	 * 获得仓库管理器
	 *
	 * @return {@link RepositoryManager}
	 */
	public static RepositoryManager getRepo() {
		return repo;
	}

	/**
	 * 安装新插件
	 *
	 * @param sender
	 *            - 命令发送者
	 * @param pluginname
	 *            - 插件名称
	 * @param version
	 *            - 插件版本
	 * @return 是否安装成功
	 */
	public static boolean install(final CommandSender sender, final String pluginname, final String url) {
		if (download.run(sender, url, new File(Bukkit.getUpdateFolderFile().getParentFile(), pluginname + ".jar"))) {
			return plugman.load(sender, pluginname);
		}
		return false;
	}

	/**
	 * 安装新插件
	 *
	 * @param pluginname
	 *            - 插件名称
	 * @param version
	 *            - 插件版本
	 * @return 是否安装成功
	 */
	public static boolean install(final String pluginname, final String url) {
		return install(null, pluginname, url);
	}

	/**
	 * 安装新插件
	 *
	 * @param sender
	 *            - 命令发送者
	 * @param pluginname
	 *            - 插件名称
	 * @return 是否安装成功
	 */
	public static boolean installfromyum(final CommandSender sender, final String pluginname) {
		return installfromyum(sender, pluginname, null);
	}

	/**
	 * 安装新插件
	 *
	 * @param sender
	 *            - 命令发送者
	 * @param pluginname
	 *            - 插件名称
	 * @param version
	 *            - 插件版本
	 * @return 是否安装成功
	 */
	public static boolean installfromyum(final CommandSender sender, final String pluginname, final String version) {
		final PluginInfo pi = repo.getPlugin(pluginname);
		if (pi != null) {
			return install(sender, pi.name, pi.getMavenUrl(sender, version));
		}
		return false;
	}

	/**
	 * 载入插件
	 *
	 * @param pluginname
	 *            - 插件名称
	 */
	public static void load(final String pluginname) {
		plugman.load(pluginname);
	}

	/**
	 * 卸载插件
	 *
	 * @param plugin
	 *            - 插件实体
	 */
	public static void reload(final Plugin plugin) {
		plugman.unload(plugin);
	}

	/**
	 * 重载插件
	 *
	 * @param plugin
	 *            - 插件实体
	 */
	public static void unload(final Plugin plugin) {
		plugman.reload(plugin);
	}

	/**
	 * 更新插件
	 *
	 * @param sender
	 *            - 命令发送者
	 * @param plugin
	 *            - 插件实体
	 * @param url
	 *            - 新插件的下载地址
	 * @return 是否更新成功
	 */
	public static boolean update(final CommandSender sender, final Plugin plugin, final URL url) {
		if (download.run(sender, url, new File(Bukkit.getUpdateFolderFile(), plugman.getPluginFile(plugin).getName()))) {
			sender.sendMessage("§6更新: §a已下载插件 " + plugin.getName() + " 到update文件夹 重启后自动更新(或使用upgrade直接升级)!");
			return true;
		}
		return false;
	}

	/**
	 * 更新插件
	 *
	 * @param plugin
	 *            - 插件实体
	 * @param url
	 *            - 新插件的下载地址
	 * @return 是否更新成功
	 */
	public static boolean update(final Plugin plugin, final URL url) {
		return update(null, plugin, url);
	}

	/**
	 * 更新支持Yum的插件
	 *
	 * @param sender
	 *            - 命令发送者
	 */
	public static void updateall(final CommandSender sender) {
		final Map<Plugin, String> updatelist = UpdatePlugin.getList();
		if (updatelist.size() > 0) {
			for (final Entry<Plugin, String> updateplugin : UpdatePlugin.getList().entrySet()) {
				updatefromyum(sender, updateplugin.getKey(), updateplugin.getValue());
			}
			UpdatePlugin.getList().clear();
			sender.sendMessage("§6更新: §c已下载所有需要插件的插件到 到update文件夹 重启后自动更新(或使用/yum upgrade直接升级)!");
		} else {
			sender.sendMessage("§6更新: §e未找到需要更新且可以用Yum处理的插件!");
		}
	}

	/**
	 * 更新插件
	 *
	 * @param sender
	 *            - 命令发送者
	 * @param plugin
	 *            - 插件实体
	 * @return 是否更新成功
	 */
	public static boolean updatefromyum(final CommandSender sender, final Plugin plugin) {
		return updatefromyum(sender, plugin, null);
	}

	/**
	 * 从Yum内部更新插件
	 *
	 * @param sender
	 *            - 命令发送者
	 * @param plugin
	 *            - 插件实体
	 * @param version
	 *            - 插件版本(null则自动获取)
	 * @return
	 */
	public static boolean updatefromyum(final CommandSender sender, final Plugin plugin, final String version) {
		final PluginInfo pi = repo.getPlugin(plugin.getName());
		if (pi != null) {
			if (download.run(sender, pi.getMavenUrl(sender, version), new File(Bukkit.getUpdateFolderFile(), plugman.getPluginFile(plugin).getName()))) {
				sender.sendMessage("§6更新: §a已下载插件 " + plugin.getName() + " 到update文件夹 重启后自动更新(或使用upgrade直接升级)!");
				return true;
			}
		} else {
			sender.sendMessage("§6更新: §c仓库缓存中未找到插件 " + plugin.getName());
		}
		return false;
	}

	/**
	 * 更新Yum源数据
	 */
	public static void updaterepo() {
		main.getServer().getScheduler().runTaskAsynchronously(main, new Runnable() {
			@Override
			public void run() {
				repo.updateRepositories(Bukkit.getConsoleSender());
			}
		});
	}

	/**
	 * @param sender
	 *            - 命令发送者
	 * @param plugin
	 *            - 插件实体
	 */
	public static void upgrade(final CommandSender sender, final Plugin plugin) {
		plugman.upgrade(sender, plugin);
	}
}