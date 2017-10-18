package com.tinytimrob.ppse.nmo.integration.webui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDevice;
import com.github.sarxos.webcam.util.jh.JHGrayFilter;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.NMOConfiguration;

public class WebcamCapture
{
	private static final Logger log = LogWrapper.getLogger();

	public static class WebcamTransformer implements WebcamImageTransformer
	{
		private static final JHGrayFilter GRAY = new JHGrayFilter();

		@Override
		public BufferedImage transform(BufferedImage image)
		{
			image = GRAY.filter(image, null);
			Graphics2D graphics = image.createGraphics();
			Font font = new Font("ARIAL", Font.PLAIN, 12);
			graphics.setFont(font);
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, 320, 20);
			graphics.setColor(Color.WHITE);
			long now = System.currentTimeMillis();
			String str = CommonUtils.convertTimestamp(now);
			if (NMOConfiguration.instance.scheduleStartedOn != 0)
			{
				str = str + "   " + MainDialog.formatTimeElapsedWithDays(NMOConfiguration.instance.scheduleStartedOn == 0 ? 0 : now, NMOConfiguration.instance.scheduleStartedOn) + "   " + MainDialog.formatTimeElapsedWithDays(NMOConfiguration.instance.scheduleStartedOn == 0 ? 0 : now, NMOConfiguration.instance.scheduleLastOversleep);
			}
			graphics.drawString(str, 4, 14);
			if (MainDialog.isCurrentlyPaused.get())
			{
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, 204, 320, 36);
				graphics.setColor(Color.WHITE);
				graphics.drawString("PAUSED for \"" + MainDialog.pauseReason + "\"\n", 4, 218);
				graphics.drawString("until " + CommonUtils.dateFormatter.format(MainDialog.pausedUntil), 4, 234);
			}
			else
			{
				String pros = MainDialog.nextActivityWarningID >= NMOConfiguration.instance.oversleepWarningThreshold ? "OVERSLEEPING" : MainDialog.nextActivityWarningID > 0 ? "MISSING" : "AWAKE";
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, 220, 320, 20);
				graphics.setColor(Color.WHITE);
				graphics.drawString(pros + (MainDialog.nextActivityWarningID > 0 ? " (" + MainDialog.nextActivityWarningID + ")" : ""), 4, 234);
			}
			image.flush();
			graphics.dispose();
			return image;
		}
	}

	static Webcam webcam = null;

	public static void init()
	{
		List<Webcam> cams = Webcam.getWebcams();
		for (Webcam cam : cams)
		{
			log.info("Found webcam: " + cam.getName());
			if (cam.getName().equals(NMOConfiguration.instance.integrations.webUI.webcamName))
			{
				webcam = cam;
			}
		}
		if (webcam == null)
		{
			webcam = Webcam.getDefault();
			NMOConfiguration.instance.integrations.webUI.webcamName = webcam.getName();
			try
			{
				Configuration.save();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		log.info("Webcam selected: " + webcam.getName());
		webcam.setImageTransformer(new WebcamTransformer());
		Dimension[] sizes = webcam.getViewSizes();
		Dimension dimension = null;
		for (Dimension d : sizes)
		{
			log.info("Found image dimension: " + d.getWidth() + "x" + d.getHeight());
			if (d.getWidth() == 320)
			{
				dimension = d;
			}
		}
		if (dimension == null)
		{
			dimension = sizes[0];
		}
		log.info("Selected image dimension: " + dimension.getWidth() + "x" + dimension.getHeight());
		webcam.setViewSize(dimension);
		webcam.open(true);
		System.out.println(webcam.getViewSize());
		WebcamDefaultDevice.FAULTY = false;
	}

	private static BufferedImage image;
	private static ArrayList<WebcamListener> listeners = new ArrayList<WebcamListener>();

	public static synchronized int count()
	{
		return listeners.size();
	}

	public static synchronized void update()
	{
		BufferedImage img = webcam.getImage();
		if (img == null || WebcamDefaultDevice.FAULTY)
		{
			webcam.close();
			ArrayList<WebcamListener> listenersclose = (ArrayList<WebcamListener>) listeners.clone();
			for (WebcamListener l : listenersclose)
			{
				removeListener(l);
			}
			webcam = null;
			init();
			for (WebcamListener l : listenersclose)
			{
				addListener(l);
			}
		}
		else
		{
			image = img;
		}
	}

	public static BufferedImage getImage()
	{
		return image;
	}

	public static String getCameraName()
	{
		return webcam == null ? "null" : webcam.getName();
	}

	public static void shutdown()
	{
		webcam.close();
		ArrayList<WebcamListener> listenersclose = (ArrayList<WebcamListener>) listeners.clone();
		for (WebcamListener l : listenersclose)
		{
			removeListener(l);
		}
	}

	public static synchronized void addListener(WebcamListener listener)
	{
		listeners.add(listener);
		webcam.addWebcamListener(listener);
	}

	public static synchronized void removeListener(WebcamListener listener)
	{
		listeners.remove(listener);
		webcam.removeWebcamListener(listener);
	}
}
