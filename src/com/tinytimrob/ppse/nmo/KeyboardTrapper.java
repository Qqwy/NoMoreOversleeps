package com.tinytimrob.ppse.nmo;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

public class KeyboardTrapper
{
	static GlobalKeyboardHook keyboardHook;

	public static void init()
	{
		keyboardHook = new GlobalKeyboardHook();
		keyboardHook.addKeyListener(new GlobalKeyAdapter()
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

	public static void shutdown()
	{
		if (keyboardHook != null)
		{
			keyboardHook.shutdownHook();
		}
	}
}
