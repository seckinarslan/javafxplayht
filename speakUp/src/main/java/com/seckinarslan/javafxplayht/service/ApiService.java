package com.seckinarslan.javafxplayht.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Value("${api.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final String secretKey;
    private final String userId;

    public ApiService(RestTemplate restTemplate, 
                      @Value("${playht_api.secret-key}") String secretKey, 
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
        String transcriptionId = null;
        String url = baseUrl + "/api/v1/convert";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", Collections.singletonList(text));
        requestBody.put("voice", voice);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, createHeaders());
        System.out.println("URL GET HAZIRLANIYOR:" + url);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK || responseEntity.getStatusCode() == HttpStatus.CREATED) {
            JsonParser jsonParser = new JsonParser();
            transcriptionId = jsonParser.parseTranscriptionId(responseEntity.getBody());
            
            System.out.println("transcriptionId=" + transcriptionId + " için checkConversionStatus çalışacak.");

            try {
        		System.out.println("Biraz bekleyip tekrar deniyorum.");
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				System.out.println("5 saniye bekleyemedim hata aldım..");
			}
            // Check conversion status
            String audioUrl = checkConversionStatus(transcriptionId);
            
            if (audioUrl != null) {
                return audioUrl;
            } else {
                return "Conversion is still pending...";
            }
        } else {
            System.out.println("Error in convertTextToAudio: ");
            return transcriptionId;
        }
    }

    public String checkConversionStatus(String transcriptionId) {
        String url = baseUrl + "/api/v1/articleStatus/?transcriptionId=" + transcriptionId;

        System.out.println("URL GET HAZIRLANIYOR:" + url);
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println("transcriptionId=" + transcriptionId + " için " + url + " url ile " + " checkConversionStatus çalıştı.");
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonParser jsonParser = new JsonParser();
            boolean statusOk = jsonParser.getConversionStatus(response.getBody());
            
            if (statusOk) {
            	String mediaUrl = jsonParser.getAudioUrl(response.getBody());
            	System.out.println("statusOk - jsonParser.getAudioUrl(response.getBody()):" + mediaUrl);
                return mediaUrl;
            } else {
                System.out.println("Conversion is not yet complete.");
                return null;
            }
        } else {
            System.out.println("Error in checkConversionStatus: ");
            return null;
        }
    }

    public String getAudioUrl(String transcriptionId) {
    	String audioUrl = null;
        String url = baseUrl + "/api/v1/conversions/" + transcriptionId;

        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonParser jsonParser = new JsonParser();
            audioUrl = jsonParser.parseTranscriptionId(response.getBody());
        } else {
            System.out.println("Error in getAudioUrl: ");
            audioUrl = null;
        }
        
        return audioUrl;
    }
    
    public List<String> getAvailableVoices() {
    	String url = baseUrl + "/api/v1/getVoices";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
        	System.out.println(url + " çalıştı ve HttpStatus.OK");
        	 JsonParser jsonParser = new JsonParser();
        	 List<Voice> voiceList = jsonParser.getAvailableVoices(response.getBody());
        	 for (Voice voice : voiceList) {
				System.out.println(voice.toString());
			}
        	return Collections.emptyList();//burada sesleri al ve dön
            // Burada JSON dönüşünü parse edip seslerin listesini elde edeceksiniz.
            // JsonParser sınıfınıza göre bu işlemi yapabilirsiniz.
            // Örnek olarak bu parçayı doldurmanız gerekecek.
            // List<String> voices = ... 
            // return voices;
        } else {
            System.out.println("Error in getAvailableVoices.");
            return Collections.emptyList();
        }
    }
    
    public List<Voice> getAvailableTurkishVoices() {
    	String url = baseUrl + "/api/v1/getVoices";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
        	System.out.println(url + " çalıştı ve HttpStatus.OK");
        	 JsonParser jsonParser = new JsonParser();
        	 List<Voice> voiceList = jsonParser.getAvailableVoices(response.getBody());
        	 List<Voice> turkishVoices = voiceList.stream()
             	    .filter(voice -> "Turkish".compareTo(voice.getLanguage()) == 0)
             	    .collect(Collectors.toList());
             
             System.out.println("Kullanılabilir Turkce sesler sayisi: " + turkishVoices.size());
        	return turkishVoices;
        } else {
            System.out.println("Error in getAvailableVoices.");
            return Collections.emptyList();
        }
    }
}
