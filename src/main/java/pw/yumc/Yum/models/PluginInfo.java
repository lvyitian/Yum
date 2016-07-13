package pw.yumc.Yum.models;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.PluginHelperLogger;
import cn.citycraft.PluginHelper.kit.PKit;
import cn.citycraft.PluginHelper.utils.IOUtil;
import pw.yumc.Yum.models.RepoSerialization.Plugin;
import pw.yumc.Yum.models.RepoSerialization.TagInfo;
import pw.yumc.Yum.models.RepoSerialization.URLType;

public class PluginInfo implements Serializable {
    public static String NMSVersion;

    static {
        try {
            NMSVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (final Exception e) {
            PKit.i().getLogger().warning("服务器NMS解析失败: " + Bukkit.getServer().getClass().getPackage().getName());
            NMSVersion = "NONMS";
        }
    }

    public String branch;
    public String name;
    public Plugin plugin;
    public String pom;
    public URLType type;
    public String repo;
    public String url;

    /**
     * 获得下载直链
     *
     * @param sender
     *            命令发送者
     * @param version
     *            版本
     * @return 下载直链
     */
    public String getDirectUrl() {
        return url;
    }

    /**
     * 获得下载直链
     *
     * @param sender
     *            命令发送者
     * @param version
     *            版本
     * @return 下载直链
     */
    public String getDirectUrl(final String version) {
        return url.replace("[version]", version);
    }

    /**
     * 获得文件名称
     *
     * @return 文件名称
     */
    public String getFileName() {
        return getFileName(plugin.version);
    }

    /**
     * 获得文件名称
     *
     * @param version
     *            插件版本
     * @return 文件名称
     */
    public String getFileName(final String version) {
        return String.format("%1$s-%2$s.jar", plugin.artifactId, version);
    }

    /**
     * 获取Maven仓库指定插件的下载地址
     *
     * @param sender
     *            - 命令发送者
     * @param version
     *            - 需要更新的版本
     * @return 更新地址
     */
    public String getMavenUrl(final String ver) {
        return String.format(url + (url.endsWith("/") ? "" : "/") + "%1$s/%2$s/%3$s/%2$s-%3$s.jar", plugin.groupId.replace(".", "/"), plugin.artifactId, (ver == null || ver.isEmpty()) ? plugin.version : ver);
    }

    public String getUrl(final CommandSender sender, final String version) {
        String ver = version;
        if (ver == null) {
            if (plugin.tags != null) {
                PluginHelperLogger.getLogger().debug("发现存在TAG标签 开始检索: " + NMSVersion);
                for (final TagInfo tagInfo : plugin.tags) {
                    if (tagInfo.tag.equalsIgnoreCase(NMSVersion)) {
                        sender.sendMessage("§6版本: §b从Tag标签中获取 §e" + NMSVersion + " §b的最新版本...");
                        ver = tagInfo.version;
                        if (tagInfo.type == URLType.DirectUrl) {
                            return tagInfo.url;
                        }
                        break;
                    }
                }
            } else if (pom != null && !pom.isEmpty()) {
                pom = pom.replace("[name]", name).replace("[branch]", branch);
                sender.sendMessage("§6版本: §b尝试从在线POM文件获取最新版本...");
                System.out.println(pom);
                ver = IOUtil.getXMLTag(pom, "version", null);
                if (ver != null) {
                    sender.sendMessage("§6版本: §a成功获取到最新版本 §e" + ver + " §a...");
                }
            }
        }
        if (ver == null) {
            ver = plugin.version;
            sender.sendMessage("§6版本: §d使用缓存的版本 §e" + ver + " §a...");
        }
        switch (type) {
        case DirectUrl:
            return getDirectUrl(ver);
        case Maven:
            return getMavenUrl(ver);
        default:
            return null;
        }
    }
}