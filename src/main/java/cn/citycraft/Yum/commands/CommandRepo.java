/**
 *
 */
package cn.citycraft.Yum.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.PluginHelper.utils.StringUtil;
import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.manager.YumManager;

/**
 * 插件删除命令类
 *
 * @author 蒋天蓓 2015年8月12日下午2:04:05
 */
public class CommandRepo extends BaseCommand {
	Yum main;

	/**
	 * @param name
	 */
	public CommandRepo(final Yum main) {
		super("repo");
		this.main = main;
		setMinimumArguments(1);
		setDescription("插件源命令");
		setPossibleArguments("<add|del|clean|list> <仓库名称>");
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
		main.getServer().getScheduler().runTaskAsynchronously(main, new Runnable() {
			@Override
			public void run() {
				final String cmd = args[0];
				switch (cmd) {
				case "add":
					if (args.length == 2) {
						if (YumManager.repo.addRepositories(sender, args[1])) {
							sender.sendMessage("§6仓库: §a插件信息已缓存!");
						} else {
							sender.sendMessage("§6仓库: §c源地址未找到仓库信息或无法访问!");
						}
					} else {
						sender.sendMessage("§6仓库: §c请输入源地址!");
					}
					break;
				case "list":
					sender.sendMessage("§6仓库: §b缓存的插件信息如下 ");
					StringUtil.sendStringArray(sender, YumManager.repo.getAllPluginsInfo());
					break;
				case "clean":
					YumManager.repo.clean();
					sender.sendMessage("§6仓库: §a缓存的插件信息已清理!");
					break;
				case "update":
					YumManager.repo.updateRepositories(sender);
					sender.sendMessage("§6仓库: §a仓库缓存数据已更新!");
					break;
				}
			}
		});
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args[0].equalsIgnoreCase("repo")) {
			return StringUtil.copyPartialMatches(args[1], Arrays.asList(new String[] {	"add",
																						"list",
																						"clean",
																						"update" }),
					new ArrayList<String>());
		}
		return null;
	}
}
