package com.tinytimrob.ppse.nmo.integration.input;

import com.ivan.xinput.XInputDevice;
import com.ivan.xinput.enums.XInputButton;
import com.ivan.xinput.exceptions.XInputNotLoadedException;
import com.ivan.xinput.listener.XInputDeviceListener;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;

public class IntegrationXboxController extends Integration
{
	public IntegrationXboxController()
	{
		super("xboxController");
	}

	public static final IntegrationXboxController INSTANCE = new IntegrationXboxController();
	XInputDevice device;
	XInputDeviceListener listener;

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.INSTANCE.integrations.xboxController.enabled;
	}

	@Override
	public void init() throws XInputNotLoadedException
	{
		this.device = XInputDevice.getDeviceFor(0);
		this.device.addListener(new XInputDeviceListener()
		{
			@Override
			public void disconnected()
			{
				// do nothing
			}

			@Override
			public void connected()
			{
				// do nothing
			}

			@Override
			public void buttonChanged(XInputButton arg0, boolean arg1)
			{
				MainDialog.resetActivityTimer(IntegrationXboxController.this.id);
			}
		});
	}

	@Override
	public void update()
	{
		this.device.poll();
	}

	@Override
	public void shutdown()
	{
		if (this.device != null && this.listener != null)
		{
			this.device.removeListener(this.listener);
		}
	}
}
