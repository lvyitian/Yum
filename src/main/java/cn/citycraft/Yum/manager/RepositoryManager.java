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
	List<String> repos;
	HashMap<String, PluginInfo> plugins;

	org.bukkit.plugin.Plugin main;

	public RepositoryManager(org.bukkit.plugin.Plugin plugin) {
		this.main = plugin;
		gson = new Gson();
		plugins = new HashMap<String, PluginInfo>();
		repos = new ArrayList<String>();
	}

	public boolean addPackage(CommandSender sender, String urlstring) {
		String json = getHtml(urlstring);
		if (json == "")
			return false;
		PackageInfo pkg = jsonToPackage(json);
		if (pkg == null)
			return false;
		updatePackage(sender, pkg);
		return true;
	}

	public boolean addRepositories(CommandSender sender, String urlstring) {
		if (urlstring == null || urlstring.isEmpty())
			return false;
		repos.add(urlstring);
		return updateRepositories(sender, urlstring);
	}

	public void cacheToJson(FileConfiguration config) {
		config.set("repocache", gson.toJson(repos));
		config.set("plugincache", gson.toJson(plugins));
	}

	public void clean() {
		plugins.clear();
	}

	public List<PluginInfo> getAllPlugin() {
		List<PluginInfo> li = new ArrayList<PluginInfo>();
		for (Entry<String, PluginInfo> plugin : plugins.entrySet())
			li.add(plugin.getValue());
		return li;
	}

	public List<String> getAllPluginName() {
		List<String> li = new ArrayList<String>();
		for (Entry<String, PluginInfo> plugin : plugins.entrySet())
			li.add(plugin.getValue().plugin.artifactId);
		return li;
	}

	public List<String> getAllPluginsInfo() {
		List<String> li = new ArrayList<String>();
		for (Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			Plugin pl = plugin.getValue().plugin;
			li.add(String.format("§d%s §a%s(%s) §6- §e%s", plugin.getValue().repo, pl.artifactId, pl.version, pl.description));
		}
		return li;
	}

	public String getHtml(String urlstring) {
		String html = "";
		try {
			URL url = new URL(urlstring);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), Charsets.UTF_8));
			String line;
			while ((line = br.readLine()) != null)
				html += line;
			return html;
		} catch (IOException e) {
			return null;
		}
	}

	public PluginInfo getPlugin(String name) {
		for (Entry<String, PluginInfo> plugin : plugins.entrySet())
			if (plugin.getValue().plugin.artifactId.equalsIgnoreCase(name))
				return plugin.getValue();
		return null;
	}

	public List<PluginInfo> getPluginInfo(String name) {
		List<PluginInfo> li = new ArrayList<PluginInfo>();
		for (Entry<String, PluginInfo> plugin : plugins.entrySet())
			if (plugin.getValue().plugin.artifactId.equalsIgnoreCase(name))
				li.add(plugin.getValue());
		return li;
	}

	public PluginInfo getPluginInfo(String groupId, String artifactId) {
		return plugins.get(groupId + "." + artifactId);
	}

	public boolean jsonToCache(FileConfiguration config) {
		String repocache = config.getString("repocache");
		String plugincache = config.getString("plugincache");
		try {
			if (repocache != null && repocache != "")
				repos = gson.fromJson(repocache, new TypeToken<List<String>>() {
				}.getType());
			if (plugincache != null && plugincache != "")
				plugins = gson.fromJson(plugincache, new TypeToken<HashMap<String, PluginInfo>>() {
				}.getType());
			return true;
		} catch (JsonSyntaxException e) {
			return false;
		}
	}

	public PackageInfo jsonToPackage(String json) {
		try {
			return gson.fromJson(json, PackageInfo.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}

	public List<Repository> jsonToRepositories(String json) {
		try {
			return gson.fromJson(json, new TypeToken<List<Repository>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			return null;
		}
	}

	public void updatePackage(CommandSender sender, PackageInfo pkg) {
		for (Plugin plugin : pkg.plugins) {
			PluginInfo pi = new PluginInfo();
			pi.plugin = plugin;
			pi.url = pkg.url;
			pi.repo = pkg.name;
			plugins.put(plugin.groupId + "." + plugin.artifactId, pi);
		}
		sender.sendMessage("§6仓库: §e" + pkg.name + " §a更新成功!");
	}

	public boolean updateRepositories(CommandSender sender) {
		plugins.clear();
		for (String string : repos)
			if (updateRepositories(sender, string))
				sender.sendMessage("§6源: §e" + string + " §a更新成功!");
			else
				sender.sendMessage("§6源: §e" + string + " §c更新失败!");
		return true;
	}

	public boolean updateRepositories(CommandSender sender, String urlstring) {
		if (sender == null)
			sender = Bukkit.getConsoleSender();
		if (!urlstring.endsWith("repo.info"))
			urlstring = urlstring + "/repo.info";
		String json = getHtml(urlstring);
		if (json == "")
			return false;
		List<Repository> lrepo = jsonToRepositories(json);
		if (lrepo == null)
			return false;
		for (Repository repository : lrepo)
			addPackage(sender, repository.url);
		return true;
	}
}
