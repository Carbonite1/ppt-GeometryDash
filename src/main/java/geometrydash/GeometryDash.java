package geometrydash;

import java.util.*;

public class GeometryDash {
    /**
     * Returns whether the given level can be completed using the given play.
     *
     * @param level is not null and not empty
     * @param play  is not null and not empty
     * @return true if the play completes the level and false otherwise
     */
    public static boolean isSuccessfulPlay(String level, String play) {
        if (level.startsWith("^") || level.endsWith("^")) {
            return false;
        }

        int positionTracker = 0;


        for (int i = 0; i < play.length(); i++) {
            int thisPlay = Character.getNumericValue(play.charAt(i));
            positionTracker += thisPlay;
            if (positionTracker >= level.length() - 1) {
                return true;
            }
            if (level.charAt(positionTracker) == '^') {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the subset of plays which can complete the given level ending
     * with the target resting energy
     *
     * @param level               is not null and not empty
     * @param possiblePlays       is not null
     * @param startingEnergy      the energy at the start of the level
     * @param targetRestingEnergy the minimum energy to end the level at
     * @return a subset of {@code possiblePlays} which complete the level with
     * {@code targetRestingEnergy} units of energy remaining
     */
    public static Set<String> successfulPlays(String level, Set<String> possiblePlays,
                                              int startingEnergy, int targetRestingEnergy) {

        boolean thisPlayFailed = false;
        Set<String> successfulPlays = new HashSet<>();
        for (String thisPlay : possiblePlays) {
            thisPlayFailed = false;

            int energyTracker = startingEnergy;

            if (level.startsWith("^") || level.endsWith("^")) {
                continue;
            }

            int positionTracker = 0;

            for (int i = 0; i < thisPlay.length(); i++) {
                if (thisPlayFailed) {
                    continue;
                }
                int currentAction = Character.getNumericValue(thisPlay.charAt(i));
                positionTracker += currentAction;

                switch (Character.getNumericValue(thisPlay.charAt(i))) {
                    case 0:
                        if (energyTracker < 3) {
                            thisPlayFailed = true;
                            continue;
                        }
                        energyTracker += 1;
                        break;

                    case 1:
                        energyTracker -= 1;
                        break;

                    case 2:
                        energyTracker -= 2;
                        break;

                    case 3:
                        energyTracker -= 3;
                        break;

                    default:
                        energyTracker = energyTracker;
                }
                if (energyTracker < 0) {
                    thisPlayFailed = true;
                    continue;
                }

                if (level.charAt(positionTracker) == '*') {
                    positionTracker += 4;
                    if (positionTracker > level.length() - 1) {
                        thisPlayFailed = true;
                        continue;
                    }
                }
                if (positionTracker == level.length() - 1) {
                    if (energyTracker < targetRestingEnergy) {
                        thisPlayFailed = true;
                        continue;
                    }
                    if (i == thisPlay.length() - 1) {
                        successfulPlays.add(thisPlay);
                    } else {
                        thisPlayFailed = true;
                    }
                    continue;
                }
                if (level.charAt(positionTracker) == '^') {
                    thisPlayFailed = true;
                    continue;
                }


            }

        }
        return successfulPlays;
    }

    public static String shortestPlay(String level, int startingEnergy, int targetRestingEnergy)
            throws UnplayableLevelException {

        if (level.startsWith("^") || level.endsWith("^")) {
            throw new UnplayableLevelException();
        }

        Queue<State> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(new State(0, startingEnergy, "", 0));

        while (!queue.isEmpty()) {
            State current = queue.poll();

            if (current.position == level.length() - 1 && current.energy >= targetRestingEnergy) {
                return current.moves;
            }

            if (visited.contains(current.position + ":" + current.energy)) {
                continue;
            }
            visited.add(current.position + ":" + current.energy);

            for (int move = 0; move <= 3; move++) {
                int newPos = current.position + move;
                int newEnergy = current.energy;

                if (move == 1) newEnergy -= 1;
                else if (move == 2) newEnergy -= 2;
                else if (move == 3) newEnergy -= 3;
                else if (move == 0 && newEnergy < 3) newEnergy += 1;

                if (newEnergy < 0 || newPos >= level.length() || level.charAt(newPos) == '^') {
                    continue;
                }

                if (level.charAt(newPos) == '*') {
                    newPos += 4;
                    if (newPos >= level.length() || level.charAt(newPos) == '^') {
                        continue;
                    }
                }

                queue.add(new State(newPos, newEnergy, current.moves + move, current.moves.length() + 1));
            }
        }

        throw new UnplayableLevelException();
    }

    public static int numberOfPlays(String level, int startingEnergy, int targetRestingEnergy) {

        int N = level.length();
        int Emax = Math.max(startingEnergy, 3); // Maximum energy possible

        // dp[position][energy] = number of ways to reach position with energy
        int[][] dp = new int[N][Emax + 1];

        dp[0][startingEnergy] = 1; // Start position

        for (int position = 0; position < N; position++) {
            for (int energy = 0; energy <= Emax; energy++) {
                if (dp[position][energy] > 0) {
                    if (position == N - 1) {
                        continue; // No moves from goal position
                    }

                    for (int move = 0; move <= 3; move++) {
                        int newPosition = position;
                        int newEnergy = energy;

                        if (move == 0) {
                            // Move 0: can only be played if energy < 3
                            if (energy >= 3) {
                                continue;
                            }
                            newEnergy = energy + 1;
                        } else {
                            // Moves 1, 2, 3: consume energy
                            if (energy < move) {
                                continue; // Not enough energy
                            }
                            newEnergy = energy - move;
                            newPosition = position + move;
                        }

                        // Check if newPosition is within bounds
                        if (newPosition >= N) {
                            continue; // Invalid move
                        }

                        // Check if tile at newPosition is dangerous
                        if (level.charAt(newPosition) == '^') {
                            continue; // Can't land on dangerous tile
                        }

                        // If tile is '*', apply teleportation
                        if (level.charAt(newPosition) == '*') {
                            newPosition += 4;
                            if (newPosition >= N) {
                                continue; // Teleportation goes outside the level
                            }
                            if (level.charAt(newPosition) == '^') {
                                continue; // Can't land on dangerous tile
                            }
                        }

                        // Update dp table
                        dp[newPosition][newEnergy] += dp[position][energy];
                    }
                }
            }
        }

        // Sum up the number of ways to reach the goal position with sufficient energy
        int totalWays = 0;
        for (int energy = targetRestingEnergy; energy <= Emax; energy++) {
            totalWays += dp[N - 1][energy];
        }

        return totalWays;
    }



    static class State {
        int position;
        int energy;
        String moves;
        int steps;

        State(int position, int energy, String moves, int steps) {
            this.position = position;
            this.energy = energy;
            this.moves = moves;
            this.steps = steps;
        }
    }
}
