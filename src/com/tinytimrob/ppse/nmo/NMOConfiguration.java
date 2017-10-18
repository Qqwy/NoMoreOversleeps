package com.tinytimrob.ppse.nmo;

import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.tinytimrob.ppse.nmo.integration.cmd.CommandLineConfiguration;
import com.tinytimrob.ppse.nmo.integration.discord.DiscordConfiguration;
import com.tinytimrob.ppse.nmo.integration.filewriter.FileWriterConfiguration;
import com.tinytimrob.ppse.nmo.integration.input.KeyboardConfiguration;
import com.tinytimrob.ppse.nmo.integration.input.MidiConfiguration;
import com.tinytimrob.ppse.nmo.integration.input.MouseConfiguration;
import com.tinytimrob.ppse.nmo.integration.input.XboxControllerConfiguration;
import com.tinytimrob.ppse.nmo.integration.noise.NoiseConfiguration;
import com.tinytimrob.ppse.nmo.integration.pavlok.PavlokConfiguration;
import com.tinytimrob.ppse.nmo.integration.philipshue.PhilipsHueConfiguration;
import com.tinytimrob.ppse.nmo.integration.twilio.TwilioConfiguration;

public class NMOConfiguration
{
	public static NMOConfiguration instance;

	@Expose
	public String hostname = "";

	@Expose
	public int jettyPort = 19992;

	@Expose
	public String webcamName = "";

	@Expose
	public int webcamFrameSkip = 2;

	@Expose
	public int garbageCollectionFrequency = 3600;

	@Expose
	public String scheduleName = "";

	@Expose
	public ArrayList<SleepEntry> schedule = new ArrayList<SleepEntry>();

	@Expose
	public long scheduleStartedOn = 0;

	@Expose
	public long scheduleLastOversleep = 0;

	@Expose
	public long schedulePersonalBest = 0;

	@Expose
	public ArrayList<ActivityTimer> timers = new ArrayList<ActivityTimer>();

	/*
	@Expose
	public int activityWarningTimeInitialMs = 300000;

	@Expose
	public int activityWarningTimeIncrementMs = 10000;
	*/

	@Expose
	public int oversleepWarningThreshold = 5;

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
		public String[] oversleepWarning = new String[0];

		@Expose
		public String[] activityWarning2 = new String[0];

		@Expose
		public String[] pauseInitiated = new String[0];

		@Expose
		public String[] pauseCancelled = new String[0];

		@Expose
		public String[] pauseExpired = new String[0];

		@Expose
		public CustomEventAction[] custom = new CustomEventAction[0];
	}

	@Expose
	public EventConfiguration events = new EventConfiguration();

	public static class IntegrationConfiguration
	{
		@Expose
		public KeyboardConfiguration keyboard = new KeyboardConfiguration();

		@Expose
		public MouseConfiguration mouse = new MouseConfiguration();

		@Expose
		public XboxControllerConfiguration xboxController = new XboxControllerConfiguration();

		@Expose
		public MidiConfiguration midiTransmitter = new MidiConfiguration();

		@Expose
		public PavlokConfiguration pavlok = new PavlokConfiguration();

		@Expose
		public TwilioConfiguration twilio = new TwilioConfiguration();

		@Expose
		public PhilipsHueConfiguration philipsHue = new PhilipsHueConfiguration();

		@Expose
		public NoiseConfiguration noise = new NoiseConfiguration();

		@Expose
		public CommandLineConfiguration cmd = new CommandLineConfiguration();

		@Expose
		public FileWriterConfiguration fileWriter = new FileWriterConfiguration();

		@Expose
		public DiscordConfiguration discord = new DiscordConfiguration();
	}

	@Expose
	public IntegrationConfiguration integrations = new IntegrationConfiguration();
}
