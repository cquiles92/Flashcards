package flashcards;


import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FlashCardManager {

    private boolean saveOnExit = false;
    private String fileToSaveOnExit;
    private Scanner scanner;
    private final List<Flashcard> flashcards = new LinkedList<>();
    private final List<String> log = new ArrayList<>();


    public FlashCardManager(String[] args) {
        if (args.length > 0) {
            processCommandArgs(args);
        }
    }

    public void start() {
        scanner = new Scanner(System.in);
        try {
            processOutput("");
            String input = "";
            while (!input.equals("exit")) {
                processOutput("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
                input = processInput(scanner.nextLine().trim().toLowerCase());
                selectOption(input);
            }
            System.out.println("Bye bye!");
        } finally {
            if (saveOnExit) {
                exportFlashcards(fileToSaveOnExit);
            }
            scanner.close();
        }
    }

    private void selectOption(String input) {
        switch (input) {
            case "add":
                addFlashCard();
                break;
            case "remove":
                removeFlashcard();
                break;
            case "import":
                importFlashcards(getFileName());
                break;
            case "export":
                exportFlashcards(getFileName());
                break;
            case "ask":
                ask();
                break;
            case "hardest card":
                printHardestCard();
                break;
            case "reset stats":
                resetStats();
                break;
            case "log":
                log();
                break;
            case "exit":
                break;
            default:
                processOutput("Invalid selection. Try again.");
        }
    }

    private void addFlashCard() {
        Map<String, String> cardMap = new HashMap<>();
        for (Flashcard flashcard : flashcards) {
            cardMap.put(flashcard.getNameOfCard(), flashcard.getCardDefinition());
        }

        processOutput("The card:");
        String nameOfCard = processInput(scanner.nextLine());
        if (cardMap.containsKey(nameOfCard)) {
            processOutput(String.format("The card \"%s\" already exists.", nameOfCard));
            return;
        }

        processOutput("The definition of the card:");
        String definitionOfCard = processInput(scanner.nextLine());
        if (cardMap.containsValue(definitionOfCard)) {
            processOutput(String.format("The definition \"%s\" already exists.", definitionOfCard));
            return;
        }

        flashcards.add(new Flashcard(nameOfCard, definitionOfCard, 0));
        processOutput(String.format("The pair (\"%s\":\"%s\") has been added.", nameOfCard, definitionOfCard));
    }

    private void removeFlashcard() {
        Map<String, Flashcard> listOfCards = new HashMap<>();
        for (Flashcard flashcard : flashcards) {
            listOfCards.put(flashcard.getNameOfCard(), flashcard);
        }

        processOutput("Which card?");
        String nameOfCard = processInput(scanner.nextLine());

        if (listOfCards.containsKey(nameOfCard)) {
            flashcards.remove(listOfCards.get(nameOfCard));
            processOutput("The card has been removed.");
        } else {
            processOutput(String.format("Can't remove \"%s\": there is no such card.", nameOfCard));
        }
    }

    private void importFlashcards(String filename) {
        Map<String, Flashcard> flashcardsInMemory = new HashMap<>();
        for (Flashcard flashcard : flashcards) {
            flashcardsInMemory.put(flashcard.getNameOfCard(), flashcard);
        }

        File file = new File(filename);

        if (file.exists()) {
            int numberOfCards = 0;
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String currentLine = bufferedReader.readLine();
                while (currentLine != null) {

                    String[] inputs = currentLine.split(":");
                    String name = inputs[0];
                    String definition = inputs[1];
                    int mistakeCount = Integer.parseInt(inputs[2]);
                    Flashcard currentFlashCard = new Flashcard(name, definition, mistakeCount);

                    if (flashcardsInMemory.containsKey(name)) {
                        flashcards.remove(flashcardsInMemory.get(name));
                    }
                    flashcards.add(currentFlashCard);
                    numberOfCards++;
                    currentLine = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            processOutput(numberOfCards + " cards have been loaded.");
        } else {
            processOutput("File not found.");
        }
    }

    private void exportFlashcards(String filename) {
        try (FileWriter fileWriter = new FileWriter(filename)) {
            for (Flashcard flashcard : flashcards) {
                String line = flashcard.getNameOfCard() + ":" + flashcard.getCardDefinition() + ":" + flashcard.getNumberOfMistakes() + "\n";
                fileWriter.write(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        processOutput(String.format("%d cards have been saved.", flashcards.size()));
    }

    private void ask() {
        int numberOfQuestions;
        Map<String, String> flashcardMap = new LinkedHashMap<>();

        for (Flashcard flashcard : flashcards) {
            flashcardMap.put(flashcard.getCardDefinition(), flashcard.getNameOfCard());
        }

        processOutput("How many times to ask?");
        numberOfQuestions = Integer.parseInt(processInput(scanner.nextLine()));

        int currentCard = 0;
        for (Flashcard flashcard : flashcards) {
            if (currentCard > numberOfQuestions || currentCard >= flashcards.size()) {
                break;
            }

            processOutput("Print the definition of \"" + flashcard.getNameOfCard() + "\":");
            String userInput = processInput(scanner.nextLine());

            if (userInput.equals(flashcard.getCardDefinition())) {
                processOutput("Correct!");
            } else if (flashcardMap.containsKey(userInput)) {
                processOutput(String.format("Wrong. The right answer is \"%s\", but your definition is correct for \"%s\".",
                        flashcard.getCardDefinition(), flashcardMap.get(userInput)));
                flashcard.mistakeMade();
            } else {
                processOutput(String.format("Wrong. The right answer is \"%s\".", flashcard.getCardDefinition()));
                flashcard.mistakeMade();
            }

            currentCard++;
        }
    }

    private void printHardestCard() {
        int highestMistakeCount = flashcards.size() == 0 ? 0 :
                flashcards.stream().mapToInt(Flashcard::getNumberOfMistakes).max().getAsInt();

        List<Flashcard> hardestCards = flashcards.stream()
                .filter(flashcard -> flashcard.getNumberOfMistakes() == highestMistakeCount)
                .collect(Collectors.toList());

        if (highestMistakeCount == 0) {
            processOutput("There are no cards with errors.");
        } else {
            if (hardestCards.size() == 1) {
                processOutput(String.format("The hardest card is \"%s\". You have %d errors answering it.",
                        hardestCards.get(0).getNameOfCard(), hardestCards.get(0).getNumberOfMistakes()));
            } else {
                StringBuilder cardsToText = new StringBuilder("The hardest cards are ");
                for (int i = 0; i < hardestCards.size(); i++) {
                    cardsToText.append("\"").append(hardestCards.get(i).getNameOfCard()).append("\"");
                    if (i != hardestCards.size() - 1) {
                        cardsToText.append(", ");
                    }
                }
                cardsToText.append(". You have ").append(highestMistakeCount).append(" errors answering them.");
                processOutput(cardsToText.toString());
            }
        }
    }

    private void log() {
        String filename = getFileName();
        String message = "The log has been saved.";
        processOutput(message);

        try (FileWriter fileWriter = new FileWriter(filename)) {
            for (String line : log) {
                fileWriter.write(line + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetStats() {
        String output;
        for (Flashcard flashcard : flashcards) {
            flashcard.resetNumberOfMistakes();
        }
        output = "Card statistics have been reset.";
        processOutput(output);
    }

    private String processInput(String input) {
        log.add(input);
        return input;
    }

    private void processOutput(String output) {
        System.out.println(output);
        log.add(output);
    }

    private String getFileName() {
        processOutput("File name:");
        return processInput(scanner.nextLine());
    }

    private void processCommandArgs(String[] args) {
        List<String> commands = Arrays.asList(args);
        if (commands.contains("-import")) {
            int index = commands.indexOf("-import");
            if (!args[index + 1].equals("-export")) {
                String filename = args[index + 1];
                importFlashcards(filename);
            }
        }

        if (commands.contains("-export")) {
            int index = commands.indexOf("-export");
            if (!args[index + 1].equals("-import")) {
                fileToSaveOnExit = args[index + 1];
                saveOnExit = true;
            }
        }
    }
}