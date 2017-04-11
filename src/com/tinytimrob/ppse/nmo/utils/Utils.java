package com.tinytimrob.ppse.nmo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils
{
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss.SSS", Locale.ENGLISH);
	private static final Logger log = LogManager.getLogger();
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
	public static final Charset charsetUTF8 = Charset.forName("UTF-8");

	public static String getFileHashSHA1(File file)
	{
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(file);
			return DigestUtils.sha1Hex(stream);
		}
		catch (Throwable e)
		{
			log.error("Failed to get file hash for " + (file == null ? "null file" : file.getAbsolutePath()), e);
			return null;
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (IOException e)
			{
				log.warn("Failed to close stream!", e);
			}
		}
	}

	public static boolean isNullOrEmpty(String s)
	{
		return s == null || s.isEmpty();
	}

	public static String convertTimestamp(long timestamp)
	{
		return dateFormatter.format(new Date(timestamp));
	}
}
