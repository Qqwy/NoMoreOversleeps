package com.tinytimrob.ppse.nmo.integrations;

import com.tinytimrob.ppse.nmo.MainDialog;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

public class IntegrationKeyboard extends Integration
{
	public static IntegrationKeyboard INSTANCE = new IntegrationKeyboard();
	GlobalKeyboardHook keyboardHook;

	@Override
	public boolean isEnabled()
	{
		// TODO Auto-generated method stub
		return true;
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
	public void shutdown()
	{
		if (this.keyboardHook != null)
		{
			this.keyboardHook.shutdownHook();
		}
	}
}
