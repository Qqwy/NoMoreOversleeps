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
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDevice;
import com.github.sarxos.webcam.util.jh.JHGrayFilter;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.NMOConfiguration;
import com.tinytimrob.ppse.nmo.utils.FormattingHelper;

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
			Font font = new Font("ARIAL", Font.PLAIN, 11);
			graphics.setFont(font);
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, 320, 20);
			graphics.setColor(Color.WHITE);
			long now = System.currentTimeMillis();
			String str = CommonUtils.convertTimestamp(now);
			if (NMOConfiguration.instance.scheduleStartedOn != 0)
			{
				str = str + "   " + FormattingHelper.formatTimeElapsedWithDays(NMOConfiguration.instance.scheduleStartedOn == 0 ? 0 : now, NMOConfiguration.instance.scheduleStartedOn) + "   " + FormattingHelper.formatTimeElapsedWithDays(NMOConfiguration.instance.scheduleStartedOn == 0 ? 0 : now, NMOConfiguration.instance.scheduleLastOversleep);
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
	public static final ArrayList<WebcamWebSocketHandler> socketHandlers = new ArrayList<WebcamWebSocketHandler>();

	public static synchronized WebcamWebSocketHandler[] getConnections()
	{
		return socketHandlers.toArray(new WebcamWebSocketHandler[0]);
	}

	public static synchronized int count()
	{
		return socketHandlers.size();
	}

	public static synchronized void update()
	{
		BufferedImage img = webcam.getImage();
		if (img == null || WebcamDefaultDevice.FAULTY)
		{
			webcam.close();
			ArrayList<WebcamWebSocketHandler> handlers = (ArrayList<WebcamWebSocketHandler>) socketHandlers.clone();
			for (WebcamWebSocketHandler handler : handlers)
			{
				removeSocketHandler(handler);
			}
			webcam = null;
			init();
			for (WebcamWebSocketHandler handler : handlers)
			{
				addSocketHandler(handler);
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
		ArrayList<WebcamWebSocketHandler> handlers = (ArrayList<WebcamWebSocketHandler>) socketHandlers.clone();
		for (WebcamWebSocketHandler handler : handlers)
		{
			removeSocketHandler(handler);
		}
	}

	public static synchronized void addSocketHandler(WebcamWebSocketHandler handler)
	{
		socketHandlers.add(handler);
		webcam.addWebcamListener(handler);
	}

	public static synchronized void removeSocketHandler(WebcamWebSocketHandler handler)
	{
		socketHandlers.remove(handler);
		webcam.removeWebcamListener(handler);
	}
}
