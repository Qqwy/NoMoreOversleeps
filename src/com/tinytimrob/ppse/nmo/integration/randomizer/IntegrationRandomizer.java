package com.tinytimrob.ppse.nmo.integration.randomizer;

import java.util.concurrent.ThreadLocalRandom;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;

public class IntegrationRandomizer extends Integration
{
	public static final IntegrationRandomizer INSTANCE = new IntegrationRandomizer();

	private IntegrationRandomizer()
	{
		super("randomizer");
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.INSTANCE.integrations.randomizer.enabled;
	}

	@Override
	public void init() throws Exception
	{
		for (int i = 0; i < NMOConfiguration.INSTANCE.integrations.randomizer.randomizers.length; i++)
		{
			final RandomizerEntry randomizer = NMOConfiguration.INSTANCE.integrations.randomizer.randomizers[i];
			this.actions.put("/randomizer/" + i, new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					int option = ThreadLocalRandom.current().nextInt(randomizer.actions.length);
					String randPath = randomizer.actions[option];
					MainDialog.triggerEvent("Randomizer " + randomizer.name + " fired", new String[] { randPath });
				}

				@Override
				public String getName()
				{
					return "RANDOM " + randomizer.name;
				}
				
				
				@Override
				public String getDescription()
				{
					return randomizer.description;
				}

				@Override
				public boolean isHiddenFromFrontend()
				{
					return randomizer.hidden;
				}

				@Override
				public boolean isHiddenFromWebUI()
				{
					return randomizer.secret;
				}

				@Override
				public boolean isBlockedFromWebUI()
				{
					return randomizer.secret;
				}
			});
		}
	}

	@Override
	public void update() throws Exception
	{
		// nothing to do
	}

	@Override
	public void shutdown() throws Exception
	{
		// nothing to do
	}

}
