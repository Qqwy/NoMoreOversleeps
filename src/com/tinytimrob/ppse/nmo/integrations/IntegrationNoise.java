package com.tinytimrob.ppse.nmo.integrations;

import java.io.File;
import com.google.gson.annotations.Expose;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.NMOConfiguration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

public class IntegrationNoise extends Integration
{
	public static IntegrationNoise INSTANCE = new IntegrationNoise();
	public MediaPlayer player = null;
	public String noiseName = null;

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
				IntegrationNoise.this.stop();
			}

			@Override
			public String getName()
			{
				return "TURN OFF NOISE";
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

	public void play(StoredNoise noise)
	{
		Media media = new Media(new File(noise.path).toURI().toString());
		this.stop();
		this.noiseName = noise.name;
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
