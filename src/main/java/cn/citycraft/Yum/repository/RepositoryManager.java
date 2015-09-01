/**
 *
 */
package cn.citycraft.Yum.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cn.citycraft.Yum.Yum;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * 仓库管理类
 *
 * @author 蒋天蓓
 */
public class RepositoryManager {
	Gson gson = new Gson();
	HashMap<String, PluginInfo> plugins = new HashMap<String, PluginInfo>();

	Yum main;

	public RepositoryManager(Yum yum) {
		this.main = yum;
	}

	public void clean() {
		plugins.clear();
	}

	public void update(Package pkg) {
		for (Plugin plugin : pkg.plugins) {
			PluginInfo pi = new PluginInfo();
			pi.plugin = plugin;
			pi.url = pkg.url;
			plugins.put(plugin.groupId + "." + plugin.artifactId, pi);
		}
	}

	public String cacheToJson() {
		return gson.toJson(plugins);
	}

	public boolean jsonToCache(String json) {
		if (json == "") {
			return false;
		}
		try {
			plugins = gson.fromJson(json, new TypeToken<HashMap<String, PluginInfo>>() {
			}.getType());
			return true;
		} catch (JsonSyntaxException e) {
			return false;
		}
	}

	public boolean addRepositories(String urlstring) {
		String json = getHtml(urlstring);
		if (json == "") {
			return false;
		}
		List<Repository> lrepo = jsonToRepositories(json);
		if (lrepo == null) {
			return false;
		}
		for (Repository repository : lrepo) {
			addPackage(repository.url);
		}
		return true;
	}

	public boolean addPackage(String urlstring) {
		String json = getHtml(urlstring);
		if (json == "") {
			return false;
		}
		Package pkg = jsonToPackage(json);
		if (pkg == null) {
			return false;
		}
		update(pkg);
		return true;
	}

	public String getHtml(String urlstring) {
		String html = "";
		try {
			URL url = new URL(urlstring);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), Charsets.UTF_8));
			String line;
			while ((line = br.readLine()) != null) {
				html += line;
			}
			return html;
		} catch (IOException e) {
			return null;
		}
	}

	public PluginInfo getPluginInfo(String groupId, String artifactId) {
		return plugins.get(groupId + "." + artifactId);
	}

	public PluginInfo getPlugin(String name) {
		for (Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			if (plugin.getValue().plugin.artifactId.equalsIgnoreCase(name))
				return plugin.getValue();
		}
		return null;
	}

	public List<PluginInfo> getAllPlugin() {
		List<PluginInfo> li = new ArrayList<PluginInfo>();
		for (Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			li.add(plugin.getValue());
		}
		return li;
	}

	public List<String> getAllPluginString() {
		List<String> li = new ArrayList<String>();
		for (Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			Plugin pl = plugin.getValue().plugin;
			li.add(String.format("%s %s(%s)", pl.groupId, pl.artifactId, pl.version));
		}
		return li;
	}

	public List<PluginInfo> getPluginInfo(String name) {
		List<PluginInfo> li = new ArrayList<PluginInfo>();
		for (Entry<String, PluginInfo> plugin : plugins.entrySet()) {
			if (plugin.getValue().plugin.artifactId.equalsIgnoreCase(name))
				li.add(plugin.getValue());
		}
		return li;
	}

	public List<Repository> jsonToRepositories(String json) {
		try {
			return gson.fromJson(json, new TypeToken<List<Repository>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			return null;
		}
	}

	public Package jsonToPackage(String json) {
		try {
			return gson.fromJson(json, Package.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
}
