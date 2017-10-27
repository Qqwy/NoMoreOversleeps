package com.tinytimrob.ppse.nmo.integration.filewriter;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import com.ivan.xinput.exceptions.XInputNotLoadedException;
import com.tinytimrob.common.PlatformData;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;
import com.tinytimrob.ppse.nmo.config.NMOStatistics;
import com.tinytimrob.ppse.nmo.utils.FormattingHelper;

public class IntegrationFileWriter extends Integration
{
	public IntegrationFileWriter()
	{
		super("fileWriter");
	}

	public static final IntegrationFileWriter INSTANCE = new IntegrationFileWriter();
	private static int lastSecond;
	private static File scheduleNameFile;
	private static File scheduleStartedOnFile;
	private static File scheduleLastOversleepFile;
	private static File schedulePersonalBestFile;
	private static File timeToNextSleepBlockFile;

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.fileWriter.scheduleName || NMOConfiguration.instance.integrations.fileWriter.scheduleStartedOn || NMOConfiguration.instance.integrations.fileWriter.scheduleLastOversleep || NMOConfiguration.instance.integrations.fileWriter.schedulePersonalBest || NMOConfiguration.instance.integrations.fileWriter.timeToNextSleepBlock;
	}

	@Override
	public void init() throws XInputNotLoadedException
	{
		File directory = new File(PlatformData.installationDirectory, "out");
		directory.mkdirs();
		scheduleNameFile = new File(directory, "scheduleName");
		scheduleStartedOnFile = new File(directory, "scheduleStartedOn");
		scheduleLastOversleepFile = new File(directory, "scheduleLastOversleep");
		schedulePersonalBestFile = new File(directory, "schedulePersonalBest");
		timeToNextSleepBlockFile = new File(directory, "timeToNextSleepBlock");
	}

	@Override
	public void update()
	{
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		long now = calendar.getTimeInMillis();
		if (lastSecond != second)
		{
			lastSecond = second;
			try
			{
				if (NMOConfiguration.instance.integrations.fileWriter.scheduleName)
				{
					FileUtils.writeStringToFile(scheduleNameFile, NMOConfiguration.instance.scheduleName, Charsets.UTF_8, false);
				}
				if (NMOConfiguration.instance.integrations.fileWriter.scheduleStartedOn)
				{
					FileUtils.writeStringToFile(scheduleStartedOnFile, FormattingHelper.formatTimeElapsedWithDays(NMOStatistics.instance.scheduleStartedOn == 0 ? 0 : now, NMOStatistics.instance.scheduleStartedOn), Charsets.UTF_8, false);
				}
				if (NMOConfiguration.instance.integrations.fileWriter.scheduleLastOversleep)
				{
					FileUtils.writeStringToFile(scheduleLastOversleepFile, FormattingHelper.formatTimeElapsedWithDays(NMOStatistics.instance.scheduleStartedOn == 0 ? 0 : now, NMOStatistics.instance.scheduleLastOversleep), Charsets.UTF_8, false);
				}
				if (NMOConfiguration.instance.integrations.fileWriter.schedulePersonalBest)
				{
					FileUtils.writeStringToFile(schedulePersonalBestFile, MainDialog.nextSleepBlock == null ? "N/A" : FormattingHelper.formatTimeElapsedWithDays(NMOStatistics.instance.schedulePersonalBest, 0), Charsets.UTF_8, false);
				}
				if (NMOConfiguration.instance.integrations.fileWriter.timeToNextSleepBlock)
				{
					int currentMinuteOfDay = ((hour * 60) + minute);
					boolean currentlySleeping = MainDialog.nextSleepBlock == null ? false : MainDialog.nextSleepBlock.containsTimeValue(System.currentTimeMillis());
					String pros = MainDialog.nextActivityWarningID >= NMOConfiguration.instance.oversleepWarningThreshold ? "OVERSLEEPING" : MainDialog.nextActivityWarningID > 0 ? "MISSING" : "AWAKE";
					if (currentlySleeping)
					{
						long tims = MainDialog.nextSleepBlock.nextEndTime;
						FileUtils.writeStringToFile(timeToNextSleepBlockFile, MainDialog.nextSleepBlock.name + " [ends in " + FormattingHelper.formatTimeElapsedWithoutDays(tims, now - 59999) + "]", Charsets.UTF_8, false);
					}
					else if (MainDialog.isCurrentlyPaused.get())
					{
						FileUtils.writeStringToFile(timeToNextSleepBlockFile, "AFK [" + MainDialog.pauseReason + " - " + FormattingHelper.formatTimeElapsedWithoutDays(MainDialog.pausedUntil, now - 59999) + " left]", Charsets.UTF_8, false);
					}
					else if (MainDialog.nextSleepBlock == null)
					{
						FileUtils.writeStringToFile(timeToNextSleepBlockFile, pros, Charsets.UTF_8, false);
					}
					else
					{
						long tims = MainDialog.nextSleepBlock.nextStartTime;
						FileUtils.writeStringToFile(timeToNextSleepBlockFile, pros + " [" + FormattingHelper.formatTimeElapsedWithoutDays(tims, now - 59999) + " until " + MainDialog.nextSleepBlock.name + "]", Charsets.UTF_8, false);
					}
				}
			}
			catch (IOException t)
			{
				t.printStackTrace();
			}
		}
	}

	@Override
	public void shutdown()
	{
		// nothing to do
	}
}
