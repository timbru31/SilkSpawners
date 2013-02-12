package de.dustplanet.util;

import net.minecraft.server.v1_4_R1.CrashReport;
import net.minecraft.server.v1_4_R1.MinecraftServer;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_4_R1.CraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
 
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
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
            HashMap<String, String> registry = loadMap();
            registry.put(name, pack);
            System.setProperty("__ErrorLogger__", JSONValue.toJSONString(registry));
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
        Throwable thorn;
        if ((thorn = record.getThrown()) == null)
            return false;
 
        String traceback = ExceptionUtils.getStackTrace(thorn);
        String regName = " ";
 
        for (Map.Entry<String, String> entry : loadMap().entrySet()) {
            if (traceback.contains(entry.getValue())) { //If the ERROR contains the package
                regName = entry.getKey();
                break;
            }
        }
 
        if (!traceback.contains(regName + " has encountered an error!") && traceback.contains(ErrorLogger.class.getName()))
            //Check if its not our own. If it is, return, to not cause a StackOverflow
            return true;
 
        Plugin problematicPlugin = Bukkit.getPluginManager().getPlugin(regName);
        if (problematicPlugin == null)
            return true; //Plugin doesn't exist!
 
        System.err.println("\n\n" + regName + " encountered an error: \n" + traceback);
        try {
            StringBuilder err = new StringBuilder();
            CrashReport report = new CrashReport(problematicPlugin.getName() + " encountered an error! What follows is the stacktrace of the current thread: ", thorn);
            try {
                File dump = new File(
                        new File(problematicPlugin.getDataFolder(), "errors").getAbsoluteFile(),
                        String.format("%s_%s.error.log",
                                thorn.getClass().getSimpleName(),                   //Get first 6 chars of hash
                                new BigInteger(1, Arrays.copyOfRange(MessageDigest.getInstance("MD5").digest(err.toString().getBytes()), 0, 6)).toString().substring(0, 6)
                        )
                );
                report.a(dump);
                System.err.println("Don't despair! This error has been saved to '.\\" + problematicPlugin.getDataFolder().getName() + "\\errors\\" + dump.getName() +
                        "'. You should report it to the developers of " + problematicPlugin.getName() + ": " + problematicPlugin.getDescription().getAuthors() + ".\n");
            } catch (Exception e) {
                err.append("\nErrors occured while saving to file. Not saved.");
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
 
    @SuppressWarnings({ "rawtypes", "unchecked" })
	static HashMap<String, String> loadMap() {
        String ser = getProperty("__ErrorLogger__");
        return ser != null ? (JSONObject) JSONValue.parse(ser) : new HashMap();
    }
 
    static {
        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    generateErrorLog(throwable);
                }
            });
 
            (mcLogger = MinecraftServer.class.getDeclaredField("log")).setAccessible(true);
            (craftbukkitServer = CraftServer.class.getDeclaredField("console")).setAccessible(true);
            (pluginLogger = JavaPlugin.class.getDeclaredField("logger")).setAccessible(true);
            (prepend = PluginLogger.class.getDeclaredField("pluginName")).setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}