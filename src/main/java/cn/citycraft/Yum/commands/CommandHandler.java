/**
 *
 */
package cn.citycraft.Yum.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.manager.YumManager;

/**
 * 子命令处理类
 *
 * @author 蒋天蓓 2015年8月22日上午8:29:44
 */
public class CommandHandler implements CommandExecutor, TabCompleter {
	/**
	 * 转移数组
	 *
	 * @param args
	 *            - 原数组
	 * @param start
	 *            - 数组开始位置
	 * @return 转移后的数组字符串
	 */
	public static String[] moveStrings(String[] args, int start) {
		String[] ret = new String[args.length - start];
		System.arraycopy(args, start, ret, 0, ret.length);
		return ret;
	}

	/**
	 * 已注册命令列表(包括别名)
	 */
	List<String> RegisterCommandList = new ArrayList<String>();;
	/**
	 * 命令监听类列表
	 */
	private List<BaseCommand> commandlist = new ArrayList<BaseCommand>();

	/**
	 * 插件主类
	 */
	Yum main;

	/**
	 * 注册子命令
	 *
	 * @param yum
	 *            - 插件主类
	 */
	public CommandHandler(Yum yum) {
		this.main = yum;
		registerCommand(new CommandList(yum));
		registerCommand(new CommandInstall(yum));
		registerCommand(new CommandUpdate(yum));
		registerCommand(new CommandDelete(yum));
		registerCommand(new CommandInfo(yum));
		registerCommand(new CommandRepo(yum));
		registerCommand(new CommandReload(yum));
		registerCommand(new CommandLoad(yum));
		registerCommand(new CommandUnload(yum));
		registerCommand(new CommandUpgrade(yum));

		RegisterCommandList = getRegisterCommands();
	}

	/**
	 * 获得已注册的命令列表
	 *
	 * @return - 返回已注册的命令List
	 */
	public List<String> getRegisterCommands() {
		List<String> cmds = new ArrayList<String>();
		for (BaseCommand command : commandlist)
			cmds.addAll(command.getCommandList());
		return cmds;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0)
			return true;
		String subcmd = args[0];
		String[] subargs = moveStrings(args, 1);
		for (BaseCommand command : commandlist)
			if (command.isValidTrigger(subcmd)) {
				if (!command.hasPermission(sender)) {
					sender.sendMessage("你没有此命令的权限!");
					return true;
				}
				if (command.isOnlyPlayerExecutable() && !(sender instanceof Player)) {
					sender.sendMessage("控制台无法使用此命令!");
					return true;
				}
				if (subargs.length >= command.getMinimumArguments())
					try {
						command.execute(sender, subcmd, subargs);
						return true;
					} catch (CommandException e) {
						sender.sendMessage(e.getMessage());
					}
				else
					sender.sendMessage("错误的参数 /yum " + command.getName() + command.getPossibleArguments());
			}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (sender.isOp() || sender.hasPermission("yum.admin") || sender.hasPermission("yum." + args[0])) {
			List<String> completions = new ArrayList<>();
			if (args.length == 1) {
				String partialCommand = args[0];
				List<String> commands = RegisterCommandList;
				StringUtil.copyPartialMatches(partialCommand, commands, completions);
			}
			if (args.length == 2) {
				String partialPlugin = args[1];
				List<String> plugins = null;
				if (args[0].equalsIgnoreCase("install"))
					plugins = YumManager.repo.getAllPluginName();
				else if (args[0].equalsIgnoreCase("repo"))
					plugins = Arrays.asList(new String[] {	"add",
															"list",
															"clean",
															"update" });
				else
					plugins = YumManager.plugman.getPluginNames(false);
				StringUtil.copyPartialMatches(partialPlugin, plugins, completions);
			}
			Collections.sort(completions);
			return completions;
		}
		return null;
	}

	/**
	 * 注册命令
	 *
	 * @param command
	 *            - 被注册的命令类
	 */
	public void registerCommand(BaseCommand command) {
		commandlist.add(command);
	}

}
