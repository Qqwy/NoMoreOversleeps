package com.tinytimrob.ppse.nmo.integrations;

import java.io.File;
import com.google.gson.annotations.Expose;
import com.tinytimrob.ppse.nmo.NMOConfiguration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

public class IntegrationNoise extends Integration
{
	public static IntegrationNoise INSTANCE = new IntegrationNoise();
	public MediaPlayer player = null;
	public String noiseID = null;

	public static class NoiseConfiguration
	{
		@Expose
		public boolean enabled;

		@Expose
		public String noisePathLong = "";

		@Expose
		public String noisePathShort = "";

		@Expose
		public String noisePathUpcomingNap = "";
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.noise.enabled;
	}

	@Override
	public void init()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void shutdown()
	{
		// TODO Auto-generated method stub
	}

	public void play(File file, String noiseID)
	{
		Media media = new Media(file.toURI().toString());
		this.stop();
		this.noiseID = noiseID;
		this.player = new MediaPlayer(media);
		this.player.setOnEndOfMedia(new Runnable()
		{
			@Override
			public void run()
			{
				IntegrationNoise.this.stop();
			}
		});
		this.player.play();
	}

	public boolean isPlaying()
	{
		return this.player != null && this.player.getStatus() == Status.PLAYING;
	}

	public void stop()
	{
		if (this.player != null)
		{
			this.player.stop();
			this.player.dispose();
			this.player = null;
		}
	}
}
