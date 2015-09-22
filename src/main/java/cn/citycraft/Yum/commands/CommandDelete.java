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
public class CommandDelete extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandDelete(Yum main) {
		super("delete");
		this.main = main;
		setDescription("删除插件");
		setMinimumArguments(1);
		setPossibleArguments("<插件名称>");
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		String pluginname = args[0];
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
		if (plugin != null)
			if (YumManager.plugman.deletePlugin(sender, plugin))
				sender.sendMessage("§c删除: §a插件 " + pluginname + " 已从服务器卸载并删除!");
			else
				sender.sendMessage("§c删除: §c插件 " + pluginname + " 卸载或删除时发生错误 删除失败!");
		else
			sender.sendMessage("§c插件 " + pluginname + " 不存在或已卸载!");
	};
}
