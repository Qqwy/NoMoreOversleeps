package com.tinytimrob.ppse.nmo.integrations;

import org.apache.logging.log4j.Logger;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Main;
import com.tinytimrob.ppse.nmo.NMOConfiguration;
import com.tinytimrob.ppse.nmo.utils.Communicator;

public class IntegrationPavlok extends Integration
{
	public IntegrationPavlok()
	{
		super("pavlok");
	}

	public static final IntegrationPavlok INSTANCE = new IntegrationPavlok();
	private static final Logger log = LogWrapper.getLogger();

	public static class PavlokConfiguration
	{
		@Expose
		public boolean enabled;

		@Expose
		public OAuthResponse auth = null;
	}

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

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.pavlok.enabled;
	}

	@Override
	public void init()
	{
		this.actions.put("/pavlok/led", new Action()
		{
			@Override
			public void onAction() throws Exception
			{
				IntegrationPavlok.this.beep(255, "Manually triggered LED flash");
			}

			@Override
			public String getName()
			{
				return "FLASH PAVLOK LED";
			}

			@Override
			public boolean isSecret()
			{
				return false;
			}
		});
		this.actions.put("/pavlok/beep", new Action()
		{
			@Override
			public void onAction() throws Exception
			{
				IntegrationPavlok.this.beep(255, "Manually triggered beep");
			}

			@Override
			public String getName()
			{
				return "BEEP PAVLOK";
			}

			@Override
			public boolean isSecret()
			{
				return false;
			}
		});
		this.actions.put("/pavlok/vibration", new Action()
		{
			@Override
			public void onAction() throws Exception
			{
				IntegrationPavlok.this.vibration(255, "Manually triggered vibration");
			}

			@Override
			public String getName()
			{
				return "VIBRATE PAVLOK";
			}

			@Override
			public boolean isSecret()
			{
				return false;
			}
		});
		this.actions.put("/pavlok/shock", new Action()
		{
			@Override
			public void onAction() throws Exception
			{
				IntegrationPavlok.this.shock(255, "Manually triggered shock");
			}

			@Override
			public String getName()
			{
				return "SHOCK PAVLOK";
			}

			@Override
			public boolean isSecret()
			{
				return false;
			}
		});
	}

	@Override
	public void update() throws Exception
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void shutdown()
	{
		// TODO Auto-generated method stub
	}

	public static OAuthResponse postAuthToken(String code) throws Exception
	{
		return Communicator.basicJsonMessage("get oauthtoken", "http://pavlok-mvp.herokuapp.com/oauth/token", new OAuthToken(code), OAuthResponse.class, false, null);
	}

	public void led(long amount, String reason) throws Exception
	{
		log.info("Sending: led " + amount + " (" + reason + ")");
		Communicator.basicJsonMessage("led", "http://pavlok-mvp.herokuapp.com/api/v1/stimuli/led/" + amount, new Stimuli(amount, NMOConfiguration.instance.integrations.pavlok.auth.access_token, reason), null, false, NMOConfiguration.instance.integrations.pavlok.auth.access_token);
	}

	public void beep(long amount, String reason) throws Exception
	{
		log.info("Sending: beep " + amount + " (" + reason + ")");
		Communicator.basicJsonMessage("beep", "http://pavlok-mvp.herokuapp.com/api/v1/stimuli/beep/" + amount, new Stimuli(amount, NMOConfiguration.instance.integrations.pavlok.auth.access_token, reason), null, false, NMOConfiguration.instance.integrations.pavlok.auth.access_token);
	}

	public void vibration(long amount, String reason) throws Exception
	{
		log.info("Sending: vibration " + amount + " (" + reason + ")");
		Communicator.basicJsonMessage("vibration", "http://pavlok-mvp.herokuapp.com/api/v1/stimuli/vibration/" + amount, new Stimuli(amount, NMOConfiguration.instance.integrations.pavlok.auth.access_token, reason), null, false, NMOConfiguration.instance.integrations.pavlok.auth.access_token);
	}

	public void shock(long amount, String reason) throws Exception
	{
		log.info("Sending: shock " + amount + " (" + reason + ")");
		Communicator.basicJsonMessage("shock", "http://pavlok-mvp.herokuapp.com/api/v1/stimuli/shock/" + amount, new Stimuli(amount, NMOConfiguration.instance.integrations.pavlok.auth.access_token, reason), null, false, NMOConfiguration.instance.integrations.pavlok.auth.access_token);
	}
}
