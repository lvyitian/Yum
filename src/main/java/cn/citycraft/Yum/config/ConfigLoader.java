package cn.citycraft.Yum.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.Plugin;

public class ConfigLoader extends FileConfig {
	protected FileConfig config;
	protected File file;
	protected boolean tip = true;
	protected Plugin plugin;

	public ConfigLoader(Plugin p, File file) {
		this.plugin = p;
		config = loadConfig(p, file, null, true);
	}

	public ConfigLoader(Plugin p, String filename) {
		this.plugin = p;
		config = loadConfig(p, new File(filename), null, true);
	}

	public FileConfig loadConfig(Plugin p, File file, String ver, boolean res) {
		tip = res;
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
			p.getLogger().info("创建新的文件夹" + file.getParentFile().getAbsolutePath() + "...");
		}
		if (!file.exists()) {
			fileCreate(p, file, res);
		} else {
			if (ver != null) {
				FileConfig configcheck = init(file);
				String version = configcheck.getString("version");
				if (version == null || !version.equals(ver)) {
					p.getLogger().warning("配置文件: " + file.getName() + " 版本过低 正在升级...");
					try {
						configcheck.save(new File(file.getParent(), file.getName() + ".backup"));
						p.getLogger().warning("配置文件: " + file.getName() + " 已备份为 " + file.getName() + ".backup !");
					} catch (IOException e) {
						p.getLogger().warning("配置文件: " + file.getName() + "备份失败!");
					}
					p.saveResource(file.getName(), true);
					p.getLogger().info("配置文件: " + file.getName() + "升级成功!");
				}
			}
		}
		if (tip)
			p.getLogger().info("载入配置文件: " + file.getName() + (ver != null ? " 版本: " + ver : ""));
		return init(file);
	}

	private void fileCreate(Plugin p, File file, boolean res) {
		if (res) {
			p.saveResource(file.getName(), false);
		} else {
			try {
				p.getLogger().info("创建新的配置文件" + file.getAbsolutePath() + "...");
				file.createNewFile();
			} catch (IOException e) {
				p.getLogger().info("配置文件" + file.getName() + "创建失败...");
				e.printStackTrace();
			}
		}
	}

	public void saveError(File file) {
		plugin.getLogger().info("配置文件" + file.getName() + "保存错误...");
	}

	public void save() {
		try {
			config.save(file);
		} catch (IOException e) {
			saveError(file);
			e.printStackTrace();
		}
	}

}
