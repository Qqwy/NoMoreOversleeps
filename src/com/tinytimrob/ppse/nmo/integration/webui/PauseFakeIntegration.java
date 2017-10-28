package com.tinytimrob.ppse.nmo.integration.webui;

import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;
import javafx.application.Platform;

public class PauseFakeIntegration extends Integration
{
	public PauseFakeIntegration()
	{
		super("pause");
	}

	public static PauseFakeIntegration INSTANCE = new PauseFakeIntegration();

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.webUI.allowRemotePauseControl;
	}

	@Override
	public void init() throws Exception
	{
		this.actions.put("/pause/0", new Action()
		{
			@Override
			public void onAction() throws Exception
			{
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						MainDialog.pausedUntil = 0;
						MainDialog.triggerEvent("Triggered unpause", NMOConfiguration.instance.events.pauseCancelled);
					}
				});
			}

			@Override
			public boolean isHiddenFromFrontend()
			{
				return true;
			}

			@Override
			public boolean isHiddenFromWebUI()
			{
				return true;
			}

			@Override
			public boolean isBlockedFromWebUI()
			{
				return false;
			}

			@Override
			public String getName()
			{
				return "TRIGGERED UNPAUSE";
			}
		});

		for (int i = 1; i <= 720; i++)
		{
			final int j = i;
			this.actions.put("/pause/" + i, new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					Platform.runLater(new Runnable()
					{
						@Override
						public void run()
						{
							MainDialog.pausedUntil = MainDialog.now + (j * 60000);
							MainDialog.pauseReason = getName();
							MainDialog.triggerEvent("Triggered pause for " + j + " minutes (until " + CommonUtils.dateFormatter.format(MainDialog.pausedUntil) + ")", NMOConfiguration.instance.events.pauseInitiated);
						}
					});
				}

				@Override
				public boolean isHiddenFromFrontend()
				{
					return true;
				}

				@Override
				public boolean isHiddenFromWebUI()
				{
					return true;
				}

				@Override
				public boolean isBlockedFromWebUI()
				{
					return false;
				}

				@Override
				public String getName()
				{
					return "TRIGGERED " + j + " MINUTE PAUSE";
				}
			});
		}
	}

	@Override
	public void update() throws Exception
	{

	}

	@Override
	public void shutdown() throws Exception
	{

	}
}
