package de.dustplanet.silkspawners.configs;

import de.dustplanet.util.CommentedConfiguration;

/**
 * Default configs.
 *
 * @author (former) mushroomhostage
 * @author xGhOsTkiLLeRx
 */
public abstract class AbstractConfiguration {
    protected CommentedConfiguration config;

    public AbstractConfiguration(CommentedConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("given config is null");
        }
        this.config = config;
    }

    protected void loadConfig() {
        config.load();
        config.options().copyDefaults(true);
        config.save();
        config.load();
    }
}
