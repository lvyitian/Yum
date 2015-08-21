/**
 *
 */
package cn.citycraft.Yum.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import com.google.common.base.Joiner;

/**
 * 插件管理类
 *
 * @author 蒋天蓓 2015年8月21日下午7:03:26
 */
public class PluginUtil {

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
			return "§c没有注册命令!";
		return Joiner.on(", ").join(parsedCommands);
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

	private static String load(Plugin plugin) {
		return load(plugin.getName());
	}

	public static String load(String name) {
		Plugin target = null;

		File pluginDir = new File("plugins");

		if (!pluginDir.isDirectory()) {
			// TODO 提示
		}
		File pluginFile = new File(pluginDir, name + ".jar");

		if (!pluginFile.isFile())
			return "§c在plugins目录未找到 " + name + " 插件 请确认文件是否存在!";
		try {
			target = Bukkit.getPluginManager().loadPlugin(pluginFile);
		} catch (InvalidDescriptionException e) {
			e.printStackTrace();
			return "§c插件: " + name + " 的plugin.yml文件存在错误!";
		} catch (InvalidPluginException e) {
			e.printStackTrace();
			return "§c文件: " + name + " 不是一个可载入的插件!";
		}

		target.onLoad();
		Bukkit.getPluginManager().enablePlugin(target);

		return "§a插件 " + name + " 已成功载入到服务器!";
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
	public static String unload(Plugin plugin) {
		String name = plugin.getName();

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
				e.printStackTrace();
				return "§c插件 " + name + " 卸载失败!";
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return "§c插件 " + name + " 卸载失败!";
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
				Logger.getLogger(PluginUtil.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		System.gc();
		return "§a插件 " + name + " 已成功卸载!";
	}
}
