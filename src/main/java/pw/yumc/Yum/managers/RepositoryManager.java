/**
 *
 */
package pw.yumc.Yum.managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.PluginHelperLogger;
import cn.citycraft.PluginHelper.jsonresult.JsonHandle;
import cn.citycraft.PluginHelper.kit.HttpKit;
import cn.citycraft.PluginHelper.kit.StrKit;
import pw.yumc.Yum.models.PluginInfo;
import pw.yumc.Yum.models.RepoCache;
import pw.yumc.Yum.models.RepoSerialization.PackageInfo;
import pw.yumc.Yum.models.RepoSerialization.Plugin;
import pw.yumc.Yum.models.RepoSerialization.Repositories;
import pw.yumc.Yum.models.RepoSerialization.Repository;
import pw.yumc.Yum.models.RepoSerialization.TagInfo;
import pw.yumc.Yum.models.RepoSerialization.URLType;

/**
 * 仓库管理类
 *
 * @author 喵♂呜
 * @since 2016年1月9日 上午10:02:57
 */
public class RepositoryManager {
    PluginHelperLogger logger = PluginHelperLogger.getLogger();
    org.bukkit.plugin.Plugin main;
    RepoCache repocache;

    public RepositoryManager(final org.bukkit.plugin.Plugin plugin) {
        this.main = plugin;
        repocache = new RepoCache();
    }

    public boolean addPackage(final CommandSender sender, final String urlstring) {
        final String json = HttpKit.get(urlstring);
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
        final String url = handerRepoUrl(urlstring);
        final Repositories repo = repocache.addRepo(url);
        if (repo == null) {
            return false;
        }
        return updateRepositories(sender, repo);
    }

    public void clean() {
        repocache.getPlugins().clear();
    }

    public boolean delRepositories(final CommandSender sender, final String urlstring) {
        return repocache.removeRepo(handerRepoUrl(urlstring));
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
        li.add("§d仓库名称  §a插件名称  §e插件描述");
        for (final Entry<String, PluginInfo> pi : repocache.getPlugins().entrySet()) {
            final Plugin plugin = pi.getValue().plugin;
            li.add(String.format("§d%s §a%s §6- §e%s", pi.getValue().repo, pi.getValue().name, plugin.description));
            if (plugin.tags != null) {
                li.add(" §b┗Tags  §c标签    §a版本    §e类型");
                final List<TagInfo> taglist = plugin.tags;
                for (int i = 0; i < taglist.size(); i++) {
                    final TagInfo tag = taglist.get(i);
                    li.add("    §b" + (i == taglist.size() - 1 ? "┗ " : "┣ ") + String.format("§c%s  §a%s  §e%s", tag.tag, tag.version, tag.type != null ? tag.type : URLType.Maven));
                }
            }
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

    public RepoCache getRepoCache() {
        return repocache;
    }

    public Repositories getRepoCache(final String urlstring) {
        return repocache.getRepos().get(handerRepoUrl(urlstring));
    }

    public Map<String, Repositories> getRepos() {
        return repocache.getRepos();
    }

    public boolean getRepositories(final CommandSender sender, final String urlstring) {
        final int urllength = urlstring.length();
        final String url = urlstring.substring(0, urlstring.endsWith("/") ? urllength - 1 : urllength);
        handerRepoUrl(url);
        final Repositories repo = repocache.addRepo(urlstring);
        if (repo == null) {
            return false;
        }
        return updateRepositories(sender, repo);
    }

    public PackageInfo jsonToPackage(final String json) {
        try {
            return JsonHandle.fromJson(json, PackageInfo.class);
        } catch (final Exception e) {
            return null;
        }
    }

    public Repositories jsonToRepositories(final String json) {
        return JsonHandle.fromJson(json, Repositories.class);
    }

    public void updatePackage(final CommandSender sender, final PackageInfo pkg) {
        for (final Plugin plugin : pkg.plugins) {
            final PluginInfo pi = new PluginInfo();
            pi.name = StrKit.getNotNull(plugin.name, plugin.artifactId);
            pi.branch = StrKit.getNotNull(plugin.branch, "master");
            pi.pom = StrKit.getNotNull(plugin.pom, pkg.pom);
            pi.url = StrKit.getNotNull(plugin.url, pkg.url);
            pi.type = plugin.type != null ? plugin.type : pkg.type;
            pi.type = pi.type != null ? pi.type : URLType.Maven;
            pi.plugin = plugin;
            pi.repo = pkg.name;
            repocache.getPlugins().put(plugin.groupId + "." + plugin.artifactId, pi);
        }
        sender.sendMessage("§6仓库: §e" + pkg.name + " §a更新成功!");
    }

    public boolean updateRepositories(final CommandSender sender) {
        repocache.getPlugins().clear();
        if (repocache.getRepos().isEmpty()) {
            repocache.addRepo("http://data.yumc.pw/yumcenter/repo.info");
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
            logger.debug("源地址为Null或源列表为空!");
            return false;
        }
        for (final Repository repo : repocenter.repos) {
            addPackage(sender, repo.url);
        }
        return true;
    }

    private String handerRepoUrl(String url) {
        final int urllength = url.length();
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
