package pw.yumc.Yum.commands;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.commands.HandlerCommand;
import cn.citycraft.PluginHelper.commands.HandlerCommands;
import cn.citycraft.PluginHelper.commands.InvokeCommandEvent;
import cn.citycraft.PluginHelper.commands.InvokeSubCommand;
import cn.citycraft.PluginHelper.utils.FileUtil;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.api.YumAPI;

/**
 * File命令基类
 *
 * @since 2016年1月9日 上午10:02:39
 * @author 喵♂呜
 */
public class FileCommand implements HandlerCommands {
    Yum plugin;

    public FileCommand(final Yum yum) {
        plugin = yum;
        final InvokeSubCommand cmdhandler = new InvokeSubCommand(yum, "file");
        cmdhandler.setAllCommandOnlyConsole(yum.getConfig().getBoolean("onlyFileCommandConsole", true));
        cmdhandler.registerCommands(this);
    }

    @HandlerCommand(name = "delete", aliases = { "del" }, minimumArguments = 1, description = "删除文件(服务器JAR为根目录)", possibleArguments = "<文件相对目录>")
    public void delete(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        final String fpath = args[0];
        final File file = new File(fpath);
        final CommandSender sender = e.getSender();
        if (!file.exists()) {
            sender.sendMessage("§c文件 " + file.getAbsolutePath() + " 不存在!");
        } else {
            if (file.isDirectory()) {
                sender.sendMessage("§e" + file.getAbsolutePath() + " §c是一个目录 请使用file rm!");
                return;
            }
            try {
                sender.sendMessage("§d文件 §e" + file.getAbsolutePath() + " " + (file.delete() ? "§a删除成功!" : "§c删除失败!"));
            } catch (final Exception ex) {
                sender.sendMessage("§d文件 §e" + file.getAbsolutePath() + " 删除失败: " + ex.getMessage());
            }
        }
    }

    @HandlerCommand(name = "download", aliases = { "d" }, minimumArguments = 1, description = "下载文件(默认保存到服务器更新文件夹)", possibleArguments = "<下载地址> [保存文件路径]")
    public void download(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        String urlstr = args[0];
        if (!urlstr.startsWith("http")) {
            urlstr = "http://" + urlstr;
        }
        File file = null;
        if (args.length == 2) {
            file = new File(args[1]);
        } else {
            file = new File(Bukkit.getUpdateFolderFile(), YumAPI.getDownload().getFileName(urlstr));
        }
        YumAPI.getDownload().run(e.getSender(), urlstr, file);
    }

    @HandlerCommand(name = "ls", aliases = { "l" }, description = "列出当前目录(服务器JAR为根目录)", possibleArguments = "<相对目录>")
    public void ls(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        File dir = new File(".");
        if (args.length == 1) {
            dir = new File(args[0]);
        }
        if (!dir.isDirectory()) {
            sender.sendMessage("§6路径: §e " + dir.getAbsolutePath() + " §c不是一个目录!");
            return;
        }
        final StringBuffer sb = new StringBuffer();
        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                sb.append("§b");
            } else {
                sb.append("§a");
            }
            sb.append(file.getName() + " ");
        }
        final String filelist = sb.toString();
        if (filelist.isEmpty()) {
            sender.sendMessage("§6目录: §e" + dir.getAbsolutePath() + " §c下没有文件或文件夹!");
        } else {
            sender.sendMessage("§6目录: §e" + dir.getAbsolutePath() + " §a存在如下文件!");
            sender.sendMessage(sb.toString());
        }
    }

    @HandlerCommand(name = "rename", aliases = { "rn" }, minimumArguments = 2, description = "重命名文件(服务器JAR为根目录)", possibleArguments = "<文件相对路径> <文件名称>")
    public void rename(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        final String fpath = args[0];
        final File file = new File(fpath);
        if (!file.exists()) {
            sender.sendMessage("§c文件 " + file.getAbsolutePath() + " 不存在!");
        } else {
            try {
                final File newFile = new File(file.getParentFile(), args[1]);
                file.renameTo(newFile);
                sender.sendMessage("§a文件 §e" + file.getAbsolutePath() + " §a重命名为 §d" + newFile.getAbsolutePath());
            } catch (final Exception ex) {
                sender.sendMessage("§c文件 §e" + file.getAbsolutePath() + " §c重命名失败: " + ex.getMessage());
            }
        }
    }

    @HandlerCommand(name = "rm", minimumArguments = 1, description = "删除文件夹(服务器JAR为根目录)", possibleArguments = "<相对目录>")
    public void rm(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        final String fpath = args[0];
        final File file = new File(fpath);
        if (!file.exists()) {
            sender.sendMessage("§c目录 " + file.getAbsolutePath() + " 不存在!");
        } else {
            if (!file.isDirectory()) {
                sender.sendMessage("§d路径 §e" + file.getAbsolutePath() + " §c是一个文件 请使用file delete!");
                return;
            }
            for (final String name : plugin.getConfig().getStringList("blacklist")) {
                if (file.getAbsolutePath().toLowerCase().endsWith(name)) {
                    sender.sendMessage("§d路径 §e" + file.getAbsolutePath() + " §c不允许被删除!");
                    return;
                }
            }
            if (file.listFiles().length != 0 && !(args.length > 1 && args[1].equalsIgnoreCase("-rf"))) {
                sender.sendMessage("§d目录 §e" + file.getAbsolutePath() + " §c不为空!");
                sender.sendMessage("§c请使用 §a/file rm " + fpath + " -rf §c强行删除!");
                return;
            }
            sender.sendMessage("§d目录 §e" + file.getAbsolutePath() + " " + (FileUtil.deleteDir(sender, file) ? "§a删除成功!" : "§c删除失败!"));
        }
    }

}
