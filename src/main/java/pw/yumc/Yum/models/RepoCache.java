package pw.yumc.Yum.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import pw.yumc.Yum.models.RepoSerialization.Repositories;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.kit.HttpKit;

public class RepoCache implements Serializable {
    Map<String, PluginInfo> plugins = new HashMap<>();
    Map<String, Repositories> repos = new HashMap<>();

    public void addPlugins(String name, PluginInfo info) {
        plugins.put(name, info);
    }

    public Repositories addRepo(String repo) {
        if (repos.containsKey(repo) || repo.isEmpty()) { return null; }
        Repositories reposes = getRepo(repo);
        if (reposes == null) { return null; }
        repos.put(repo, reposes);
        return reposes;
    }

    public List<String> getAllRepoInfo() {
        List<String> repoinfo = new ArrayList<>();
        for (Entry<String, Repositories> repo : repos.entrySet()) {
            repoinfo.add(String.format("§d仓库: §e%s §6- §3%s", repo.getValue().name, repo.getKey()));
        }
        return repoinfo;
    }

    public Map<String, PluginInfo> getPlugins() {
        return plugins;
    }

    public Repositories getRepo(String repo) {
        String json = HttpKit.get(repo);
        if (json == null || json.isEmpty()) {
            Log.console("§c源地址获取数据为空 §b" + repo);
            return null;
        }
        Repositories reposes = new Repositories((JSONObject) JSONValue.parse(json));
        if (reposes.repos.isEmpty()) {
            Log.console("§c源地址解析Json为空 §b" + repo);
            return null;
        }
        return reposes;
    }

    public Map<String, Repositories> getRepos() {
        return repos;
    }

    public boolean removeRepo(String repo) {
        if (repo.isEmpty() || !repos.containsKey(repo)) { return false; }
        repos.remove(repo);
        return true;
    }
}
