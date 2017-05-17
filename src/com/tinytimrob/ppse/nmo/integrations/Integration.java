package com.tinytimrob.ppse.nmo.integrations;

public abstract class Integration
{
	public abstract boolean isEnabled();

	public abstract void init() throws Exception;

	public abstract void shutdown() throws Exception;
}
