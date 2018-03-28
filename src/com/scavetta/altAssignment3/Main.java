package com.scavetta.altAssignment3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {

    private static Random rng = new Random();
    private static int stopWalkValue = 1000;
    private static boolean debugLogging = false;

    public static void main(String[] args) {

        // winning board
        //int[] myIntArray = new int[]{5,1,8,4,2,7,3,6};

        // one queen misplaced
        //int[] myIntArray = new int[]{5,2,8,4,2,7,3,6};

        // board on assignment
        //int[] myIntArray = new int[]{3,5,0,2,2,4,6,5};

        //Random Array
        int[] myIntArray = getRandomIntArray(8);

        runStats(myIntArray, SelectionStrategy.TwoStage);

        runStats(myIntArray, SelectionStrategy.AnyConflict);
    }

    public static void runStats(int[] arrayToRun, SelectionStrategy strat){
        int numberOfTrials = 5000;
        int numberOfFailedAttempts = 0;
        int numberOfPassedAttempts = 0;

        System.out.println("Running " + numberOfTrials +  " Trials on Array: " + Arrays.toString(arrayToRun) + " Using Strategy: " + strat);

        for(int i = 0; i < numberOfTrials; i++){
            if(getNumberOfConflicts(localSearchQueens(arrayToRun, strat)).totalNumberOfConflicts == 0){
                numberOfPassedAttempts++;
            } else {
                numberOfFailedAttempts++;
            }
        }

        System.out.println("Number of failed attempts: " + numberOfFailedAttempts);
        System.out.println("Number of passed attempts: " + numberOfPassedAttempts);

        System.out.println("Percentage Passed: " + ((double)numberOfPassedAttempts/(double)numberOfTrials) * 100d);
    }

    /**
     * Preform the local search for the n queens problem.
     * @param board The board that we are trying to solve
     * @param strat The strategy to use
     * @return Integer array containing a possibly solved board.
     */
    public static int[] localSearchQueens(int[] board, SelectionStrategy strat){
        int[] solvedBoard = board.clone();
        BoardState currentBoardStateEval = getNumberOfConflicts(solvedBoard);
        int stopWalk = 0;

        //While there are conflicts on the board, keep going
        while(currentBoardStateEval.totalNumberOfConflicts > 0 && stopWalk < stopWalkValue){
            if(debugLogging) {
                System.out.println("Current Board: " + Arrays.toString(solvedBoard) + " Conflicts: " + currentBoardStateEval.totalNumberOfConflicts);
            }

            int indexToAlter = -1;

            //Which Strat are we using
            if(strat == SelectionStrategy.TwoStage) {
                //two-stage method: select index that has the most conflicts
                indexToAlter = getIndexWithMostConflicts(currentBoardStateEval);
                if(debugLogging) {
                    System.out.println("index with most conflicts: " + indexToAlter);
                }
            } else if (strat == SelectionStrategy.AnyConflict) {
                //any conflict method: select any index that has a conflict
                indexToAlter = randomlySelectedIndexWithConflict(currentBoardStateEval);
                if(debugLogging) {
                    System.out.println("random index With conflicts: " + indexToAlter);
                }
            }

            //examine possible states and find one that has the least conflicts
            solvedBoard = getBoardWithLeastConflicts(solvedBoard, indexToAlter);
            currentBoardStateEval = getNumberOfConflicts(solvedBoard);

            if(debugLogging) {
                System.out.println("Board with least conflicts: " + Arrays.toString(solvedBoard));
            }

            stopWalk++;
            if(debugLogging) {
                System.out.println("Finished loop: " + stopWalk);
            }
        }

        if(debugLogging)
            if(stopWalk < stopWalkValue){
                System.out.println("Actually found answer in " + stopWalk + " steps");
            } else {
                System.out.println("Walk Stopped, too many loops");
            }

        return solvedBoard;
    }

    /**
     * Return the integer array that represents the board that has the least number of conflicts.
     * @param currentBoard Current board that will be modified
     * @param indexToModify index that has is going to be modified
     * @return
     */
    private static int[] getBoardWithLeastConflicts(int[] currentBoard, int indexToModify){
        List<BoardState> possibleStates = getPossibleBoardStates(currentBoard, indexToModify);

        if(debugLogging) {
            for (BoardState s : possibleStates) {
                System.out.println(s);
            }
        }

        BoardState leastConflictedState = null;

        for(BoardState state : possibleStates){
            if(leastConflictedState == null){
                leastConflictedState = state;
            } else if(leastConflictedState.totalNumberOfConflicts > state.totalNumberOfConflicts){
                if(debugLogging) {
                    System.out.println("Found a new least conflicted state, Current: " + leastConflictedState.totalNumberOfConflicts + " > New:" + state.totalNumberOfConflicts);
                }
                leastConflictedState = state;
            }
        }

        return leastConflictedState.board;
    }

    /**
     * Get the possible board states for a given board and the index we are going to change
     * @param board The current board to modify
     * @param mostConflicts The index that has the most conflicts. This is the index we are going to modify
     * @return List of possible board states
     */
    private static List<BoardState> getPossibleBoardStates(int[] board, int mostConflicts) {
        ArrayList<BoardState> possibleStates = new ArrayList<>();

        int[] tmpArray = board.clone();
        for(int i = 0; i < board.length; i++){
            tmpArray[mostConflicts] = i;
            possibleStates.add(getNumberOfConflicts(tmpArray));
        }

        return possibleStates;
    }

    /**
     * Returns the index that has the most conflicts for a given board
     * @param board Board to get the index from
     * @return index that represents the location on the board that has the most conflicts
     */
    private static int getIndexWithMostConflicts(BoardState board){
        int mostConflictsIndex = -1;
        int mostConflicts = -1;

        for(int i = 0; i < board.whereConflictsAre.length; i++){
            if(mostConflicts < board.whereConflictsAre[i]){
                mostConflictsIndex = i;
                mostConflicts = board.whereConflictsAre[i];
                if(debugLogging) {
                    System.out.println("Found a new conflicted index: " + mostConflicts + " < " + board.whereConflictsAre[i]);
                }
            }
        }

        return mostConflictsIndex;
    }

    /**
     * Return a random integer that represents an index on the board that has conflicts
     * @param board Board to get random index from
     * @return index of random conflicted cell
     */
    private static int randomlySelectedIndexWithConflict(BoardState board){

        int randomIndex;
        boolean found = false;

        do {
            randomIndex = rng.nextInt(board.whereConflictsAre.length);
            if(board.whereConflictsAre[randomIndex] > 0 ){
                found = true;
            }

        } while (!found);

        return randomIndex;
    }

    /**
     * Return a board state for the given board.
     * @param board Board represented by an integer array
     * @return Board state for the given board.
     */
    public static BoardState getNumberOfConflicts(int[] board){
        int conflicts = 0;
        int[] conflictsArray = new int[board.length];

        for(int row = 0; row < board.length; row++){

            int column = board[row];

            for(int otherRow = row + 1; otherRow < board.length; otherRow++){

                if(column == board[otherRow]){
                    conflicts++;
                    conflictsArray[row]++;
                    conflictsArray[otherRow]++;
                }

                int rowDiff = otherRow - row;
                int badColumn = column - rowDiff;

                if(badColumn >= 0){
                    if(board[otherRow] == badColumn){
                        conflicts++;
                        conflictsArray[row]++;
                        conflictsArray[otherRow]++;
                    }
                }

                badColumn = column + rowDiff;
                if(badColumn <= 7){
                    if(board[otherRow] == badColumn){
                        conflicts++;
                        conflictsArray[row]++;
                        conflictsArray[otherRow]++;
                    }
                }
            }
        }

        return new BoardState(conflicts, conflictsArray, board);
    }

    /**
     * Returns an integer array containing random ints bound by the size of the array. The parameter is used to specify
     * the new size of the returned array an the bounds of the random integers
     * @param size Size of the new array
     * @return random integer array bound by the passed in parameter
     */
    private static int[] getRandomIntArray(int size){
        int[] randomIntArray = new int[size];

        for(int i = 0; i < randomIntArray.length; i++){
            randomIntArray[i] = rng.nextInt(size);
        }

        return randomIntArray;
    }

    /**
     * Model class that represents a board state. Contains the total number of conflicts, where they are, and the board
     * that was evaluated.
     */
    public static class BoardState {
        public int totalNumberOfConflicts;
        public int[] whereConflictsAre;
        public int[] board;

        /**
         * Constructor for the BoardState Class
         * @param totalNumberOfConflicts Total number of conflicts
         * @param whereConflictsAre integer array that represents the number of conflicts at each index
         * @param board Board that was evaluated
         */
        public BoardState(int totalNumberOfConflicts, int[] whereConflictsAre, int[] board){
            this.totalNumberOfConflicts = totalNumberOfConflicts;
            this.whereConflictsAre = whereConflictsAre;
            this.board = Arrays.copyOf(board, board.length);
        }

        /**
         * @return String representation of the board state
         */
        @Override
        public String toString() {
            return "Total Conflicts: " + totalNumberOfConflicts + " , conflict Array: " + Arrays.toString(whereConflictsAre) + " , Board Array: " + Arrays.toString(board);
        }
    }

    /**
     * Enum representing the different selection strategies that could be used in the local search algorithm
     */
    public enum SelectionStrategy {
        TwoStage,
        AnyConflict
    }
}
