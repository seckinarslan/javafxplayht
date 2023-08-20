package com.seckinarslan.javafxplayht.api;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JsonResponseofGetAvailableVoices {
	@Getter
	@Setter
    private List<Voice> voices;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Voice {
    	public Voice(String value, String name, String language, String voiceType, String languageCode, String gender, String service, String sample) {
            this.value = value;
            this.name = name;
            this.language = language;
            this.voiceType = voiceType;
            this.languageCode = languageCode;
            this.gender = gender;
            this.service = service;
            this.sample = sample;
        }
		@Getter
    	@Setter
        private String value;
    	@Getter
    	@Setter
        private String name;
    	@Getter
    	@Setter
        private String language;
    	@Getter
    	@Setter
        private String voiceType;
    	@Getter
    	@Setter
        private String languageCode;
    	@Getter
    	@Setter
        private String gender;
    	@Getter
    	@Setter
        private String service;
    	@Getter
    	@Setter
        private String sample;
		public String getLanguage() {
			return this.language;
		}
		
		public String getSeslendirici() {
			return this.name;
		}
		
		public String getLanguageCode() {
			return this.languageCode;
		}
		
		public String getVoiceValue() {
			return this.value;
		}
		
		
		
		@Override
	    public String toString() {
	        return "Voice{" +
	                "value='" + value + '\'' +
	                ", name='" + name + '\'' +
	                ", language='" + language + '\'' +
	                ", voiceType='" + voiceType + '\'' +
	                ", languageCode='" + languageCode + '\'' +
	                ", gender='" + gender + '\'' +
	                ", service='" + service + '\'' +
	                ", sample='" + sample + '\'' +
	                '}';
	    }
    }

}
