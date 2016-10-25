package pw.yumc.Yum.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import pw.yumc.Yum.Yum;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.models.BukkitDev;
import pw.yumc.Yum.models.BukkitDev.Files;
import pw.yumc.Yum.models.BukkitDev.Projects;
import pw.yumc.Yum.models.RepoSerialization.Repositories;
import pw.yumc.YumCore.callback.CallBack.One;
import pw.yumc.YumCore.commands.CommandManager;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;
import pw.yumc.YumCore.commands.annotation.Sort;
import pw.yumc.YumCore.commands.interfaces.CommandExecutor;
import pw.yumc.YumCore.kit.FileKit;
import pw.yumc.YumCore.kit.HttpKit;
import pw.yumc.YumCore.kit.PKit;
import pw.yumc.YumCore.kit.ZipKit;
import pw.yumc.YumCore.tellraw.Tellraw;

/**
 * Yum命令基类
 *
 * @since 2016年1月9日 上午10:02:24
 * @author 喵♂呜
 */
public class YumCommand implements Listener, CommandExecutor {
    private String prefix = "§6[§bYum §a插件管理§6] ";

    private String searchlimit = prefix + "§c为保证搜索速度和准确性 关键词必须大于 3 个字符!";
    private String searching = prefix + "§a正在从 §eBukkitDev §a获取 §b%s §a的相关数据...";
    private String not_found_from_bukkit = prefix + "§c未在 §eBukkitDev §c搜索到 §b%s §c的相关插件!";
    private String result = prefix + "§6关键词 §b%s §6的搜索结果如下:";
    private String bukkitlistprefix = " §6插件ID  §3插件名称                  §d发布类型   §a操作";
    private String bukkitlist = "§6- §e%-6s §b%-25s §d%-10s";

    private String fsearching = prefix + "§a正在从 §eBukkitDev §a获取ID §b%s §a的文件列表...";
    private String not_found_id_from_bukkit = prefix + "§c未在 §eBukkitDev §c搜索到ID为 §b%s §c的相关插件!";
    private String filelistprefix = "  §6插件名称             §3游戏版本      §d发布类型   §a操作";
    private String filelist = "§6- §b%-20s §3%-15s §d%-10s";

    private String del = "§c删除: §a插件 §b%s §a版本 §d%s §a已从服务器卸载并删除!";
    private String delFailed = "§c删除: §a插件 §b%s §c卸载或删除时发生错误 删除失败!";

    private String look = "§6查看";
    private String install = "§a安装";
    private String install_tip = "§a点击安装";
    private String update = "§a更新";
    private String unload = "§d卸载";
    private String reload = "§6重载";
    private String delete = "§c删除";

    private String unzip_error = prefix + "ZIP文件解压错误!";

    Yum main;

    public YumCommand(Yum yum) {
        main = yum;
        Bukkit.getPluginManager().registerEvents(this, yum);
        new CommandManager("yum", this, PluginTabComplete.instence);
    }

    @Cmd(aliases = "br", minimumArguments = 2)
    @Help(value = "从BukkitDev查看安装插件", possibleArguments = "<操作符> <项目ID|项目名称> [地址]")
    public void bukkitrepo(final CommandSender sender, final String opt, final String id, final String url) {
        PKit.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                switch (opt) {
                case "look": {
                    sender.sendMessage(String.format(fsearching, id));
                    List<Files> lf = Files.parseList(HttpKit.get(String.format(BukkitDev.PLUGIN, id)));
                    if (lf.isEmpty()) {
                        sender.sendMessage(String.format(not_found_id_from_bukkit, id));
                        return;
                    }
                    sender.sendMessage(filelistprefix);
                    for (int i = 0; i < lf.size() || i < 8; i++) {
                        Files f = lf.get(i);
                        Tellraw tr = Tellraw.create();
                        tr.text(String.format(filelist, f.name, f.gameVersion, f.releaseType));
                        tr.then(" ");
                        tr.then(install).command(String.format("/yum br ai %s %s", f.name, f.downloadUrl));
                        tr.tip(install_tip);
                        tr.send(sender);
                    }
                    break;
                }
                case "ai": {
                    if (url == null) { return; }
                    File file = new File(Bukkit.getUpdateFolderFile(), YumAPI.getDownload().getFileName(url));
                    YumAPI.getDownload().run(sender, url, file, new One<File>() {
                        @Override
                        public void run(File file) {
                            if (file.getName().endsWith(".zip")) {
                                try {
                                    ZipKit.unzip(file, Bukkit.getUpdateFolderFile(), ".jar");
                                    file.delete();
                                } catch (IOException e) {
                                    sender.sendMessage(unzip_error);
                                }
                            }
                            YumAPI.upgrade(sender);
                        }
                    });
                    break;
                }
                case "i":
                case "install": {
                    sender.sendMessage(String.format(fsearching, id));
                    List<Files> lf = Files.parseList(HttpKit.get(String.format(BukkitDev.PLUGIN, id)));
                    if (lf.isEmpty()) {
                        sender.sendMessage(String.format(not_found_id_from_bukkit, id));
                        return;
                    }
                    Files f = lf.get(0);
                    String url = f.downloadUrl;
                    File file = new File(Bukkit.getUpdateFolderFile(), YumAPI.getDownload().getFileName(url));
                    YumAPI.getDownload().run(sender, url, file, new One<File>() {
                        @Override
                        public void run(File file) {
                            if (file.getName().endsWith(".zip")) {
                                try {
                                    ZipKit.unzip(file, Bukkit.getUpdateFolderFile(), ".jar");
                                } catch (IOException e) {
                                    sender.sendMessage(unzip_error);
                                }
                            }
                            YumAPI.upgrade(sender);
                        }
                    });
                    break;
                }
                default:
                    break;

                }
            }
        });
    }

    @Cmd(aliases = "del", minimumArguments = 1)
    @Help(value = "删除插件", possibleArguments = "<插件名称>")
    @Sort(6)
    public void delete(CommandSender sender, String pluginname) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            String version = StringUtils.substring(plugin.getDescription().getVersion(), 0, 15);
            if (YumAPI.getPlugman().deletePlugin(sender, plugin)) {
                sender.sendMessage(String.format(del, pluginname, version));
            } else {
                sender.sendMessage(String.format(delFailed, pluginname));
            }
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @Cmd(aliases = "ddel", minimumArguments = 1)
    @Help(value = "删除插件数据文件夹", possibleArguments = "<插件名称>")
    @Sort(7)
    public void dirdelete(CommandSender sender, String pluginname) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            FileKit.deleteDir(sender, plugin.getDataFolder());
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @Cmd(aliases = "f", minimumArguments = 1)
    @Help(value = "通过类名查找插件", possibleArguments = "<插件类名>")
    @Sort(10)
    public void find(CommandSender sender, String classname) {
        try {
            Class<?> clazz = Class.forName(classname);
            Field field = clazz.getClassLoader().getClass().getDeclaredField("plugin");
            field.setAccessible(true);
            Plugin plugin = (JavaPlugin) field.get(clazz.getClassLoader());
            Bukkit.dispatchCommand(sender, "yum info " + plugin.getName());
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e2) {
            sender.sendMessage("§4错误: 无法找到类 " + classname + " 所对应的插件信息 异常:" + e2.getClass().getSimpleName() + " "
                    + e2.getMessage() + "!");
        }
    }

    @Cmd(aliases = "fdel", minimumArguments = 1)
    @Help(value = "删除插件以及数据文件夹", possibleArguments = "<插件名称>")
    @Sort(7)
    public void fulldelete(CommandSender sender, String pluginname) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            String version = StringUtils.substring(plugin.getDescription().getVersion(), 0, 15);
            if (YumAPI.getPlugman().fullDeletePlugin(sender, plugin)) {
                sender.sendMessage(String.format(del, pluginname, version));
            } else {
                sender.sendMessage(String.format(delFailed, pluginname));
            }
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "查看插件详情", possibleArguments = "<插件名称>")
    @Sort(2)
    @Async
    public void info(CommandSender sender, String pluginname) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            PluginDescriptionFile desc = plugin.getDescription();
            sender.sendMessage("§6插件名称: §3" + plugin.getName());
            sender.sendMessage("§6插件版本: §3" + StringUtils.substring(plugin.getDescription().getVersion(), 0, 15));
            sender.sendMessage("§6插件作者: §3" + StringUtils.join(desc.getAuthors(), " "));
            sender.sendMessage("§6插件描述: §3" + (desc.getDescription() == null ? "无" : desc.getDescription()));
            sender.sendMessage("§6插件依赖: §3" + (desc.getDepend().isEmpty() ? "无" : ""));
            sendStringArray(sender, desc.getDepend(), "§6 - §a");
            sender.sendMessage("§6插件软依赖: §3" + (desc.getSoftDepend().isEmpty() ? "无" : ""));
            sendStringArray(sender, desc.getSoftDepend(), "§6 - §a");
            Map<String, Map<String, Object>> clist = desc.getCommands();
            if (clist != null) {
                sender.sendMessage("§6插件注册命令: §3" + (clist.isEmpty() ? "无" : ""));
                for (Entry<String, Map<String, Object>> entry : clist.entrySet()) {
                    sender.sendMessage("§6 - §a" + entry.getKey());
                    sendEntryList(sender, "§6   别名: §a", entry.getValue(), "aliases");
                    sendEntry(sender, "§6   描述: §a", entry.getValue(), "description");
                    sendEntry(sender, "§6   权限: §a", entry.getValue(), "permission");
                    sendEntry(sender, "§6   用法: §a", entry.getValue(), "usage");
                }
            }
            List<Permission> plist = desc.getPermissions();
            if (plist != null) {
                sender.sendMessage("§6插件注册权限: " + (plist.isEmpty() ? "无" : ""));
                for (Permission perm : plist) {
                    sender.sendMessage("§6 - §a" + perm.getName() + "§6 - §e"
                            + (perm.getDescription().isEmpty() ? "无描述" : perm.getDescription()));
                }
            }
            sender.sendMessage("§6插件物理路径: §3" + YumAPI.getPlugman().getPluginFile(plugin).getAbsolutePath());
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @Cmd(aliases = "i", minimumArguments = 1)
    @Help(value = "安装插件", possibleArguments = "<插件名称>")
    @Sort(12)
    public void install(final CommandSender sender, final String pluginname, final String pluginversion) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin == null) {
            Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
                @Override
                public void run() {
                    if (pluginversion == null) {
                        YumAPI.installFromYum(sender, pluginname);
                    } else {
                        YumAPI.installFromYum(sender, pluginname, pluginversion);
                    }
                }
            });
        } else {
            sender.sendMessage("§4错误: §c插件 §b" + pluginname + " §c已安装在服务器 需要更新请使用 §b/yum update " + pluginname + "!");
        }
    }

    @Cmd(aliases = "l")
    @Help(value = "列出已安装插件列表")
    @Sort(1)
    @Async
    public void list(CommandSender sender) {
        sender.sendMessage("§6[Yum仓库]§3服务器已安装插件: ");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            String pname = plugin.getName();
            Tellraw fm = Tellraw.create();
            fm.text(String.format("§6- %-32s", YumAPI.getPlugman().getFormattedName(plugin, true)));
            fm.then(" ");
            fm.then(update).cmd_tip("/yum u " + pname, update);
            fm.then(" ");
            fm.then(unload).cmd_tip("/yum unload " + pname, unload);
            fm.then(" ");
            fm.then(reload).cmd_tip("/yum re " + pname, reload);
            fm.then(" ");
            fm.then(delete).cmd_tip("/yum del " + pname, delete);
            fm.send(sender);
        }
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "载入插件", possibleArguments = "<插件名称>")
    @Sort(3)
    public void load(CommandSender sender, String pluginname) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin == null) {
            YumAPI.getPlugman().load(sender, pluginname);
        } else {
            sender.sendMessage("§c错误: §a插件 §b" + pluginname + " §c已加载到服务器!");
        }
    }

    @EventHandler
    public void onAdminJoin(PlayerJoinEvent e) {
        if (e.getPlayer().isOp()) {
            YumAPI.updateCheck(e.getPlayer());
        }
    }

    @Cmd(aliases = "re")
    @Help(value = "重载插件", possibleArguments = "<插件名称|all|*>")
    @Sort(5)
    public void reload(CommandSender sender, String pluginname) {
        if (pluginname == null) {
            ConfigManager.i().reload();
            sender.sendMessage("§6重载: §a配置文件已重载!");
            return;
        }
        if (pluginname.equalsIgnoreCase("all") || pluginname.equalsIgnoreCase("*")) {
            YumAPI.getPlugman().reloadAll(sender);
            return;
        }
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            YumAPI.getPlugman().reload(sender, plugin);
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @Cmd(aliases = "r", minimumArguments = 1)
    @Help(value = "插件源命令", possibleArguments = "<add|del|all|clean|list> <仓库名称>")
    @Sort(16)
    @Async
    public void repo(CommandSender sender, String cmd, String arg1) {
        switch (cmd) {
        case "add":
            if (arg1 != null) {
                if (YumAPI.getRepo().addRepositories(sender, arg1)) {
                    String reponame = YumAPI.getRepo().getRepoCache(arg1).name;
                    sender.sendMessage("§6仓库: §a源仓库 §e" + reponame + " §a的插件信息已缓存!");
                } else {
                    sender.sendMessage("§6仓库: §c源地址未找到仓库信息或当前地址已缓存!");
                }
            } else {
                sender.sendMessage("§6仓库: §c请输入需要添加的源地址!");
            }
            break;
        case "del":
            if (arg1 != null) {
                Repositories delrepo = YumAPI.getRepo().getRepoCache(arg1);
                if (delrepo != null) {
                    YumAPI.getRepo().delRepositories(sender, arg1);
                    sender.sendMessage("§6仓库: §a源仓库 §e" + delrepo.name + " §c已删除 §a请使用 §b/yum repo update §a更新缓存!");
                } else {
                    sender.sendMessage("§6仓库: §c源地址未找到!");
                }
            } else {
                sender.sendMessage("§6仓库: §c请输入需要删除的源地址!");
            }
            break;
        case "delall":
            YumAPI.getRepo().getRepoCache().getRepos().clear();
            sender.sendMessage("§6仓库: §a缓存的仓库信息已清理!");
            break;
        case "list":
            sender.sendMessage("§6仓库: §b缓存的插件信息如下 ");
            sendStringArray(sender, YumAPI.getRepo().getAllPluginsInfo());
            break;
        case "all":
            sender.sendMessage("§6仓库: §b缓存的仓库信息如下 ");
            sendStringArray(sender, YumAPI.getRepo().getRepoCache().getAllRepoInfo());
            break;
        case "clean":
            YumAPI.getRepo().clean();
            sender.sendMessage("§6仓库: §a缓存的插件信息已清理!");
            break;
        case "update":
            YumAPI.getRepo().updateRepositories(sender);
            sender.sendMessage("§6仓库: §a仓库缓存数据已更新!");
            break;
        }
    }

    @Cmd(aliases = "s", minimumArguments = 1)
    @Help(value = "从BukkitDev搜索插件", possibleArguments = "插件名称")
    @Sort(11)
    @Async
    public void search(CommandSender sender, String pname) {
        if (pname.length() < 3) {
            sender.sendMessage(searchlimit);
            return;
        }
        sender.sendMessage(String.format(searching, pname));
        List<Projects> list = Projects.parseList(HttpKit.get(String.format(BukkitDev.SEARCH, pname.toLowerCase())));
        if (list.isEmpty()) {
            sender.sendMessage(String.format(not_found_from_bukkit, pname));
            return;
        }
        sender.sendMessage(String.format(result, pname));
        sender.sendMessage(bukkitlistprefix);
        for (Projects p : list) {
            Tellraw fm = Tellraw.create();
            fm.text(String.format(bukkitlist, p.id, p.name, p.stage));
            fm.then(" ");
            fm.then(look).cmd_tip("/yum br look " + p.id, look);
            fm.send(sender);
        }
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "卸载插件", possibleArguments = "<插件名称>")
    @Sort(4)
    public void unload(CommandSender sender, String pluginname) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            YumAPI.getPlugman().unload(sender, plugin);
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @Cmd(aliases = "u")
    @Help(value = "更新插件或缓存", possibleArguments = "[插件名称] [插件版本]")
    @Sort(13)
    @Async
    public void update(CommandSender sender, String argstring) {
        String[] args = argstring.split(" ");
        switch (args.length) {
        case 0:
            YumAPI.getRepo().updateRepositories(sender);
            sender.sendMessage("§6仓库: §a仓库缓存数据已更新!");
            break;
        case 1:
        case 2:
            String pluginname = args[0];
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
            sender.sendMessage("§a开始更新插件: " + pluginname);
            if (plugin != null) {
                if (args.length < 2) {
                    YumAPI.updateFromYum(sender, plugin);
                } else {
                    YumAPI.updateFromYum(sender, plugin, args[1]);
                }
            } else {
                sender.sendMessage("§c插件" + pluginname + "未安装或已卸载 需要安装请使用 §b/yum install " + pluginname + "!");
            }
            break;
        default:
            sender.sendMessage("§c命令参数错误!");
        }
    }

    @Cmd(aliases = "ua")
    @Help("更新所有可更新插件")
    @Sort(14)
    public void updateall(CommandSender sender) {
        YumAPI.updateAll(sender);
    }

    @Cmd(aliases = "ug")
    @Help(value = "升级或安装插件", possibleArguments = "[插件名称]")
    @Sort(15)
    public void upgrade(CommandSender sender, String pluginname) {
        if (pluginname == null) {
            YumAPI.getPlugman().upgrade(sender);
        } else {
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
            sender.sendMessage("§a开始升级插件: §b" + pluginname);
            if (plugin != null) {
                YumAPI.upgrade(sender, plugin);
            } else {
                sender.sendMessage(
                        "§c错误: §b插件 " + pluginname + " §c未安装或已卸载 需要安装请使用 §b/yum install " + pluginname + "!");
            }
        }
    }

    private String pnf(String pname) {
        return String.format("§4错误: §c插件 §b %s §c不存在或已卸载!", pname);
    }

    /**
     * 发生实体消息
     *
     * @param sender
     *            命令发送者
     * @param prefix
     *            实体前缀
     * @param map
     *            实体
     * @param key
     *            实体Key
     */
    private void sendEntry(CommandSender sender, String prefix, Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value != null) {
            sender.sendMessage(prefix + (String) value);
        }
    }

    /**
     * 发生实体消息
     *
     * @param sender
     *            命令发送者
     * @param prefix
     *            实体前缀
     * @param map
     *            实体
     * @param key
     *            实体Key
     */
    @SuppressWarnings("unchecked")
    private void sendEntryList(CommandSender sender, String prefix, Map<String, Object> map, String key) {
        List<String> values = (List<String>) map.get(key);
        if (values != null) {
            for (String value : values) {
                sender.sendMessage(prefix + value);
            }
        }
    }

    /**
     * 给玩家或控制台发送消息组
     *
     * @param sender
     *            接收消息的玩家
     * @param msg
     *            消息组
     */
    public static void sendStringArray(CommandSender sender, Collection<String> msg) {
        for (String string : msg) {
            sender.sendMessage(string);
        }
    }

    /**
     * 给玩家或控制台发送消息组
     *
     * @param sender
     *            接收消息的玩家
     * @param msg
     *            消息组
     * @param prefix
     *            消息前缀
     */
    public static void sendStringArray(CommandSender sender, Collection<String> msg, String prefix) {
        for (String string : msg) {
            sender.sendMessage(prefix + string);
        }
    }

    /**
     * 给玩家或控制台发送消息组
     *
     * @param sender
     *            接收消息的玩家
     * @param msg
     *            消息组
     * @param prefix
     *            消息前缀
     * @param suffix
     *            消息后缀
     */
    public static void sendStringArray(CommandSender sender, Collection<String> msg, String prefix, String suffix) {
        for (String string : msg) {
            sender.sendMessage(prefix + string + suffix);
        }
    }
}
