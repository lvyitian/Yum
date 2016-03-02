/**
 *
 */
package cn.citycraft.Yum;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.CommonData.UpdatePlugin;
import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.utils.VersionChecker;
import cn.citycraft.Yum.api.YumAPI;
import cn.citycraft.Yum.commands.FileCommand;
import cn.citycraft.Yum.commands.YumCommand;

/**
 * MC插件仓库
 *
 * @author 喵♂呜
 * @since 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
    public FileConfig config;

    @Override
    public void onEnable() {
        new YumAPI(this);
        new YumCommand(this);
        new FileCommand(this);
        new VersionChecker(this);
        YumAPI.updaterepo(Bukkit.getConsoleSender());
        YumAPI.updatecheck(Bukkit.getConsoleSender());
    }

    @Override
    public void onLoad() {
        config = new FileConfig(this);
        // 初始化更新列
        UpdatePlugin.getUpdateList();
    }
}
