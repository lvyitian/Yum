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
	public CommandReload(Yum main) {
		super("reload");
		this.main = main;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		String pluginname = args[0];
		if (pluginname.equalsIgnoreCase("all") || pluginname.equalsIgnoreCase("*")) {
			YumManager.plugman.reloadAll(sender);
			return;
		}
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin != null)
			YumManager.plugman.reload(sender, plugin);
		else
			sender.sendMessage("§c插件 " + pluginname + " 不存在或已卸载!");
	};

	@Override
	public String getDescription() {
		return "重载插件";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public String getPossibleArguments() {
		return "<插件名称|all|*>";
	}

	@Override
	public boolean isOnlyPlayerExecutable() {
		return false;
	}
}
