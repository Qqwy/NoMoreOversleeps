package com.tinytimrob.ppse.nmo.integration.tplink;

import com.google.gson.annotations.Expose;

public class TPLinkDeviceEntry
{
	@Expose
	public String name = "";

	@Expose
	public String ipAddress = "";

	@Expose
	public boolean secret;
}
