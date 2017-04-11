package com.tinytimrob.ppse.nmo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tinytimrob.ppse.nmo.utils.Communicator;

public class Pavlok
{
	private static final Logger log = LogManager.getLogger();
	public static OAuthResponse RESPONSE = null;

	public static class OAuthToken
	{
		public OAuthToken(String code)
		{
			this.code = code;
		}

		@Expose
		@SerializedName("client_id")
		public String client_id = Main.CLIENT_ID;

		@Expose
		@SerializedName("client_secret")
		public String client_secret = Main.CLIENT_SECRET;

		@Expose
		@SerializedName("code")
		public String code = "";

		@Expose
		@SerializedName("grant_type")
		public String grant_type = "authorization_code";

		@Expose
		@SerializedName("redirect_uri")
		public String redirect_uri = Main.CLIENT_CALLBACK;
	}

	public static class OAuthResponse
	{
		@Expose
		@SerializedName("access_token")
		public String access_token;

		@Expose
		@SerializedName("token_type")
		public String token_type;

		@Expose
		@SerializedName("expires_in")
		public long expires_in;

		@Expose
		@SerializedName("refresh_token")
		public String refresh_token;

		@Expose
		@SerializedName("scope")
		public String scope;

		@Expose
		@SerializedName("created_at")
		public long created_at;

		@Expose
		@SerializedName("device")
		public String device;
	}

	public static void postAuthToken(String code) throws Exception
	{
		RESPONSE = Communicator.basicJsonMessage("get oauthtoken", "http://pavlok-mvp.herokuapp.com/oauth/token", new OAuthToken(code), OAuthResponse.class, false, null);
	}

	public static class Stimuli
	{
		public Stimuli(long value, String access_token, String reason)
		{
			this.value = value;
			this.access_token = access_token;
			this.reason = reason;
		}

		@Expose
		@SerializedName("value")
		public long value;

		@Expose
		@SerializedName("access_token")
		public String access_token;

		@Expose
		@SerializedName("reason")
		public String reason;
	}

	public static void beep(long amount, String reason) throws Exception
	{
		log.info("Sending: beep " + amount + " (" + reason + ")");
		Communicator.basicJsonMessage("beep", "http://pavlok-mvp.herokuapp.com/api/v1/stimuli/beep/" + amount, new Stimuli(amount, Configuration.instance.pavlokAuth.access_token, reason), null, false, Configuration.instance.pavlokAuth.access_token);
	}

	public static void vibration(long amount, String reason) throws Exception
	{
		log.info("Sending: vibration " + amount + " (" + reason + ")");
		Communicator.basicJsonMessage("vibration", "http://pavlok-mvp.herokuapp.com/api/v1/stimuli/vibration/" + amount, new Stimuli(amount, Configuration.instance.pavlokAuth.access_token, reason), null, false, Configuration.instance.pavlokAuth.access_token);
	}

	public static void shock(long amount, String reason) throws Exception
	{
		log.info("Sending: shock " + amount + " (" + reason + ")");
		Communicator.basicJsonMessage("shock", "http://pavlok-mvp.herokuapp.com/api/v1/stimuli/shock/" + amount, new Stimuli(amount, Configuration.instance.pavlokAuth.access_token, reason), null, false, Configuration.instance.pavlokAuth.access_token);
	}
}
