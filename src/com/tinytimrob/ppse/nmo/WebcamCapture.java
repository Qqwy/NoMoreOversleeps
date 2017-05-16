package com.tinytimrob.ppse.nmo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.util.jh.JHGrayFilter;
import com.tinytimrob.common.CommonUtils;

public class WebcamCapture
{
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
			graphics.drawString(CommonUtils.convertTimestamp(System.currentTimeMillis()), 4, 14);
			image.flush();
			graphics.dispose();
			return image;
		}
	}

	static Webcam webcam;

	public static void init()
	{
		webcam = Webcam.getDefault();
		webcam.setImageTransformer(new WebcamTransformer());
		Dimension[] sizes = webcam.getViewSizes();
		webcam.setViewSize(sizes[sizes.length - 2]);
		webcam.open(true);
		System.out.println(webcam.getViewSize());
	}

	public static BufferedImage getImage()
	{
		return webcam.getImage();
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
