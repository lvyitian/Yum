package cn.citycraft.Yum.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.citycraft.PluginHelper.jsonresult.JsonHandler;
import cn.citycraft.PluginHelper.utils.IOUtil;
import cn.citycraft.Yum.manager.RepoSerialization.Repositories;

public class RepoCache {
	Map<String, PluginInfo> plugins = new HashMap<String, PluginInfo>();
	Map<String, Repositories> repos = new HashMap<String, Repositories>();

	public static RepoCache fromJson(final String json) {
		return JsonHandler.fromJson(json, RepoCache.class);
	}

	public void addPlugins(final String name, final PluginInfo info) {
		plugins.put(name, info);
	}

	public Repositories addRepo(final String repo) {
		if (repos.containsKey(repo) || repo.isEmpty()) {
			return null;
		}
		final Repositories reposes = getRepo(repo);
		if (reposes == null) {
			return null;
		}
		repos.put(repo, reposes);
		return reposes;
	}

	public List<String> getAllRepoInfo() {
		final List<String> repoinfo = new ArrayList<String>();
		for (final Entry<String, Repositories> repo : repos.entrySet()) {
			repoinfo.add(String.format("§d仓库: §e%s §6- §3%s", repo.getValue().name, repo.getKey()));
		}
		return repoinfo;
	}

	public Map<String, PluginInfo> getPlugins() {
		return plugins;
	}

	public Repositories getRepo(final String repo) {
		final String json = IOUtil.getData(repo);
		if (json == null || json.isEmpty()) {
			return null;
		}
		final Repositories reposes = JsonHandler.fromJson(json, Repositories.class);
		if (reposes == null || reposes.repos.isEmpty()) {
			return null;
		}
		return reposes;
	}

	public Map<String, Repositories> getRepos() {
		return repos;
	}

	public boolean removeRepo(final String repo) {
		if (repo.isEmpty() || !repos.containsKey(repo)) {
			return false;
		}
		repos.remove(repo);
		return true;
	}

	@Override
	public String toString() {
		return JsonHandler.toJson(this);
	}
}
