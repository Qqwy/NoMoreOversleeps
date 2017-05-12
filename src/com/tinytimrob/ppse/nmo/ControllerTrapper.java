package com.tinytimrob.ppse.nmo;

import com.ivan.xinput.XInputDevice;
import com.ivan.xinput.enums.XInputButton;
import com.ivan.xinput.exceptions.XInputNotLoadedException;
import com.ivan.xinput.listener.XInputDeviceListener;

public class ControllerTrapper
{
	static XInputDevice device;
	static XInputDeviceListener listener;

	public static void init() throws XInputNotLoadedException
	{
		device = XInputDevice.getDeviceFor(0);
		device.addListener(new XInputDeviceListener()
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

	public static void shutdown()
	{
		if (device != null && listener != null)
		{
			device.removeListener(listener);
		}
	}
}
