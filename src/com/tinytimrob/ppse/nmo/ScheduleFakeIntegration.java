package com.tinytimrob.ppse.nmo;

import com.tinytimrob.common.Configuration;

public class ScheduleFakeIntegration extends Integration
{
	public ScheduleFakeIntegration()
	{
		super("schedule");
	}

	public static ScheduleFakeIntegration INSTANCE = new ScheduleFakeIntegration();

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void init() throws Exception
	{
		if (NMOConfiguration.instance.scheduleStartedOn > 0)
		{
			this.actions.put("/schedule/resetLastOversleep", new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					NMOConfiguration.instance.scheduleLastOversleep = System.currentTimeMillis();
					try
					{
						Configuration.save();
					}
					catch (Throwable t)
					{
						t.printStackTrace(); // damn
					}
				}

				@Override
				public String getName()
				{
					return "RESET LAST OVERSLEEP";
				}

				@Override
				public boolean isSecret()
				{
					return true;
				}
			});
		}
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
