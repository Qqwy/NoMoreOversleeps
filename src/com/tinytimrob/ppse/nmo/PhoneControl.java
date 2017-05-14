package com.tinytimrob.ppse.nmo;

import java.net.URI;
import org.apache.logging.log4j.Logger;
import com.tinytimrob.common.LogWrapper;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

public class PhoneControl
{
	private static final Logger log = LogWrapper.getLogger();

	public static void call(String fromS, String toS)
	{
		TwilioRestClient client = new TwilioRestClient.Builder(NMOConfiguration.instance.twilioAccountSID, NMOConfiguration.instance.twilioAuthToken).build();
		PhoneNumber from = new PhoneNumber(fromS);
		PhoneNumber to = new PhoneNumber(toS);
		URI uri = URI.create(NMOConfiguration.instance.twilioCallingURI);
		Call call = Call.creator(to, from, uri).create(client);
		log.info("Call from " + fromS + " to " + toS + " executed with SID " + call.getSid());
	}

	public static void callSwitchboard()
	{
		call(NMOConfiguration.instance.twilioNumberFrom, NMOConfiguration.instance.phoneSwitchboard);
	}

	public static void callMobile()
	{
		call(NMOConfiguration.instance.twilioNumberFrom, NMOConfiguration.instance.phoneMobile);
	}
}