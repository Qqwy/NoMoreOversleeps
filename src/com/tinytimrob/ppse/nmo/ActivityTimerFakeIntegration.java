package com.tinytimrob.ppse.nmo;

import com.tinytimrob.ppse.nmo.config.NMOConfiguration;

public class ActivityTimerFakeIntegration extends Integration
{
	public ActivityTimerFakeIntegration()
	{
		super("timer");
	}

	public static ActivityTimerFakeIntegration INSTANCE = new ActivityTimerFakeIntegration();

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void init() throws Exception
	{
		if (NMOConfiguration.INSTANCE.timers.isEmpty())
		{
			ActivityTimer activityWarningTimer = new ActivityTimer();
			activityWarningTimer.name = "DEFAULT TIMER";
			activityWarningTimer.secondsForFirstWarning = 300;
			activityWarningTimer.secondsForSubsequentWarnings = 10;
			NMOConfiguration.INSTANCE.timers.add(activityWarningTimer);
			NMOConfiguration.save();
		}
		final int numTimers = NMOConfiguration.INSTANCE.timers.size();
		if (numTimers > 1)
		{
			for (int i = 0; i < numTimers; i++)
			{
				final ActivityTimer timer = NMOConfiguration.INSTANCE.timers.get(i);
				this.actions.put("/timer/" + i, new Action()
				{
					@Override
					public void onAction() throws Exception
					{
						MainDialog.pendingTimer = timer;
					}

					@Override
					public String getName()
					{
						return "SET TIMER: " + timer.name + " (" + timer.secondsForFirstWarning + "s/" + timer.secondsForSubsequentWarnings + "s)";
					}

					@Override
					public boolean isHiddenFromFrontend()
					{
						return false;
					}

					@Override
					public boolean isHiddenFromWebUI()
					{
						return numTimers < 2;
					}

					@Override
					public boolean isBlockedFromWebUI()
					{
						return numTimers < 2;
					}
				});
			}
		}
		MainDialog.timer = NMOConfiguration.INSTANCE.timers.get(0);
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
}
