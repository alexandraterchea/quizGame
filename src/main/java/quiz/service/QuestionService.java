package quiz.service;

import quiz.model.Question;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

public class QuestionService {

    private List<Question> questions;

    public QuestionService(List<Question> questions) {
        this.questions = questions;
    }

    // filtrare intrebari dupa dificultate
    public List<Question> getQuestionsByDifficulty(int difficultyLevel) {
        return questions.stream()
            .filter(q -> q.getDifficultyLevel() == difficultyLevel)
            .collect(Collectors.toList());
    }

    // sortare intrebari dupa categorie
    public List<Question> getQuestionsSortedByCategory() {
        return questions.stream()
            .sorted(Comparator.comparing(Question::getCategoryName))
            .collect(Collectors.toList());
    }

    // Filtrare întrebări dificile (nivel 3), sortate după categorie
    public List<Question> getHardQuestionsSorted() {
        return questions.stream()
            .filter(q -> q.getDifficultyLevel() == 3) // presupun că 3 = dificil
            .sorted(Comparator.comparing(Question::getCategoryName))
            .collect(Collectors.toList());
    }

    // Găsirea primei întrebări dintr-o anumită categorie
    public Optional<Question> findFirstQuestionByCategory(String category) {
        return questions.stream()
            .filter(q -> q.getCategoryName().equals(category))
            .findFirst();
    }

    // Filtrare după ID-ul categoriei
    public List<Question> getQuestionsByCategoryId(int categoryId) {
        return questions.stream()
            .filter(q -> q.getCategoryId() == categoryId)
            .collect(Collectors.toList());
    }

    // Sortare după nivel de dificultate
    public List<Question> getQuestionsSortedByDifficulty() {
        return questions.stream()
            .sorted(Comparator.comparing(Question::getDifficultyLevel))
            .collect(Collectors.toList());
    }
}