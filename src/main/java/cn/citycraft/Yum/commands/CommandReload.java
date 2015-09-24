/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.manager.YumManager;

/**
 * 插件删除命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandReload extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandReload(final Yum main) {
		super("reload", "重载插件");
		this.main = main;
		setMinimumArguments(1);
		setPossibleArguments("<插件名称|all|*>");
	}

	@Override
	public void execute(final CommandSender sender, final String label, final String[] args) throws CommandException {
		final String pluginname = args[0];
		if (pluginname.equalsIgnoreCase("all") || pluginname.equalsIgnoreCase("*")) {
			YumManager.plugman.reloadAll(sender);
			return;
		}
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin != null) {
			YumManager.plugman.reload(sender, plugin);
		} else {
			sender.sendMessage("§c插件 " + pluginname + " 不存在或已卸载!");
		}
	};
}
