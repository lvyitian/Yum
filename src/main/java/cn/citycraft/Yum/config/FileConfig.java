package cn.citycraft.Yum.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * An implementation of {@link Configuration} which saves all files in Yaml. Note that this
 * implementation is not synchronized.
 */
public class FileConfig extends YamlConfiguration {

	public static FileConfig init(File file) {
		return FileConfig.loadConfiguration(file);
	}

	public static FileConfig loadConfiguration(File file) {
		Validate.notNull(file, "File cannot be null");
		FileConfig config = new FileConfig();
		try {
			config.load(file);
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
		} catch (InvalidConfigurationException ex) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
		}
		return config;
	}

	protected final DumperOptions yamlOptions = new DumperOptions();

	protected final Representer yamlRepresenter = new YamlRepresenter();

	protected final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

	@Override
	public void load(File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
		Validate.notNull(file, "File cannot be null");
		final FileInputStream stream = new FileInputStream(file);
		load(new InputStreamReader(stream, Charsets.UTF_8));
	}

	@Override
	public void load(Reader reader) throws IOException, InvalidConfigurationException {
		BufferedReader input = (reader instanceof BufferedReader) ? (BufferedReader) reader
				: new BufferedReader(reader);
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
