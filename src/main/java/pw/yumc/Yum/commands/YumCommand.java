package pw.yumc.Yum.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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

import cn.citycraft.PluginHelper.commands.HandlerCommand;
import cn.citycraft.PluginHelper.commands.HandlerCommands;
import cn.citycraft.PluginHelper.commands.HandlerTabComplete;
import cn.citycraft.PluginHelper.commands.InvokeCommandEvent;
import cn.citycraft.PluginHelper.commands.InvokeSubCommand;
import cn.citycraft.PluginHelper.utils.StrKit;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.managers.PluginsManager;
import pw.yumc.Yum.managers.RepositoryManager;
import pw.yumc.Yum.models.RepoSerialization.Repositories;

/**
 * Yum命令基类
 *
 * @since 2016年1月9日 上午10:02:24
 * @author 喵♂呜
 */
public class YumCommand implements HandlerCommands, Listener {
    Yum main;
    RepositoryManager repo;
    PluginsManager plugman;

    public YumCommand(final Yum yum) {
        main = yum;
        repo = YumAPI.getRepo();
        plugman = YumAPI.getPlugman();
        Bukkit.getPluginManager().registerEvents(this, yum);
        final InvokeSubCommand cmdhandler = new InvokeSubCommand(yum, "yum");
        cmdhandler.setAllCommandOnlyConsole(yum.getConfig().getBoolean("onlyCommandConsole", false));
        cmdhandler.registerCommands(this);
    }

    @HandlerCommand(name = "delete", aliases = { "del" }, minimumArguments = 1, description = "删除插件", possibleArguments = "<插件名称>")
    public void delete(final InvokeCommandEvent e) {
        final String pluginname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            final String version = StringUtils.substring(plugin.getDescription().getVersion(), 0, 15);
            if (plugman.deletePlugin(sender, plugin)) {
                sender.sendMessage("§c删除: §a插件 §b" + pluginname + " §a版本 §d" + version + " §a已从服务器卸载并删除!");
            } else {
                sender.sendMessage("§c删除: §a插件 §b" + pluginname + " §c卸载或删除时发生错误 删除失败!");
            }
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @HandlerCommand(name = "find", aliases = { "f" }, minimumArguments = 1, possibleArguments = "<插件类名>", description = "通过类名查找插件")
    public void find(final InvokeCommandEvent e) {
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

    @HandlerCommand(name = "fulldelete", aliases = { "fdel" }, minimumArguments = 1, description = "删除插件以及数据文件夹", possibleArguments = "<插件名称>")
    public void fulldelete(final InvokeCommandEvent e) {
        final String pluginname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            final String version = StringUtils.substring(plugin.getDescription().getVersion(), 0, 15);
            if (plugman.fullDeletePlugin(sender, plugin)) {
                sender.sendMessage("§c删除: §a插件 §b" + pluginname + " §a版本 §d" + version + " §a已从服务器卸载并删除!");
            } else {
                sender.sendMessage("§c删除: §c插件 §b" + pluginname + " §c卸载或删除时发生错误 删除失败!");
            }
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @HandlerCommand(name = "info", minimumArguments = 1, description = "查看插件详情", possibleArguments = "<插件名称>")
    public void info(final InvokeCommandEvent e) {
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
            sender.sendMessage("§6插件物理路径: §3" + plugman.getPluginFile(plugin).getAbsolutePath());
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @HandlerCommand(name = "install", aliases = { "i" }, minimumArguments = 1, description = "安装插件", possibleArguments = "<插件名称>")
    public void install(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        final String pluginname = args[0];
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin == null) {
            Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
                @Override
                public void run() {
                    if (args.length < 2) {
                        YumAPI.installfromyum(sender, pluginname);
                    } else {
                        YumAPI.installfromyum(sender, pluginname, args[1]);
                    }
                }
            });
        } else {
            sender.sendMessage("§4错误: §c插件 §b" + pluginname + " §c已安装在服务器 需要更新请使用 §b/yum update " + pluginname + "!");
        }
    }

    @HandlerCommand(name = "list", aliases = { "l" }, description = "列出已安装插件列表")
    public void list(final InvokeCommandEvent e) {
        final CommandSender sender = e.getSender();
        sender.sendMessage("§6[Yum仓库]§3服务器已安装插件: ");
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            sender.sendMessage("§6- " + plugman.getFormattedName(plugin, true));
        }
    }

    @HandlerTabComplete()
    public List<String> listtab(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        if (!args[0].equalsIgnoreCase("install") && !args[0].equalsIgnoreCase("repo")) {
            return StrKit.copyPartialMatches(args[1], plugman.getPluginNames(false), new ArrayList<String>());
        }
        if (args[0].equalsIgnoreCase("install")) {
            return StrKit.copyPartialMatches(args[1], repo.getAllPluginName(), new ArrayList<String>());
        }
        if (args[0].equalsIgnoreCase("repo")) {
            if (args.length == 2) {
                return StrKit.copyPartialMatches(args[1], Arrays.asList(new String[] { "add", "all", "list", "delall", "clean", "update", "del" }), new ArrayList<String>());
            }
            if (args.length == 3 && (args[1] == "add" || args[1] == "del")) {
                return StrKit.copyPartialMatches(args[2], repo.getRepos().keySet(), new ArrayList<String>());
            }
        }
        return null;
    }

    @HandlerCommand(name = "load", minimumArguments = 1, description = "载入插件", possibleArguments = "<插件名称>")
    public void load(final InvokeCommandEvent e) {
        final CommandSender sender = e.getSender();
        final String pluginname = e.getArgs()[0];
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin == null) {
            plugman.load(sender, pluginname);
        } else {
            sender.sendMessage("§c错误: §a插件 §b" + pluginname + " §c已加载到服务器!");
        }
    }

    @EventHandler
    public void onAdminJoin(final PlayerJoinEvent e) {
        if (e.getPlayer().isOp()) {
            YumAPI.updatecheck(e.getPlayer());
        }
    }

    @HandlerCommand(name = "reload", aliases = { "re" }, minimumArguments = 1, description = "重载插件", possibleArguments = "<插件名称|all|*>")
    public void reload(final InvokeCommandEvent e) {
        final CommandSender sender = e.getSender();
        final String pluginname = e.getArgs()[0];
        if (pluginname.equalsIgnoreCase("all") || pluginname.equalsIgnoreCase("*")) {
            plugman.reloadAll(sender);
            return;
        }
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            plugman.reload(sender, plugin);
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @HandlerCommand(name = "repo", aliases = { "r" }, minimumArguments = 1, description = "插件源命令", possibleArguments = "<add|del|all|clean|list> <仓库名称>")
    public void repo(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        main.getServer().getScheduler().runTaskAsynchronously(main, new Runnable() {
            @Override
            public void run() {
                final String cmd = args[0];
                switch (cmd) {
                case "add":
                    if (args.length == 2) {
                        if (repo.addRepositories(sender, args[1])) {
                            final String reponame = repo.getRepoCache(args[1]).name;
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
                        final Repositories delrepo = repo.getRepoCache(args[1]);
                        if (delrepo != null) {
                            repo.delRepositories(sender, args[1]);
                            sender.sendMessage("§6仓库: §a源仓库 §e" + delrepo.name + " §c已删除 §a请使用 §b/yum repo update §a更新缓存!");
                        } else {
                            sender.sendMessage("§6仓库: §c源地址未找到!");
                        }
                    } else {
                        sender.sendMessage("§6仓库: §c请输入需要删除的源地址!");
                    }
                    break;
                case "delall":
                    repo.getRepoCache().getRepos().clear();
                    sender.sendMessage("§6仓库: §a缓存的仓库信息已清理!");
                    break;
                case "list":
                    sender.sendMessage("§6仓库: §b缓存的插件信息如下 ");
                    StrKit.sendStringArray(sender, repo.getAllPluginsInfo());
                    break;
                case "all":
                    sender.sendMessage("§6仓库: §b缓存的仓库信息如下 ");
                    StrKit.sendStringArray(sender, repo.getRepoCache().getAllRepoInfo());
                    break;
                case "clean":
                    repo.clean();
                    sender.sendMessage("§6仓库: §a缓存的插件信息已清理!");
                    break;
                case "update":
                    repo.updateRepositories(sender);
                    sender.sendMessage("§6仓库: §a仓库缓存数据已更新!");
                    break;
                }
            }
        });
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
    public void sendEntry(final CommandSender sender, final String prefix, final Map<String, Object> map, final String key) {
        final Object value = map.get(key);
        if (value != null) {
            sender.sendMessage(prefix + (String) value);
        }
    }

    @HandlerCommand(name = "unload", minimumArguments = 1, description = "卸载插件", possibleArguments = "<插件名称>")
    public void unload(final InvokeCommandEvent e) {
        final String pluginname = e.getArgs()[0];
        final CommandSender sender = e.getSender();
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            plugman.unload(sender, plugin);
        } else {
            sender.sendMessage(pnf(pluginname));
        }
    }

    @HandlerCommand(name = "update", aliases = { "u" }, description = "更新插件或缓存", possibleArguments = "[插件名称] [插件版本]")
    public void update(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        switch (args.length) {
        case 0:
            repo.updateRepositories(sender);
            sender.sendMessage("§6仓库: §a仓库缓存数据已更新!");
            break;
        case 1:
        case 2:
            final String pluginname = args[0];
            final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
            sender.sendMessage("§a开始更新插件: " + pluginname);
            if (plugin != null) {
                Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
                    @Override
                    public void run() {
                        if (args.length < 2) {
                            YumAPI.updatefromyum(sender, plugin);
                        } else {
                            YumAPI.updatefromyum(sender, plugin, args[1]);
                        }
                    }
                });
            } else {
                sender.sendMessage("§c插件" + pluginname + "未安装或已卸载 需要安装请使用 §b/yum install " + pluginname + "!");
            }
            break;
        default:
            sender.sendMessage("§c命令参数错误!");
        }
    }

    @HandlerCommand(name = "updateall", aliases = { "ua" }, description = "更新所有可更新插件")
    public void updateall(final InvokeCommandEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
            @Override
            public void run() {
                YumAPI.updateall(e.getSender());
            }
        });
    }

    @HandlerCommand(name = "upgrade", aliases = { "ug" }, description = "升级或载入插件", possibleArguments = "[插件名称]")
    public void upgrade(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
            @Override
            public void run() {
                if (args.length == 0) {
                    plugman.upgrade(sender);
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
        });
    }

    private String pnf(final String pname) {
        return String.format("§4错误: §c插件 §b %s §c不存在或已卸载!", pname);
    }

}
