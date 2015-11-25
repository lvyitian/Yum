/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.api.YumAPI;

/**
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandUpdate extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandUpdate(final Yum main) {
		super();
		this.main = main;
		setMinimumArguments(1);
		setDescription("更新插件");
		setPossibleArguments("<插件名称> [插件版本]");
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
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
			sender.sendMessage("§c插件" + pluginname + "未安装或已卸载 需要安装请使用/yum install " + pluginname + "!");
		}
	};
}
