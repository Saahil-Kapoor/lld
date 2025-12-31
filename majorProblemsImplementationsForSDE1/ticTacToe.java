import java.util.Arrays;
import java.util.Scanner;

public class ticTacToe {
    public static int switchTurn(int x) {
        if (x == 1) {
            return 0;
        }
        return 1;
    }

    public static boolean checkWin(int[][] mat, int currInd, int x) {
        int row = currInd / 3;
        int col = currInd % 3;
        boolean checkRow = true;
        for (int i = 0; i < 3; i++) {
            if (mat[row][i] != x) {
                checkRow = false;
                break;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (mat[i][col] != x) {
                checkRow = false;
                break;
            }
        }

        if (checkRow) {
            return true;
        }
        checkRow = true;
        int dirx = 1;
        int diry = 1;
        row = 0;
        col = 0;
        for (int i = 0; i < 3; i++) {
            if (mat[row][col] != x) {
                checkRow = false;
                break;
            }
            row += dirx;
            col += diry;
        }

        if (checkRow) {
            return true;
        }
        checkRow = true;
        dirx = -1;
        diry = -1;
        for (int i = 0; i < 3; i++) {
            if (mat[row][col] != x) {
                checkRow = false;
                break;
            }
            row += dirx;
            col += diry;
        }
        if (checkRow) {
            return true;
        }
        checkRow = true;
        return false;
    }

    public static void printGrid(int[][] mat) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (mat[i][j] == 0) {
                    System.out.print(" O |");
                } else if (mat[i][j] == 1) {
                    System.out.print(" X |");
                } else {
                    int ind = i * 3 + j;
                    System.out.print(ind + "|");
                }
            }
            System.out.println();
            System.out.println("_____________");
        }
    }

    public static int takeCorrectInput(Scanner sc,int[][] grid) {
        System.out.println("Enter the index where you want to place");
        int ind = sc.nextInt();
        int row = ind/3;
        int col = ind%3;
        while (ind>=9 || ind<0 || grid[row][col] != -1) {
            ind = sc.nextInt();
            System.out.println("Invalid index, pls enter a valid index, either the place is taken or index is out of range");
            row = ind/3;
            col = ind%3;
        }
        

        return ind;
    }

    public static void main(String[] args) {
        System.out.println("the grid/board has the following numbers:");
        int[][] mat = new int[3][3];
        int currTurn = 0;
        Scanner sc = new Scanner(System.in);
        for (int[] row : mat) {
            Arrays.fill(row, -1);
        }
        for (int i = 0; i < 9; i++) {
            if (currTurn == 0) {
                System.out.println("Turn of O");
            } else {
                System.out.println("Turn of X");
            }
            int ind = takeCorrectInput(sc,mat);
            int row = ind/3;
            int col = ind%3;
            mat[row][col] = currTurn;
            printGrid(mat);
            boolean HasWon = checkWin(mat, ind, currTurn);
            if (HasWon) {
                if (currTurn == 0) {
                    System.out.println("Congratulation 0 has won");
                } else {
                    System.out.println("Congratulation X has won");
                }
                break;
            }

            currTurn = switchTurn(currTurn);
        }
        sc.close();
    }
}