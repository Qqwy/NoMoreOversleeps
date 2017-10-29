package com.tinytimrob.ppse.nmo.integration.wemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.Main;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;

public class IntegrationWemo extends Integration
{
	public static final IntegrationWemo INSTANCE = new IntegrationWemo();

	private IntegrationWemo()
	{
		super("wemo");
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.INSTANCE.integrations.wemo.enabled;
	}

	@Override
	public void init() throws Exception
	{
		for (int i = 0; i < NMOConfiguration.INSTANCE.integrations.wemo.devices.length; i++)
		{
			final WemoDeviceEntry entry = NMOConfiguration.INSTANCE.integrations.wemo.devices[i];
			this.actions.put("/wemo/" + i + "/on", new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					IntegrationWemo.this.setSwitchState(entry.ipAddress, 1);
				}

				@Override
				public String getName()
				{
					return "TURN ON " + entry.name;
				}
				
				@Override
				public String getDescription()
				{
					return "Turns on the WEMO device "+ entry.name +".\n\n" + entry.description;
				}

				@Override
				public boolean isHiddenFromFrontend()
				{
					return entry.hidden;
				}

				@Override
				public boolean isHiddenFromWebUI()
				{
					return entry.secret;
				}

				@Override
				public boolean isBlockedFromWebUI()
				{
					return entry.secret;
				}
			});
			this.actions.put("/wemo/" + i + "/off", new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					IntegrationWemo.this.setSwitchState(entry.ipAddress, 0);
				}

				@Override
				public String getName()
				{
					return "TURN OFF " + entry.name;
				}
				
				
				@Override
				public String getDescription()
				{
					return "Turns off the WEMO device "+ entry.name +".\n\n" + entry.description;
				}

				@Override
				public boolean isHiddenFromFrontend()
				{
					return entry.hidden;
				}

				@Override
				public boolean isHiddenFromWebUI()
				{
					return entry.secret;
				}

				@Override
				public boolean isBlockedFromWebUI()
				{
					return entry.secret;
				}
			});
		}
	}

	protected void setSwitchState(String ipAddress, int i)
	{
		try
		{
			HttpURLConnection connection = null;
			OutputStream out = null;
			InputStream in = null;

			try
			{
				connection = (HttpURLConnection) new URL("http://" + ipAddress + ":49153/upnp/control/basicevent1").openConnection();
				connection.setConnectTimeout(15000);
				connection.setReadTimeout(15000);
				connection.setUseCaches(false);
				connection.setRequestProperty("User-Agent", "NoMoreOversleeps/" + Main.VERSION);
				String datastr = "<?xml version=\"1.0\" encoding=\"utf-8\"?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body><u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\"><BinaryState>" + i + "</BinaryState></u:SetBinaryState></s:Body></s:Envelope>";
				byte[] data = datastr.getBytes(CommonUtils.charsetUTF8);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
				connection.setRequestProperty("Content-Length", Integer.toString(data.length));
				connection.setRequestProperty("Content-Language", "en-US");
				connection.setRequestProperty("Accept", "");
				connection.setRequestProperty("SOAPACTION", "\"urn:Belkin:service:basicevent:1#SetBinaryState\"");
				connection.setDoOutput(true);
				connection.connect();
				out = connection.getOutputStream();
				out.write(data);
				int responseCode = connection.getResponseCode();
				in = connection.getInputStream();
				String responseString = IOUtils.toString(in, CommonUtils.charsetUTF8);
				//System.out.println(responseString);
			}
			catch (Throwable t)
			{
				throw new Exception("Communication error while setting WeMo switch status on IP address '" + ipAddress + "'", t);
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
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void update() throws Exception
	{
		// nothing to do
	}

	@Override
	public void shutdown() throws Exception
	{
		// nothing to do
	}

}
