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

	@Expose
	@SerializedName("twilioAccountSID")
	public String twilioAccountSID = "";

	@Expose
	@SerializedName("twilioAuthToken")
	public String twilioAuthToken = "";

	@Expose
	@SerializedName("twilioNumberFrom")
	public String twilioNumberFrom = "";

	@Expose
	@SerializedName("twilioCallingURI")
	public String twilioCallingURI = "http://twimlets.com/holdmusic?Bucket=com.twilio.music.ambient";

	@Expose
	@SerializedName("phoneSwitchboard")
	public String phoneSwitchboard = "";

	@Expose
	@SerializedName("phoneMobile")
	public String phoneMobile = "";

	@Expose
	@SerializedName("noisePath")
	public String noisePath = "";

	@Expose
	@SerializedName("hueBridgeIP")
	public String hueBridgeIP = "";

	@Expose
	@SerializedName("hueBridgeUsername")
	public String hueBridgeUsername = "";
}
