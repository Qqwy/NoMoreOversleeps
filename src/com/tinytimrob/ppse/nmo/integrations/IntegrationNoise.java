package com.tinytimrob.ppse.nmo.integrations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.NMOConfiguration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class IntegrationNoise extends Integration
{
	public IntegrationNoise()
	{
		super("noise");
	}

	public static final IntegrationNoise INSTANCE = new IntegrationNoise();
	public static List<PlayingNoise> PLAYING_NOISES = Collections.synchronizedList(new ArrayList<PlayingNoise>());

	public static class PlayingNoise
	{
		PlayingNoise(MediaPlayer player, String name)
		{
			this.player = player;
			this.name = name;
		}

		final MediaPlayer player;
		final String name;

		public void stop()
		{
			if (this.player != null)
			{
				this.player.stop();
				this.player.dispose();
			}
			PLAYING_NOISES.remove(this);
		}
	}

	public static class StoredNoise
	{
		@Expose
		public String name;

		@Expose
		public String path;

		@Expose
		public boolean secret;
	}

	public static class NoiseConfiguration
	{
		@Expose
		public boolean enabled;

		@Expose
		public StoredNoise[] noises = new StoredNoise[0];
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.noise.enabled;
	}

	@Override
	public void init()
	{
		for (int i = 0; i < NMOConfiguration.instance.integrations.noise.noises.length; i++)
		{
			final StoredNoise noise = NMOConfiguration.instance.integrations.noise.noises[i];
			this.actions.put("/noise/" + i, new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					IntegrationNoise.this.play(noise);
				}

				@Override
				public String getName()
				{
					return "PLAY " + noise.name;
				}

				@Override
				public boolean isSecret()
				{
					return noise.secret;
				}
			});
		}
		this.actions.put("/noise/stop", new Action()
		{
			@Override
			public void onAction() throws Exception
			{
				PlayingNoise[] noises = PLAYING_NOISES.toArray(new PlayingNoise[0]);
				for (PlayingNoise noise : noises)
				{
					noise.stop();
				}
			}

			@Override
			public String getName()
			{
				return "STOP ALL NOISES";
			}

			@Override
			public boolean isSecret()
			{
				return false;
			}
		});
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

	public String getNoiseList()
	{
		PlayingNoise[] noises = PLAYING_NOISES.toArray(new PlayingNoise[0]);
		if (noises.length == 0)
		{
			return "STOPPED";
		}
		else
		{
			String pnl = "";
			for (int i = 0; i < noises.length; i++)
			{
				pnl += (i > 0 ? ", " : "PLAYING (" + noises.length + "): ") + noises[i].name;
			}
			return pnl;
		}
	}

	public void play(StoredNoise noise)
	{
		Media media = new Media(new File(noise.path).toURI().toString());
		final PlayingNoise playingNoise = new PlayingNoise(new MediaPlayer(media), noise.name);
		Runnable endHook = new Runnable()
		{
			@Override
			public void run()
			{
				playingNoise.stop();
			}
		};
		playingNoise.player.setOnEndOfMedia(endHook);
		playingNoise.player.setOnError(endHook);
		PLAYING_NOISES.add(playingNoise);
		playingNoise.player.play();
	}
}
