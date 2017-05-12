package com.tinytimrob.common;

import java.io.File;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

/**
 * Deals with loading and saving of configuration to JSON file (config.json) in installation directory
 * @author Robert Dennington
 */
public class Configuration
{
	private static final Logger log = LogWrapper.getLogger();
	static Object data = null;

	/**
	 * Load configuration data into a Java object
	 * @param <T> The type of the class, automatically determined from the passed in class
	 * @param clazz The class to use for storing configuration
	 * @return The configuration data, loaded into an object of that class
	 * @throws Exception If loading fails horribly for some reason and the program can't continue
	 */
	@SuppressWarnings("unchecked")
	public static <T> T load(Class<T> clazz) throws Exception
	{
		log.info("Loading configuration from disk");

		File file = new File(PlatformData.installationDirectory, "config.json");
		boolean loaded = false;
		if (file.exists())
		{
			try
			{
				String configurationString = FileUtils.readFileToString(file, CommonUtils.charsetUTF8);
				data = CommonUtils.GSON.fromJson(configurationString, clazz);
				loaded = true;
			}
			catch (Throwable e)
			{
				log.error("Failed to load configuration");
			}
		}
		if (!loaded)
		{
			log.warn("Unable to load configuration, reverting to defaults and saving");
			data = clazz.newInstance();
		}
		save();
		return (T) data;
	}

	/**
	 * Saves the current state of configuration to disk
	 * @throws Exception If saving fails horribly for some reason
	 */
	public static void save() throws Exception
	{
		log.info("Saving configuration to disk");

		File file = new File(PlatformData.installationDirectory, "config.json");
		String jsonString = CommonUtils.GSON.toJson(data);
		try
		{
			FileUtils.writeStringToFile(file, jsonString, CommonUtils.charsetUTF8);
		}
		catch (Throwable t)
		{
			log.error("Failed to save configuration!");
			throw new RuntimeException("Unable to save configuration data, check disk permissions", t);
		}
	}

	/**
	 * Deletes any configuration file that already exists
	 * @throws Exception If deleting fails horribly for some reason
	 */
	public static void delete() throws Exception
	{
		try
		{
			File file = new File(PlatformData.installationDirectory, "config.json");
			if (file.exists())
			{
				Files.delete(file.toPath());
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to delete configuration!");
			throw new Exception("Unable to delete old configuration data, check disk permissions", t);
		}
	}
}
