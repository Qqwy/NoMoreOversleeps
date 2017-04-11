package com.tinytimrob.ppse.nmo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tinytimrob.ppse.nmo.utils.JavaFxHelper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainDialog extends Application
{
	private static final Logger log = LogManager.getLogger();
	public static Scene scene;

	@Override
	public void start(Stage stage) throws Exception
	{
		log.info("JavaFX application start");

		//==================================================================
		// CONFIGURE THE STAGE
		//==================================================================
		stage.setTitle("NoMoreOversleeps v" + Main.VERSION);
		stage.getIcons().add(new Image(JavaFxHelper.buildResourcePath("icon.png")));
		stage.setResizable(false);
		stage.setMinWidth(300);
		stage.setMinHeight(400);

		//==================================================================
		// CONFIGURE THE SCENE
		//==================================================================
		scene = new Scene(new StackPane(), 300, 400, Color.WHITE);
		scene.getStylesheets().add(JavaFxHelper.buildResourcePath("application.css"));

		//==================================================================
		// SHOW STAGE
		//==================================================================
		stage.setScene(scene);
		stage.show();
	}
}
