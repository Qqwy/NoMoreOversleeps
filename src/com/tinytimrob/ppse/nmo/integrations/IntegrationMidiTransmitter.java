package com.tinytimrob.ppse.nmo.integrations;

import java.util.ArrayList;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import org.apache.logging.log4j.Logger;
import com.google.gson.annotations.Expose;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.NMOConfiguration;

public class IntegrationMidiTransmitter extends Integration
{
	private static final Logger log = LogWrapper.getLogger();
	int transmitterLoop = 0;

	public static class MidiConfiguration
	{
		@Expose
		public boolean enabled;

		@Expose
		public ArrayList<String> transmitters = new ArrayList<String>();
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.midiTransmitter.enabled;
	}

	@Override
	public void init()
	{

	}

	@Override
	public void update() throws Exception
	{
		this.transmitterLoop++;
		if (this.transmitterLoop % 60 == 0)
		{
			MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
			for (int i = 0; i < infos.length; i++)
			{
				MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
				if (NMOConfiguration.instance.integrations.midiTransmitter.transmitters.contains(device.getDeviceInfo().getName()))
				{
					if (!device.isOpen() && device.getMaxTransmitters() != 0)
					{
						final String name = device.getDeviceInfo().getName() + "/" + device.getDeviceInfo().getDescription() + "/" + device.getDeviceInfo().getVendor();
						log.info("Connected MIDI device: " + name);
						device.getTransmitter().setReceiver(new Receiver()
						{
							@Override
							public void send(MidiMessage message, long timeStamp)
							{
								MainDialog.resetActivityTimer();
							}

							@Override
							public void close()
							{
								log.info("Disconnected MIDI device: " + name);
							}
						});
						device.open();
					}
				}
			}
		}
	}

	@Override
	public void shutdown()
	{

	}
}
