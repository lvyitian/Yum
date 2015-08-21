/**
 * 
 */
package cn.citycraft.Yum.utils;

/**
 * 字符串工具
 * 
 * @author 蒋天蓓
 *         2015年8月21日下午7:05:51
 */
public class StringUtil {

	/**
	 * 转移数组
	 * 
	 * @param args
	 *            - 原数组
	 * @param start
	 *            - 数组开始位置
	 * @return 转移后的数组
	 */
	public static String consolidateStrings(String[] args, int start) {
		String ret = args[start];
		if (args.length > start + 1) {
			for (int i = start + 1; i < args.length; i++)
				ret = ret + " " + args[i];
		}
		return ret;
	}
}