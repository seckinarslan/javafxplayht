package com.seckinarslan.javafxplayht;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

@SpringBootApplication
public class SpeakUpApplication extends Application {

	private ConfigurableApplicationContext springContext;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() throws Exception {
		springContext = SpringApplication.run(SpeakUpApplication.class);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
		fxmlLoader.setControllerFactory(springContext::getBean);
		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root, 350, 250);
		primaryStage.setTitle("SpeakUp Uygulaması");
//		primaryStage.setWidth(800);
//	    primaryStage.setHeight(600);
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		primaryStage.setWidth(screenBounds.getWidth() / 2);
        primaryStage.setHeight(screenBounds.getHeight() / 2);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@Override
	public void stop() throws Exception {
		springContext.close();
	}
	/*Yaptığımız değişiklikler:

    SpeakUpApplication sınıfını javafx.application.Application sınıfından türettik.
    Spring Boot uygulama bağlamını başlatmak için init() metodunu kullandık.
    JavaFX penceresini başlatmak için start(Stage primaryStage) metodunu kullandık.
    Spring Boot'un uygulama bağlamını kapatmak için stop() metodunu kullandık.

Bu yapı, JavaFX ve Spring Boot'un birlikte daha sıkı bir entegrasyonunu sağlar ve JavaFX penceresi ve Spring Boot uygulama bağlamı arasındaki ilişkiyi daha net bir şekilde tanımlar. Bu şekilde, JavaFX pencerenizin Spring Boot servislerini, bileşenlerini ve diğer bağımlılıklarını kullanabilmesini sağlar.*/
}
