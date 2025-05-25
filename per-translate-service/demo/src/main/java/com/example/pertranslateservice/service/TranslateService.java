package com.example.pertranslateservice.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final WebClient.Builder webClientBuilder;

    @Value("${libretranslate.api-url}")
    private String apiUrl;

    /**
     * Détecte si le texte semble être en français (basé sur des mots courants).
     */
    private boolean isFrench(String text) {
        String lower = text.toLowerCase();
        return lower.matches(".*\\b(je|tu|il|elle|le|la|est|et|un|une|des|bonjour|avec|pour|pas)\\b.*");
    }

    public String translateAuto(String text) {
        String sourceLang = isFrench(text) ? "fr" : "en";
        String targetLang = sourceLang.equals("fr") ? "en" : "fr";
        return translate(text, sourceLang, targetLang);
    }

    public String translate(String text, String sourceLang, String targetLang) {
        try {
            Map<String, String> request = Map.of(
                    "q", text,
                    "source", sourceLang,
                    "target", targetLang,
                    "format", "text"
            );

            return webClientBuilder.build()
                    .post()
                    .uri(apiUrl)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(res -> (String) res.get("translatedText"))
                    .block();

        } catch (Exception e) {
            return "[Erreur de traduction]";
        }
    }
}
