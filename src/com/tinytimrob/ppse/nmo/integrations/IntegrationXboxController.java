package com.tinytimrob.ppse.nmo.integrations;

import com.ivan.xinput.XInputDevice;
import com.ivan.xinput.enums.XInputButton;
import com.ivan.xinput.exceptions.XInputNotLoadedException;
import com.ivan.xinput.listener.XInputDeviceListener;
import com.tinytimrob.ppse.nmo.MainDialog;

public class IntegrationXboxController extends Integration
{
	public static IntegrationXboxController INSTANCE = new IntegrationXboxController();
	XInputDevice device;
	XInputDeviceListener listener;

	@Override
	public boolean isEnabled()
	{
		// TODO Auto-generated method stub
		return true;
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
				MainDialog.lastActivityTime = System.currentTimeMillis();
			}
		});
	}

	public void poll()
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
