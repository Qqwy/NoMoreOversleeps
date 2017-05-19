package com.tinytimrob.ppse.nmo.integrations;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import com.google.gson.annotations.Expose;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.NMOConfiguration;

public class IntegrationMouse extends Integration
{
	public static volatile Point lastCursorPoint = MouseInfo.getPointerInfo().getLocation();

	public static class MouseConfiguration
	{
		@Expose
		public boolean enabled;
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.mouse.enabled;
	}

	@Override
	public void init()
	{

	}

	@Override
	public void update() throws Exception
	{
		PointerInfo pi = MouseInfo.getPointerInfo();
		Point epoint = pi == null ? lastCursorPoint : pi.getLocation();
		if (!epoint.equals(lastCursorPoint))
		{
			lastCursorPoint = epoint;
			MainDialog.resetActivityTimer();
		}
	}

	@Override
	public void shutdown()
	{

	}
}
