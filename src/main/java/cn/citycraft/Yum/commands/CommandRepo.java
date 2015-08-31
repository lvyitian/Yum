/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import cn.citycraft.Yum.Yum;

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
	public CommandRepo(Yum main) {
		super("delete");
		this.main = main;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		String cmd = args[0];
		switch (cmd) {
		case "add":
			if (args.length == 2) {
				main.repo.addRepositories(args[1]);
			}
		case "list":
		case "clean":
			main.repo.clean();
		}
	};

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public String getPossibleArguments() {
		return "<add|del|clean|list> <仓库名称>";
	}

	@Override
	public boolean isOnlyPlayerExecutable() {
		return false;
	}
}
