package com.tinytimrob.ppse.nmo;

import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

public class Noise
{
	public static MediaPlayer player = null;
	public static String noiseID = null;

	public static void play(File file, String noiseID)
	{
		Media media = new Media(file.toURI().toString());
		stop();
		Noise.noiseID = noiseID;
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
