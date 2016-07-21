package pw.yumc.Yum.managers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPluginLoader;

import com.google.common.base.Joiner;

import cn.citycraft.PluginHelper.utils.FileUtil;
import cn.citycraft.PluginHelper.utils.StringUtil;

/**
 * 插件管理类
 *
 * @author 喵♂呜
 * @since 2015年8月21日下午7:03:26
 */
public class PluginsManager {
    private final Set<String> ignoreList = new HashSet<>();
    private final Plugin main;

    public PluginsManager(final Plugin plugin) {
        this.main = plugin;
    }

    public static String getVersion(final Plugin plugin) {
        return StringUtils.substring(plugin.getDescription().getVersion(), 0, 15);
    }

    /**
     * 添加到忽略列表
     *
     * @param name
     *            - 插件名称
     * @return 是否成功
     */
    public boolean addIgnore(final Collection<? extends String> name) {
        return ignoreList.addAll(name);
    }

    /**
     * 添加到忽略列表
     *
     * @param name
     *            - 插件名称
     * @return 是否成功
     */
    public boolean addIgnore(final String name) {
        return ignoreList.add(name);
    }

    /**
     * 删除插件
     *
     * @param sender
     *            - 命令发送者
     * @param plugin
     *            - 插件
     * @return 是否成功
     */
    public boolean deletePlugin(final CommandSender sender, final Plugin plugin) {
        return unload(sender, plugin) && getPluginFile(plugin).delete();
    }

    /**
     * 删除插件
     *
     * @param plugin
     *            - 插件
     * @return 是否成功
     */
    public boolean deletePlugin(final Plugin plugin) {
        return deletePlugin(Bukkit.getConsoleSender(), plugin);
    }

    /**
     * 关闭插件
     *
     * @param plugin
     *            - 插件
     */
    public void disable(final Plugin plugin) {
        if ((plugin != null) && (plugin.isEnabled())) {
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * 关闭所有插件
     */
    public void disableAll() {
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (!isIgnored(plugin)) {
                disable(plugin);
            }
        }
    }

    /**
     * 启用插件
     *
     * @param plugin
     *            - 插件
     */
    public void enable(final Plugin plugin) {
        if ((plugin != null) && (!plugin.isEnabled())) {
            Bukkit.getPluginManager().enablePlugin(plugin);
        }
    }

    /**
     * 启用所有插件
     */
    public void enableAll() {
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (!isIgnored(plugin)) {
                enable(plugin);
            }
        }
    }

    /**
     * 删除插件(包括数据)
     *
     * @param sender
     *            - 命令发送者
     * @param plugin
     *            - 插件
     * @return 是否成功
     */
    public boolean fullDeletePlugin(final CommandSender sender, final Plugin plugin) {
        return unload(sender, plugin) && getPluginFile(plugin).delete() && FileUtil.deleteDir(sender, plugin.getDataFolder());
    }

    /**
     * 获得格式化的插件名称
     *
     * @param plugin
     *            - 插件
     * @return 格式化的插件名称
     */
    public String getFormattedName(final Plugin plugin) {
        return getFormattedName(plugin, false);
    }

    /**
     * 获得格式化的插件名称(可带版本)
     *
     * @param plugin
     *            - 插件
     * @param includeVersions
     *            - 是否包括版本
     * @return 格式化的插件名称
     */
    public String getFormattedName(final Plugin plugin, final boolean includeVersions) {
        final ChatColor color = plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
        String pluginName = color + plugin.getName();
        if (includeVersions) {
            pluginName = pluginName + " (" + getVersion(plugin) + ")";
        }
        return pluginName;
    }

    /**
     * 通过名称获得插件
     *
     * @param name
     *            - 名称
     * @return 插件
     */
    public Plugin getPluginByName(final String name) {
        return Bukkit.getPluginManager().getPlugin(name);
    }

    /**
     * 通过名称获得插件(处理带空格的插件)
     *
     * @param name
     *            - 名称
     * @return 插件
     */
    public Plugin getPluginByName(final String[] args, final int start) {
        return getPluginByName(StringUtil.consolidateStrings(args, start));
    }

    /**
     * 获得插件绝对路径
     *
     * @param plugin
     *            - 插件
     * @return 插件的绝对路径
     */
    public File getPluginFile(final Plugin plugin) {
        File file = null;
        final ClassLoader cl = plugin.getClass().getClassLoader();
        if ((cl instanceof URLClassLoader)) {
            @SuppressWarnings("resource")
            final URLClassLoader ucl = (URLClassLoader) cl;
            final URL url = ucl.getURLs()[0];
            try {
                file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
            }
        }
        return file;
    }

    public List<String> getPluginNames(final boolean fullName) {
        final List<String> plugins = new ArrayList<String>();
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            plugins.add(fullName ? plugin.getDescription().getFullName() : plugin.getName());
        }
        return plugins;
    }

    /**
     * 获得插件版本
     *
     * @param name
     *            - 插件名称
     * @return 插件版本
     */
    public String getPluginVersion(final String name) {
        final Plugin plugin = getPluginByName(name);
        if ((plugin != null) && (plugin.getDescription() != null)) {
            return getVersion(plugin);
        }
        return null;
    }

    /**
     * 获得插件命令
     *
     * @param plugin
     *            - 插件
     * @return 插件命令
     */
    public String getUsages(final Plugin plugin) {
        final List<String> parsedCommands = new ArrayList<String>();

        final Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();

        if (commands != null) {
            final Iterator<Entry<String, Map<String, Object>>> commandsIt = commands.entrySet().iterator();
            while (commandsIt.hasNext()) {
                final Entry<String, Map<String, Object>> thisEntry = commandsIt.next();
                if (thisEntry != null) {
                    parsedCommands.add(thisEntry.getKey());
                }
            }
        }
        if (parsedCommands.isEmpty()) {
            return null;
        }
        return Joiner.on(", ").join(parsedCommands);
    }

    /**
     * 判断插件是否在忽略列表
     *
     * @param plugin
     *            - 插件
     * @return 是否
     */
    public boolean isIgnored(final Plugin plugin) {
        return isIgnored(plugin.getName());
    }

    /**
     * 判断插件是否在忽略列表
     *
     * @param plugin
     *            - 插件名称
     * @return 是否
     */
    public boolean isIgnored(final String plugin) {
        for (final String name : ignoreList) {
            if (name.equalsIgnoreCase(plugin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 载入插件
     *
     * @param sender
     *            - 命令发送者
     * @param pluginFile
     *            - 插件文件
     * @return 是否成功
     */
    public boolean load(final CommandSender sender, final File pluginFile) {
        Plugin target = null;
        final String name = pluginFile.getName();
        try {
            target = Bukkit.getPluginManager().loadPlugin(pluginFile);
        } catch (final InvalidDescriptionException e) {
            sender.sendMessage("§4异常: §c" + e.getMessage());
            sender.sendMessage("§4插件: §c" + name + " 的 plugin.yml 文件存在错误!");
            return false;
        } catch (final UnsupportedClassVersionError e) {
            sender.sendMessage("§4异常: §c" + e.getMessage());
            sender.sendMessage("§c服务器或JAVA的版本低于插件: " + name + " 所需要的版本!!");
            return false;
        } catch (final InvalidPluginException e) {
            sender.sendMessage("§4异常: §c" + e.getMessage());
            sender.sendMessage("§4文件: §c" + name + " 不是一个可载入的插件!");
            sender.sendMessage("§4注意: §cMOD服重载插件3次以上需重启服务器");
            return false;
        } catch (final UnknownDependencyException e) {
            sender.sendMessage("§4异常: §c服务器未安装必须依赖: " + e.getMessage());
            sender.sendMessage("§4插件: §c" + name + " 载入失败 缺少部分依赖项目!");
            return false;
        }
        if (target == null) {
            sender.sendMessage("§4异常: §c服务器类加载器载入插件失败!");
            return false;
        }
        target.onLoad();
        Bukkit.getPluginManager().enablePlugin(target);
        sender.sendMessage("§6载入: §a插件 §b" + target.getName() + " §a版本 §d" + getVersion(target) + " §a已成功载入到服务器!");
        return true;
    }

    /**
     * 载入插件
     *
     * @param sender
     *            - 命令发送者
     * @param name
     *            - 插件名称
     * @return 是否成功
     */
    public boolean load(CommandSender sender, final String name) {
        String filename = null;
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }
        if (!name.endsWith(".jar")) {
            filename = name + ".jar";
        }
        final File pluginDir = new File("plugins");
        final File updateDir = new File(pluginDir, "update");
        if (!pluginDir.isDirectory()) {
            sender.sendMessage("§6载入: §c插件目录不存在或IO错误!");
            return false;
        }

        File pluginFile = new File(pluginDir, filename);

        if (!pluginFile.isFile() && !new File(updateDir, filename).isFile()) {
            pluginFile = null;
            for (final File file : pluginDir.listFiles()) {
                if (file.getName().endsWith(".jar")) {
                    try {
                        final PluginDescriptionFile desc = main.getPluginLoader().getPluginDescription(file);
                        if (desc.getName().equalsIgnoreCase(name)) {
                            pluginFile = file;
                            break;
                        }
                    } catch (final InvalidDescriptionException e) {
                    }
                }
            }
            if (pluginFile == null) {
                sender.sendMessage("§6载入: §c在插件目录和更新目录均未找到 §b" + name + " §c插件 请确认文件是否存在!");
                return false;
            }
        }
        return load(sender, pluginFile);
    }

    /**
     * 载入插件
     *
     * @param pluginFile
     *            - 插件名称
     * @return 是否成功
     */
    public boolean load(final File pluginFile) {
        return load(Bukkit.getConsoleSender(), pluginFile);
    }

    /**
     * 载入插件
     *
     * @param name
     *            - 插件名称
     * @return 是否成功
     */
    public boolean load(final String name) {
        return load(Bukkit.getConsoleSender(), name);
    }

    /**
     * 删除重载插件
     *
     * @param sender
     *            - 命令发送者
     * @param plugin
     *            - 插件
     * @return 是否成功
     */
    public boolean reload(final CommandSender sender, final Plugin plugin) {
        if (plugin != null) {
            return unload(sender, plugin) && load(sender, plugin.getName());
        }
        return false;
    }

    /**
     * 删除重载插件
     *
     * @param sender
     *            - 命令发送者
     * @param main
     *            - 插件
     * @return 是否成功
     */
    public boolean reload(final CommandSender sender, final String name) {
        if (name != null) {
            return unload(sender, name) && load(sender, name);
        }
        return false;
    }

    /**
     * 重载插件
     *
     * @param plugin
     *            - 插件
     * @return 是否成功
     */
    public boolean reload(final Plugin plugin) {
        return reload(Bukkit.getConsoleSender(), plugin);
    }

    /**
     * 重载所有插件
     */
    public void reloadAll() {
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (!isIgnored(plugin)) {
                unload(plugin);
            }
        }
        Bukkit.getPluginManager().loadPlugins(Bukkit.getUpdateFolderFile().getParentFile());
    }

    /**
     * 重载所有插件
     */
    public void reloadAll(final CommandSender sender) {
        final Plugin[] plist = Bukkit.getPluginManager().getPlugins();
        for (final Plugin plugin : plist) {
            if (!isIgnored(plugin)) {
                unload(sender, plugin);
            }
        }
        for (final Plugin plugin : plist) {
            if (!isIgnored(plugin)) {
                load(sender, plugin.getName());
            }
        }
    }

    /**
     * 从忽略列表移除
     *
     * @param name
     *            - 插件名称
     * @return 是否成功
     */
    public boolean removeIgnore(final String name) {
        return ignoreList.remove(name);
    }

    /**
     * 卸载插件
     *
     * @param sender
     *            - 命令发送者
     * @param plugin
     *            - 插件
     * @return 是否成功
     */
    public boolean unload(final CommandSender sender, final Plugin plugin) {
        return unload(sender, plugin.getName());
    }

    /**
     * 卸载插件
     *
     * @param sender
     *            - 命令发送者
     * @param name
     *            - 插件名称
     * @return 是否成功
     */
    @SuppressWarnings("unchecked")
    public boolean unload(CommandSender sender, final String name) {
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }
        final PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = null;
        List<Plugin> plugins = null;
        Map<String, Plugin> lookupNames = null;
        Map<String, Command> knownCommands = null;
        final Map<Pattern, JavaPluginLoader> fileAssociations = null;
        if (pluginManager == null) {
            sender.sendMessage("§4异常: §c插件管理类反射获取失败!");
            return false;
        }
        try {
            final Field pluginsField = pluginManager.getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            plugins = (List<Plugin>) pluginsField.get(pluginManager);

            final Field lookupNamesField = pluginManager.getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            lookupNames = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            final Field commandMapField = pluginManager.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            final Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            // XXX 暂时用不到
            // final Field fileAssociationsField = pluginManager.getClass().getDeclaredField("fileAssociations");
            // fileAssociationsField.setAccessible(true);
            // fileAssociations = (Map<Pattern, JavaPluginLoader>) fileAssociationsField.get(pluginManager);

        } catch (final Exception e) {
            sender.sendMessage("§4异常: §c" + e.getMessage() + " 插件 §b" + name + " §c卸载失败!");
            return false;
        }
        String pluginVersion = "";
        for (final Plugin next : pluginManager.getPlugins()) {
            if (next.getName().equals(name)) {
                pluginManager.disablePlugin(next);
                if ((plugins != null) && (plugins.contains(next))) {
                    pluginVersion = getVersion(next);
                    plugins.remove(next);
                    sender.sendMessage("§6卸载: §a从服务器插件列表删除 §b" + name + " §a的实例!");
                }

                if ((lookupNames != null) && (lookupNames.containsKey(name))) {
                    lookupNames.remove(name);
                    sender.sendMessage("§6卸载: §a从插件查找列表删除 §b" + name + " §a的实例!");
                }

                for (final Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry<String, Command> entry = it.next();
                    if ((entry.getValue() instanceof PluginCommand)) {
                        final PluginCommand command = (PluginCommand) entry.getValue();
                        if (command.getPlugin() == next) {
                            command.unregister(commandMap);
                            it.remove();
                        }
                    }
                }
                // try {
                // if (fileAssociations != null) {
                // // XXX 不能移除 会导致无法加载
                // for (final Iterator<Entry<Pattern, JavaPluginLoader>> filter = fileAssociations.entrySet().iterator(); filter.hasNext();) {
                // final Entry<Pattern, JavaPluginLoader> entry = filter.next();
                // final Matcher match = entry.getKey().matcher(getPluginFile(next).getName());
                // if (match.find()) {
                // final JavaPluginLoader pluginLoader = entry.getValue();
                // final Field loadersField = pluginLoader.getClass().getDeclaredField("loaders");
                // loadersField.setAccessible(true);
                // final Map<String, URLClassLoader> loaders = (Map<String, URLClassLoader>) loadersField.get(pluginLoader);
                // // XXX 不能移除 会导致无法调用其他插件
                // loaders.clear();
                // sender.sendMessage("§6卸载: §a移除插件 §b" + name + " §a的类实例缓存!");
                // }
                // }
                // }
                // } catch (final Exception e) {
                // e.printStackTrace();
                // }
                sender.sendMessage("§6卸载: §a注销插件 §b" + name + " §a的所有命令!");
                final ClassLoader cl = next.getClass().getClassLoader();
                try {
                    ((URLClassLoader) cl).close();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
                System.gc();
            }
        }
        if (!pluginVersion.isEmpty())

        {
            sender.sendMessage("§6卸载: §a插件 §b" + name + " §a版本 §d" + pluginVersion + " §a已成功卸载!");
            return true;
        }
        return false;
    }

    /**
     * 卸载插件
     *
     * @param plugin
     *            - 卸载插件
     * @return 是否成功
     */
    public boolean unload(final Plugin plugin) {
        return unload(Bukkit.getConsoleSender(), plugin);
    }

    /**
     * 重载update文件夹的插件
     *
     * @return 是否成功
     */
    public boolean upgrade(final CommandSender sender) {
        sender.sendMessage("§6升级: §a开始升级 服务器更新 目录下的所有插件!");
        return upgrade(sender, null, null);
    }

    /**
     * 重载update文件夹的插件
     *
     * @param sender
     *            - 命令发送者
     * @param directory
     *            - 更新目录
     * @return 是否成功
     */
    public boolean upgrade(final CommandSender sender, final File directory, final Plugin plugin) {
        boolean result = false;
        final PluginLoader loader = main.getPluginLoader();
        File updateDirectory;
        if (directory == null || !directory.isDirectory()) {
            updateDirectory = Bukkit.getServer().getUpdateFolderFile();
        } else {
            updateDirectory = directory;
        }
        if (updateDirectory == null) {
            sender.sendMessage("§4异常: §c文件夹 §d服务器更新文件夹 §c未找到或IO错误!");
            return false;
        }
        try {
            sender.sendMessage("§6升级: §b从 §d" + updateDirectory.getCanonicalPath() + " §b文件夹检索插件插件!");
        } catch (SecurityException | IOException e1) {
            sender.sendMessage("§4异常: §c文件夹 §d" + updateDirectory.getName() + " §c权限不足或IO错误!");
            return false;
        }
        final File[] plugins = updateDirectory.listFiles();
        if (plugins.length == 0) {
            sender.sendMessage("§6升级: §d更新文件夹未找到插件!");
            return false;
        }
        for (final File file : updateDirectory.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            PluginDescriptionFile description = null;
            try {
                description = loader.getPluginDescription(file);
                final String name = description.getName();
                if (plugin != null && !name.equals(plugin.getName())) {
                    continue;
                }
                final Plugin oldplugin = Bukkit.getPluginManager().getPlugin(name);
                result = true;
                File dest = null;
                if (!unload(sender, name)) {
                    sender.sendMessage("§6升级: §d开始安装 §b" + name + " §d插件!");
                    dest = new File(Bukkit.getUpdateFolderFile().getParentFile(), File.separatorChar + file.getName());
                } else {
                    if (oldplugin != null) {
                        dest = new File(Bukkit.getUpdateFolderFile(), File.separatorChar + getPluginFile(oldplugin).getName());
                    }
                    sender.sendMessage("§6升级: §a开始升级 §b" + name + " §a插件!");
                }
                if (dest != null) {
                    file.renameTo(dest);
                }
                load(sender, name);
            } catch (final InvalidDescriptionException e) {
                sender.sendMessage("§4异常: §c" + e.getMessage());
                sender.sendMessage("§4文件: §c" + file.getName() + " 的plugin.yml文件存在错误!");
            }
            if (file.exists()) {
                file.delete();
            }
        }
        sender.sendMessage("§6升级: §a所有插件升级完毕!");
        return result;
    }

    /**
     * 重载update文件夹的插件
     *
     * @return 是否成功
     */
    public boolean upgrade(final CommandSender sender, final Plugin plugin) {
        return upgrade(sender, null, plugin);
    }

    /**
     * 重载update文件夹的插件
     *
     * @return 是否成功
     */
    public boolean upgrade(final File directory) {
        Bukkit.getConsoleSender().sendMessage("§6升级: §a开始升级 §d" + directory.getName() + " §a目录下的所有插件!");
        return upgrade(Bukkit.getConsoleSender(), directory, null);
    }
}
