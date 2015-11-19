/**
 *
 */
package cn.citycraft.Yum.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.gson.JsonSyntaxException;

import cn.citycraft.PluginHelper.jsonresult.JsonResult;
import cn.citycraft.PluginHelper.utils.IOUtil;
import cn.citycraft.PluginHelper.utils.StringUtil;
import cn.citycraft.Yum.manager.RepoSerialization.PackageInfo;
import cn.citycraft.Yum.manager.RepoSerialization.Plugin;
import cn.citycraft.Yum.manager.RepoSerialization.Repositories;
import cn.citycraft.Yum.manager.RepoSerialization.Repository;

/**
 * 仓库管理类
 *
 * @author 蒋天蓓
 */
public class RepositoryManager {
	JsonResult jr = JsonResult.newJsonResult();
	org.bukkit.plugin.Plugin main;
	RepoCache repocache;

	public RepositoryManager(final org.bukkit.plugin.Plugin plugin) {
		this.main = plugin;
		repocache = new RepoCache();
	}

	public boolean addPackage(final CommandSender sender, final String urlstring) {
		final String json = IOUtil.getData(urlstring);
		if (json == null || json.isEmpty()) {
			return false;
		}
		final PackageInfo pkg = jsonToPackage(json);
		if (pkg == null) {
			return false;
		}
		updatePackage(sender, pkg);
		return true;
	}

	public boolean addRepositories(final CommandSender sender, final String urlstring) {
		final int urllength = urlstring.length();
		String url = urlstring.substring(0, urlstring.endsWith("/") ? urllength - 1 : urllength);
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}
		if (!url.endsWith("repo.info")) {
			url = url + "/repo.info";
		}
		final Repositories repo = repocache.addRepo(urlstring);
		if (repo == null) {
			return false;
		}
		return updateRepositories(sender, repo);
	}

	public void cacheToJson(final FileConfiguration config) {
		config.set("reposcache", repocache.toString());
	}

	public void clean() {
		repocache.getPlugins().clear();
	}

	public boolean delRepositories(final CommandSender sender, final String urlstring) {
		return repocache.removeRepo(urlstring);
	}

	public List<PluginInfo> getAllPlugin() {
		final List<PluginInfo> li = new ArrayList<PluginInfo>();
		for (final Entry<String, PluginInfo> plugin : repocache.getPlugins().entrySet()) {
			li.add(plugin.getValue());
		}
		return li;
	}

	public List<String> getAllPluginName() {
		final List<String> li = new ArrayList<String>();
		for (final Entry<String, PluginInfo> plugin : repocache.getPlugins().entrySet()) {
			li.add(plugin.getValue().name);
		}
		return li;
	}

	public List<String> getAllPluginsInfo() {
		final List<String> li = new ArrayList<String>();
		for (final Entry<String, PluginInfo> pi : repocache.getPlugins().entrySet()) {
			final Plugin plugin = pi.getValue().plugin;
			li.add(String.format("§d%s §a%s(%s) §6- §e%s", pi.getValue().repo, pi.getValue().name, plugin.version, plugin.description));
		}
		return li;
	}

	public PluginInfo getPlugin(final String name) {
		for (final Entry<String, PluginInfo> plugin : repocache.getPlugins().entrySet()) {
			if (plugin.getValue().name.equalsIgnoreCase(name)) {
				return plugin.getValue();
			}
		}
		return null;
	}

	public List<PluginInfo> getPluginInfo(final String name) {
		final List<PluginInfo> li = new ArrayList<PluginInfo>();
		for (final Entry<String, PluginInfo> plugin : repocache.getPlugins().entrySet()) {
			if (plugin.getValue().name.equalsIgnoreCase(name)) {
				li.add(plugin.getValue());
			}
		}
		return li;
	}

	public PluginInfo getPluginInfo(final String groupId, final String artifactId) {
		return repocache.getPlugins().get(groupId + "." + artifactId);
	}

	public Map<String, PluginInfo> getPlugins() {
		return repocache.getPlugins();
	}

	public Map<String, Repositories> getRepos() {
		return repocache.getRepos();
	}

	public boolean jsonToCache(final FileConfiguration config) {
		try {
			final String reposcache = config.getString("reposcache");
			if (reposcache != null && !reposcache.isEmpty()) {
				repocache = RepoCache.fromJson(reposcache);
				return true;
			}
		} catch (final Exception e) {
		}
		return false;
	}

	public PackageInfo jsonToPackage(final String json) {
		try {
			return jr.fromJson(json, PackageInfo.class);
		} catch (final JsonSyntaxException e) {
			return null;
		}
	}

	public Repositories jsonToRepositories(final String json) {
		return jr.fromJson(json, Repositories.class);
	}

	public void updatePackage(final CommandSender sender, final PackageInfo pkg) {
		for (final Plugin plugin : pkg.plugins) {
			final PluginInfo pi = new PluginInfo();
			pi.name = StringUtil.getNotNull(plugin.name, plugin.artifactId);
			pi.branch = StringUtil.getNotNull(plugin.branch, "master");
			pi.pom = StringUtil.getNotNull(plugin.pom, pkg.pom);
			pi.plugin = plugin;
			pi.url = pkg.url;
			pi.repo = pkg.name;
			repocache.getPlugins().put(plugin.groupId + "." + plugin.artifactId, pi);
		}
		sender.sendMessage("§6仓库: §e" + pkg.name + " §a更新成功!");
	}

	public boolean updateRepositories(final CommandSender sender) {
		repocache.getPlugins().clear();
		if (repocache.getRepos().isEmpty()) {
			repocache.addRepo("http://citycraft.cn/yumcenter/repo.info");
		}
		final Iterator<Entry<String, Repositories>> keys = repocache.getRepos().entrySet().iterator();
		while (keys.hasNext()) {
			final Entry<String, Repositories> string = keys.next();
			final Repositories repo = repocache.getRepo(string.getKey());
			if (updateRepositories(sender, repo)) {
				sender.sendMessage("§6源: §e" + repo.name + " §a更新成功!");
			} else {
				sender.sendMessage("§6源: §e" + string.getKey() + " §c未找到任何仓库信息 已删除!");
				keys.remove();
			}
		}
		return true;
	}

	public boolean updateRepositories(CommandSender sender, final Repositories repocenter) {
		if (sender == null) {
			sender = Bukkit.getConsoleSender();
		}
		if (repocenter == null || repocenter.repos.isEmpty()) {
			return false;
		}
		for (final Repository repo : repocenter.repos) {
			addPackage(sender, repo.url);
		}
		return true;
	}
}
