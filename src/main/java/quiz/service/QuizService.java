// În pachetul: com.quiz.service
package quiz.service;

import quiz.dao.QuestionDAO;
import quiz.dao.ScoreDAO;
import quiz.dao.QuizSessionDAO;
import quiz.exceptions.DatabaseException;
import quiz.model.Question;
import java.util.List;

public class QuizService {
    public List<Question> startAdaptiveQuiz(int userId, int questionCount) throws DatabaseException {
        QuestionDAO questionDAO = new QuestionDAO();

        List<Question> adaptiveQuestions = questionDAO.getAdaptiveQuestions(userId, questionCount);

        System.out.println("Întrebări adaptive pentru utilizatorul " + userId + ":");
        for (Question q : adaptiveQuestions) {
            System.out.println("Dificultate: " + q.getDifficultyLevel() + " - " + q.getText());
        }

        return adaptiveQuestions;
    }
}