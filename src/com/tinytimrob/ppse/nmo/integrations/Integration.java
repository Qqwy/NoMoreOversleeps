package com.tinytimrob.ppse.nmo.integrations;

import java.util.LinkedHashMap;
import com.tinytimrob.ppse.nmo.Action;

public abstract class Integration
{
	public abstract boolean isEnabled();

	public abstract void init() throws Exception;

	public abstract void update() throws Exception;

	public abstract void shutdown() throws Exception;

	public final LinkedHashMap<String, Action> actions = new LinkedHashMap<String, Action>();

	public final LinkedHashMap<String, Action> getActions()
	{
		return this.actions;
	}
}
