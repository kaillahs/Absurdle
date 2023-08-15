// Kaillah Selvaretnam
// Absurdle
// This program creats a game similar to the popular NYT game "Wordle", called "Absurdle".
// The player gets to indicate which dictionary they would like the game to be based off of, 
// as well as the length of the word they will be expected to guess. Each incorrect guess recieves
// feedback in the form of colored tiles indicating how closely each letter matches the target
// word. Guessed letters that exactly match the target word are marked green while letters that
// are in the target word but not in the right possition are marked yellow, and letters that are
// not in the taget word are marked gray. The caviat with Absurdle is that it does not actually
// pick a target word at the beginning of the game. Absurdle gives the impression of picking a
// single secret word, but instead what it actually does is consider the entire list of all 
// possible secret words. Each time you guess, Absurdle prunes its internal list as little as 
// possible, attempting to intentionally prolong the game as much as possible.

import java.util.*;
import java.io.*;

public class Absurdle  {
    public static final String GREEN = "🟩";
    public static final String YELLOW = "🟨";
    public static final String GRAY = "⬜";

    // [[ ALL OF MAIN PROVIDED ]]
    public static void main(String[] args) throws FileNotFoundException {
        Scanner console = new Scanner(System.in);
        System.out.println("Welcome to the game of Absurdle.");

        System.out.print("What dictionary would you like to use? ");
        String dictName = console.next();

        System.out.print("What length word would you like to guess? ");
        int wordLength = console.nextInt();

        List<String> contents = loadFile(new Scanner(new File(dictName)));
        Set<String> words = pruneDictionary(contents, wordLength);

        List<String> guessedPatterns = new ArrayList<>();
        while (!isFinished(guessedPatterns)) {
            System.out.print("> ");
            String guess = console.next();
            String pattern = record(guess, words, wordLength);
            guessedPatterns.add(pattern);
            System.out.println(": " + pattern);
            System.out.println();
        }
        System.out.println("Absurdle " + guessedPatterns.size() + "/∞");
        System.out.println();
        printPatterns(guessedPatterns);
    }

    // [[ PROVIDED ]]
    // Prints out the given list of patterns.
    // - List<String> patterns: list of patterns from the game
    public static void printPatterns(List<String> patterns) {
        for (String pattern : patterns) {
            System.out.println(pattern);
        }
    }

    // [[ PROVIDED ]]
    // Returns true if the game is finished, meaning the user guessed the word. Returns
    // false otherwise.
    // - List<String> patterns: list of patterns from the game
    public static boolean isFinished(List<String> patterns) {
        if (patterns.isEmpty()) {
            return false;
        }
        String lastPattern = patterns.get(patterns.size() - 1);
        return !lastPattern.contains("⬜") && !lastPattern.contains("🟨");
    }

    // [[ PROVIDED ]]
    // Loads the contents of a given file Scanner into a List<String> and returns it.
    // - Scanner dictScan: contains file contents
    public static List<String> loadFile(Scanner dictScan) {
        List<String> contents = new ArrayList<>();
        while (dictScan.hasNext()) {
            contents.add(dictScan.next());
        }
        return contents;
    }

    // This method creates a new set of words consisting of words in the indicated dictionary that
    // are of the word length that the user specified. The parameter of the method is a 
    // List<String> representing the full indicated dictionary as well as an integer representing
    // the users chosen wordlength that they would like the target word to be. The method returs
    // a Set<String> which represents the set of words that are both in the indicated dictionary
    // as well as of the given word length. An IllegalArgumentException is thrown if the user
    // indicated a word length that was less than 1. 
    public static Set<String> pruneDictionary(List<String> contents, int wordLength) {
        if (wordLength < 1) {
            throw new IllegalArgumentException();
        }

        Set<String> prunedDict = new TreeSet<>();
        
        for (String word : contents) {
            if (word.length() == wordLength) {
                prunedDict.add(word);
            }
        }
        return prunedDict;
    }

    // This method creates an association between a pattern and all possible target words that
    // would create a certain pattern given the user's guess word. In other words, all words 
    // possible target words that create the same pattern are grouped together and associated 
    // with that pattern. The parameter of the method is a String representing the users guess
    // as well as a Set<String> representing all possible target words. The method returns a 
    // Map<String, Set<String>> where the key is the pattern in the form of a String and the 
    // value is a Set<String> containing all possible target words associated with that pattern
    // given the users guess. 
    public static Map<String, Set<String>> patternToWordMap(String guess, Set<String> words) {
        Map<String, Set<String>> patternToWord = new TreeMap<>();
        for (String possibleWord: words) {
            String pattern = patternFor(possibleWord, guess);
            if (!patternToWord.containsKey(pattern)) {
                Set<String> associatedWordSet = new TreeSet<>();
                associatedWordSet.add(possibleWord);
                patternToWord.put(pattern, associatedWordSet);
            } else {
                Set<String> associatedWordSet = patternToWord.get(pattern);
                associatedWordSet.add(possibleWord);
            }
        }
        return patternToWord;
    }

    // This method alters the a set of words that contains all possible target words. The method
    // alters the Set by changing it to contain only the words associated with the pattern that has
    // the most words associated with it. If there is a tie between two word sets, the method picks
    // the pattern that comes first in sorted order. Additionally, the method then gives the user 
    // the pattern that is associated with the chosen word set. 
    // The parameter of this method is a String representing the users guess, a Set<String> 
    // representing the possible target words, as well a the wordlength indicated by the user. The 
    // method returns the pattern, in the form of a String, that has the most target words 
    // associated with it. An IllegalArgumentException is thrown if the users guess is not equal to
    // the prior indicated word lenght or if there are no more possible target words. 
    public static String record(String guess, Set<String> words, int wordLength) {
        if (guess.length() != wordLength || words.size() == 0) {
            throw new IllegalArgumentException();
        }
        
        Map<String, Set<String>> patternToWordMap = patternToWordMap(guess, words);        
        int max = 0;
        String chosenPattern = "";
        Set<String> chosenWordSet = new TreeSet<>();
        for (String possiblePattern : patternToWordMap.keySet()) {
            Set<String> wordSet = patternToWordMap.get(possiblePattern);
            int size = wordSet.size();
            if (size > max) {
                max = size;
                chosenWordSet = wordSet;
                chosenPattern = possiblePattern;
            }
        }
        words.clear();
        words.addAll(chosenWordSet);
        return chosenPattern;
    }

    // This method creates a pattern associated with the users guess for a particular target word.
    // This pattern consists of green, yellow, and gray squares. A green square replace the
    // letters in the guessed word that are used in the target word and also in the correct spot.
    // The yellow squares replace letters in the guessed word that are used in the target word but
    // not in the correct spot. The gray squares replace letter in the guessed word that are not
    // used in the target word. The methods parameters are the target word as well as the users 
    // guess both in the form of a String. The return of this method is a String which represents
    // the pattern of Grey, Yellow, and Green squares that was created. 
    public static String patternFor(String word, String guess) {
        
        //creates list of characters contained in guess word
        List<String> guessCharList = new ArrayList<>();
        for (int i = 0; i < guess.length(); i++) {
            guessCharList.add(String.valueOf(guess.charAt(i)));
        }

        //assigns count to each character in word 
        Map<String, Integer> wordCharMap = new TreeMap<>();
        for (int i = 0; i < word.length(); i++) {
            String character = String.valueOf(word.charAt(i));
            if (!wordCharMap.containsKey(character)) {
                wordCharMap.put(character, 1);
            } else {
                int count = wordCharMap.get(character);
                wordCharMap.put(character, count + 1);
            }
        }

        //see if guess letter is in correct spot compared to word and replace w green.
        for (int i = 0; i < guessCharList.size(); i++) {
            String wordChar = String.valueOf(word.charAt(i));
            int updateCount = wordCharMap.get(wordChar) - 1;
            if (guessCharList.get(i).equals(wordChar)) {
                wordCharMap.put(wordChar, updateCount);
                guessCharList.set(i, GREEN);
            }
        }

        //change to YELLOW if letter is correct but in wrong spot
        for (int i = 0; i < guessCharList.size(); i++) {
            if (word.contains(guessCharList.get(i))) {               
                if (wordCharMap.get(guessCharList.get(i)) > 0) {
                    int count = wordCharMap.get(guessCharList.get(i));
                    wordCharMap.put(guessCharList.get(i), count - 1);
                    guessCharList.set(i, YELLOW);
               }             
            }
        }
        // make the rest gray
        for (int i = 0; i < guessCharList.size(); i++) {
            if (!guessCharList.get(i).equals(GREEN) && !guessCharList.get(i).equals(YELLOW)) {     
                    guessCharList.set(i, GRAY);
               }             
        }        
        
        String output = "";
        for (int i = 0; i < guessCharList.size(); i++) {
            String color = guessCharList.get(i);
            output = output + color;
        }
        return output;
    }
}
