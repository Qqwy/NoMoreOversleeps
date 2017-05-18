package com.tinytimrob.ppse.nmo;

import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.tinytimrob.ppse.nmo.integrations.IntegrationKeyboard.KeyboardConfiguration;
import com.tinytimrob.ppse.nmo.integrations.IntegrationNoise.NoiseConfiguration;
import com.tinytimrob.ppse.nmo.integrations.IntegrationPavlok.PavlokConfiguration;
import com.tinytimrob.ppse.nmo.integrations.IntegrationPhilipsHue.PhilipsHueConfiguration;
import com.tinytimrob.ppse.nmo.integrations.IntegrationTwilio.TwilioConfiguration;
import com.tinytimrob.ppse.nmo.integrations.IntegrationXboxController.XboxControllerConfiguration;

public class NMOConfiguration
{
	public static NMOConfiguration instance;

	@Expose
	public int jettyPort = 19992;

	@Expose
	public String webcamName = "";

	@Expose
	public ArrayList<SleepEntry> schedule = new ArrayList<SleepEntry>();

	@Expose
	public int sleepBlockApproachingTimeMins = 5;

	@Expose
	public int activityWarningTimeInitialMs = 300000;

	@Expose
	public int activityWarningTimeIncrementMs = 10000;

	public static class EventConfiguration
	{
		@Expose
		public String[] sleepBlockApproaching = new String[0];

		@Expose
		public String[] sleepBlockStarted = new String[0];

		@Expose
		public String[] sleepBlockEnded = new String[0];

		@Expose
		public String[] activityWarning1 = new String[0];

		@Expose
		public String[] activityWarning2 = new String[0];

		@Expose
		public String[] pauseInitiated = new String[0];

		@Expose
		public String[] pauseCancelled = new String[0];

		@Expose
		public String[] pauseExpired = new String[0];
	}

	@Expose
	public EventConfiguration events = new EventConfiguration();

	public static class IntegrationConfiguration
	{
		@Expose
		public KeyboardConfiguration keyboard = new KeyboardConfiguration();

		@Expose
		public XboxControllerConfiguration xboxController = new XboxControllerConfiguration();

		@Expose
		public PavlokConfiguration pavlok = new PavlokConfiguration();

		@Expose
		public TwilioConfiguration twilio = new TwilioConfiguration();

		@Expose
		public PhilipsHueConfiguration philipsHue = new PhilipsHueConfiguration();

		@Expose
		public NoiseConfiguration noise = new NoiseConfiguration();
	}

	@Expose
	public IntegrationConfiguration integrations = new IntegrationConfiguration();
}
