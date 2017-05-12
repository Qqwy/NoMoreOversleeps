package com.tinytimrob.ppse.nmo;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import com.github.sarxos.webcam.Webcam;

public class WebcamCapture
{
	static Webcam webcam;

	public static void init()
	{
		webcam = Webcam.getDefault();
		Dimension[] sizes = webcam.getViewSizes();
		webcam.setViewSize(sizes[sizes.length - 1]);
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
}
