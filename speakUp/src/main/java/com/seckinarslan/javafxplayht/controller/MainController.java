package com.seckinarslan.javafxplayht.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.seckinarslan.javafxplayht.api.JsonResponseofGetAvailableVoices.Voice;
import com.seckinarslan.javafxplayht.service.ApiService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;
import lombok.Setter;

@Controller
public class MainController {

	@Autowired
	private ApiService apiService;

	@FXML
	private Label resultLabel;

	@FXML
	private TextArea inputTextField;

	@FXML
	private ComboBox<String> voicesComboBox;

	@Getter
	@Setter
	private List<Voice> availableVoiceList;
	
	@Getter
	@Setter
	private Voice selectedVoice;

	@FXML
	private Label errorLabel;

	private String transcriptionId;
	private Map<String, List<Voice>> seslendiricilerMap;

	public MainController() {
		System.out.println("MainController constructor called");
	}

	@FXML
	public void initialize() {
		inputTextField.setText("Merhaba, SpeakUp uygulaması gerçekçi ve akıcı bir Türkçe ile seslendiricilerimizden seçim yaptıktan sonra 'Girilen Yazıyı Sese Dönüştür' butonuna tıklarsanız, onlar tarafından buraya yazdıklarınız seslendirilecektir. 'Audio URL:' kısmında bir güncelleme görüyorsanız, oynat butonuna tıklayarak sesi dinleyebilirsiniz.");
		System.out.println("Initialize method called. resultLabel: " + resultLabel);
		loadVoices();
	}

	@FXML
	public void onButtonClick() {
		String textToConvert = inputTextField.getText();

		if (textToConvert.isEmpty()) {
			resultLabel.setText("Please enter text to convert.");
			return;
		}
		
		String voice = "en-US-JennyNeural";

		String selectedVoiceName = voicesComboBox.getValue();
		List<Voice> voiceList = seslendiricilerMap.get(selectedVoiceName);
		voice = voiceList.iterator().hasNext() ? voiceList.iterator().next().getVoiceValue() : voice;

		transcriptionId = apiService.convertTextToAudio(textToConvert, voice);

		// Check the conversion status
		if (transcriptionId != null) {
			if (transcriptionId.startsWith("-")) {
				String audioUrl = apiService.getAudioUrl(transcriptionId);
				resultLabel.setText("Audio URL: " + audioUrl);
			} else {
				resultLabel.setText(transcriptionId);
			}
		} else if ("Conversion is still pending...".equals(transcriptionId)) {
			System.out.println("Conversion is still pending...");
		} else {
			resultLabel.setText("Error in converting text to audio.");
		}
	}

	@FXML
	public void playText(ActionEvent event) {
		System.out.println("playText method called, transcriptionId is : " + transcriptionId);
		
		if (transcriptionId != null) {
			String audioUrl = null;

			if (transcriptionId.startsWith("https://media.play.ht/")) {
				audioUrl = transcriptionId;
			} else {
				audioUrl = apiService.getAudioUrl(transcriptionId);
			}

			if (audioUrl != null) {
				resultLabel.setText("Audio URL: " + audioUrl);
				playAudio(audioUrl);
			} else {
				resultLabel.setText("Conversion is still in progress...");
			}
		} else {
			resultLabel.setText("Please convert text to audio first.");
		}
	}

	private void playAudio(String audioUrl) {
		Media media = new Media(audioUrl);
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
	}

	@FXML
	private void loadVoices() {
		availableVoiceList = apiService.getAvailableTurkishVoices();
		if (availableVoiceList.isEmpty()) {
			errorLabel.setText("Kullanılabilir sesler yüklenirken bir hata oluştu.");
			System.out.println("loadVoices için apiService den getAvailableTurkishVoices boş döndü.");
		} else {
			Map<String, List<Voice>> trSeslendiriciler = availableVoiceList.stream()
					.collect(Collectors.groupingBy(Voice::getSeslendirici));
			seslendiricilerMap = trSeslendiriciler;
			voicesComboBox.setItems(FXCollections.observableArrayList(seslendiricilerMap.keySet()));
		}
	}
}
