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
import cn.citycraft.Yum.api.YumAPI;

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
		super();
		this.main = main;
		setMinimumArguments(1);
		setDescription("插件源命令");
		setPossibleArguments("<add|del|all|clean|list> <仓库名称>");
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
						if (YumAPI.repo.addRepositories(sender, args[1])) {
							final String reponame = YumAPI.repo.getRepoCache(args[1]).name;
							sender.sendMessage("§6仓库: §a源仓库 §e" + reponame + " §a的插件信息已缓存!");
						} else {
							sender.sendMessage("§6仓库: §c源地址未找到仓库信息或当前地址已缓存!");
						}
					} else {
						sender.sendMessage("§6仓库: §c请输入需要添加的源地址!");
					}
					break;
				case "del":
					if (args.length == 2) {
						if (YumAPI.repo.delRepositories(sender, args[1])) {
							final String reponame = YumAPI.repo.getRepoCache(args[1]).name;
							sender.sendMessage("§6仓库: §a源仓库 §e" + reponame + " §c已删除 §a请使用 §b/yum repo update §a更新缓存!");
						} else {
							sender.sendMessage("§6仓库: §c源地址未找到!");
						}
					} else {
						sender.sendMessage("§6仓库: §c请输入需要删除的源地址!");
					}
					break;
				case "list":
					sender.sendMessage("§6仓库: §b缓存的插件信息如下 ");
					StringUtil.sendStringArray(sender, YumAPI.repo.getAllPluginsInfo());
					break;
				case "all":
					sender.sendMessage("§6仓库: §b缓存的仓库信息如下 ");
					StringUtil.sendStringArray(sender, YumAPI.repo.getRepoCache().getAllRepoInfo());
					break;
				case "clean":
					YumAPI.repo.clean();
					sender.sendMessage("§6仓库: §a缓存的插件信息已清理!");
					break;
				case "update":
					YumAPI.repo.updateRepositories(sender);
					sender.sendMessage("§6仓库: §a仓库缓存数据已更新!");
					break;
				}
			}
		});
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args[0].equalsIgnoreCase("repo")) {
			if (args.length == 2) {
				return StringUtil.copyPartialMatches(args[1], Arrays.asList(new String[] { "add", "all", "list", "clean", "update", "del" }), new ArrayList<String>());
			}
			if (args.length == 3 && (args[1] == "add" || args[1] == "del")) {
				return StringUtil.copyPartialMatches(args[2], YumAPI.repo.getRepos().keySet(), new ArrayList<String>());
			}
		}
		return null;
	}
}
