package pw.yumc.Yum.managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import pw.yumc.Yum.models.PluginInfo;
import pw.yumc.Yum.models.RepoCache;
import pw.yumc.Yum.models.RepoSerialization.PackageInfo;
import pw.yumc.Yum.models.RepoSerialization.Plugin;
import pw.yumc.Yum.models.RepoSerialization.Repositories;
import pw.yumc.Yum.models.RepoSerialization.Repository;
import pw.yumc.Yum.models.RepoSerialization.TagInfo;
import pw.yumc.Yum.models.RepoSerialization.URLType;
import pw.yumc.YumCore.kit.HttpKit;

/**
 * 仓库管理类
 *
 * @author 喵♂呜
 * @since 2016年1月9日 上午10:02:57
 */
public class RepositoryManager {
    org.bukkit.plugin.Plugin main;
    RepoCache repocache;

    public RepositoryManager(org.bukkit.plugin.Plugin plugin) {
        this.main = plugin;
        repocache = new RepoCache();
    }

    public boolean addPackage(CommandSender sender, String urlstring) {
        String json = HttpKit.get(urlstring);
        if (json.isEmpty()) { return false; }
        PackageInfo pkg = jsonToPackage(json);
        if (pkg == null) { return false; }
        updatePackage(sender, pkg);
        return true;
    }

    public boolean addRepositories(CommandSender sender, String urlstring) {
        String url = handerRepoUrl(urlstring);
        Repositories repo = repocache.addRepo(url);
        return repo != null && updateRepositories(sender, repo);
    }

    public void clean() {
        repocache.getPlugins().clear();
    }

    public boolean delRepositories(CommandSender sender, String urlstring) {
        return repocache.removeRepo(handerRepoUrl(urlstring));
    }

    public List<PluginInfo> getAllPlugin() {
        List<PluginInfo> li = new ArrayList<>();
        for (Entry<String, PluginInfo> plugin : repocache.getPlugins().entrySet()) {
            li.add(plugin.getValue());
        }
        return li;
    }

    public List<String> getAllPluginName() {
        List<String> li = new ArrayList<>();
        for (Entry<String, PluginInfo> plugin : repocache.getPlugins().entrySet()) {
            li.add(plugin.getValue().name);
        }
        return li;
    }

    public List<String> getAllPluginsInfo() {
        List<String> li = new ArrayList<>();
        li.add("§d仓库名称  §a插件名称  §e插件描述");
        for (Entry<String, PluginInfo> pi : repocache.getPlugins().entrySet()) {
            Plugin plugin = pi.getValue().plugin;
            li.add(String.format("§d%s §a%s §6- §e%s", pi.getValue().repo, pi.getValue().name, plugin.description));
            if (plugin.tags != null) {
                li.add(" §b┗Tags  §c标签    §a版本    §e类型");
                List<TagInfo> taglist = plugin.tags;
                for (int i = 0; i < taglist.size(); i++) {
                    TagInfo tag = taglist.get(i);
                    li.add("    §b" + (i == taglist.size() - 1 ? "┗ " : "┣ ") + String.format("§c%s  §a%s  §e%s", tag.tag, tag.version, tag.type != null ? tag.type : URLType.Maven));
                }
            }
        }
        return li;
    }

    public PluginInfo getPlugin(String name) {
        for (Entry<String, PluginInfo> plugin : repocache.getPlugins().entrySet()) {
            if (plugin.getValue().name.equalsIgnoreCase(name)) { return plugin.getValue(); }
        }
        return null;
    }

    public List<PluginInfo> getPluginInfo(String name) {
        List<PluginInfo> li = new ArrayList<>();
        for (Entry<String, PluginInfo> plugin : repocache.getPlugins().entrySet()) {
            if (plugin.getValue().name.equalsIgnoreCase(name)) {
                li.add(plugin.getValue());
            }
        }
        return li;
    }

    public PluginInfo getPluginInfo(String groupId, String artifactId) {
        return repocache.getPlugins().get(groupId + "." + artifactId);
    }

    public Map<String, PluginInfo> getPlugins() {
        return repocache.getPlugins();
    }

    public RepoCache getRepoCache() {
        return repocache;
    }

    public Repositories getRepoCache(String urlstring) {
        return repocache.getRepos().get(handerRepoUrl(urlstring));
    }

    public Map<String, Repositories> getRepos() {
        return repocache.getRepos();
    }

    public boolean getRepositories(CommandSender sender, String urlstring) {
        int urllength = urlstring.length();
        String url = urlstring.substring(0, urlstring.endsWith("/") ? urllength - 1 : urllength);
        handerRepoUrl(url);
        Repositories repo = repocache.addRepo(urlstring);
        return repo != null && updateRepositories(sender, repo);
    }

    public PackageInfo jsonToPackage(String json) {
        return new PackageInfo((JSONObject) JSONValue.parse(json));
    }

    public void updatePackage(CommandSender sender, PackageInfo pkg) {
        for (Plugin plugin : pkg.plugins) {
            PluginInfo pi = new PluginInfo();
            pi.name = getNotNull(plugin.name, plugin.artifactId);
            pi.branch = getNotNull(plugin.branch, "master");
            pi.pom = getNotNull(plugin.pom, pkg.pom);
            pi.url = getNotNull(plugin.url, pkg.url);
            pi.type = plugin.type != null ? plugin.type : pkg.type;
            pi.type = pi.type != null ? pi.type : URLType.Maven;
            pi.plugin = plugin;
            pi.repo = pkg.name;
            repocache.getPlugins().put(plugin.groupId + "." + plugin.artifactId, pi);
        }
        sender.sendMessage("§6仓库: §e" + pkg.name + " §a更新成功!");
    }

    public static String getNotNull(final String vault, final String def) {
        return (vault == null || vault.isEmpty() || vault.equalsIgnoreCase("null")) ? def : vault;
    }

    public boolean updateRepositories(CommandSender sender) {
        repocache.getPlugins().clear();
        if (repocache.getRepos().isEmpty()) {
            repocache.addRepo("http://data.yumc.pw/yumcenter/repo.info");
        }
        Iterator<Entry<String, Repositories>> keys = repocache.getRepos().entrySet().iterator();
        while (keys.hasNext()) {
            Entry<String, Repositories> string = keys.next();
            Repositories repo = repocache.getRepo(string.getKey());
            if (updateRepositories(sender, repo)) {
                sender.sendMessage("§6源: §e" + repo.name + " §a更新成功!");
            } else {
                sender.sendMessage("§6源: §e" + string.getKey() + " §c未找到任何仓库信息 已删除!");
                keys.remove();
            }
        }
        return true;
    }

    public boolean updateRepositories(CommandSender sender, Repositories repocenter) {
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }
        if (repocenter == null || repocenter.repos.isEmpty()) {
            sender.sendMessage(String.format("§6[§bYum§6] 源 %s 数据为空或列表为空!", repocenter == null ? "null" : repocenter.name));
            return false;
        }
        for (Repository repo : repocenter.repos) {
            addPackage(sender, repo.url);
        }
        return true;
    }

    private String handerRepoUrl(String url) {
        int urllength = url.length();
        url = url.substring(0, url.endsWith("/") ? urllength - 1 : urllength);
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        if (!url.endsWith("repo.info")) {
            url = url + "/repo.info";
        }
        return url;
    }

}
