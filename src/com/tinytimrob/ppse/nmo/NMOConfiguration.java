package com.tinytimrob.ppse.nmo;

import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
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
	@SerializedName("schedule")
	public ArrayList<SleepEntry> schedule = new ArrayList<SleepEntry>();

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
