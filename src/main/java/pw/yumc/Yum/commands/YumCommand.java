package pw.yumc.Yum.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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

import cn.citycraft.PluginHelper.callback.CallBack.One;
import cn.citycraft.PluginHelper.kit.PluginKit;
import cn.citycraft.PluginHelper.kit.ZipKit;
import cn.citycraft.PluginHelper.tellraw.FancyMessage;
import cn.citycraft.PluginHelper.utils.IOUtil;
import cn.citycraft.PluginHelper.utils.StrKit;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.models.BukkitDev;
import pw.yumc.Yum.models.BukkitDev.Files;
import pw.yumc.Yum.models.BukkitDev.Projects;
import pw.yumc.Yum.models.RepoSerialization.Repositories;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.commands.CommandArgument;
import pw.yumc.YumCore.commands.CommandExecutor;
import pw.yumc.YumCore.commands.CommandManager;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;
import pw.yumc.YumCore.commands.annotation.Sort;

/**
 * Yum命令基类
 *
 * @since 2016年1月9日 上午10:02:24
 * @author 喵♂呜
 */
public class YumCommand implements Listener, CommandExecutor {
    private final String prefix = "§6[§bYum §a插件管理§6] ";

    private final String searchlimit = prefix + "§c为保证搜索速度和准确性 关键词必须大于 3 个字符!";
    private final String searching = prefix + "§a正在从 §eBukkitDev §a获取 §b%s §a的相关数据...";
    private final String not_found_from_bukkit = prefix + "§c未在 §eBukkitDev §c搜索到 §b%s §c的相关插件!";
    private final String result = prefix + "§6关键词 §b%s §6的搜索结果如下:";
    private final String bukkitlistprefix = " §6插件ID  §3插件名称                  §d发布类型   §a操作";
    private final String bukkitlist = "§6- §e%-6s §b%-25s §d%-10s";

    private final String fsearching = prefix + "§a正在从 §eBukkitDev §a获取ID §b%s §a的文件列表...";
    private final String not_found_id_from_bukkit = prefix + "§c未在 §eBukkitDev §c搜索到ID为 §b%s §c的相关插件!";
    private final String filelistprefix = "  §6插件名称             §3游戏版本      §d发布类型   §a操作";
    private final String filelist = "§6- §b%-20s §3%-15s §d%-10s";

    private final String del = "§c删除: §a插件 §b%s §a版本 §d%s §a已从服务器卸载并删除!";
    private final String delFailed = "§c删除: §a插件 §b%s §c卸载或删除时发生错误 删除失败!";

    private final String look = "§6查看";
    private final String install = "§a安装";
    private final String update = "§a更新";
    private final String unload = "§d卸载";
    private final String reload = "§6重载";
    private final String delete = "§c删除";

    private final String unzip_error = prefix + "ZIP文件解压错误!";

    Yum main;

    public YumCommand(final Yum yum) {
        main = yum;
        Bukkit.getPluginManager().registerEvents(this, yum);
        new CommandManager("yum", this, PluginTabComplete.instence);
    }

    @Cmd(aliases = "br", minimumArguments = 2)
    @Help(value = "从BukkitDev查看安装插件", possibleArguments = "<操作符> <项目ID|项目名称> [地址]")
    public void bukkitrepo(final CommandArgument e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        PluginKit.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final String id = args[1];
                switch (args[0]) {
                case "look": {
                    sender.sendMessage(String.format(fsearching, id));
                    final List<Files> lf = Files.parseList(IOUtil.getData(String.format(BukkitDev.PLUGIN, id)));
                    if (lf.isEmpty()) {
                        sender.sendMessage(String.format(not_found_id_from_bukkit, id));
                        return;
                    }
                    sender.sendMessage(filelistprefix);
                    for (int i = 0; i < lf.size() || i < 8; i++) {
                        final Files f = lf.get(i);
                        final FancyMessage fm = FancyMessage.newFM();
                        fm.text(String.format(filelist, f.name, f.gameVersion, f.releaseType));
                        fm.then(" ");
                        fm.then(install).command(String.format("/yum br ai %s %s", f.name, f.downloadUrl));
                        fm.send(sender);
                    }
                    break;
                }
                case "ai": {
                    if (args.length < 3) {
                        return;
                    }
                    final String url = args[2];
                    final File file = new File(Bukkit.getUpdateFolderFile(), YumAPI.getDownload().getFileName(url));
                    YumAPI.getDownload().run(e.getSender(), url, file, new One<File>() {
                        @Override
                        public void run(final File file) {
                            if (file.getName().endsWith(".zip")) {
                                try {
                                    ZipKit.unzip(file, Bukkit.getUpdateFolderFile(), ".jar");
                                    file.delete();
                                } catch (final IOException e) {
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
                    final List<Files> lf = Files.parseList(IOUtil.getData(String.format(BukkitDev.PLUGIN, id)));
                    if (lf.isEmpty()) {
                        sender.sendMessage(String.format(not_found_id_from_bukkit, id));
                        return;
                    }
                    final Files f = lf.get(0);
                    final String url = f.downloadUrl;
                    final File file = new File(Bukkit.getUpdateFolderFile(), YumAPI.getDownload().getFileName(url));
                    YumAPI.getDownload().run(e.getSender(), url, file, new One<File>() {
                        @Override
                        public void run(final File file) {
                            if (file.getName().endsWith(".zip")) {
                                try {
                                    ZipKit.unzip(file, Bukkit.getUpdateFolderFile(), ".jar");
                                } catch (final IOException e) {
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
    public void delete(final CommandArgument e) {
        final String pluginname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            final String version = StringUtils.substring(plugin.getDescription().getVersion(), 0, 15);
            if (YumAPI.getPlugman().deletePlugin(sender, plugin)) {
                sender.sendMessage(String.format(del, pluginname, version));
            } else {
                sender.sendMessage(String.format(delFailed, pluginname));
            }
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @Cmd(aliases = "f", minimumArguments = 1)
    @Help(value = "通过类名查找插件", possibleArguments = "<插件类名>")
    @Sort(10)
    public void find(final CommandArgument e) {
        final String classname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        try {
            final Class<?> clazz = Class.forName(classname);
            final Field field = clazz.getClassLoader().getClass().getDeclaredField("plugin");
            field.setAccessible(true);
            final Plugin plugin = (JavaPlugin) field.get(clazz.getClassLoader());
            Bukkit.dispatchCommand(sender, "yum info " + plugin.getName());
        } catch (final ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e2) {
            sender.sendMessage("§4错误: 无法找到类 " + classname + " 所对应的插件信息 异常:" + e2.getClass().getSimpleName() + " " + e2.getMessage() + "!");
        }
    }

    @Cmd(aliases = "fdel", minimumArguments = 1)
    @Help(value = "删除插件以及数据文件夹", possibleArguments = "<插件名称>")
    @Sort(7)
    public void fulldelete(final CommandArgument e) {
        final String pluginname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            final String version = StringUtils.substring(plugin.getDescription().getVersion(), 0, 15);
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
    public void info(final CommandArgument e) {
        final String pluginname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            final PluginDescriptionFile desc = plugin.getDescription();
            sender.sendMessage("§6插件名称: §3" + plugin.getName());
            sender.sendMessage("§6插件版本: §3" + StringUtils.substring(plugin.getDescription().getVersion(), 0, 15));
            sender.sendMessage("§6插件作者: §3" + StringUtils.join(desc.getAuthors(), " "));
            sender.sendMessage("§6插件描述: §3" + (desc.getDescription() == null ? "无" : desc.getDescription()));
            sender.sendMessage("§6插件依赖: §3" + (desc.getDepend().isEmpty() ? "无" : ""));
            StrKit.sendStringArray(sender, desc.getDepend(), "§6 - §a");
            sender.sendMessage("§6插件软依赖: §3" + (desc.getSoftDepend().isEmpty() ? "无" : ""));
            StrKit.sendStringArray(sender, desc.getSoftDepend(), "§6 - §a");
            final Map<String, Map<String, Object>> clist = desc.getCommands();
            if (clist != null) {
                sender.sendMessage("§6插件注册命令: §3" + (clist.isEmpty() ? "无" : ""));
                for (final Entry<String, Map<String, Object>> entry : clist.entrySet()) {
                    sender.sendMessage("§6 - §a" + entry.getKey());
                    sendEntryList(sender, "§6   别名: §a", entry.getValue(), "aliases");
                    sendEntry(sender, "§6   描述: §a", entry.getValue(), "description");
                    sendEntry(sender, "§6   权限: §a", entry.getValue(), "permission");
                    sendEntry(sender, "§6   用法: §a", entry.getValue(), "usage");
                }
            }
            final List<Permission> plist = desc.getPermissions();
            if (plist != null) {
                sender.sendMessage("§6插件注册权限: " + (plist.isEmpty() ? "无" : ""));
                for (final Permission perm : plist) {
                    sender.sendMessage("§6 - §a" + perm.getName() + "§6 - §e" + (perm.getDescription().isEmpty() ? "无描述" : perm.getDescription()));
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
    public void install(final CommandArgument e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        final String pluginname = args[0];
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin == null) {
            Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
                @Override
                public void run() {
                    if (args.length < 2) {
                        YumAPI.installFromYum(sender, pluginname);
                    } else {
                        YumAPI.installFromYum(sender, pluginname, args[1]);
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
    public void list(final CommandArgument e) {
        final CommandSender sender = e.getSender();
        sender.sendMessage("§6[Yum仓库]§3服务器已安装插件: ");
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            final String pname = plugin.getName();
            final FancyMessage fm = FancyMessage.newFM();
            fm.text(String.format("§6- %-32s", YumAPI.getPlugman().getFormattedName(plugin, true)));
            fm.then(" ");
            fm.then(update).command("/yum u " + pname);
            fm.then(" ");
            fm.then(unload).command("/yum unload " + pname);
            fm.then(" ");
            fm.then(reload).command("/yum re " + pname);
            fm.then(" ");
            fm.then(delete).command("/yum del " + pname);
            fm.send(sender);
        }
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "载入插件", possibleArguments = "<插件名称>")
    @Sort(3)
    public void load(final CommandArgument e) {
        final CommandSender sender = e.getSender();
        final String pluginname = e.getArgs()[0];
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin == null) {
            YumAPI.getPlugman().load(sender, pluginname);
        } else {
            sender.sendMessage("§c错误: §a插件 §b" + pluginname + " §c已加载到服务器!");
        }
    }

    @EventHandler
    public void onAdminJoin(final PlayerJoinEvent e) {
        if (e.getPlayer().isOp()) {
            YumAPI.updateCheck(e.getPlayer());
        }
    }

    @Cmd(aliases = "re")
    @Help(value = "重载插件", possibleArguments = "<插件名称|all|*>")
    @Sort(5)
    public void reload(final CommandArgument e) {
        final CommandSender sender = e.getSender();
        if (e.getArgs().length == 0) {
            ConfigManager.i().reload();
            sender.sendMessage("§6重载: §a配置文件已重载!");
            return;
        }
        final String pluginname = e.getArgs()[0];
        if (pluginname.equalsIgnoreCase("all") || pluginname.equalsIgnoreCase("*")) {
            YumAPI.getPlugman().reloadAll(sender);
            return;
        }
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
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
    public void repo(final CommandArgument e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        final String cmd = args[0];
        switch (cmd) {
        case "add":
            if (args.length == 2) {
                if (YumAPI.getRepo().addRepositories(sender, args[1])) {
                    final String reponame = YumAPI.getRepo().getRepoCache(args[1]).name;
                    sender.sendMessage("§6仓库: §a源仓库 §e" + reponame + " §a的插件信息已缓存!");
                } else {
                    sender.sendMessage("§6仓库: §c源地址未找到仓库信息或当前地址已缓存!");
                }
            } else {
                sender.sendMessage("§6仓库: §c请输入需要添加的源地址!");
            }
            break;
        case "del":
            if (args.length == 2) {
                final Repositories delrepo = YumAPI.getRepo().getRepoCache(args[1]);
                if (delrepo != null) {
                    YumAPI.getRepo().delRepositories(sender, args[1]);
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
            StrKit.sendStringArray(sender, YumAPI.getRepo().getAllPluginsInfo());
            break;
        case "all":
            sender.sendMessage("§6仓库: §b缓存的仓库信息如下 ");
            StrKit.sendStringArray(sender, YumAPI.getRepo().getRepoCache().getAllRepoInfo());
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
    public void search(final CommandArgument e) {
        final String pname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        if (pname.length() < 3) {
            sender.sendMessage(searchlimit);
            return;
        }
        sender.sendMessage(String.format(searching, pname));
        final List<Projects> list = Projects.parseList(IOUtil.getData(String.format(BukkitDev.SEARCH, pname.toLowerCase())));
        if (list.isEmpty()) {
            sender.sendMessage(String.format(not_found_from_bukkit, pname));
            return;
        }
        sender.sendMessage(String.format(result, pname));
        sender.sendMessage(bukkitlistprefix);
        for (final Projects p : list) {
            final FancyMessage fm = FancyMessage.newFM();
            fm.text(String.format(bukkitlist, p.id, p.name, p.stage));
            fm.then(" ");
            fm.then(look).command("/yum br look " + p.id);
            fm.send(sender);
        }
    }

    @Cmd
    public void test(final CommandArgument e) {
        Log.toSender(e.getSender(), "Test");
        Log.toSender(e.getSender(), new String[] { "Test1", "Test2", "Test3" });
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "卸载插件", possibleArguments = "<插件名称>")
    @Sort(4)
    public void unload(final CommandArgument e) {
        final String pluginname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
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
    public void update(final CommandArgument e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        switch (args.length) {
        case 0:
            YumAPI.getRepo().updateRepositories(sender);
            sender.sendMessage("§6仓库: §a仓库缓存数据已更新!");
            break;
        case 1:
        case 2:
            final String pluginname = args[0];
            final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
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
    public void updateall(final CommandArgument e) {
        YumAPI.updateAll(e.getSender());
    }

    @Cmd(aliases = "ug")
    @Help(value = "升级或安装插件", possibleArguments = "[插件名称]")
    @Sort(15)
    public void upgrade(final CommandArgument e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        if (args.length == 0) {
            YumAPI.getPlugman().upgrade(sender);
        } else {
            final String pluginname = args[0];
            final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
            sender.sendMessage("§a开始升级插件: §b" + pluginname);
            if (plugin != null) {
                YumAPI.upgrade(sender, plugin);
            } else {
                sender.sendMessage("§c错误: §b插件 " + pluginname + " §c未安装或已卸载 需要安装请使用 §b/yum install " + pluginname + "!");
            }
        }
    }

    private String pnf(final String pname) {
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
    private void sendEntry(final CommandSender sender, final String prefix, final Map<String, Object> map, final String key) {
        final Object value = map.get(key);
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
    private void sendEntryList(final CommandSender sender, final String prefix, final Map<String, Object> map, final String key) {
        final List<String> values = (List<String>) map.get(key);
        if (values != null) {
            for (final String value : values) {
                sender.sendMessage(prefix + value);
            }
        }
    }

}
