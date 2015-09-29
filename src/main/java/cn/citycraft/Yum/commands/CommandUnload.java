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
import cn.citycraft.Yum.manager.YumManager;

/**
 * 插件删除命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandUnload extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandUnload(final Yum main) {
		super("unload");
		this.main = main;
		setMinimumArguments(1);
		setDescription("卸载插件");
		setPossibleArguments("<插件名称>");
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		final String pluginname = args[0];
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin != null) {
			YumManager.plugman.unload(sender, plugin);
		} else {
			sender.sendMessage("§c插件 " + pluginname + " 不存在或已卸载!");
		}
	};
}
