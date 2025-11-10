# 🇦🇪  API du Moteur De recherche  

Une API REST puissante développée avec **Spring Boot** pour l’analyse de textes arabes, basée sur **SAFAR (System for Arabic Finite-state Automata Research)**.  
Elle offre des fonctionnalités de **tokenisation**, **racinisation (stemming)**, **filtrage de mots vides**, **calcul TF-IDF**, et **analyse de similarité cosinus**.

---

##  Fonctionnalités
-  **Tokenisation** : segmentation du texte arabe avec le tokenizer SAFAR  
-  **Stemming** : extraction des racines des mots via l’algorithme ISRI  
-  **Filtrage des stop words** : suppression des mots arabes fréquents et non pertinents  
-  **TF-IDF** : calcul de l’importance des termes dans un document  
-  **Similarité cosinus** : mesure de similarité entre un texte et les documents du corpus  
-  **Statistiques** : suivi des métriques et des résultats d’analyse  

---

##  Technologies
- Java 17  
- Spring Boot 3.3
- SAFAR Library (NLP arabe)  
- Maven  

---
## Utilisation (avec Postman)
###  Analyser un texte

** Méthode : POST **
URL : http://localhost:8080/text/mining

Headers : Content-Type: text/plain; charset=UTF-8

Body (raw, Text) :  أحد القضاة العاملين بوزارة العدل

Exemple de réponse : 
<img width="1336" height="732" alt="image" src="https://github.com/user-attachments/assets/84250b63-4b23-48ed-a5f6-891c8353362e" />
<img width="1366" height="703" alt="image" src="https://github.com/user-attachments/assets/b688401e-aad2-4ab4-8009-14e24c0869d2" />


**Realise par Bakir Mohammed **


