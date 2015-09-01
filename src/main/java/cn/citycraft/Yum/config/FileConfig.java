package cn.citycraft.Yum.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * An implementation of {@link Configuration} which saves all files in Yaml.
 * Note that this
 * implementation is not synchronized.
 */
public class FileConfig extends YamlConfiguration {
	protected File file;
	protected Logger loger;
	protected Plugin plugin;

	protected final DumperOptions yamlOptions = new DumperOptions();

	protected final Representer yamlRepresenter = new YamlRepresenter();

	protected final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

	private FileConfig(File file) {
		Validate.notNull(file, "File cannot be null");
		this.file = file;
		loger = Bukkit.getLogger();
		init(file);
	}

	private FileConfig(InputStream stream) {
		loger = Bukkit.getLogger();
		init(stream);
	}

	public FileConfig(Plugin plugin, File file) {
		Validate.notNull(file, "File cannot be null");
		Validate.notNull(plugin, "Plugin cannot be null");
		this.plugin = plugin;
		this.file = file;
		loger = plugin.getLogger();
		check(file);
		init(file);
	}

	public FileConfig(Plugin plugin, String filename) {
		this(plugin, new File(plugin.getDataFolder(), filename));
	}

	private void check(File file) {
		String filename = file.getName();
		InputStream stream = plugin.getResource(filename);
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				if (stream == null) {
					file.createNewFile();
					loger.info("配置文件 " + filename + " 不存在 创建新文件...");
				} else {
					plugin.saveResource(filename, true);
					loger.info("配置文件 " + filename + " 不存在 从插件释放...");
				}
			} else {
				FileConfig newcfg = new FileConfig(stream);
				FileConfig oldcfg = new FileConfig(file);
				String newver = newcfg.getString("version");
				String oldver = oldcfg.getString("version");
				if (newver != null && newver != oldver) {
					loger.warning("配置文件: " + filename + " 版本 " + oldver + " 过低 正在升级到 " + newver + " ...");
					try {
						oldcfg.save(new File(file.getParent(), filename + ".backup"));
						loger.warning("配置文件: " + filename + " 已备份为 " + filename + ".backup !");
					} catch (IOException e) {
						loger.warning("配置文件: " + filename + "备份失败!");
					}
					plugin.saveResource(filename, true);
					loger.info("配置文件: " + filename + "升级成功!");
				}
			}
		} catch (IOException e) {
			loger.info("配置文件 " + filename + " 创建失败...");
		}
	}

	private void init(File file) {
		Validate.notNull(file, "File cannot be null");
		FileInputStream stream;
		try {
			stream = new FileInputStream(file);
			init(stream);
		} catch (FileNotFoundException e) {
			loger.info("配置文件 " + file.getName() + " 不存在...");
		}
	}

	private void init(InputStream stream) {
		Validate.notNull(stream, "Stream cannot be null");
		try {
			this.load(new InputStreamReader(stream, Charsets.UTF_8));
		} catch (IOException ex) {
			loger.info("配置文件 " + file.getName() + " 读取错误...");
		} catch (InvalidConfigurationException ex) {
			loger.info("配置文件 " + file.getName() + " 格式错误...");
		}
	}

	@Override
	public void load(File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
		Validate.notNull(file, "File cannot be null");
		final FileInputStream stream = new FileInputStream(file);
		load(new InputStreamReader(stream, Charsets.UTF_8));
	}

	@Override
	public void load(Reader reader) throws IOException, InvalidConfigurationException {
		BufferedReader input = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
		StringBuilder builder = new StringBuilder();
		try {
			String line;
			while ((line = input.readLine()) != null) {
				builder.append(line);
				builder.append('\n');
			}
		} finally {
			input.close();
		}
		loadFromString(builder.toString());
	}

	public void reload() {
		init(file);
	}

	public void save() {
		if (file == null) {
			loger.info("未定义配置文件路径 保存失败!");
		}
		try {
			this.save(file);
		} catch (IOException e) {
			loger.info("配置文件 " + file.getName() + " 保存错误...");
			e.printStackTrace();
		}
	}

	@Override
	public void save(File file) throws IOException {
		Validate.notNull(file, "File cannot be null");
		Files.createParentDirs(file);
		String data = saveToString();
		Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
		try {
			writer.write(data);
		} finally {
			writer.close();
		}
	}

	@Override
	public String saveToString() {
		yamlOptions.setIndent(options().indent());
		yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		String header = buildHeader();
		String dump = yaml.dump(getValues(false));
		if (dump.equals(BLANK_CONFIG)) {
			dump = "";
		}
		return header + dump;
	}
}
