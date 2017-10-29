package com.tinytimrob.ppse.nmo.integration.noise;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;
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

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.INSTANCE.integrations.noise.enabled;
	}

	@Override
	public void init()
	{
		for (int i = 0; i < NMOConfiguration.INSTANCE.integrations.noise.noises.length; i++)
		{
			final StoredNoise noise = NMOConfiguration.INSTANCE.integrations.noise.noises[i];
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
				
				public String getDescription()
				{
					return "Plays the audio clip `" + noise.name + "`.\n\n" + noise.description;
				}

				@Override
				public boolean isHiddenFromFrontend()
				{
					return noise.hidden;
				}

				@Override
				public boolean isHiddenFromWebUI()
				{
					return noise.secret;
				}

				@Override
				public boolean isBlockedFromWebUI()
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
			public String getDescription()
			{
				return "Stops all noises from playing immediately.";
			}

			@Override
			public boolean isHiddenFromFrontend()
			{
				return false;
			}

			@Override
			public boolean isHiddenFromWebUI()
			{
				return false;
			}

			@Override
			public boolean isBlockedFromWebUI()
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
		File file = CommonUtils.redirectRelativePathToAppDirectory(noise.path);
		Media media = new Media(file.getAbsoluteFile().toURI().toString());
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
