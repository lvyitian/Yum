package pw.yumc.Yum.managers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import pw.yumc.YumCore.callback.CallBack.One;

/**
 * 下载管理类
 *
 * @author 喵♂呜
 * @since 2015年8月21日下午6:08:09
 */
public class DownloadManager {
    Plugin plugin;

    public DownloadManager(Plugin main) {
        this.plugin = main;
    }

    /**
     * 从地址获得文件名称
     *
     * @param url
     *            - 地址
     * @return 文件名称
     */
    public String getFileName(String url) {
        int end = url.lastIndexOf('/');
        return url.substring(end + 1);
    }

    /**
     * 从地址获得文件名称
     *
     * @param url
     *            - 地址
     * @return 文件名称
     */
    public String getFileName(URL url) {
        return getFileName(url.getFile());
    }

    /**
     * 从网络下载文件
     *
     * @param sender
     *            - 命令发送者
     * @param urlstring
     *            - 下载地址
     * @return 是否成功
     */
    public boolean run(CommandSender sender, String urlstring) {
        return run(sender, urlstring, new File("plugins", getFileName(urlstring)), null);
    }

    /**
     * 从网络下载文件
     *
     * @param sender
     *            - 命令发送者
     * @param urlstring
     *            - 下载地址
     * @param file
     *            - 保存文件
     * @return 是否成功
     */
    public boolean run(CommandSender sender, String urlstring, File file) {
        try {
            URL url = new URL(urlstring);
            return run(sender, url, file);
        } catch (MalformedURLException e) {
            sender.sendMessage("§4错误: §c无法识别的URL地址...");
            sender.sendMessage("§4地址: §c" + urlstring);
            return false;
        }
    }

    /**
     * 从网络下载文件
     *
     * @param sender
     *            - 命令发送者
     * @param urlstring
     *            - 下载地址
     * @param file
     *            - 保存文件
     * @param callback
     *            -回调函数
     * @return 是否成功
     */
    public boolean run(CommandSender sender, String urlstring, File file, One<File> callback) {
        try {
            URL url = new URL(urlstring);
            return run(sender, url, file, callback);
        } catch (MalformedURLException e) {
            sender.sendMessage("§4错误: §c无法识别的URL地址...");
            sender.sendMessage("§4地址: §c" + urlstring);
            return false;
        }
    }

    /**
     * 从网络下载文件
     *
     * @param sender
     *            - 命令发送者
     * @param url
     *            - 下载地址
     * @param file
     *            - 保存文件
     * @return 是否成功
     */
    public boolean run(CommandSender sender, URL url, File file) {
        return run(sender, url, file, null);
    }

    /**
     * 从网络下载文件
     *
     * @param sender
     *            - 命令发送者
     * @param url
     *            - 下载地址
     * @param file
     *            - 保存文件
     * @param callback
     *            -回调函数
     * @return 是否成功
     */
    public boolean run(CommandSender sender, URL url, File file, One<File> callback) {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }
        try {
            sender.sendMessage("§6开始下载: §3" + getFileName(url));
            sender.sendMessage("§6下载地址: §3" + url.toString());
            URLConnection uc = reload(sender, url.openConnection());
            int status = ((HttpURLConnection) uc).getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                switch (status) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new IllegalStateException(status + " 文件未找到!");
                case HttpURLConnection.HTTP_FORBIDDEN:
                    throw new IllegalStateException(status + " 服务器拒绝了访问!");
                case HttpURLConnection.HTTP_BAD_GATEWAY:
                    throw new IllegalStateException(status + " 无效的网关!");
                }
            }
            int fileLength = uc.getContentLength();
            boolean dyml = "chunked".equalsIgnoreCase(uc.getHeaderField("Transfer-Encoding"));
            if (fileLength < 0 && !dyml) {
                sender.sendMessage("§6下载: §c文件 " + file.getName() + " 获取长度错误(可能是网络问题)!");
                sender.sendMessage("§6文件: §c" + file.getName() + " 下载失败!");
                return false;
            }
            sender.sendMessage("§6文件长度: §3" + (dyml ? "动态长度" : fileLength));
            in = new BufferedInputStream(uc.getInputStream());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                sender.sendMessage("§6创建新目录: §d" + file.getParentFile().getAbsolutePath());
            }
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            sender.sendMessage("§6创建新文件: §d" + file.getAbsolutePath());
            fout = new FileOutputStream(file);
            byte[] data = new byte[1024];
            long downloaded = 0L;
            int count;
            long time = System.currentTimeMillis();
            while ((count = in.read(data)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                if (dyml) {
                    if (System.currentTimeMillis() - time > 1000) {
                        sender.sendMessage(String.format("§6已下载: §a%sk", downloaded / 1024));
                        time = System.currentTimeMillis();
                    }
                } else {
                    int percent = (int) (downloaded * 100L / fileLength);
                    if (percent % 10 == 0) {
                        if (System.currentTimeMillis() - time > 500) {
                            sender.sendMessage(String.format("§6已下载: §a" + getPer(percent / 10) + " %s%%", percent));
                            time = System.currentTimeMillis();
                        }
                    }
                }
            }
            String pVer = null;
            try {
                PluginDescriptionFile desc = plugin.getPluginLoader().getPluginDescription(file);
                pVer = StringUtils.substring(desc.getVersion(), 0, 15);
            } catch (Exception e) {
                pVer = "";
            }
            sender.sendMessage("§6" + (pVer.isEmpty() ? "文件" : "插件") + ": §b" + file.getName()
                    + (pVer.isEmpty() ? "" : " §a版本 §e" + pVer) + " §a下载完成!");
        } catch (Exception ex) {
            sender.sendMessage("§6异常: §c" + ex.getMessage());
            sender.sendMessage("§6文件: §c" + file.getName() + " 下载失败!");
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (Exception ex) {
            }
        }
        if (callback != null) {
            callback.run(file);
        }
        return true;
    }

    /**
     * 从网络下载文件
     *
     * @param urlstring
     *            - 下载地址
     * @return 是否成功
     */
    public boolean run(String urlstring) {
        return run(null, urlstring);
    }

    /**
     * 从网络下载文件
     *
     * @param urlstring
     *            - 下载地址
     * @param file
     *            - 保存文件
     * @return 是否成功
     */
    public boolean run(String urlstring, File file) {
        return run(null, urlstring, file, null);
    }

    /**
     * 从网络下载文件
     *
     * @param url
     *            - 下载地址
     * @param file
     *            - 保存文件
     * @return 是否成功
     */
    public boolean run(URL url, File file) {
        return run(null, url, file, null);
    }

    private String getPer(int per) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            if (per > i) {
                sb.append("==");
            } else if (per == i) {
                sb.append("> ");
            } else {
                sb.append("  ");
            }
        }
        return sb.toString();
    }

    /**
     * 302 301跳转处理
     *
     * @param 跳转地址
     * @return 最终地址
     * @throws Exception
     */
    private URLConnection reload(CommandSender sender, URLConnection uc) throws Exception {
        HttpURLConnection huc = (HttpURLConnection) uc;
        // 302, 301, 307
        if (huc.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                || huc.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || huc.getResponseCode() == 307) {
            String url = huc.getHeaderField("Location");
            sender.sendMessage("§6跳转至地址: §3" + url);
            return reload(sender, new URL(url).openConnection());
        }
        return uc;
    }

}
