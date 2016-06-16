/**
 *
 */
package pw.yumc.Yum;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.CommonData.UpdatePlugin;
import cn.citycraft.PluginHelper.kit.PluginKit;
import cn.citycraft.PluginHelper.utils.VersionChecker;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.commands.FileCommand;
import pw.yumc.Yum.commands.NetCommand;
import pw.yumc.Yum.commands.YumCommand;
import pw.yumc.Yum.listeners.PluginNetworkListener;
import pw.yumc.Yum.listeners.SecurityListener;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.managers.NetworkManager;

/**
 * MC插件仓库
 *
 * @author 喵♂呜
 * @since 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
    NetworkManager netmgr;

    public void initCommands() {
        new YumCommand(this);
        new NetCommand(this);
        new FileCommand(this);
    }

    public void initListeners() {
        if (ConfigManager.i().isSetOpEnable()) {
            try {
                final ClassLoader cl = Class.forName("pw.yumc.injected.event.SetOpEvent").getClassLoader();
                try {
                    cl.getClass().getDeclaredField("plugin");
                    throw new ClassNotFoundException();
                } catch (final NoSuchFieldException | SecurityException e) {
                    new SecurityListener(this);
                    PluginKit.scp("§a安全管理系统已启用...");
                }
            } catch (final ClassNotFoundException e) {
                PluginKit.scp("§c服务端未注入安全拦截器 关闭功能...");
            }
        }
        if (ConfigManager.i().isNetworkEnable()) {
            new PluginNetworkListener(this);
            PluginKit.scp("§a网络管理系统已启用...");
        }
    }

    @Override
    public void onDisable() {
        netmgr.unregister();
    }

    @Override
    public void onEnable() {
        new YumAPI(this);
        initCommands();
        initListeners();
        new VersionChecker(this);
        YumAPI.updaterepo(Bukkit.getConsoleSender());
        YumAPI.updatecheck(Bukkit.getConsoleSender());
    }

    @Override
    public void onLoad() {
        // 初始化配置
        ConfigManager.i();
        // 初始化更新列
        UpdatePlugin.getUpdateList();
        // 启用网络注入
        netmgr = new NetworkManager().register(this);
    }
}
