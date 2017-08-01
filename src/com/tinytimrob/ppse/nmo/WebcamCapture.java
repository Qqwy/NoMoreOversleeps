package com.tinytimrob.ppse.nmo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.apache.logging.log4j.Logger;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.util.jh.JHGrayFilter;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.LogWrapper;

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
				str = str + "      " + MainDialog.formatTimeElapsedWithDays(NMOConfiguration.instance.scheduleStartedOn == 0 ? 0 : now, NMOConfiguration.instance.scheduleStartedOn) + "      " + MainDialog.formatTimeElapsedWithDays(NMOConfiguration.instance.scheduleStartedOn == 0 ? 0 : now, NMOConfiguration.instance.scheduleLastOversleep);
			}
			graphics.drawString(str, 4, 14);
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
			if (cam.getName().equals(NMOConfiguration.instance.webcamName))
			{
				webcam = cam;
			}
		}
		if (webcam == null)
		{
			webcam = Webcam.getDefault();
			NMOConfiguration.instance.webcamName = webcam.getName();
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
	}

	public static BufferedImage getImage()
	{
		return webcam.getImage();
	}

	public static String getCameraName()
	{
		return webcam.getName();
	}

	public static void shutdown()
	{
		webcam.close();
	}

	public static void addListener(WebcamListener listener)
	{
		webcam.addWebcamListener(listener);
	}

	public static void removeListener(WebcamListener listener)
	{
		webcam.removeWebcamListener(listener);
	}
}
