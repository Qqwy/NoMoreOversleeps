package com.tinytimrob.ppse.nmo.integration.webui;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.Logger;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.tinytimrob.common.LogWrapper;

public class WebcamData implements WebcamListener
{
	private static final Logger log = LogWrapper.getLogger();
	static volatile String imageBase64 = "";

	@Override
	public void webcamImageObtained(WebcamEvent we)
	{
		BufferedImage image = WebcamCapture.getImage();
		if (image == null)
		{
			return;
		}
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "JPG", baos);
			byte[] data = baos.toByteArray();
			imageBase64 = new String(Base64.getEncoder().encode(data), "UTF8");
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void webcamOpen(WebcamEvent we)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void webcamClosed(WebcamEvent we)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void webcamDisposed(WebcamEvent we)
	{
		// TODO Auto-generated method stub
	}
}
