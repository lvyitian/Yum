package cn.citycraft.Yum.manager;

import java.util.HashMap;
import java.util.Map;

import cn.citycraft.PluginHelper.jsonresult.JsonResult;
import cn.citycraft.PluginHelper.utils.IOUtil;
import cn.citycraft.Yum.manager.RepoSerialization.Repositories;

public class RepoCache {
	protected static JsonResult jr = JsonResult.newJsonResult();
	Map<String, PluginInfo> plugins = new HashMap<String, PluginInfo>();
	Map<String, Repositories> repos = new HashMap<String, Repositories>();

	public static RepoCache fromJson(final String json) {
		return jr.fromJson(json, RepoCache.class);
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

	public Map<String, PluginInfo> getPlugins() {
		return plugins;
	}

	public Repositories getRepo(final String repo) {
		final String json = IOUtil.getData(repo);
		if (json == null || json.isEmpty()) {
			return null;
		}
		final Repositories reposes = jr.fromJson(json, Repositories.class);
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
		return jr.toJson(this);
	}
}
