package de.dustplanet.util;

import net.minecraft.server.v1_4_6.MinecraftServer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_4_6.CraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static java.lang.System.getProperty;

/**
 * Custom pluginLogger to save errors. Multiple-instance safe!
 *
 * @author Icyene, Xiaomao
 */

public class ErrorLogger extends PluginLogger {

	private static Field mcLogger, craftbukkitServer, pluginLogger, prepend;

	private ErrorLogger(Plugin context) {
		super(context);
	}

	@Override
	public void log(LogRecord logRecord) {
		if (!generateErrorLog(logRecord))
			super.log(logRecord);
	}

	public static void register(Plugin context, String name, String pack, String tracker) {
		try {
			if (!(pluginLogger.get(context) instanceof ErrorLogger)) {
				ErrorLogger cLog = new ErrorLogger(context);
				pluginLogger.set(context, cLog);
			}
			if (!(mcLogger.get(craftbukkitServer) instanceof ErrorLogger)) {
				ErrorLogger pLog = new ErrorLogger(context);
				prepend.set(pLog, "");
				mcLogger.set(craftbukkitServer, pLog);
			}
			HashMap<String, List<String>> registry = loadMap();
			registry.put(name, Arrays.asList(pack, tracker, "\n" + StringUtils.center(name, 54 + name.length(), '=')));
			saveMap(registry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateErrorLog(Throwable thorn) {
		LogRecord screw = new LogRecord(Level.SEVERE, null);
		screw.setMessage("Bukkit did not catch this, so no additional info is available.");
		screw.setThrown(thorn);
		generateErrorLog(screw);
	}

	private static boolean generateErrorLog(LogRecord record) {
		Throwable thrown;
		if ((thrown = record.getThrown()) == null)
			return false;

		String ERROR = "", NAME = "", TICKETS = "", ENDL = "";

		try {
			ERROR = ExceptionUtils.getStackTrace(thrown);
			NAME = "";
			TICKETS = "";
			ENDL = "";
		} catch (Exception e) {
			return true;
		}
		for (Map.Entry<String, List<String>> entry : loadMap().entrySet()) {
			try {
				List<String> data = entry.getValue();
				if (ERROR.contains(data.get(0))) { //If the ERROR contains the package
					NAME = entry.getKey();
					TICKETS = data.get(1);
					ENDL = data.get(2);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return true;
			}
		}
		if (NAME == null || TICKETS == null || ENDL == null)
			return true;
		if (ERROR.contains(NAME + " has encountered an error!") && ERROR.contains(ErrorLogger.class.getName())) //Check if its not our own
			return true;
		try {
			Plugin PLUGIN = Bukkit.getPluginManager().getPlugin(NAME);
			StringBuilder err = new StringBuilder();
			err.append("\n=============").append(NAME).append(" has encountered an error!=============")
			.append("\nStacktrace:\n").append(ERROR).append("\n")
			.append(NAME).append(" version: ").append(PLUGIN.getDescription().getVersion())
			.append("\nBukkit message: ").append(record.getMessage())
			.append("\nPlugins loaded: ").append(Arrays.asList(Bukkit.getPluginManager().getPlugins()))
			.append("\nCraftBukkit version: ").append(Bukkit.getServer().getBukkitVersion())
			.append("\nJava info: ").append(getProperty("java.version"))
			.append("\nOS info: ").append(getProperty("os.arch")).append(" ").append(getProperty("os.name")).append(", ").append(getProperty("os.version"))
			.append("\nPlease report this error to the ").append(NAME).append(" ticket tracker (").append(TICKETS).append(")!");
			try {
				//One-liner beauty.
				String FILE_NAME = String.format("%s_%s_%s.error.log", NAME, thrown.getClass().getSimpleName(), new BigInteger(1,
						Arrays.copyOfRange(MessageDigest.getInstance("MD5").digest(err.toString().getBytes()), 0, 6)).toString().substring(0, 6));
				File root = new File(PLUGIN.getDataFolder(), "errors");
				if (root.exists() || root.mkdir()) {
					File dump = new File(root.getAbsoluteFile(), FILE_NAME);
					if (!dump.exists() && dump.createNewFile()) {
						BufferedWriter writer = new BufferedWriter(new FileWriter(dump));
						writer.write((err.toString() + ENDL).substring(1)); //Remove the extra /n
						writer.close();
						err.append("\nThis has been saved to the file ./").append(PLUGIN.getName()).append("/errors/").append(FILE_NAME);
					}
				}
			} catch (Exception e) {
				err.append("\nErrors occured while saving to file. Not saved.");
				e.printStackTrace();
			}
			System.err.println(err.append(ENDL));
			return true;
		} catch (Exception e) {
			return true;
		}
	}

	private static void saveMap(HashMap<String, List<String>> map) {
		System.setProperty("__ErrorLogger__", map.toString());
	}

	private static HashMap<String, List<String>> loadMap() {
		String pro = getProperty("__ErrorLogger__");
		if (StringUtils.isEmpty(pro))
			return new HashMap<String, List<String>>();
		List<String> format = Arrays.asList(pro.replace("=[", ", ").replace("]", "").replace("{", "").replace("}", "").split(", "));
		HashMap<String, List<String>> ret = new HashMap<String, List<String>>();
		ret.put(format.get(0), Arrays.asList(format.get(1), format.get(2), format.get(3)));
		return ret;
	}


	private static void setup() {
		try {
			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable throwable) {
					generateErrorLog(throwable);
				}
			});
			mcLogger = MinecraftServer.class.getDeclaredField("log");
			mcLogger.setAccessible(true);
			craftbukkitServer = CraftServer.class.getDeclaredField("console");
			craftbukkitServer.setAccessible(true);
			pluginLogger = JavaPlugin.class.getDeclaredField("logger");
			pluginLogger.setAccessible(true);
			prepend = PluginLogger.class.getDeclaredField("pluginName");
			prepend.setAccessible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static {
		setup();
	}
}