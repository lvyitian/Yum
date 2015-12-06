/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.api.YumAPI;

/**
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandUpdateAll extends BaseCommand {
	Yum main;

	public CommandUpdateAll(final Yum main) {
		super("ua");
		this.main = main;
		setDescription("更新所有可更新插件");
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		sender.sendMessage("§d开始更新服务器可更新插件");
		Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
			@Override
			public void run() {
				YumAPI.updateall(sender);
			}
		});
	};
}
