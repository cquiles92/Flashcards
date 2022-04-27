package flashcards;


public class Flashcard {
    private final String nameOfCard;
    private final String cardDefinition;

    private int numberOfMistakes;


    Flashcard(String nameOfCard, String cardDefinition, int numberOfMistakes) {
        this.nameOfCard = nameOfCard;
        this.cardDefinition = cardDefinition;
        this.numberOfMistakes = numberOfMistakes;
    }

    public String getNameOfCard() {
        return nameOfCard;
    }

    public String getCardDefinition() {
        return cardDefinition;
    }

    public int getNumberOfMistakes() {
        return numberOfMistakes;
    }

    protected void resetNumberOfMistakes() {
        this.numberOfMistakes = 0;
    }

    protected void mistakeMade() {
        this.numberOfMistakes++;
    }
}
