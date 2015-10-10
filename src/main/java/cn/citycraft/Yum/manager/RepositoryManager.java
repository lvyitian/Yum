/**
 *
 */
package cn.citycraft.Yum.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import cn.citycraft.Yum.manager.Repositories.PackageInfo;
import cn.citycraft.Yum.manager.Repositories.Plugin;
import cn.citycraft.Yum.manager.Repositories.Repository;;

/**
 * 仓库管理类
 *
 * @author 蒋天蓓
 */
public class RepositoryManager {
	Gson gson;
	org.bukkit.plugin.Plugin main;
	HashMap<String, PluginInfo> plugins;

	List<String> repos;

	public RepositoryManager(final org.bukkit.plugin.Plugin plugin) {
		this.main = plugin;
		gson = new Gson();
		plugins = new HashMap<String, PluginInfo>();
		repos = new ArrayList<String>();
	}

	public boolean addPackage(final CommandSender sender, final String urlstring) {
		final String json = getHtml(urlstring);
		if (json.isEmpty()) {
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
		if (urlstring.isEmpty()) {
			return false;
		}
		repos.add(urlstring);
		return updateRepositories(sender, urlstring);
	}

	public void cacheToJson(final FileConfiguration config) {
		config.set("repocache", gson.toJson(repos));
		config.set("plugincache", gson.toJson(plugins));
	}

	public void clean() {
		plugins.clear();
	}

	public List<PluginInfo> getAllPlugin() {
		final List<PluginInfo> li = new ArrayList<PluginInfo>();
		for (final Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			li.add(plugin.getValue());
		}
		return li;
	}

	public List<String> getAllPluginName() {
		final List<String> li = new ArrayList<String>();
		for (final Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			li.add(plugin.getValue().plugin.artifactId);
		}
		return li;
	}

	public List<String> getAllPluginsInfo() {
		final List<String> li = new ArrayList<String>();
		for (final Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			final Plugin pl = plugin.getValue().plugin;
			li.add(String.format("§d%s §a%s(%s) §6- §e%s", plugin.getValue().repo, pl.artifactId, pl.version, pl.description));
		}
		return li;
	}

	public String getHtml(final String urlstring) {
		String html = "";
		try {
			final URL url = new URL(urlstring);
			final BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), Charsets.UTF_8));
			String line;
			while ((line = br.readLine()) != null) {
				html += line;
			}
			return html;
		} catch (final IOException e) {
			return null;
		}
	}

	public PluginInfo getPlugin(final String name) {
		for (final Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			if (plugin.getValue().plugin.artifactId.equalsIgnoreCase(name)) {
				return plugin.getValue();
			}
		}
		return null;
	}

	public List<PluginInfo> getPluginInfo(final String name) {
		final List<PluginInfo> li = new ArrayList<PluginInfo>();
		for (final Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			if (plugin.getValue().plugin.artifactId.equalsIgnoreCase(name)) {
				li.add(plugin.getValue());
			}
		}
		return li;
	}

	public PluginInfo getPluginInfo(final String groupId, final String artifactId) {
		return plugins.get(groupId + "." + artifactId);
	}

	public boolean jsonToCache(final FileConfiguration config) {
		final String repocache = config.getString("repocache");
		final String plugincache = config.getString("plugincache");
		try {
			if (repocache != null && !repocache.isEmpty()) {
				repos = gson.fromJson(repocache, new TypeToken<List<String>>() {
				}.getType());
			}
			if (plugincache != null && !plugincache.isEmpty()) {
				plugins = gson.fromJson(plugincache, new TypeToken<HashMap<String, PluginInfo>>() {
				}.getType());
			}
			return true;
		} catch (final JsonSyntaxException e) {
			return false;
		}
	}

	public PackageInfo jsonToPackage(final String json) {
		try {
			return gson.fromJson(json, PackageInfo.class);
		} catch (final JsonSyntaxException e) {
			return null;
		}
	}

	public List<Repository> jsonToRepositories(final String json) {
		try {
			return gson.fromJson(json, new TypeToken<List<Repository>>() {
			}.getType());
		} catch (final JsonSyntaxException e) {
			return new ArrayList<Repository>();
		}
	}

	public void updatePackage(final CommandSender sender, final PackageInfo pkg) {
		for (final Plugin plugin : pkg.plugins) {
			final PluginInfo pi = new PluginInfo();
			pi.plugin = plugin;
			pi.url = pkg.url;
			pi.repo = pkg.name;
			plugins.put(plugin.groupId + "." + plugin.artifactId, pi);
		}
		sender.sendMessage("§6仓库: §e" + pkg.name + " §a更新成功!");
	}

	public boolean updateRepositories(final CommandSender sender) {
		plugins.clear();
		for (final String string : repos) {
			if (updateRepositories(sender, string)) {
				sender.sendMessage("§6源: §e" + string + " §a更新成功!");
			} else {
				sender.sendMessage("§6源: §e" + string + " §c更新失败!");
			}
		}
		return true;
	}

	public boolean updateRepositories(CommandSender sender, String urlstring) {
		if (sender == null) {
			sender = Bukkit.getConsoleSender();
		}
		if (!urlstring.endsWith("repo.info")) {
			urlstring = urlstring + "/repo.info";
		}
		final String json = getHtml(urlstring);
		if (json == null || json.isEmpty()) {
			return false;
		}
		final List<Repository> lrepo = jsonToRepositories(json);
		if (lrepo == null || lrepo.isEmpty()) {
			return false;
		}
		for (final Repository repository : lrepo) {
			addPackage(sender, repository.url);
		}
		return true;
	}
}
