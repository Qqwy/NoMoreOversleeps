package com.tinytimrob.ppse.nmo;

import java.io.File;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tinytimrob.ppse.nmo.Pavlok.OAuthResponse;
import com.tinytimrob.ppse.nmo.utils.PlatformData;
import com.tinytimrob.ppse.nmo.utils.Utils;

public class Configuration
{
	private static final Logger log = LogManager.getLogger();
	public static Configuration instance = new Configuration();

	@Expose
	@SerializedName("pavlokAuth")
	public OAuthResponse pavlokAuth = null;

	public static void load() throws Exception
	{
		log.info("Loading configuration from disk");

		File file = new File(PlatformData.installationDirectory, "config.json");
		boolean loaded = false;
		if (file.exists())
		{
			try
			{
				String configurationString = FileUtils.readFileToString(file, Utils.charsetUTF8);
				Configuration.instance = Utils.GSON.fromJson(configurationString, Configuration.class);
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
			Configuration.instance = new Configuration();
		}
		save();
	}

	public static void save() throws Exception
	{
		log.info("Saving configuration to disk");

		File file = new File(PlatformData.installationDirectory, "config.json");
		String jsonString = Utils.GSON.toJson(Configuration.instance);
		try
		{
			FileUtils.writeStringToFile(file, jsonString, Utils.charsetUTF8);
		}
		catch (Throwable t)
		{
			log.error("Failed to save configuration!");
			throw new RuntimeException("Unable to save configuration data, check disk permissions", t);
		}
	}

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
			throw new RuntimeException("Unable to delete old configuration data, check disk permissions", t);
		}
	}
}
