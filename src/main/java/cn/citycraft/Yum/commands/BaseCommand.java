/**
 *
 */
package cn.citycraft.Yum.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

/**
 * 基础命令类
 *
 * @author 蒋天蓓
 *         2015年8月12日下午12:49:34
 */
public abstract class BaseCommand {
	private String name;
	private String permission;
	private String[] aliases;
	private String description;
	private int minimumArguments = 0;
	private String possibleArguments = "";
	private boolean onlyPlayerExecutable = false;

	public BaseCommand(String name, String description) {
		this(name, description, new String[0]);
	}

	public BaseCommand(String name, String description, String... aliases) {
		this.name = name;
		this.description = description;
		this.aliases = aliases;
	}

	/**
	 * 执行命令参数
	 *
	 * @param sender
	 *            - 命令发送者
	 * @param label
	 *            - 命令
	 * @param args
	 *            - 命令附加参数
	 * @throws CommandException
	 *             - 命令异常
	 */
	public abstract void execute(CommandSender sender, String label, String[] args) throws CommandException;

	/**
	 * 获得当前命令的别名
	 * 
	 * @return 当前命令的别名
	 */
	public List<String> getCommandList() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(name);
		cmds.addAll(Arrays.asList(aliases));
		return cmds;
	}

	/**
	 * 获得命令描述
	 *
	 * @return 命令描述
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 获得最小参数个数
	 *
	 * @return 最小参数个数
	 */
	public int getMinimumArguments() {
		return minimumArguments;
	}

	/**
	 * 获取命令名称
	 *
	 * @return 命令名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 获得命令权限
	 *
	 * @return 目录命令权限
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * 获得可能的参数
	 *
	 * @return
	 */
	public String getPossibleArguments() {
		return possibleArguments;
	}

	/**
	 * 检查Sender权限
	 *
	 * @param sender
	 *            - 命令发送者
	 * @return 是否有权限执行命令
	 */
	public final boolean hasPermission(CommandSender sender) {
		if (permission == null)
			return true;
		return sender.hasPermission(permission);
	}

	/**
	 * 是否只有玩家才能执行此命令
	 *
	 * @return 是否为玩家命令
	 */
	public boolean isOnlyPlayerExecutable() {
		return onlyPlayerExecutable;
	}

	/**
	 * 命令匹配检测
	 *
	 * @param name
	 *            - 命令
	 * @return 是否匹配
	 */
	public final boolean isValidTrigger(String name) {
		if (this.name.equalsIgnoreCase(name))
			return true;
		if (aliases != null)
			for (String alias : aliases)
				if (alias.equalsIgnoreCase(name))
					return true;
		return false;
	}

	/**
	 * 设置命令的最小参数个数
	 *
	 * @param minimumArguments
	 *            - 最小参数个数
	 */
	public void setMinimumArguments(int minimumArguments) {
		this.minimumArguments = minimumArguments;
	}

	/**
	 * 设置是否只允许玩家执行
	 *
	 * @param onlyPlayerExecutable
	 *            - 是否只允许玩家执行
	 */
	public void setOnlyPlayerExecutable(boolean onlyPlayerExecutable) {
		this.onlyPlayerExecutable = onlyPlayerExecutable;
	}

	/**
	 * 设置命令权限
	 *
	 * @param permission
	 *            - 命令权限
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}

	/**
	 * 设置可能的命令参数
	 *
	 * @param possibleArguments
	 *            - 可能的命令参数
	 */
	public void setPossibleArguments(String possibleArguments) {
		this.possibleArguments = possibleArguments;
	}

}
