package com.example.clientconsdb.kafka;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.clientconsdb.model.Client;
import com.example.clientconsdb.model.RawMessage;
import com.example.clientconsdb.model.TranslatedMessage;
import com.example.clientconsdb.repository.ClientRepository;
import com.example.clientconsdb.repository.RawMessageRepository;
import com.example.clientconsdb.repository.TranslatedMessageRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaListeners {

    private static final Logger log = LoggerFactory.getLogger(KafkaListeners.class);

    private final ClientRepository clientRepo;
    private final RawMessageRepository rawRepo;
    private final TranslatedMessageRepository translatedRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "topictechout", groupId = "client-cons-db")
    public void listenTechOut(String message) {
        log.info("TOPICTECHOUT: {}", message);

        if (message.startsWith("CONNECT:")) {
            String name = message.split(":")[1];
            if (!clientRepo.existsByNom(name)) {
                Client client = new Client(null, name, LocalDateTime.now());
                clientRepo.save(client);
                log.info("Client connecté et ajouté : {}", name);
            } else {
                log.info("⚠️ Client déjà enregistré : {}", name);
            }
        } else if (message.startsWith("DISCONNECT:")) {
            String name = message.split(":")[1];
            clientRepo.findByNom(name).ifPresent(client -> {
                clientRepo.delete(client);
                log.info("Client supprimé de la base : {}", name);
            });
        }
        else if (message.startsWith("GET:")) {
            String requester = message.split(":")[1];
            List<Client> clients = clientRepo.findAll();
            String list = clients.stream()
                    .map(Client::getNom)
                    .collect(Collectors.joining(", "));
            String response = String.format("FROM:client-cons-db#TO:%s#\"%s\"", requester, list);
            kafkaTemplate.send("topictechin", response); 
            log.info("Liste envoyée à {} via topictechin : {}", requester, list);
        }
        else if (message.startsWith("ISCONNECTED:")) {
            String[] parts = message.split(":")[1].split("#");
            String fromClient = parts[0];
            String targetClient = parts[1];
        
            boolean isConnected = clientRepo.existsByNom(targetClient);
            String response = String.format("FROM:client-cons-db#TO:%s#\"%s\"", fromClient, isConnected);
            kafkaTemplate.send("topictechin", response);
            log.info("ISCONNECTED demandé par {} ➤ {} est {}", fromClient, targetClient, isConnected);
        }
    }

    @KafkaListener(topics = "topicout", groupId = "client-cons-db")
    public void listenOut(String message) {
        log.info("Réception message brut sur topicout : {}", message);

        try {
            // Format attendu : FROM:ClientA#TO:ClientB#"Hello"
            String[] parts = message.split("#");
            if (parts.length < 3 || !parts[0].startsWith("FROM:") || !parts[1].startsWith("TO:")) {
                log.warn("Format de message OUT invalide : {}", message);
                return;
            }

            String from = parts[0].substring(5).trim();
            String to = parts[1].substring(3).trim();
            String msg = parts[2].replaceAll("\"", "").trim();

            // Vérifier si le destinataire est connecté (présent en base)
            if (!clientRepo.existsByNom(to)) {
                log.warn("Destinataire non connecté ({}) — message ignoré", to);
                return;
            }

            RawMessage rawMsg = new RawMessage();
            rawMsg.setFromClient(from);
            rawMsg.setToClient(to);
            rawMsg.setMessage(msg);
            rawMsg.setTimestamp(LocalDateTime.now());

            rawRepo.save(rawMsg);
            log.info("Message brut enregistré : {} → {}", from, to);

        } catch (Exception e) {
            log.error("Erreur lors du traitement du message OUT : {}", message, e);
        }
    }



    @KafkaListener(topics = "topicin", groupId = "client-cons-db")
    public void listenIn(String message) {
        log.info("Réception message traduit sur topicin : {}", message);

        try {
            // Exemple : FROM:ClientA#TO:ClientB#"Bonjour"
            String[] parts = message.split("#");
            if (parts.length < 3 || !parts[0].startsWith("FROM:") || !parts[1].startsWith("TO:")) {
                log.warn("Format invalide reçu dans topicin : {}", message);
                return;
            }

            String from = parts[0].substring(5).trim();
            String to = parts[1].substring(3).trim();
            String msg = parts[2].replaceAll("\"", "").trim();

            // Vérifier si le destinataire est connecté
            if (!clientRepo.existsByNom(to)) {
                log.warn("Destinataire non connecté ({}) — traduction ignorée", to);
                return;
            }

            TranslatedMessage translatedMsg = new TranslatedMessage();
            translatedMsg.setFromClient(from);
            translatedMsg.setToClient(to);
            translatedMsg.setMessage(msg);
            translatedMsg.setTimestamp(LocalDateTime.now());

            translatedRepo.save(translatedMsg);
            log.info("Message traduit enregistré : {} → {}", from, to);

        } catch (Exception e) {
            log.error("Erreur lors du traitement du message sur topicin : {}", message, e);
        }
    }

}
