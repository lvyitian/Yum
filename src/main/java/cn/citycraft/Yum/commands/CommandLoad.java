/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.Yum.Yum;

/**
 * 插件删除命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandLoad extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandLoad(Yum main) {
		super("load");
		this.main = main;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		String pluginname = args[0];
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin != null) {
			main.plugman.load(sender, pluginname);
		} else {
			sender.sendMessage("§c插件 " + pluginname + " 不存在或已卸载!");
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
