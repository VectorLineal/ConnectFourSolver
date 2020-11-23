/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nsgl.agents.examples.games.fourinrow;

import nsgl.agents.Action;
import nsgl.agents.AgentProgram;
import nsgl.agents.Percept;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jonatan
 */
public class DummyFourInRowAgentProgram implements AgentProgram {
    protected String color;
    protected int[] searchOrder;
    public DummyFourInRowAgentProgram( String color ){
        this.color = color;
    }

    public static final class Minimax{
        static int MAX = 1000;
        static int MIN = -1000;

        private Minimax() {}

        public static State minimaxDecision(State state) {
            return state.getActions().stream().max(Comparator.comparing(Minimax::minValue)).get();
        }

        private static double maxValue(State state, int alpha, int beta) {
            if(state.isTerminal()){
                return state.getUtility();
            }

            int best = MIN;
            for (State action : state.getActions()) {
                double val = minValue(action, alpha, beta);
                best = (int) Math.max(best, val);
                alpha = Math.max(alpha, best);

                // Alpha Beta Pruning
                if (beta <= alpha)
                    break;
            }
            return best;
//        return state.getActions().stream()
//                .map(Minimax::minValue)
//                .max(Comparator.comparing(Double::valueOf)).get();
        }

        private static double minValue(State state) {
            return minValue(state, MIN, MAX);
        }

        private static double minValue(State state, int alpha, int beta) {
            if(state.isTerminal()){
                return state.getUtility();
            }

            int best = MAX;
            for (State action : state.getActions()) {
                double val = maxValue(action, alpha, beta);
                best = (int) Math.min(best, val);
                beta = Math.min(beta, best);

                // Alpha Beta Pruning
                if (beta <= alpha)
                    break;
            }
            return best;
//        return state.getActions().stream()
//                .map(Minimax::maxValue)
//                .min(Comparator.comparing(Double::valueOf)).get();
        }
        public static class State {

            int w;
            int[] move;
            int[][] board;
            int color;
            int stone;

            public State(int[] move, int color, int[][] board, int stone){
                this.move = move;
                this.board = board;
                this.color = color;
                this.w = this.checkWin();
                this.stone = stone;
            }

            Collection<State> getActions(){
                //System.out.println(this.stone);
                List<State> actions = new LinkedList<>();
                int n = this.board.length;
                for (int j = 0; j < n; j++){
                    for (int i = n-1; i >= 0; i--){
                        if (this.board[i][j]==0){
                            int [][] temp = new int[this.board.length][];
                            for(int k = 0; k < this.board.length; k++)
                                temp[k] = this.board[k].clone();
                            if (this.stone%2==0){
                                temp[i][j] = this.color;
                            }else{
                                temp[i][j] = -this.color;
                            }
                            actions.add(new State(new int[]{i, j},this.color,temp, this.stone+1));
                            i=n;
                            j++;
                            if (j==n){
                                break;
                            }
                        }
                    }
                }
                return actions;
            }

            public int checkWin() {
                int HEIGHT = this.board.length;
                int WIDTH = this.board[0].length;
                int EMPTY_SLOT = 0;
                for (int r = 0; r < HEIGHT; r++) { // iterate rows, bottom to top
                    for (int c = 0; c < WIDTH; c++) { // iterate columns, left to right
                        int player = this.board[r][c];
                        if (player == EMPTY_SLOT)
                            continue; // don't check empty slots

                        if (c + 3 < WIDTH &&
                                player == this.board[r][c+1] && // look right
                                player == this.board[r][c+2] &&
                                player == this.board[r][c+3])
                            return player;
                        if (r + 3 < HEIGHT) {
                            if (player == this.board[r+1][c] && // look up
                                    player == this.board[r+2][c] &&
                                    player == this.board[r+3][c])
                                return player;
                            if (c + 3 < WIDTH &&
                                    player == this.board[r+1][c+1] && // look up & right
                                    player == this.board[r+2][c+2] &&
                                    player == this.board[r+3][c+3])
                                return player;
                            if (c - 3 >= 0 &&
                                    player == this.board[r+1][c-1] && // look up & left
                                    player == this.board[r+2][c-2] &&
                                    player == this.board[r+3][c-3])
                                return player;
                        }
                    }
                }
                return EMPTY_SLOT; // no winner found
            }

            public boolean full(){
                boolean flag = true;
                for( int i=0; i<this.board.length&&flag; i++){
                    for( int j=0; j<this.board[0].length&&flag; j++){
                        flag &= this.board[i][j] != 0;
                    }
                }
                return flag;
            }


            boolean isTerminal() {
                return this.w != 0 || full();
            }

            double getUtility() {
                int n = (this.board.length*this.board.length/2);
                if( this.w > 0 ){
                    if (this.color==1){
                        return n-this.stone;
                    }else{
                        return -(n-this.stone);
                    }
                }else{
                    if( this.w < 0 ){
                        if (this.color==-1){
                            return n-this.stone;
                        }else{
                            return -(n-this.stone);
                        }
                    }else{
                        return 0;
                    }
                }
            }
        }
    }

    private int[][] generateBoardMatrix(int n, Percept p){
        int[][] boardMatrix = new int[n][n];
        for (int i = 0; i < n; i++) { //this equals to the row in our matrix.
            for (int j = 0; j < n; j++) { //this equals to the column in each row.
                Object temp = p.getAttribute(i+":"+j);
                int k = 0;
                if (temp.equals("white")){k=1;}else if (temp.equals("black")){k=-1;}
                boardMatrix[i][j] = k;
            }
        }
        return boardMatrix;
    }

    @Override
    public Action compute(Percept p) {        
        long time = (long)(10 * Math.random());
        try{
           Thread.sleep(time);
        }catch(Exception e){}
        if( p.getAttribute(FourInRow.TURN).equals(color) ){
            int n = Integer.parseInt((String)p.getAttribute(FourInRow.SIZE));
            int[][] matrix = generateBoardMatrix(n, p);
            int colorCode = 0;
            if(this.color.equals(FourInRow.WHITE)){
                colorCode = 1;
            }else if(this.color.equals(FourInRow.BLACK)){
                colorCode = -1;
            }
            Minimax.State original = new Minimax.State(new int[]{0, 0}, colorCode, matrix, 0);
            Minimax.State decision = Minimax.minimaxDecision(original);
            int[] movement = decision.move;
            int i = movement[0];
            int j = movement[1];
            /*boolean flag = (i==n-1) || !p.getAttribute((i+1)+":"+j).equals((String)FourInRow.SPACE);
            while( !flag ){
                i = (int)(n*Math.random());
                j = (int)(n*Math.random());
                flag = (i==n-1) || !p.getAttribute((i+1)+":"+j).equals((String)FourInRow.SPACE);
            }*/
            System.out.println(i+":"+j+":"+color );
            return new Action( i+":"+j+":"+color );
        }
        //System.out.println("I skip turn");
        return new Action(FourInRow.PASS);
    }

    @Override
    public void init() {
    }
    
}