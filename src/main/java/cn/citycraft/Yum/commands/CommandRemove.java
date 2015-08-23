/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.utils.PluginsManager;

/**
 * 插件删除命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandRemove extends BaseCommand {
	Yum yum;

	/**
	 * @param name
	 */
	public CommandRemove(Yum main) {
		super("remove");
		this.yum = main;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		String pluginname = args[0];
		Plugin plugin = yum.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin != null) {
			PluginsManager.unload(sender, plugin);
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
