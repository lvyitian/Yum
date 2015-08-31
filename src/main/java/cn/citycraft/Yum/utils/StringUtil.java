/**
 * 
 */
package cn.citycraft.Yum.utils;

import java.util.Collection;

import org.bukkit.command.CommandSender;

/**
 * 字符串工具
 * 
 * @author 蒋天蓓
 *         2015年8月22日下午12:41:59
 */
public class StringUtil {
	/**
	 * 转移数组后获取字符串
	 * 
	 * @param args
	 *            - 原数组
	 * @param start
	 *            - 数组开始位置
	 * @return 转移后的数组字符串
	 */
	public static String consolidateStrings(String[] args, int start) {
		String ret = args[start];
		if (args.length > start + 1) {
			for (int i = start + 1; i < args.length; i++)
				ret = ret + " " + args[i];
		}
		return ret;
	}

	/**
	 * 给玩家或控制台发送消息组
	 * 
	 * @param sender
	 *            - 接收消息的玩家
	 * @param msg
	 *            - 消息组
	 */
	public static void sendStringArray(CommandSender sender, Collection<String> msg) {
		for (String string : msg) {
			sender.sendMessage("§6 - §3" + string);
		}
	}
}
