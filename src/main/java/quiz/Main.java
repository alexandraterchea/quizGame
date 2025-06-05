package quiz;

import quiz.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

//oop->facut
//de facut pattern uri
//Streams: Filtrarea întrebărilor pe dificultate, categoria, scorul utilizatorilor->facut
//questions.stream()
//    .filter(q -> q.getDifficulty() == Difficulty.HARD)
//    .sorted(Comparator.comparing(Question::getCategory))
//    .collect(Collectors.toList());

//interfata swing -> facut
//Web component: Creează un REST API cu Spring Boot pentru managementul quiz-urilor

//db -> facut

//file operations Logging: Logarea activității utilizatorilor în fișiere

//Services/Networking (3 puncte):
//
//REST API: Endpoints pentru CRUD operations pe întrebări, utilizatori
//WebSocket: Quiz multiplayer în timp real
//Networking: Client-server architecture pentru jocuri online

//alg/complexitate Algoritmi de sortare: Pentru leaderboard-uri, ranking-uri

//bonus  Sistem de autentificare cu JWT
//Gamification: Achievements, badges, nivele -> facut ish
//AI Integration: Generarea automată de întrebări


//pt sgbd un profile +achievments + nivel