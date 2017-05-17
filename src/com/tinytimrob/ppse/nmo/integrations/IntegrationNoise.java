package com.tinytimrob.ppse.nmo.integrations;

import java.io.File;
import com.google.gson.annotations.Expose;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

public class IntegrationNoise extends Integration
{
	public static IntegrationNoise INSTANCE = new IntegrationNoise();
	public static MediaPlayer player = null;
	public static String noiseID = null;

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
		return false;
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

	public static void play(File file, String noiseID)
	{
		Media media = new Media(file.toURI().toString());
		stop();
		IntegrationNoise.noiseID = noiseID;
		player = new MediaPlayer(media);
		player.setOnEndOfMedia(new Runnable()
		{
			@Override
			public void run()
			{
				stop();
			}
		});
		player.play();
	}

	public static boolean isPlaying()
	{
		return player != null && player.getStatus() == Status.PLAYING;
	}

	public static void stop()
	{
		if (player != null)
		{
			player.stop();
			player.dispose();
			player = null;
		}
	}
}
