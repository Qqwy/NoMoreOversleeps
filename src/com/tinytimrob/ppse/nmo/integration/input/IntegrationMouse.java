package com.tinytimrob.ppse.nmo.integration.input;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.NMOConfiguration;

public class IntegrationMouse extends Integration
{
	public IntegrationMouse()
	{
		super("mouse");
	}

	public static final IntegrationMouse INSTANCE = new IntegrationMouse();
	public static volatile Point lastCursorPoint = MouseInfo.getPointerInfo().getLocation();

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
			MainDialog.resetActivityTimer(this.id);
		}
	}

	@Override
	public void shutdown()
	{

	}
}
