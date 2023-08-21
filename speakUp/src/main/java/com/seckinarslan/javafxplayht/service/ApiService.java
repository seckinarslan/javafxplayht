package com.seckinarslan.javafxplayht.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.seckinarslan.javafxplayht.api.JsonParser;
import com.seckinarslan.javafxplayht.api.JsonResponseofGetAvailableVoices.Voice;

@Service
public class ApiService {
	private static final Logger logger = LoggerFactory.getLogger(com.seckinarslan.javafxplayht.util.MyLogger.class);
	@Value("${api.base.url}")
	private String baseUrl;

	private String apiV1 = "/api/v1";

	private final RestTemplate restTemplate;
	private final String secretKey;
	private final String userId;

	public ApiService(RestTemplate restTemplate, @Value("${playht_api.secret-key}") String secretKey,
			@Value("${playht_api.user-id}") String userId) {

		this.restTemplate = restTemplate;
		this.secretKey = secretKey;
		this.userId = userId;
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", secretKey);
		headers.set("X-User-Id", userId);
		return headers;
	}

	public String convertTextToAudio(String text, String voice) {
		String url = baseUrl + apiV1 + "/convert";
		System.out.println(url + " HttpMethod.POST icin hazirlaniyor.");
		String transcriptionId = null;

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("content", Collections.singletonList(text));
		requestBody.put("voice", voice);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, createHeaders());
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
				String.class);

		if (responseEntity.getStatusCode() == HttpStatus.OK || responseEntity.getStatusCode() == HttpStatus.CREATED) {
			if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
				logger.debug("convertTextToAudio çalıştı. HttpStatus.OK");
			} else if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
				logger.debug("convertTextToAudio çalıştı. HttpStatus.CREATED");
			}

			JsonParser jsonParser = new JsonParser();
			transcriptionId = jsonParser.parseTranscriptionId(responseEntity.getBody());
			logger.debug(url + " den donen transcriptionId: " + transcriptionId);
			try {
				logger.debug("3 saniye bekleyip, checkConversionStatus calistirilacak..");
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				System.out.println("3 saniye bekleyemedim hata aldım..");
			}
			String audioUrl = checkConversionStatus(transcriptionId);
			if (audioUrl != null) {
				return audioUrl;
			} else {
				System.out.println("Conversion is still pending... ");
				return "Conversion is still pending...";
			}
		} else {
			logger.debug("Error in convertTextToAudio: ");
			return transcriptionId;
		}
	}

	public String checkConversionStatus(String transcriptionId) {
		String url = baseUrl + apiV1 + "/articleStatus/?transcriptionId=" + transcriptionId;
		logger.debug(url + " HttpMethod.GET icin hazirlaniyor.");

		HttpEntity<String> entity = new HttpEntity<>(createHeaders());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			logger.debug("checkConversionStatus çalıştı. HttpStatus.OK");
			JsonParser jsonParser = new JsonParser();
			boolean statusOk = jsonParser.getConversionStatus(response.getBody());

			if (statusOk) {
				String mediaUrl = jsonParser.getAudioUrl(response.getBody());
				logger.debug("statusOk - jsonParser.getAudioUrl(response.getBody()):" + mediaUrl);
				return mediaUrl;
			} else {
				logger.debug("Conversion is not yet complete.");
				return null;
			}
		} else {
			logger.debug("Error in checkConversionStatus: ");
			return null;
		}
	}

	public String getAudioUrl(String transcriptionId) {
		String audioUrl = null;
		String url = baseUrl + apiV1 + "conversions/" + transcriptionId;

		HttpEntity<String> entity = new HttpEntity<>(createHeaders());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			logger.debug("getAudioUrl çalıştı. HttpStatus.OK");
			JsonParser jsonParser = new JsonParser();
			audioUrl = jsonParser.parseTranscriptionId(response.getBody());
		} else {
			logger.debug("Error in getAudioUrl: ");
			audioUrl = null;
		}

		return audioUrl;
	}

	public List<String> getAvailableVoices() {
		String url = baseUrl + "/api/v1/getVoices";
		HttpEntity<String> entity = new HttpEntity<>(createHeaders());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			logger.debug("getAvailableVoices çalıştı. HttpStatus.OK");
			JsonParser jsonParser = new JsonParser();
			List<Voice> voiceList = jsonParser.getAvailableVoices(response.getBody());
			for (Voice voice : voiceList) {
				logger.debug(voice.toString());
			}
			return Collections.emptyList();
		} else {
			logger.debug("Error in getAvailableVoices.");
			return Collections.emptyList();
		}
	}

	public List<Voice> getAvailableTurkishVoices() {
		String url = baseUrl + "/api/v1/getVoices";
		HttpEntity<String> entity = new HttpEntity<>(createHeaders());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			logger.debug("getAvailableTurkishVoices çalıştı. HttpStatus.OK");
			JsonParser jsonParser = new JsonParser();
			List<Voice> voiceList = jsonParser.getAvailableVoices(response.getBody());
			List<Voice> turkishVoices = voiceList.stream()
					.filter(voice -> "Turkish".compareTo(voice.getLanguage()) == 0).collect(Collectors.toList());

			logger.debug("Kullanılabilir Turkce sesler sayisi: " + turkishVoices.size());
			return turkishVoices;
		} else {
			logger.debug("Error in getAvailableVoices.");
			return Collections.emptyList();
		}
	}
}
