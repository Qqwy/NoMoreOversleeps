package com.tinytimrob.ppse.nmo;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tinytimrob.ppse.nmo.utils.JavaFxHelper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MainDialog extends Application
{
	private static final Logger log = LogManager.getLogger();
	public static Scene scene;

	@Override
	public void start(Stage stage) throws Exception
	{
		log.info("JavaFX application start");

		Configuration.load();
		try
		{
			//Pavlok.vibration(255, "Connection test");
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			Configuration.instance.pavlokAuth = null;
		}

		//==================================================================
		// CONFIGURE THE STAGE
		//==================================================================
		stage.setTitle("NoMoreOversleeps v" + Main.VERSION);
		stage.getIcons().add(new Image(JavaFxHelper.buildResourcePath("icon.png")));
		stage.setResizable(false);
		stage.setMinWidth(800);
		stage.setMinHeight(600);

		//==================================================================
		// CONFIGURE THE SCENE
		//==================================================================
		final StackPane outerPane = new StackPane();
		outerPane.setId("root");
		scene = new Scene(outerPane, 800, 600, Color.WHITE);
		scene.getStylesheets().add(JavaFxHelper.buildResourcePath("application.css"));

		//==================================================================
		// BUILD THE PRIMARY PANE
		//==================================================================
		final BorderPane innerPane = new BorderPane();
		{ // Main container pane
			innerPane.setStyle("-fx-background-color: #222;");
		}

		final HBox innerTopPane = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 35, new Insets(8, 4, 8, 4));
		{ // Top section
			innerTopPane.setStyle("-fx-background-color: #414C7D;");
			final Label label = JavaFxHelper.createLabel(" NoMoreOversleeps", Color.WHITE, "-fx-font-weight: bold; -fx-font-size:16;", new Insets(0, 0, 0, 3), 320, Control.USE_COMPUTED_SIZE);
			label.setGraphic(JavaFxHelper.createIcon(FontAwesomeIcon.BED, "16", Color.WHITE));
			innerTopPane.getChildren().add(label);
			innerPane.setTop(innerTopPane);
		}

		final GridPane innerRightPane = new GridPane();
		{ // Manual Pavlok controls
			innerRightPane.setMinWidth(200);
			innerRightPane.setMaxWidth(200);
			innerRightPane.setStyle("-fx-background-color: #444;");
			innerRightPane.setVgap(10);
			innerRightPane.setPadding(new Insets(10, 10, 10, 10));
			final Label label = JavaFxHelper.createLabel("Manual controls", Color.WHITE, "", new Insets(0, 0, 0, 3), 160, Control.USE_COMPUTED_SIZE);
			final Button beepButton = JavaFxHelper.createButton("BEEP", JavaFxHelper.createIcon(FontAwesomeIcon.VOLUME_UP, "12", Color.BLACK));
			beepButton.setMinWidth(180);
			beepButton.setMaxWidth(180);
			beepButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						Pavlok.beep(255, "Manually triggered beep");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			final Button vibrateButton = new Button("VIBRATE");
			vibrateButton.setMinWidth(180);
			vibrateButton.setMaxWidth(180);
			vibrateButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						Pavlok.vibration(255, "Manually triggered vibration");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			final Button shockButton = new Button("SHOCK");
			shockButton.setMinWidth(180);
			shockButton.setMaxWidth(180);
			shockButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						Pavlok.shock(255, "Manually triggered shock");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			innerRightPane.addRow(0, label);
			innerRightPane.addRow(1, beepButton);
			innerRightPane.addRow(2, vibrateButton);
			innerRightPane.addRow(3, shockButton);
			innerPane.setRight(innerRightPane);
		}

		//==================================================================
		// PAVLOK CRAP
		//==================================================================

		if (Configuration.instance.pavlokAuth == null)
		{
			final String url = "https://pavlok-mvp.herokuapp.com/oauth/authorize?client_id=" + Main.CLIENT_ID + "&redirect_uri=" + Main.CLIENT_CALLBACK + "&response_type=code";
			BorderPane authPane = new BorderPane();
			WebView browser = new WebView();
			WebEngine webEngine = browser.getEngine();
			webEngine.load(url);
			authPane.setCenter(browser);
			webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>()
			{
				boolean handledCallbackYet = false;

				@Override
				public void handle(WebEvent<String> event)
				{
					if (!this.handledCallbackYet && event.getSource() instanceof WebEngine)
					{
						WebEngine we = (WebEngine) event.getSource();
						String location = we.getLocation();
						if (location.startsWith(Main.CLIENT_CALLBACK) && location.contains("?code"))
						{
							this.handledCallbackYet = true;
							System.out.println("Detected location: " + location);
							try
							{
								String[] params = location.split("\\Q?\\E")[1].split("\\Q&\\E");
								Map<String, String> map = new HashMap<String, String>();
								for (String param : params)
								{
									String name = param.split("=")[0];
									String value = param.split("=")[1];
									map.put(name, value);
								}
								Pavlok.postAuthToken(map.get("code"));
								Configuration.instance.pavlokAuth = Pavlok.RESPONSE;
								Configuration.save();
								Pavlok.vibration(255, "Connection test");
								outerPane.getChildren().clear();
								outerPane.getChildren().add(innerPane);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			});
			outerPane.getChildren().add(authPane);
		}
		else
		{
			outerPane.getChildren().add(innerPane);
		}

		//==================================================================
		// SHOW STAGE
		//==================================================================
		stage.setScene(scene);
		stage.show();
	}
}
