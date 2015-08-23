/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.utils.PluginsManager;

/**
 * 插件安装命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandReinstall extends BaseCommand {
	Yum yum;

	/**
	 * @param name
	 */
	public CommandReinstall(Yum main) {
		super("reinstall");
		this.yum = main;
	}

	@Override
	public void execute(final CommandSender sender, String label, String[] args) throws CommandException {
		final String pluginname = args[0];
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		sender.sendMessage("§a开始重装插件: " + pluginname);
		if (plugin != null) {
			Bukkit.getScheduler().runTaskAsynchronously(yum, new Runnable() {
				@Override
				public void run() {
					if (yum.download.yum(sender, plugin.getName())) {
						PluginsManager.deletePlugin(plugin);
						PluginsManager.installFromYum(sender, plugin.getName());
					}
				}
			});
		} else {
			sender.sendMessage("§c插件未安装或已卸载 需要安装请使用yum install " + pluginname + "!");
		}
	};

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public String getPossibleArguments() {
		return "<插件名称>";
	}

	@Override
	public boolean isOnlyPlayerExecutable() {
		return false;
	}
}
