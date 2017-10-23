package com.tinytimrob.ppse.nmo.integration.tplink;

import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.NMOConfiguration;

public class IntegrationTPLink extends Integration
{
	public static final IntegrationTPLink INSTANCE = new IntegrationTPLink();

	private IntegrationTPLink()
	{
		super("tplink");
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.tplink.enabled;
	}

	@Override
	public void init() throws Exception
	{
		for (int i = 0; i < NMOConfiguration.instance.integrations.tplink.devices.length; i++)
		{
			final TPLinkDeviceEntry entry = NMOConfiguration.instance.integrations.tplink.devices[i];
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
				public boolean isSecret()
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
				public boolean isSecret()
				{
					return entry.secret;
				}
			});
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
