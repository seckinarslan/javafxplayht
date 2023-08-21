package com.seckinarslan.javafxplayht.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger logger = LoggerFactory.getLogger(com.seckinarslan.javafxplayht.util.MyLogger.class);

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
		logger.debug("MainController constructor called");
	}

	@FXML
	public void initialize() {
		logger.debug("Initialize method called.");
		inputTextField.setText("Merhaba, SpeakUp uygulaması gerçekçi ve akıcı bir Türkçe ile seslendiricilerimizden seçim yaptıktan sonra 'Girilen Yazıyı Sese Dönüştür' butonuna tıklarsanız, onlar tarafından buraya yazdıklarınız seslendirilecektir. 'Audio URL:' kısmında bir güncelleme görüyorsanız, oynat butonuna tıklayarak sesi dinleyebilirsiniz.");
		logger.debug("loadVoices() calling from Initialize method.");
		loadVoices();
	}

	@FXML
	public void convertGivenTextToAudio() {
		logger.debug("convertGivenTextToAudio method called.");
		String textToConvert = inputTextField.getText();
		logger.debug("took the text from inputTextField, textToConvert is " + textToConvert);
		if (textToConvert.isEmpty()) {
			resultLabel.setText("Please enter text to convert.");
			return;
		}

		String voice = "en-US-JennyNeural";
		String selectedVoiceName = voicesComboBox.getValue();
		logger.debug("voicesComboBox dan secilen seslendirici: " + selectedVoiceName);
		List<Voice> voiceList = seslendiricilerMap.get(selectedVoiceName);
		voice = voiceList.iterator().hasNext() ? voiceList.iterator().next().getVoiceValue() : voice;
		logger.debug("voices: " + voice.toString());
		transcriptionId = apiService.convertTextToAudio(textToConvert, voice);
		if (transcriptionId != null) {
			if (transcriptionId.startsWith("-")) {
				String audioUrl = apiService.getAudioUrl(transcriptionId);
				resultLabel.setText("Audio URL: " + audioUrl);
			} else {
				resultLabel.setText(transcriptionId);
			}
		} else if ("Conversion is still pending...".equals(transcriptionId)) {
			logger.debug("Conversion is still pending...");
		} else {
			resultLabel.setText("Error in converting text to audio.");
		}
	}

	@FXML
	public void playConvertedTextAudio(ActionEvent event) {
		logger.debug("playConvertedTextAudio method called, transcriptionId is : " + transcriptionId);

		if (transcriptionId != null) {
			String audioUrl = null;

			if (transcriptionId.startsWith("https://media.play.ht/")) {
				audioUrl = transcriptionId;
			} else {
				audioUrl = apiService.getAudioUrl(transcriptionId);
			}

			if (audioUrl != null) {
				logger.debug("Audio URL: " + audioUrl);
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
		logger.debug("playAudio method called, audioUrl is : " + audioUrl);
		Media media = new Media(audioUrl);
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
	}

	@FXML
	private void loadVoices() {
		availableVoiceList = apiService.getAvailableTurkishVoices();
		if (availableVoiceList.isEmpty()) {
			errorLabel.setText("Kullanılabilir sesler yüklenirken bir hata oluştu.");
			logger.debug("loadVoices için apiService den getAvailableTurkishVoices boş döndü.");
		} else {
			Map<String, List<Voice>> trSeslendiriciler = availableVoiceList.stream()
					.collect(Collectors.groupingBy(Voice::getSeslendirici));
			seslendiricilerMap = trSeslendiriciler;
			logger.debug("kullanilabilir seslendiriciler: " + seslendiricilerMap.keySet());
			voicesComboBox.setItems(FXCollections.observableArrayList(seslendiricilerMap.keySet()));
		}
	}
}
