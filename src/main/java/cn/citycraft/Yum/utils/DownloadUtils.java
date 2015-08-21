/**
 * 
 */
package cn.citycraft.Yum.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import org.bukkit.Bukkit;

/**
 * @author 蒋天蓓
 *         2015年8月21日下午6:08:09
 *         TODO
 */
public class DownloadUtils {
	public static boolean download(String url, String dir, String filename) {

		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			URL fileUrl = new URL(url);
			System.out.println("下载地址: " + url);
			int fileLength = fileUrl.openConnection().getContentLength();
			System.out.println("文件长度: " + fileLength);
			in = new BufferedInputStream(fileUrl.openStream());
			File file = new File(dir, filename + ".jar");
			if (!file.exists()) {
				file.createNewFile();
			}
			fout = new FileOutputStream(file);

			byte[] data = new byte[1024];

			// long downloaded = 0L;
			int count;
			while ((count = in.read(data)) != -1) {
				// downloaded += count;
				fout.write(data, 0, count);
				// int percent = (int) (downloaded / fileLength);
			}
			return true;
		} catch (Exception ex) {
			Bukkit.getLogger().log(Level.WARNING, "The auto-updater tried to download a new update, but was unsuccessful.", ex);
			return false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				Bukkit.getLogger().log(Level.SEVERE, null, ex);
			}
			try {
				if (fout != null) {
					fout.close();
				}
			} catch (IOException ex) {
				Bukkit.getLogger().log(Level.SEVERE, null, ex);
			}
		}
	}
}
