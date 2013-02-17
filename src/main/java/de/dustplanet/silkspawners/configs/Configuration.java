package de.dustplanet.silkspawners.configs;

import de.dustplanet.util.CommentedConfiguration;

public class Configuration {
	private CommentedConfiguration config;

	public Configuration(CommentedConfiguration config, int configNumber) {
		if (config != null) {
			// Load configuration
			config.load();
			switch (configNumber) {
			case 1:
				loadDefaultsConfig();
				break;
			case 2:
				loadDefaultsLocalization();
				break;
			case 3:
				loadDefaultsMobs();
				break;
			default:
				loadDefaultsConfig();
				break;
			}
			// Copy defaults and save
			config.options().copyDefaults(true);
			config.save();
		}
	}

	private void loadDefaultsMobs() {
		// TODO Automatisch generierter Methodenstub
	}

	private void loadDefaultsLocalization() {
		// TODO Automatisch generierter Methodenstub
	}

	private void loadDefaultsConfig() {
		config.addDefault("useErrorLogger", true);
		config.addComment("useErrorLogger", "#This is a test!");
	}
}