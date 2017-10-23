package com.tinytimrob.ppse.nmo.integration.philipshue;

import com.google.gson.annotations.Expose;

public class PhilipsHueConfiguration
{
	@Expose
	public boolean enabled;

	@Expose
	public String bridgeIP = "";

	@Expose
	public String bridgeUsername = "";

	@Expose
	public String[] lights = new String[0];
}