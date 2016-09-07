package pw.yumc.Yum.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import cn.citycraft.PluginHelper.kit.PluginKit;
import cn.citycraft.PluginHelper.utils.IOUtil;
import pw.yumc.Yum.models.RepoSerialization.Repositories;

public class RepoCache implements Serializable {
    Map<String, PluginInfo> plugins = new HashMap<String, PluginInfo>();
    Map<String, Repositories> repos = new HashMap<String, Repositories>();

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
            PluginKit.sc("§6[§bYum§6] §c源地址获取数据为空 §b" + repo);
            return null;
        }
        final Repositories reposes = new Repositories((JSONObject) JSONValue.parse(json));
        if (reposes.repos.isEmpty()) {
            PluginKit.sc("§6[§bYum§6] §c源地址解析Json为空 §b" + repo);
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
}
