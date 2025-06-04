package quiz.model;

public class Question {
    private int id;
    private int categoryId;
    private String text;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private char correctOption;
    private int difficultyLevel;
    private String categoryName;

    public Question() {}

    public Question(int id, String text, String optionA, String optionB, String optionC, String optionD, char correctOption,int difficultyLevel) {
        this.id = id;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.difficultyLevel=difficultyLevel;
    }


    public int getId() { return id; }
    public int getCategoryId(){return categoryId;}
    public String getText() { return text; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public char getCorrectOption() { return correctOption; }
    public int getDifficultyLevel() { return difficultyLevel;}
    public String getCategoryName(){return categoryName;}

    public void setId(int id) { this.id = id; }
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    public void setText(String text) { this.text = text; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public void setCorrectOption(char correctOption) { this.correctOption = correctOption; }
    public void setDifficultyLevel(int difficultyLevel){this.difficultyLevel=difficultyLevel;}
    public void setCategoryName(String categoryName){this.categoryName=categoryName;}

}
