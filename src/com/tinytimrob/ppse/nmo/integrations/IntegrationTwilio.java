package com.tinytimrob.ppse.nmo.integrations;

import java.net.URI;
import org.apache.logging.log4j.Logger;
import com.google.gson.annotations.Expose;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.NMOConfiguration;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

public class IntegrationTwilio extends Integration
{
	private static final Logger log = LogWrapper.getLogger();

	public static class StoredPhoneNumber
	{
		@Expose
		public String name;

		@Expose
		public String number;
	}

	public static class TwilioConfiguration
	{
		@Expose
		public boolean enabled;

		@Expose
		public String accountSID = "";

		@Expose
		public String authToken = "";

		@Expose
		public String numberFrom = "";

		@Expose
		public String callingURI = "http://twimlets.com/holdmusic?Bucket=com.twilio.music.ambient";

		@Expose
		public StoredPhoneNumber[] phoneNumbers = new StoredPhoneNumber[0];
	}

	public void call(String fromS, String toS)
	{
		TwilioRestClient client = new TwilioRestClient.Builder(NMOConfiguration.instance.integrations.twilio.accountSID, NMOConfiguration.instance.integrations.twilio.authToken).build();
		PhoneNumber from = new PhoneNumber(fromS);
		PhoneNumber to = new PhoneNumber(toS);
		URI uri = URI.create(NMOConfiguration.instance.integrations.twilio.callingURI);
		Call call = Call.creator(to, from, uri).create(client);
		log.info("Call from " + fromS + " to " + toS + " executed with SID " + call.getSid());
	}

	public void call(StoredPhoneNumber number)
	{
		this.call(NMOConfiguration.instance.integrations.twilio.numberFrom, number.number);
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.twilio.enabled;
	}

	@Override
	public void init()
	{
		for (int i = 0; i < NMOConfiguration.instance.integrations.twilio.phoneNumbers.length; i++)
		{
			final StoredPhoneNumber number = NMOConfiguration.instance.integrations.twilio.phoneNumbers[i];
			this.actions.put("/twilio/" + i, new Action()
			{
				@Override
				public String getName()
				{
					return "CALL " + number.name + ": " + number.number;
				}

				@Override
				public void onAction() throws Exception
				{
					IntegrationTwilio.this.call(number);
				}

				@Override
				public boolean isSecret()
				{
					return false;
				}
			});
		}
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
}