package com.tinytimrob.ppse.nmo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.ppse.nmo.exceptions.BadRequestException;
import com.tinytimrob.ppse.nmo.exceptions.BadResponseException;
import com.tinytimrob.ppse.nmo.exceptions.BlankResponseException;

public class Communicator
{
	/** 418 Generic Failure. The error code 418 I'm A Teapot is defined in RFC as an error but isn't a real error, so we can
	 * safely use this code without worrying about it appearing elsewhere or being replaced with a code that has a purpose later.
	 * (we don't want to be LogMeIn, Inc. and use some unoccupied code, only for it to be filled by something later with another meaning) */
	static final int GENERIC_FAILURE = 418;

	public static class GenericFailureResponse
	{
		@Expose
		@SerializedName("code")
		public int code;
		@Expose
		@SerializedName("message")
		public String message;
	}

	private static final Logger log = LogWrapper.getLogger();

	public static <T> T basicJsonMessage(String humanDesc, String path, Object constructable, Class<T> clazz, boolean returnGineverFailureData, String bearer) throws Exception
	{
		HttpURLConnection connection = null;
		OutputStream out = null;
		InputStream in = null;

		try
		{
			connection = (HttpURLConnection) new URL(path).openConnection();
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(15000);
			connection.setUseCaches(false);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36"); //"NoMoreOversleeps/" + Main.VERSION.replace(" ", ""));
			if (bearer != null)
			{
				connection.setRequestProperty("Authorization", "Bearer " + bearer);
			}
			if (constructable != null)
			{
				String request = CommonUtils.GSON.toJson(constructable);
				System.out.println(request);
				byte[] data = request.getBytes(CommonUtils.charsetUTF8);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				connection.setRequestProperty("Content-Length", Integer.toString(data.length));
				connection.setRequestProperty("Content-Language", "en-US");
				connection.setDoOutput(true);
				connection.connect();
				out = connection.getOutputStream();
				out.write(data);
			}
			else
			{
				connection.setRequestMethod("GET");
				connection.connect();
			}
			int responseCode = connection.getResponseCode();
			if (responseCode == 200)
			{
				in = connection.getInputStream();
				if (clazz == null)
					return null;
				String REPLY = IOUtils.toString(in, CommonUtils.charsetUTF8);
				System.out.println(REPLY);
				T response = CommonUtils.GSON.fromJson(REPLY, clazz);
				if (response == null)
					throw new BlankResponseException("Server sent back no data");
				return response;
			}
			else if (returnGineverFailureData && responseCode == GENERIC_FAILURE)
			{
				in = connection.getErrorStream();
				GenericFailureResponse response = CommonUtils.GSON.fromJson(IOUtils.toString(in, CommonUtils.charsetUTF8), GenericFailureResponse.class);
				if (response != null && response.message != null && !response.message.isEmpty())
				{
					throw new BadRequestException(response.message);
				}
			}
			else
			{
				in = connection.getErrorStream();
				System.out.println(IOUtils.toString(in, CommonUtils.charsetUTF8));
			}
			throw new BadResponseException(responseCode);
		}
		catch (Throwable t)
		{
			log.error("Communication error while performing task '" + humanDesc + "'");
			throw t;
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
				}
			}

			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
				}
			}

			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}
}
