package com.tinytimrob.ppse.nmo.integration.wemo;

import com.google.gson.annotations.Expose;

public class WemoDeviceEntry
{
	@Expose
	public String name = "";

	@Expose
	public String ipAddress = "";

	@Expose
	public boolean secret;
}
