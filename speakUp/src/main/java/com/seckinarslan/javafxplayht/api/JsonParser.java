package com.seckinarslan.javafxplayht.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class JsonParser {

	public String parseTranscriptionId(String jsonResponse) {
		try {
			JSONObject jsonObject = new JSONObject(jsonResponse);
			if (jsonObject.has("transcriptionId")) {
				return jsonObject.getString("transcriptionId");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String createConvertRequestJson(String text, String voice) {
		try {
			JSONArray contentArray = new JSONArray();
			contentArray.put(text);

			JSONObject requestJson = new JSONObject();
			requestJson.put("content", contentArray);
			requestJson.put("voice", voice);

			return requestJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getAudioUrl(String jsonResponse) {
		try {
			JSONObject jsonObject = new JSONObject(jsonResponse);
			if (jsonObject.has("audioUrl")) {
				return jsonObject.getString("audioUrl");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getAvailableVoicesX(String jsonResponse) {
		try {
			JSONObject jsonObject = new JSONObject(jsonResponse);
			if (jsonObject.has("audioUrl")) {
				return jsonObject.getString("audioUrl");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<JsonResponseofGetAvailableVoices.Voice> getAvailableVoices(String jsonResponse) {
		List<JsonResponseofGetAvailableVoices.Voice> voiceList = new ArrayList<>();

		try {
			JSONObject jsonObject = new JSONObject(jsonResponse);

			if (jsonObject.has("voices")) {
				JSONArray voicesArray = jsonObject.getJSONArray("voices");
				for (int i = 0; i < voicesArray.length(); i++) {
					JSONObject voiceObject = voicesArray.getJSONObject(i);
					JsonResponseofGetAvailableVoices.Voice voice = new JsonResponseofGetAvailableVoices.Voice(
							voiceObject.optString("value"), voiceObject.optString("name"),
							voiceObject.optString("language"), voiceObject.optString("voiceType"),
							voiceObject.optString("languageCode"), voiceObject.optString("gender"),
							voiceObject.optString("service"), voiceObject.optString("sample"));
					voiceList.add(voice);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		System.out.println("Kullanılabilir sesler sayisi: " + voiceList.size());

		return voiceList;
	}

	public boolean getConversionStatus(String jsonResponse) {
		try {
			JSONObject jsonObject = new JSONObject(jsonResponse);
			if (jsonObject.has("converted")) {
				return jsonObject.getBoolean("converted");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false; // defaulting to false if not found or an error occurred
	}
}
