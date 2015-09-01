/**
 *
 */
package cn.citycraft.Yum.manager;

import java.io.File;
import java.io.IOException;
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
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;

import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.utils.StringUtil;

import com.google.common.base.Joiner;

/**
 * 插件管理类
 *
 * @author 蒋天蓓 2015年8月21日下午7:03:26
 */
public class PluginsManager {
	Yum main;

	public PluginsManager(Yum yum) {
		this.main = yum;
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
	public boolean deletePlugin(CommandSender sender, Plugin plugin) {
		return unload(sender, plugin) && getPluginFile(plugin).delete();
	}

	/**
	 * 删除插件
	 * 
	 * @param plugin
	 *            - 插件
	 * @return 是否成功
	 */
	public boolean deletePlugin(Plugin plugin) {
		return deletePlugin(Bukkit.getConsoleSender(), plugin);
	}

	/**
	 * 关闭插件
	 * 
	 * @param plugin
	 *            - 插件
	 */
	public void disable(Plugin plugin) {
		if ((plugin.isEnabled()) && (plugin != null)) {
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}

	/**
	 * 关闭所有插件
	 */
	public void disableAll() {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
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
	public void enable(Plugin plugin) {
		if ((!plugin.isEnabled()) && (plugin != null)) {
			Bukkit.getPluginManager().enablePlugin(plugin);
		}
	}

	/**
	 * 启用所有插件
	 */
	public void enableAll() {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (!isIgnored(plugin)) {
				enable(plugin);
			}
		}
	}

	/**
	 * 获得格式化的插件名称
	 * 
	 * @param plugin
	 *            - 插件
	 * @return 格式化的插件名称
	 */
	public String getFormattedName(Plugin plugin) {
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
	public String getFormattedName(Plugin plugin, boolean includeVersions) {
		ChatColor color = plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
		String pluginName = color + plugin.getName();
		if (includeVersions) {
			pluginName = pluginName + " (" + plugin.getDescription().getVersion() + ")";
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
	public Plugin getPluginByName(String name) {
		return Bukkit.getPluginManager().getPlugin(name);
	}

	/**
	 * 通过名称获得插件(处理带空格的插件)
	 * 
	 * @param name
	 *            - 名称
	 * @return 插件
	 */
	public Plugin getPluginByName(String[] args, int start) {
		return getPluginByName(StringUtil.consolidateStrings(args, start));
	}

	/**
	 * 获得插件绝对路径
	 * 
	 * @param plugin
	 *            - 插件
	 * @return 插件的绝对路径
	 */
	public File getPluginFile(Plugin plugin) {
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

	public List<String> getPluginNames(boolean fullName) {
		List<String> plugins = new ArrayList<String>();
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
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
	public String getPluginVersion(String name) {
		Plugin plugin = getPluginByName(name);
		if ((plugin != null) && (plugin.getDescription() != null))
			return plugin.getDescription().getVersion();
		return null;
	}

	/**
	 * 获得插件命令
	 * 
	 * @param plugin
	 *            - 插件
	 * @return 插件命令
	 */
	public String getUsages(Plugin plugin) {
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

	/**
	 * 判断插件是否在忽略列表
	 * 
	 * @param plugin
	 *            - 插件
	 * @return 是否
	 */
	public boolean isIgnored(Plugin plugin) {
		return isIgnored(plugin.getName());
	}

	/**
	 * 判断插件是否在忽略列表
	 * 
	 * @param plugin
	 *            - 插件名称
	 * @return 是否
	 */
	public boolean isIgnored(String plugin) {
		for (String name : new ArrayList<String>()) {
			if (name.equalsIgnoreCase(plugin))
				return true;
		}
		return false;
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
	public boolean load(CommandSender sender, String name) {
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
			pluginFile = null;
			for (File file : pluginDir.listFiles()) {
				if (file.getName().endsWith(".jar")) {
					try {
						PluginDescriptionFile desc = main.getPluginLoader().getPluginDescription(file);
						if (desc.getName().equalsIgnoreCase(name)) {
							pluginFile = file;
							break;
						}
					} catch (InvalidDescriptionException e) {
					}
				}
			}
			if (pluginFile == null) {
				sender.sendMessage("§c在插件目录和更新目录均未找到 " + name + " 插件 请确认文件是否存在!");
				return false;
			}
		}

		try {
			target = Bukkit.getPluginManager().loadPlugin(pluginFile);
		} catch (InvalidDescriptionException e) {
			sender.sendMessage("§4异常: §c" + e.getMessage() + " 插件: " + name + " 的plugin.yml文件存在错误!");
			return false;
		} catch (InvalidPluginException e) {
			sender.sendMessage("§4异常: §c" + e.getMessage() + " 文件: " + name + " 不是一个可载入的插件!");
			return false;
		} catch (UnknownDependencyException e) {
			sender.sendMessage("§4异常: §c" + e.getMessage() + " 插件: " + name + " 缺少部分依赖项目!");
			return false;
		}

		target.onLoad();
		Bukkit.getPluginManager().enablePlugin(target);
		sender.sendMessage("§6载入: §a插件 " + name + " 已成功载入到服务器!");
		return true;
	}

	public boolean load(String name) {
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
	public boolean reload(CommandSender sender, Plugin plugin) {
		if (plugin != null)
			return unload(sender, plugin) && load(sender, plugin.getName());
		return false;
	}

	/**
	 * 重载插件
	 * 
	 * @param plugin
	 *            - 插件
	 * @return 是否成功
	 */
	public boolean reload(Plugin plugin) {
		return reload(Bukkit.getConsoleSender(), plugin);
	}

	/**
	 * 重载所有插件
	 */
	public void reloadAll() {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (!isIgnored(plugin)) {
				reload(plugin);
			}
		}
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
	@SuppressWarnings("unchecked")
	public boolean unload(CommandSender sender, Plugin plugin) {
		String name = plugin.getName();
		if (sender == null) {
			sender = Bukkit.getConsoleSender();
		}
		PluginManager pluginManager = Bukkit.getPluginManager();
		SimpleCommandMap commandMap = null;
		List<Plugin> plugins = null;
		Map<String, Plugin> lookupNames = null;
		Map<String, Command> knownCommands = null;
		Map<Event, SortedSet<RegisteredListener>> listeners = null;
		boolean reloadlisteners = true;
		if (pluginManager != null) {
			try {
				Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
				pluginsField.setAccessible(true);
				plugins = (List<Plugin>) pluginsField.get(pluginManager);

				Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
				lookupNamesField.setAccessible(true);
				lookupNames = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

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
				knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
			} catch (Exception e) {
				sender.sendMessage("§4异常: §c" + e.getMessage() + " 插件 " + name + " 卸载失败!");
				return false;
			}
		}
		for (Plugin next : pluginManager.getPlugins()) {
			if (next.getName().equals(name)) {
				pluginManager.disablePlugin(next);
				if ((plugins != null) && (plugins.contains(next))) {
					plugins.remove(next);
				}

				if ((lookupNames != null) && (lookupNames.containsKey(name))) {
					lookupNames.remove(name);
				}

				if (commandMap != null) {
					for (Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator(); it.hasNext();) {
						Map.Entry<String, Command> entry = it.next();

						if ((entry.getValue() instanceof PluginCommand)) {
							PluginCommand command = (PluginCommand) entry.getValue();
							if (command.getPlugin() == next) {
								command.unregister(commandMap);
								it.remove();
								sender.sendMessage("§6卸载: §a插件: " + name + " 的命令!");
							}
						}
					}
				}
			}
		}
		if (listeners != null && reloadlisteners) {
			for (SortedSet<RegisteredListener> set : listeners.values()) {
				for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext();) {
					RegisteredListener value = it.next();
					if (value.getPlugin().getName().equals(name)) {
						it.remove();
						sender.sendMessage("§6卸载: §a插件: " + name + " 的监听器!");
					}
				}
			}
		}
		ClassLoader cl = plugin.getClass().getClassLoader();
		if ((cl instanceof URLClassLoader)) {
			try {
				((URLClassLoader) cl).close();
				sender.sendMessage("§6卸载: §a插件: " + name + " 的类加载器!");
			} catch (IOException ex) {
			}
		}
		sender.sendMessage("§6卸载: §a插件: " + name + " 已成功卸载!");
		return true;
	}

	/**
	 * 卸载插件
	 * 
	 * @param plugin
	 *            - 卸载插件
	 * @return 是否成功
	 */
	public boolean unload(Plugin plugin) {
		return unload(Bukkit.getConsoleSender(), plugin);
	}
}