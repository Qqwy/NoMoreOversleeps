package com.tinytimrob.ppse.nmo.integrations;

import com.google.gson.annotations.Expose;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.NMOConfiguration;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

public class IntegrationKeyboard extends Integration
{
	GlobalKeyboardHook keyboardHook;

	public static class KeyboardConfiguration
	{
		@Expose
		public boolean enabled;
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.keyboard.enabled;
	}

	@Override
	public void init()
	{
		this.keyboardHook = new GlobalKeyboardHook();
		this.keyboardHook.addKeyListener(new GlobalKeyAdapter()
		{
			@Override
			public void keyPressed(GlobalKeyEvent event)
			{
				MainDialog.lastActivityTime = System.currentTimeMillis();
			}

			@Override
			public void keyReleased(GlobalKeyEvent event)
			{
				MainDialog.lastActivityTime = System.currentTimeMillis();
			}
		});
	}

	@Override
	public void update() throws Exception
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void shutdown()
	{
		if (this.keyboardHook != null)
		{
			this.keyboardHook.shutdownHook();
		}
	}
}
