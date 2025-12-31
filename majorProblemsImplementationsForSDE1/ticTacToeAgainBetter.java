//this code has 8.5 rating from chat gpt



import java.util.*;
/*
in games there are states
GameState
inprogressstate
DrawState
WinnerState

classes present are 
classes
Player
-symbol
-name
-id
cell
-symbol
board
-size
-cells
-moveCounts

enums
Symbols
Empty('_')
X('x')
O('O')

WinningstrategyPattern
rowWinning
colWinning
diagonal Winning

Observer Pattern is also there
scoreboard is the observer
board is the subject

*/

enum Symbols {
    EMPTY('_'),
    X('x'),
    O('o');

    public final char symbol;

    private Symbols(char symbols) {
        this.symbol = symbols;
    }

    public char getChar() {
        return symbol;
    }
}

enum GameStatus{
    IN_PROGRESS,
    WINNER_X,
    WINNER_O,
    DRAW
}

class Player {
    String name;
    Symbols symbol;

    public Player(String name, Symbols symbol) {
        this.name = name;
        this.symbol = symbol;
    }
}

class Cell {
    private Symbols symbol;

    public Cell(Symbols symbols) {
        this.symbol = symbols;
    }

    public Cell() {
        this.symbol = Symbols.EMPTY;
    }

    public Symbols getSymbol() {
        return symbol;
    }
    public void setSymbol(Player p){
        this.symbol = p.symbol;
    }
}   



class Board {
    int size;
    Cell[][] board;
    int movesCount;

    public Board(int size) {
        this.size = size;
        board = new Cell[size][size];
        this.movesCount = 0;
        initializeBoard();
    }

    public void increaseMoveCount(){
        movesCount++;
    }

    public int getMoveCount(){
        return movesCount;
    }

    public void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = new Cell();
            }
        }
    }

    public void printBoard() {
        System.out.println("-----------");
        for (int i = 0; i < size; i++) {
            System.out.print("| ");
            for (int j = 0; j < size; j++) {
                Symbols symbol = board[i][j].getSymbol();
                System.out.print(symbol.getChar() + " |");
            }
            System.out.println("\n---------------");
        }
    }

    public Symbols getCell(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return null;
        }
        return board[row][col].getSymbol();
    }

    public int getSize() {
        return size;
    }
}

interface WinningStrategy {
    public boolean checkWin(Board board, Player player);
}

class RowWinningStrategy implements WinningStrategy {
    public boolean checkWin(Board board, Player player) {
        boolean won = true;
        for (int i = 0; i < board.getSize(); i++) {
            won = true;
            for (int j = 0; j < board.getSize(); j++) {
                if (board.getCell(i, j) != player.symbol) {
                    won = false;
                }
            }
            if (won) {
                return true;
            }
        }
        return false;
    }
}

class ColWinningStrategy implements WinningStrategy {
    public boolean checkWin(Board board, Player player) {
        boolean won = true;
        for (int i = 0; i < board.getSize(); i++) {
            won = true;
            for (int j = 0; j < board.getSize(); j++) {
                if (board.getCell(j, i) != player.symbol) {
                    won = false;
                }
            }
            if (won) {
                return true;
            }
        }
        return false;
    }
}

class DiagonalWinningStrategy implements WinningStrategy {
    public boolean checkWin(Board board, Player player) {
        boolean won = true;
        for (int i = 0; i < board.getSize(); i++) {
            if (board.getCell(i, i) != player.symbol) {
                won = false;
            }
        }
        if(won){
            return true;
        }
        won = true;
        int str = board.getSize()-1;
        int stc = 0;
        for (int i = 0; i < board.getSize(); i++) {
            if (board.getCell(str, stc) != player.symbol) {
                won = false;
            }
            str--;
            stc++;
        }
        if(won){
            return true;
        }
        return false;
    }
}
interface Subject{
    public void addObserver(Observer o);
    public void removeObserver(Observer o);
    public void notifyObservers();
}

interface Observer{
    public void update(Game game);
}

class ScoreBoard implements Observer{
    HashMap<String ,Integer> scores;
    ScoreBoard(){
        scores = new HashMap<>();
    }
    public void update(Game game){
        if(game.winner!= null){
            scores.put(game.winner.name, scores.getOrDefault(game.winner.name, 0)+1);
            System.out.println("Scoreboard changed");
        }
    }
    public void printScores(){
        for(String key:scores.keySet()){
            System.out.println(key+" : "+scores.get(key));
        }
    }
}

class Game implements Subject{
    Board board;
    Player p1;
    Player p2;
    Player currentPlayer;
    Player winner;
    GameState state;
    GameStatus status;
    List<WinningStrategy> winningStrategies;


    List<Observer> observers = new ArrayList<>();


    Game(Player p1,Player p2,int size){
        board = new Board(size);
        this.p1 = p1;
        this.p2 = p2;
        this.currentPlayer = p1;
        this.status = GameStatus.IN_PROGRESS;
        this.state = new InprogresState();
        this.winningStrategies = List.of(new RowWinningStrategy(),new ColWinningStrategy(),new DiagonalWinningStrategy());
    }

    public boolean checkWinner(){
        for(WinningStrategy strategy:winningStrategies){
            if(strategy.checkWin(board, currentPlayer)){
                return true;
            }
        }
        return false;
    }
    public void makeMove(int row,int col){
        state.handleMove(this, currentPlayer, row, col);
    }

    public Player switchPlayer(){
        return (currentPlayer == p1)?p2:p1;
    }

    //Now the subject classes
    public void addObserver(Observer o){
        observers.add(o);
    }
    public void removeObserver(Observer o){
        observers.add(o);
    }
    public void notifyObservers(){
        for(Observer o:observers){
            o.update((Game)this);
        }
    }
}


interface GameState{
    public void handleMove(Game game,Player p, int row,int col);
}

class DrawState implements GameState{
    public void handleMove(Game game,Player p,int row,int col){
        throw new InvalidMoveException("game is already over");
    }
}

class WinnerState implements GameState{
    public void handleMove(Game game,Player p,int row,int col){
        throw new InvalidMoveException("Game is over , the winner is:"+game.winner.name+"has won");
    }
}

class InprogresState implements GameState{
    public void handleMove(Game game,Player p,int row,int col){
        if(game.currentPlayer != p){
            throw new InvalidMoveException("Not your turn");
        }
        if(game.board.getCell(row, col)!=null && game.board.getCell(row, col) == Symbols.EMPTY){
            game.board.board[row][col].setSymbol(game.currentPlayer);
            if(game.checkWinner()){
                game.winner = p;
                game.status= p.symbol==Symbols.O?GameStatus.WINNER_O:GameStatus.WINNER_X;
                game.state = new WinnerState();
                game.notifyObservers();
            }
            else if(game.board.getMoveCount() == (int)Math.pow(game.board.size,2)){
                game.status = GameStatus.DRAW;
                game.state = new DrawState();
            }
            else{
                game.currentPlayer = game.switchPlayer();
                game.board.increaseMoveCount();
            }
        }
    }
}



class InvalidMoveException extends RuntimeException{
    public InvalidMoveException(String message){
        super(message);
    }
}


public class ticTacToeAgainBetter {
    public static void main(String[] args) {
        Player aarav = new Player("Aarav", Symbols.O);
        Player ram = new Player("Ram", Symbols.X);
        Game game = new Game(aarav, ram, 3);
        Scanner sc = new Scanner(System.in);
        ScoreBoard scoreBoard = new ScoreBoard();
        game.addObserver(scoreBoard); 
        while(game.status == GameStatus.IN_PROGRESS){
            int row = sc.nextInt();
            int col = sc.nextInt();
            game.makeMove(row, col);
            game.board.printBoard();
        }
        System.out.println(game.winner.name);
        scoreBoard.printScores();
        sc.close();
    }
}
