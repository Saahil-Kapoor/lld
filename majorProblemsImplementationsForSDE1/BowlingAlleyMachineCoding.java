import java.util.*;
//in game there are x players and 5 rounds , each rounds has x frames, each frame corresponds to a player , x frames has rolls.
/*
entities:

-Players
-id
-name

-Game
-id
-noOfPlayer
-playersId
-currentFrame
-currentPlayer
-status
-ScoringStrategy

Frames
-frameNumber
-playerId
-rolls
-score


Rolls{
-rollId
-pinsKnocked
}

there can be different bonus strategies
game can have various states
scoreboard can have observer strategy
and differentScoringStrategy
*/
import java.util.concurrent.atomic.AtomicInteger;

enum GameStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

class IdGenerator {

    static AtomicInteger count = new AtomicInteger(0);

    public static int getId() {
        return count.getAndIncrement();
    }
}

interface GameSubject {
    void registerObserver(GameObserver observer);

    void removeObserver(GameObserver observer);

    void notifyObservers();
}

interface GameObserver {
    void update(Game game);
}

class Player {
    int id;
    String name;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Game implements GameSubject {
    int id;
    int noOfPlayers;
    List<Integer> playersId;
    int currentFrame;
    int currentPlayer;
    GameStatus status;
    ScoringStrategy scoringStrategy;

    ArrayList<GameObserver> observers;

    public Game(int id, int noOfPlayers, List<Integer> playersId, ScoringStrategy scoringStrategy) {
        this.id = id;
        this.noOfPlayers = noOfPlayers;
        this.playersId = playersId;
        this.currentFrame = 1;
        this.currentPlayer = 0;
        this.status = GameStatus.NOT_STARTED;
        this.scoringStrategy = scoringStrategy;
        this.observers = new ArrayList<>();
    }

    public void registerObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.update(this);
        }
    }

    public void handleFramesAndPlayers() {
        System.out.println("Game is in progress...");
        System.out.println("Starting Frame " + currentFrame);
        if (currentFrame != 5) {
            int playerId = currentPlayer;
            Frame frame = new Frame(currentFrame, id, playerId);
            Scanner sc = new Scanner(System.in);
            System.out.println("Player " + PlayerRepo.getInstance().getPlayer(playerId).name + "'s turn:");
            for (int rollNo = 1; rollNo <= 2; rollNo++) {
                System.out.println("Enter pins knocked in roll " + rollNo + ":");
                int pinsKnocked = sc.nextInt();
                Roll roll = new Roll(IdGenerator.getId(), pinsKnocked);
                frame.rolls.add(roll);
                if (pinsKnocked == 10) {
                    System.out.println("Strike! Moving to next player.");
                    break;
                }
            }
            int frameScore = scoringStrategy.calculateScore(frame);
            frame.score = frameScore;
            System.out.println("Player " + PlayerRepo.getInstance().getPlayer(playerId).name
                    + "'s score in Frame " + currentFrame + " is: " + frameScore);
            sc.close();
        } else {
            int playerId = currentPlayer;
            Frame frame = new Frame(currentFrame, id, playerId);
            Scanner sc = new Scanner(System.in);
            System.out.println("Player " + PlayerRepo.getInstance().getPlayer(playerId).name + "'s turn:");
            for (int rollNo = 1; rollNo <= 2; rollNo++) {
                System.out.println("Enter pins knocked in roll " + rollNo + ":");
                int pinsKnocked = sc.nextInt();
                Roll roll = new Roll(IdGenerator.getId(), pinsKnocked);
                frame.rolls.add(roll);
                if (pinsKnocked == 10) {
                    System.out.println("Strike! Moving to next player.");
                    break;
                }
            }
            if (frame.rolls.size() == 1 || (frame.rolls.get(0).pinsKnocked + frame.rolls.get(1).pinsKnocked) == 10) {
                System.out.println("You earned an extra roll! Enter pins knocked in extra roll:");
                int pinsKnocked = sc.nextInt();
                Roll roll = new Roll(IdGenerator.getId(), pinsKnocked);
                frame.rolls.add(roll);
                pinsKnocked = sc.nextInt();
                roll = new Roll(IdGenerator.getId(), pinsKnocked);
                frame.rolls.add(roll);
            }
            int frameScore = scoringStrategy.calculateScore(frame);
            frame.score = frameScore;
            System.out.println("Player " + PlayerRepo.getInstance().getPlayer(playerId).name
                    + "'s score in Frame " + currentFrame + " is: " + frameScore);
            sc.close();
        }
        notifyObservers();
    }
}

class ScoreBoard implements GameObserver {
    Game game; // playerId -> score

    public ScoreBoard() {
    }

    @Override
    public void update(Game game) {
        // Update scoreboard based on game's current state
        this.game = game;
        displayScores();
    }



    public void displayScores() {
        int currentFrame = game.currentFrame;
        int currentPlayer = game.currentPlayer;
        List<Frame> frames = FrameRepo.getInstance().getFrames(game.id);
        List<Roll> rolls = new ArrayList<>();
        for(Frame frame : frames) {
            if (frame.frameNumber == currentFrame && frame.playerId == currentPlayer) {
                rolls = frame.rolls;
                break;
            }
        }
        System.out.println("++++++++++++++++++++++++++++++++++++++");
        for(int i = 0;i<rolls.size();i++) {
            Roll roll = rolls.get(i);
            if(roll.pinsKnocked == 10){
                System.out.print("X ");
            } else if(i > 0 && rolls.get(i-1).pinsKnocked + roll.pinsKnocked == 10){
                System.out.print("/ ");
            }
            else{
                System.out.print(roll.pinsKnocked + " ");
            }
        }
    }
}

class Roll {
    int rollId;
    int pinsKnocked;

    public Roll(int rollId, int pinsKnocked) {
        this.rollId = rollId;
        this.pinsKnocked = pinsKnocked;
    }
}

class Frame {
    int frameNumber;
    int score;
    int gameId;
    List<Roll> rolls;
    int playerId;

    public Frame(int frameNumber, int gameId, int playerId) {
        this.frameNumber = frameNumber;
        this.score = 0;
        this.rolls = new ArrayList<>();
        this.playerId = playerId;
        this.gameId = gameId;
    }
}

class PlayerRepo {
    HashMap<Integer, Player> playerMap;
    private static PlayerRepo instance = null;

    private PlayerRepo() {
        playerMap = new HashMap<>();
    }

    public static PlayerRepo getInstance() {
        if (instance == null) {
            instance = new PlayerRepo();
        }
        return instance;
    }

    public void addPlayer(Player player) {
        playerMap.put(player.id, player);
    }

    public Player getPlayer(int playerId) {
        return playerMap.get(playerId);
    }
}

class GameRepo {
    HashMap<Integer, Game> gameMap;
    private static GameRepo instance = null;

    private GameRepo() {
        gameMap = new HashMap<>();
    }

    public static GameRepo getInstance() {
        if (instance == null) {
            instance = new GameRepo();
        }
        return instance;
    }

    public void addGame(Game game) {
        gameMap.put(game.id, game);
    }

    public Game getGame(int gameId) {
        return gameMap.get(gameId);
    }
}

class FrameRepo {
    HashMap<Integer,List<Frame>> gameFramesMap; //frameNumber_PlayerNumber -> Frame
    private static FrameRepo instance = null;

    private FrameRepo() {
        gameFramesMap = new HashMap<>();
    }

    public static FrameRepo getInstance() {
        if (instance == null) {
            instance = new FrameRepo();
        }
        return instance;
    }

    public void addFrame(int gameId, Frame frame) {
        gameFramesMap.putIfAbsent(gameId, new ArrayList<>());
        gameFramesMap.get(gameId).add(frame);
    }

    public List<Frame> getFrames(int gameId) {
        return gameFramesMap.getOrDefault(gameId, new ArrayList<>());
    }
}

interface ScoringStrategy {
    int calculateScore(Frame frame);
}

class StandardScoringStrategy implements ScoringStrategy {
    @Override
    public int calculateScore(Frame frame) {
        List<Roll> rolls = frame.rolls;
        int score = 0;
        int n = rolls.size();
        // {3,7,2,6};
        for (int i = 0; i < n; i++) {
            int curr = rolls.get(i).pinsKnocked;
            if (curr == 10) {
                score += 20;
            } else {
                if (i + 1 < n && (curr + rolls.get(i + 1).pinsKnocked) == 10) {
                    score += 15;
                    i++;
                } else {
                    score += curr;
                }
            }
        }
        return score;
    }
}

interface BowlingStates {
    void startGame(Game game, GameContext context);

    void inprogressState(Game game, GameContext context);

    void endGame(Game game, GameContext context);
}

class GameContext {
    BowlingStates state;

    public GameContext(BowlingStates state) {
        this.state = state;
    }

    public void setState(BowlingStates state) {
        this.state = state;
    }
}

class IdleGameState implements BowlingStates {
    @Override
    public void startGame(Game game, GameContext context) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of players to start the game:");
        int noOfPlayers = sc.nextInt();
        game.noOfPlayers = noOfPlayers;
        List<Integer> playersId = new ArrayList<>();
        for (int i = 0; i < noOfPlayers; i++) {
            System.out.println("Enter Player " + (i + 1) + " name:");
            String name = sc.next();
            Player player = new Player(IdGenerator.getId(), name);
            PlayerRepo.getInstance().addPlayer(player);
            playersId.add(player.id);
        }
        sc.close();
        game.playersId = playersId;
        game.currentFrame = 1;
        game.currentPlayer = 0;
        game.scoringStrategy = new StandardScoringStrategy();
        System.out.println("Enter the Players name and details to start the game");
        game.status = GameStatus.IN_PROGRESS;
        System.out.println("Game started successfully");
        context.setState(new InProgressGameState());
    }

    @Override
    public void inprogressState(Game game, GameContext context) {
        System.out.println("Game is not started yet");
    }

    @Override
    public void endGame(Game game, GameContext context) {
        System.out.println("Game is not started yet");
    }
}

class InProgressGameState implements BowlingStates {
    @Override
    public void startGame(Game game, GameContext context) {
        System.out.println("Game is already in progress");
    }

    @Override
    public void inprogressState(Game game, GameContext context) {
        game.handleFramesAndPlayers();
        game.currentPlayer++;
        if (game.currentPlayer >= game.noOfPlayers) {
            game.currentPlayer = 0;
            game.currentFrame++;
        }
        if (game.currentFrame > 5) {
            System.out.println("All frames completed. Ending game.");
            endGame(game, context);
        }
    }

    @Override
    public void endGame(Game game, GameContext context) {
        System.out.println("Ending the game...");
        game.status = GameStatus.COMPLETED;
        context.setState(new CompletedGameState());
        System.out.println("Game ended successfully");
    }
}

class CompletedGameState implements BowlingStates {
    @Override
    public void startGame(Game game, GameContext context) {
        System.out.println("Game has already been completed");
    }

    @Override
    public void inprogressState(Game game, GameContext context) {
        System.out.println("Game has already been completed");
    }

    @Override
    public void endGame(Game game, GameContext context) {
        System.out.println("Game has already been completed");
    }
}

public class BowlingAlleyMachineCoding {
    public static void main(String[] args) {

    }
}