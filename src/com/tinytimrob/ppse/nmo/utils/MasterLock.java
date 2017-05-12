package com.tinytimrob.ppse.nmo.utils;

import java.io.File;
import java.nio.channels.FileLock;
import com.tinytimrob.common.PlatformData;

public class MasterLock
{
	static FileLock _lock_ = null;

	public static boolean obtain()
	{
		FileLock lock = LockingHelper.getLock(new File(PlatformData.installationDirectory, "lockfile"));
		if (lock != null)
		{
			_lock_ = lock;
			return true;
		}
		else
		{
			return false;
		}
	}

	public static void release()
	{
		LockingHelper.releaseLock(new File(PlatformData.installationDirectory, "lockfile"), _lock_, true);
	}
}
