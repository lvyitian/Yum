/**
 *
 */
package cn.citycraft.Yum.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;

import com.google.common.base.Joiner;

/**
 * 插件管理类
 *
 * @author 蒋天蓓 2015年8月21日下午7:03:26
 */
public class PluginsManager {

	public static boolean copyFile(File src, File des) {
		InputStream inStream = null; // 读入原文件
		FileOutputStream fs = null;
		try {
			int byteread = 0;
			if (!src.exists())
				return false;
			inStream = new FileInputStream(src); // 读入原文件
			fs = new FileOutputStream(des);
			byte[] buffer = new byte[1024];
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
			fs.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean deletePlugin(Plugin plugin) {
		unload(plugin);
		getPluginFile(plugin).delete();
		return true;
	}

	public static void disable(Plugin plugin) {
		if ((plugin.isEnabled()) && (plugin != null)) {
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}

	public static void disableAll() {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (!isIgnored(plugin)) {
				disable(plugin);
			}
		}
	}

	public static void enable(Plugin plugin) {
		if ((!plugin.isEnabled()) && (plugin != null)) {
			Bukkit.getPluginManager().enablePlugin(plugin);
		}
	}

	public static void enableAll() {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (!isIgnored(plugin)) {
				enable(plugin);
			}
		}
	}

	public static String getFormattedName(Plugin plugin) {
		return getFormattedName(plugin, false);
	}

	public static String getFormattedName(Plugin plugin, boolean includeVersions) {
		ChatColor color = plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
		String pluginName = color + plugin.getName();
		if (includeVersions) {
			pluginName = pluginName + " (" + plugin.getDescription().getVersion() + ")";
		}
		return pluginName;
	}

	public static Plugin getPluginByName(String name) {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (name.equalsIgnoreCase(plugin.getName()))
				return plugin;
		}
		return null;
	}

	public static Plugin getPluginByName(String[] args, int start) {
		return getPluginByName(StringUtil.consolidateStrings(args, start));
	}

	public static File getPluginFile(Plugin plugin) {
		File file = null;
		ClassLoader cl = plugin.getClass().getClassLoader();
		if ((cl instanceof URLClassLoader)) {
			@SuppressWarnings("resource")
			URLClassLoader ucl = (URLClassLoader) cl;
			URL url = ucl.getURLs()[0];
			file = new File(url.getFile());
		}
		return file;
	}

	public static List<String> getPluginNames(boolean fullName) {
		List<String> plugins = new ArrayList<String>();
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			plugins.add(fullName ? plugin.getDescription().getFullName() : plugin.getName());
		}
		return plugins;
	}

	public static String getPluginVersion(String name) {
		Plugin plugin = getPluginByName(name);
		if ((plugin != null) && (plugin.getDescription() != null))
			return plugin.getDescription().getVersion();
		return null;
	}

	public static String getUsages(Plugin plugin) {
		List<String> parsedCommands = new ArrayList<String>();

		Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();

		if (commands != null) {
			Iterator<Entry<String, Map<String, Object>>> commandsIt = commands.entrySet().iterator();
			while (commandsIt.hasNext()) {
				Entry<String, Map<String, Object>> thisEntry = commandsIt.next();
				if (thisEntry != null) {
					parsedCommands.add(thisEntry.getKey());
				}
			}
		}
		if (parsedCommands.isEmpty())
			return null;
		return Joiner.on(", ").join(parsedCommands);
	}

	public static boolean installFromYum(CommandSender sender, String filename) {
		if (sender == null) {
			sender = Bukkit.getConsoleSender();
		}
		File file = new File("plugins/YumCenter", filename + ".jar");
		if (!file.exists()) {
			sender.sendMessage("§c仓库不存在该插件!");
			return false;
		}
		File pluginfile = new File("plugins", filename + ".jar");
		copyFile(file, pluginfile);
		load(sender, filename + ".jar");
		return false;
	}

	public static boolean isIgnored(Plugin plugin) {
		return isIgnored(plugin.getName());
	}

	public static boolean isIgnored(String plugin) {
		for (String name : new ArrayList<String>()) {
			if (name.equalsIgnoreCase(plugin))
				return true;
		}
		return false;
	}

	public static boolean load(CommandSender sender, Plugin plugin) {
		String filename = getPluginFile(plugin).getName();
		return load(sender, filename);
	}

	public static boolean load(CommandSender sender, String name) {
		Plugin target = null;

		if (sender == null) {
			sender = Bukkit.getConsoleSender();
		}

		if (!name.endsWith(".jar")) {
			name = name + ".jar";
		}

		File pluginDir = new File("plugins");
		File updateDir = new File(pluginDir, "update");

		if (!pluginDir.isDirectory()) {
			sender.sendMessage("§c插件目录不存在或IO错误!");
			return false;
		}

		File pluginFile = new File(pluginDir, name);

		if (!pluginFile.isFile() && !new File(updateDir, name).isFile()) {
			sender.sendMessage("§c在插件目录和更新目录未找到 " + name + " 插件 请确认文件是否存在!");
			return false;
		}

		try {
			target = Bukkit.getPluginManager().loadPlugin(pluginFile);
		} catch (InvalidDescriptionException e) {
			sender.sendMessage("§c异常: " + e.getMessage() + " 插件: " + name + " 的plugin.yml文件存在错误!");
			return false;
		} catch (InvalidPluginException e) {
			sender.sendMessage("§c异常: " + e.getMessage() + " 文件: " + name + " 不是一个可载入的插件!");
			return false;
		} catch (UnknownDependencyException e) {
			sender.sendMessage("§c异常: " + e.getMessage() + " 插件: " + name + " 缺少部分依赖!");
			return false;
		}

		target.onLoad();
		Bukkit.getPluginManager().enablePlugin(target);

		return true;
		// "§a插件: " + name + " 已成功载入到服务器!";
	}

	public static boolean load(Plugin plugin) {
		return load(null, plugin);
	}

	public static boolean load(String name) {
		return load(null, name);
	}

	public static void reload(Plugin plugin) {
		if (plugin != null) {
			unload(plugin);
			load(plugin);
		}
	}

	public static void reloadAll() {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (!isIgnored(plugin)) {
				reload(plugin);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean unload(CommandSender sender, Plugin plugin) {
		String name = plugin.getName();
		if (sender == null) {
			sender = Bukkit.getConsoleSender();
		}
		PluginManager pluginManager = Bukkit.getPluginManager();
		SimpleCommandMap commandMap = null;
		List<Plugin> plugins = null;
		Map<String, Plugin> names = null;
		Map<String, Command> commands = null;
		Map<Event, SortedSet<RegisteredListener>> listeners = null;
		boolean reloadlisteners = true;
		if (pluginManager != null) {
			try {
				Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
				pluginsField.setAccessible(true);
				plugins = (List<Plugin>) pluginsField.get(pluginManager);

				Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
				lookupNamesField.setAccessible(true);
				names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

				try {
					Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
					listenersField.setAccessible(true);
					listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
				} catch (Exception e) {
					reloadlisteners = false;
				}

				Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
				commandMapField.setAccessible(true);
				commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

				Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
				knownCommandsField.setAccessible(true);
				commands = (Map<String, Command>) knownCommandsField.get(commandMap);
			} catch (NoSuchFieldException e) {
				return false;
				// "§c异常: " + e.getMessage() + " 插件 " + name + " 卸载失败!";
			} catch (IllegalAccessException e) {
				return false;
				// "§c异常: " + e.getMessage() + " 插件 " + name + " 卸载失败!";
			}
		}
		pluginManager.disablePlugin(plugin);
		if (plugins != null && plugins.contains(plugin)) {
			plugins.remove(plugin);
		}
		if (names != null && names.containsKey(name)) {
			names.remove(name);
		}
		if (listeners != null && reloadlisteners) {
			for (SortedSet<RegisteredListener> set : listeners.values()) {
				for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext();) {
					RegisteredListener value = it.next();
					if (value.getPlugin() == plugin) {
						it.remove();
					}
				}
			}
		}
		if (commandMap != null) {
			for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Command> entry = it.next();
				if (entry.getValue() instanceof PluginCommand) {
					PluginCommand c = (PluginCommand) entry.getValue();
					if (c.getPlugin() == plugin) {
						c.unregister(commandMap);
						it.remove();
					}
				}
			}
		}
		ClassLoader cl = plugin.getClass().getClassLoader();
		if ((cl instanceof URLClassLoader)) {
			try {
				((URLClassLoader) cl).close();
			} catch (IOException ex) {
			}
		}
		System.gc();
		return true;
		// "§a插件: " + name + " 已成功卸载!";
	}

	public static boolean unload(Plugin plugin) {
		return unload(null, plugin);
	}
}
