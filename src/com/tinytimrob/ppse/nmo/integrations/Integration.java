package com.tinytimrob.ppse.nmo.integrations;

import java.util.LinkedHashMap;
import com.tinytimrob.ppse.nmo.ClickableButton;

public abstract class Integration
{
	public abstract boolean isEnabled();

	public abstract void init() throws Exception;

	public abstract void shutdown() throws Exception;

	public abstract LinkedHashMap<String, ClickableButton> getButtons();
}
