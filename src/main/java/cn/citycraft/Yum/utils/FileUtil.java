/**
 * 
 */
package cn.citycraft.Yum.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 文件处理工具
 * 
 * @author 蒋天蓓
 *         2015年8月31日上午9:09:54
 */
public class FileUtil {
	/**
	 * 复制文件
	 * 
	 * @param src
	 *            - 源文件
	 * @param des
	 *            - 目标文件
	 * @return 是否成功
	 */
	public static boolean copyFile(File src, File des) {
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			int byteread = 0;
			if (!src.exists())
				return false;
			inStream = new FileInputStream(src); // 读入原文件
			fs = new FileOutputStream(des);
			byte[] buffer = new byte[1024];
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread); // 写入到目标文件
			}
			inStream.close();
			fs.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
