# 💬 Messagerie Kafka + Traduction (LibreTranslate)

Ce projet est un système de messagerie distribué entre plusieurs clients Kafka avec support automatique de traduction des messages via **LibreTranslate**. Les messages sont archivés dans une base de données PostgreSQL, et les clients interagissent via une interface shell (`spring-shell`).

---

## 🧱 Architecture Globale

![image](https://github.com/user-attachments/assets/fd05f9c1-14c5-4280-9a46-aad876be5dfd)

---

## Prérequis

- Java 17
- Maven
- Docker + Docker Compose
- Kafka (via docker-compose)
- PostgreSQL (via docker-compose)
- LibreTranslate (via script run.sh)

---
##  Lancement du projet

1. Lancer Kafka + PostgreSQL et créer les topics
   
`sudo docker compose up -d`

2. Lancer Libretranslate

`sudo ./go-enfr.sh`

3. Lancer le service de traduction et le service d'archivage

- Via un éditeur de coce: Ouvrir chaque projet (Client-Cons-DB, per-translate-service) dans VSCode
et cliquer sur "Run" sur la méthode main() de la classe principale

4. Lancer un client X (on peut lancer autant de client qu'on veut mais les clients doivent avoir impérativement des noms et group_id différents)

```bash
cd shellclient
mvn clean install
cd target
export spring_kafka_consumer_group_id=ClientX
java -Dapplication.monnom=ClientX -jar shellClient-0.0.1.jar
```

---

## Commandes disponibles côté client (shell)

| Commande             | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `whoami`             | Affiche le nom du client et l'adresse du serveur Kafka                      |
| `env`                | Liste les variables d'environnement système                                 |
| `message "txt" --dst ClientX`            | Envoie un message à un autre client |
| `traduire "txt"`           | Demande la traduction d’un texte (auto-adressé)                             |
| `lister-clients`     | Affiche la liste des clients actuellement connectés                         |
| `EnregistrerClient`  | Enregistre manuellement le client sur le broker                             |
| `DeconnecterClient`  | Déconnecte le client de la messagerie sans quitter                          |
| `isConnected ClientX`        | Vérifie si un autre client est connecté                                     |
| `byebye`             | Déconnecte et quitte le shell                                                |

---
## Fonctionnalités

🔸 Gestion des clients

- Lorsqu’un client se connecte, il est automatiquement enregistré dans la base PostgreSQL.

- Lorsqu’un client se déconnecte ou quitte, il est automatiquement supprimé de la base.

- Il est possible de lister tous les clients actuellement connectés via une commande.

🔸 Envoi et réception de messages

 - Les clients peuvent envoyer des messages à d’autres clients à travers Kafka.

 - Si le client destinataire est connecté, le message est stocké en brut (non traduit).

 - Le destinataire reçoit automatiquement la traduction du message.

🔸 Traduction automatique

 - Le service per-translate-service utilise LibreTranslate pour traduire les messages automatiquement.

 - La direction de la traduction est détectée automatiquement :

   - Si le message est en anglais, il est traduit en français.

   - Si le message est en français, il est traduit en anglais.

 - Les traductions sont envoyées via Kafka et stockées séparément dans la base PostgreSQL.

🔸 Vérification de la connexion

  - Il est possible de vérifier si un client donné est connecté à tout moment.

🔸 Interface CLI interactive

  - Chaque client fonctionne dans un shell Spring Boot interactif, permettant d’exécuter des commandes personnalisées pour : Envoyer, traduire, lister les clients connectés, vérifier un client, se déconnecter, etc.

## Auteur

Ndeye Birame DIA

📧 ndeyebiramdia@gmail.com / ndeyebirame.dia@uphf.fr




