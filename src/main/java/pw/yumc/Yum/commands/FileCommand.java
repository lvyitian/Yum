package pw.yumc.Yum.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.kit.StrKit;
import cn.citycraft.PluginHelper.utils.FileUtil;
import pw.yumc.Yum.Yum;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.YumCore.commands.CommandArgument;
import pw.yumc.YumCore.commands.CommandExecutor;
import pw.yumc.YumCore.commands.CommandManager;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;

/**
 * File命令基类
 *
 * @since 2016年1月9日 上午10:02:39
 * @author 喵♂呜
 */
public class FileCommand implements CommandExecutor {
    private static String prefix = "§6[§bYum §a文件管理§6] ";

    private static String file_not_found = prefix + "§b%s §c文件未找到!";
    private static String file_is_dir = prefix + "§b%s §c文件是一个目录!";

    private static String copySuccess = prefix + "§a文件 §b%s §a复制成功!";
    private static String copyFailed = prefix + "§c文件 §b%s §c复制失败!";

    private static String waitCommand = prefix + "§a命令已发送,请等待执行完毕...";
    private static String runResult = prefix + "§a命令运行结果如下:";
    private static String noResult = prefix + "§d当前命令没有返回结果!";
    private static String runError = prefix + "§c命令运行错误: %s %s";

    private static String addStartupSuccess = "§a成功添加开机自动启...";
    private static String addStartupFailed = "§c添加开机自动启失败!";
    private static String STARTPATH = "C:\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs\\StartUp";

    Yum plugin;

    public FileCommand(final Yum yum) {
        plugin = yum;
        new CommandManager("file", this, PluginTabComplete.instence);
    }

    @Cmd(aliases = "cp", minimumArguments = 2)
    @Help(value = "复制文件", possibleArguments = "<源文件> <目标目录>")
    @Async
    public void copy(final CommandArgument e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        final File src = new File(args[0]);
        final File des = new File(args[1]);
        if (!src.exists()) {
            sender.sendMessage(String.format(file_not_found, args[0]));
            return;
        }
        if (src.isDirectory()) {
            sender.sendMessage(String.format(file_is_dir, args[0]));
            return;
        }
        if (FileUtil.copyFile(src, des)) {
            sender.sendMessage(String.format(copySuccess, args[0]));
        } else {
            sender.sendMessage(String.format(copyFailed, args[0]));
        }
    }

    @Cmd(aliases = "del", minimumArguments = 1)
    @Help(value = "删除文件(服务器JAR为根目录)", possibleArguments = "<文件相对目录>")
    @Async
    public void delete(final CommandArgument e) {
        final String[] args = e.getArgs();
        String fpath = args[0];
        final File file = new File(fpath);
        final CommandSender sender = e.getSender();
        fpath = file.getAbsolutePath();
        if (!file.exists()) {
            sender.sendMessage(String.format(file_not_found, fpath));
        } else {
            if (file.isDirectory()) {
                sender.sendMessage(String.format(file_is_dir, fpath));
                return;
            }
            try {
                sender.sendMessage("§d文件 §e" + fpath + " " + (file.delete() ? "§a删除成功!" : "§c删除失败!"));
            } catch (final Exception ex) {
                sender.sendMessage("§d文件 §e" + fpath + " 删除失败: " + ex.getMessage());
            }
        }
    }

    @Cmd(aliases = "d", minimumArguments = 1)
    @Help(value = "下载文件(默认保存到服务器更新文件夹)", possibleArguments = "<下载地址> [保存文件路径]")
    @Async
    public void download(final CommandArgument e) {
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

    @Cmd(aliases = "ls")
    @Help(value = "列出当前目录(服务器JAR为根目录)", possibleArguments = "<相对目录>")
    @Async
    public void ls(final CommandArgument e) {
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

    @Cmd(aliases = "rn", minimumArguments = 2)
    @Help(value = "重命名文件(服务器JAR为根目录)", possibleArguments = "<文件相对路径> <文件名称>")
    @Async
    public void rename(final CommandArgument e) {
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

    @Cmd(minimumArguments = 1)
    @Help(value = "删除文件夹(服务器JAR为根目录)", possibleArguments = "<相对目录>")
    @Async
    public void rm(final CommandArgument e) {
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

    @Cmd(aliases = "r", minimumArguments = 1)
    @Help(value = "运行一个命令或文件", possibleArguments = "<命令或文件绝对路径>")
    @Async
    public void run(final CommandArgument e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        try {
            final Process process = Runtime.getRuntime().exec(StrKit.join(args, " "));
            sender.sendMessage(waitCommand);
            final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            final StringBuilder build = new StringBuilder();
            while ((line = br.readLine()) != null) {
                build.append(line);
                build.append("\n");
            }
            if (build.toString().replace("\n", "").isEmpty()) {
                sender.sendMessage(noResult);
            } else {
                sender.sendMessage(runResult);
                sender.sendMessage(build.toString().split("\n"));
            }
        } catch (final Exception e2) {
            sender.sendMessage(String.format(runError, e2.getClass().getSimpleName(), e2.getMessage()));
        }
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "添加开机自启动", possibleArguments = "<文件绝对路径>")
    @Async
    public void startup(final CommandArgument e) {
        final String[] args = e.getArgs();
        final CommandSender sender = e.getSender();
        final File src = new File(args[0]);
        final File des = new File(STARTPATH, src.getName());
        if (!src.exists()) {
            sender.sendMessage(String.format(file_not_found, args[0]));
            return;
        }
        if (src.isDirectory()) {
            sender.sendMessage(String.format(file_is_dir, args[0]));
            return;
        }
        if (FileUtil.copyFile(src, des)) {
            sender.sendMessage(String.format(addStartupSuccess, args[0]));
        } else {
            sender.sendMessage(String.format(addStartupFailed, args[0]));
        }
    }
}
