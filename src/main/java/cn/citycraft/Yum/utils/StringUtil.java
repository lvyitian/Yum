/**
 * 
 */
package cn.citycraft.Yum.utils;

import java.util.Collection;

import org.bukkit.command.CommandSender;

/**
 * @author 蒋天蓓
 *         2015年8月22日下午12:41:59
 *         TODO
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

	public static void sendStringArray(CommandSender player, Collection<String> msg) {
		for (String string : msg) {
			player.sendMessage("§6 - §3" + string);
		}
	}
}
