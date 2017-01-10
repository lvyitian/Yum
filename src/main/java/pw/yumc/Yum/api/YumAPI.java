package pw.yumc.Yum.api;

import java.io.File;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import pw.yumc.Yum.inject.CommandInjector;
import pw.yumc.Yum.inject.ListenerInjector;
import pw.yumc.Yum.inject.TaskInjector;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.managers.DownloadManager;
import pw.yumc.Yum.managers.PluginsManager;
import pw.yumc.Yum.managers.RepositoryManager;
import pw.yumc.Yum.models.PluginInfo;
import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.kit.PKit;

/**
 * Yum管理中心
 *
 * @author 喵♂呜
 * @since 2015年9月1日上午10:59:47
 */
public class YumAPI {
    private static DownloadManager download;
    private static Plugin main;
    private static PluginsManager plugman;
    private static RepositoryManager repo;
    private static boolean runlock = false;

    /**
     * 初始化Yum管理中心
     */
    public YumAPI() {
        YumAPI.main = P.instance;
        plugman = new PluginsManager(main);
        download = new DownloadManager(main);
        repo = new RepositoryManager(main);
        plugman.addIgnore(ConfigManager.i().getIgnoreList());
    }

    /**
     * 删除插件
     *
     * @param plugin
     *            插件实体
     */
    public static void delete(Plugin plugin) {
        plugman.deletePlugin(plugin);
    }

    /**
     * 获得下载管理器
     *
     * @return {@link DownloadManager}
     */
    public static DownloadManager getDownload() {
        return download;
    }

    /**
     * 获得插件管理器
     *
     * @return {@link PluginsManager}
     */
    public static PluginsManager getPlugman() {
        return plugman;
    }

    /**
     * 获得仓库管理器
     *
     * @return {@link RepositoryManager}
     */
    public static RepositoryManager getRepo() {
        return repo;
    }

    /**
     * 注入性能监控器
     *
     * @param plugin
     *            插件
     */
    public static void inject(Plugin plugin) {
        if (plugin.isEnabled() && !ConfigManager.i().getMonitorIgnoreList().contains(plugin.getName())) {
            CommandInjector.inject(plugin);
            ListenerInjector.inject(plugin);
            TaskInjector.inject(plugin);
        }
    }

    /**
     * 安装新插件
     *
     * @param sender
     *            命令发送者
     * @param pluginname
     *            插件名称
     * @return 是否安装成功
     */
    public static boolean install(CommandSender sender, String pluginname, String url) {
        File pluginFile = new File(Bukkit.getUpdateFolderFile().getParentFile(), pluginname + ".jar");
        return download.run(sender, url, pluginFile) && plugman.load(sender, pluginFile);
    }

    /**
     * 安装新插件
     *
     * @param pluginname
     *            插件名称
     * @return 是否安装成功
     */
    public static boolean install(String pluginname, String url) {
        return install(null, pluginname, url);
    }

    /**
     * 安装新插件
     *
     * @param sender
     *            命令发送者
     * @param pluginname
     *            插件名称
     * @return 是否安装成功
     */
    public static boolean installFromYum(CommandSender sender, String pluginname) {
        return installFromYum(sender, pluginname, null);
    }

    /**
     * 安装新插件
     *
     * @param sender
     *            命令发送者
     * @param pluginname
     *            插件名称
     * @param version
     *            插件版本
     * @return 是否安装成功
     */
    public static boolean installFromYum(CommandSender sender, String pluginname, String version) {
        PluginInfo pi = repo.getPlugin(pluginname);
        if (pi != null) { return install(sender, pi.name, pi.getUrl(sender, version)); }
        sender.sendMessage("§4错误: §c仓库中未找到插件 §b" + pluginname + " §c安装失败!");
        return false;
    }

    /**
     * 载入插件
     */
    public static void load(File pluginFile) {
        plugman.load(pluginFile);
    }

    /**
     * 载入插件
     *
     * @param pluginname
     *            插件名称
     */
    public static void load(String pluginname) {
        plugman.load(pluginname);
    }

    /**
     * 重载插件
     *
     * @param plugin
     *            插件实体
     */
    public static void reload(Plugin plugin) {
        plugman.reload(plugin);
    }

    /**
     * 取消注入
     */
    public static void uninject() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            YumAPI.uninject(plugin);
        }
    }

    /**
     * 取消注入
     *
     * @param plugin
     *            插件
     */
    public static void uninject(Plugin plugin) {
        CommandInjector.uninject(plugin);
        ListenerInjector.uninject(plugin);
        TaskInjector.uninject(plugin);
    }

    /**
     * 卸载插件
     *
     * @param plugin
     *            插件实体
     */
    public static void unload(Plugin plugin) {
        plugman.unload(plugin);
    }

    /**
     * 更新插件
     *
     * @param sender
     *            命令发送者
     * @param plugin
     *            插件实体
     * @param url
     *            新插件的下载地址
     * @return 是否更新成功
     */
    public static boolean update(CommandSender sender, Plugin plugin, URL url) {
        if (download.run(sender,
                url,
                new File(Bukkit.getUpdateFolderFile(), plugman.getPluginFile(plugin).getName()))) {
            sender.sendMessage("§6更新: §e已下载 " + plugin.getName() + " 插件到服务器更新文件夹");
            sender.sendMessage("§6更新: §e插件将在重启后自动更新(或使用§b/yum upgrade§e直接升级)!");
            return true;
        }
        return false;
    }

    /**
     * 更新插件
     *
     * @param plugin
     *            插件实体
     * @param url
     *            新插件的下载地址
     * @return 是否更新成功
     */
    public static boolean update(Plugin plugin, URL url) {
        return update(null, plugin, url);
    }

    /**
     * 更新插件
     *
     * @param sender
     *            命令发送者
     * @param plugin
     *            插件实体
     * @return 是否更新成功
     */
    public static boolean updateFromYum(CommandSender sender, Plugin plugin) {
        return updateFromYum(sender, plugin, null);
    }

    /**
     * 从Yum内部更新插件
     *
     * @param sender
     *            命令发送者
     * @param plugin
     *            插件实体
     * @param version
     *            插件版本(null则自动获取)
     * @return
     */
    public static boolean updateFromYum(CommandSender sender, Plugin plugin, String version) {
        return updateFromYum(sender, plugin, version, false);
    }

    /**
     * 从Yum内部更新插件
     *
     * @param sender
     *            命令发送者
     * @param plugin
     *            插件实体
     * @param version
     *            插件版本(null则自动获取)
     * @param oneKeyUpdate
     *            是否一键更新
     * @return
     */
    public static boolean updateFromYum(CommandSender sender, Plugin plugin, String version, boolean oneKeyUpdate) {
        PluginInfo pi = repo.getPlugin(plugin.getName());
        if (pi != null) {
            File pFile = new File(Bukkit.getUpdateFolderFile(), plugman.getPluginFile(plugin).getName());
            if (download.run(sender, pi.getUrl(sender, version), pFile)) {
                if (!oneKeyUpdate) {
                    sender.sendMessage("§6更新: §e已下载 " + plugin.getName() + " 插件到服务器更新文件夹");
                    sender.sendMessage("§6更新: §e插件将在重启后自动更新(或使用§b/yum upgrade§e直接升级)!");
                }
                //UpdatePlugin.getUpdateList().remove(plugin.getName());
                return true;
            }
        } else {
            sender.sendMessage("§6更新: §c仓库缓存中未找到插件 " + plugin.getName());
        }
        return false;
    }

    /**
     * 更新注入
     */
    public static void updateInject() {
        PKit.runTaskLater(new Runnable() {
            @Override
            public void run() {
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    YumAPI.inject(plugin);
                }
            }
        }, 60);
    }

    /**
     * 更新Yum源数据
     */
    public static void updateRepo(final CommandSender sender) {
        PKit.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                repo.updateRepositories(sender);
            }
        });
    }

    /**
     * 更新或安装插件
     *
     * @param sender
     *            命令发送者
     */
    public static void upgrade(CommandSender sender) {
        plugman.upgrade(sender);
    }

    /**
     * 更新或安装指定插件
     *
     * @param sender
     *            命令发送者
     * @param plugin
     *            插件实体
     */
    public static void upgrade(CommandSender sender, Plugin plugin) {
        plugman.upgrade(sender, plugin);
    }
}
