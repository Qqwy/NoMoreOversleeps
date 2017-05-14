package com.tinytimrob.ppse.nmo;

import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Noise
{
	static MediaPlayer player = null;

	public static void play(File file)
	{
		Media media = new Media(file.toURI().toString());
		stop();
		player = new MediaPlayer(media);
		player.play();
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
