package com.tinytimrob.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Random utilities
 * @author Robert Dennington
 */
public class CommonUtils
{
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss.SSS", Locale.ENGLISH);
	public static final SimpleDateFormat dateFormatter2 = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.ENGLISH);
	private static final Logger log = LogWrapper.getLogger();
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
	public static final Charset charsetUTF8 = Charset.forName("UTF-8");

	/**
	 * Returns the SHA1 hash of a file
	 * @param file The file
	 * @return That file's SHA1 hash
	 */
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

	/**
	 * Returns whether or not a string is null or empty
	 * @param s The string
	 * @return whether or not it is null or empty
	 */
	public static boolean isNullOrEmpty(String s)
	{
		return s == null || s.isEmpty();
	}

	/** 
	 * Converts epoch timestamp to human readable format 
	 * @param timestamp The timestamp to convert
	 * @return A human readable version of the timestamp
	 */
	public static String convertTimestamp(long timestamp)
	{
		return dateFormatter.format(new Date(timestamp));
	}

	/** Redirect a path which may or may not be relative so that, if relative, its location is
	 * determined as a subpath of the installation directory of the application instead of from
	 * Java's working directory
	 * 
	 * @param path Path to potentially redirect
	 * @return A file object which may have been redirected
	 */
	public static File redirectRelativePathToAppDirectory(String path)
	{
		File f = new File(path);
		return f.isAbsolute() ? f : new File(PlatformData.installationDirectory, path).getAbsoluteFile();
	}
}
