package com.tinytimrob.ppse.nmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tinytimrob.ppse.nmo.Pavlok.OAuthResponse;

public class NMOConfiguration
{
	public static NMOConfiguration instance;

	@Expose
	@SerializedName("pavlokAuth")
	public OAuthResponse pavlokAuth = null;

	@Expose
	@SerializedName("jettyPort")
	public int jettyPort = 19992;
}
