package com.tinytimrob.ppse.nmo.integration.tplink;

import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;

public class IntegrationTPLink extends Integration
{
	public static final IntegrationTPLink INSTANCE = new IntegrationTPLink();
	private int updateloop = 0;

	private IntegrationTPLink()
	{
		super("tplink");
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.INSTANCE.integrations.tplink.enabled;
	}

	@Override
	public void init() throws Exception
	{
		for (int i = 0; i < NMOConfiguration.INSTANCE.integrations.tplink.devices.length; i++)
		{
			final TPLinkDeviceEntry entry = NMOConfiguration.INSTANCE.integrations.tplink.devices[i];
			final TPLinkDevice device = new TPLinkDevice(entry.ipAddress);
			this.actions.put("/tplink/" + i + "/on", new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					device.toggle(true);
				}

				@Override
				public String getName()
				{
					return "TURN ON " + entry.name;
				}
				
				
				@Override
				public String getDescription()
				{
					return "Turns on TPLink device " + entry.name + ".\n\n" +  entry.description;
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
			this.actions.put("/tplink/" + i + "/off", new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					device.toggle(false);
				}

				@Override
				public String getName()
				{
					return "TURN OFF " + entry.name;
				}
				
				@Override
				public String getDescription()
				{
					return "Turns off TPLink device " + entry.name + ".\n\n" +  entry.description;
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

	@Override
	public void update() throws Exception
	{
		this.updateloop++;
		if (this.updateloop > 30)
		{
			this.updateloop -= 30;
			for (int i = 0; i < NMOConfiguration.INSTANCE.integrations.tplink.devices.length; i++)
			{
				final TPLinkDeviceEntry entry = NMOConfiguration.INSTANCE.integrations.tplink.devices[i];
				final TPLinkDevice device = new TPLinkDevice(entry.ipAddress);
				entry.isSwitchedOn = device.isOn();
			}
		}
	}

	@Override
	public void shutdown() throws Exception
	{
		// nothing to do
	}

}
