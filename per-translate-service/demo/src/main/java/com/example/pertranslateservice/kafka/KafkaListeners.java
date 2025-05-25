package com.example.pertranslateservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.pertranslateservice.service.TranslateService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaListeners {

    private static final Logger log = LoggerFactory.getLogger(KafkaListeners.class);

    private final TranslateService translator;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "topicout", groupId = "per-translate-service")
    public void onMessage(String message) {
        log.info("Reçu de topicout : {}", message);

        try {
            // Format attendu : FROM:ClientX#TO:ClientY#"Texte"
            String[] parts = message.split("#");
            if (parts.length < 3 || !parts[0].startsWith("FROM:") || !parts[1].startsWith("TO:")) {
                log.warn("Message mal formé : {}", message);
                return;
            }

            String from = parts[0].substring(5).trim();
            String to = parts[1].substring(3).trim();
            String content = parts[2].replaceAll("\"", "").trim();

            String translated = translator.translateAuto(content);

            String finalMessage = String.format("FROM:%s#TO:%s#\"%s\"", from, to, translated);
            kafkaTemplate.send("topicin", finalMessage);

            log.info("✅ Message traduit envoyé : {}", finalMessage);

        } catch (Exception e) {
            log.error("Erreur de traitement du message : {}", message, e);
        }
    }
}
