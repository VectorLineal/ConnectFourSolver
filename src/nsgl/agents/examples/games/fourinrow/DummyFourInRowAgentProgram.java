    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */
    package nsgl.agents.examples.games.fourinrow;

    import java.util.Arrays;
    import nsgl.agents.Action;
    import nsgl.agents.AgentProgram;
    import nsgl.agents.Percept;
    import java.util.HashMap;
    import java.util.Map;

    public class DummyFourInRowAgentProgram implements AgentProgram {
        protected String color;
        public DummyFourInRowAgentProgram( String color ){
            this.color = color;
        }
        static Map<Integer, byte[]> transTable = new HashMap<>();//Transposition table

        private byte[][] generateBoardMatrix(int n, Percept p){
            byte[][] boardMatrix = new byte[n][n];
            for (int i = 0; i < n; i++) { //this equals to the row in our matrix.
                for (int j = 0; j < n; j++) { //this equals to the column in each row.
                    Object temp = p.getAttribute(i+":"+j);
                    byte k = 0;
                    if (temp.equals("white")){k=1;}else if (temp.equals("black")){k=-1;}
                    boardMatrix[i][j] = k;
                }
            };
            return boardMatrix;
        }
        public static class State{
            public int moveCount;
            public byte[][] board;
            public byte color;
            public byte win;
            public State(byte[][] board, byte color){
                this.board = board;
                this.color = color;
                this.win = this.check();
                this.moveCount = this.countMoves(this.board);
            }

            private int countMoves(byte[][] values){
                int k = 0;
                for (byte i = 0; i < values.length; i++) {
                    for (byte j = 0; j < values.length; j++) {
                        if(values[i][j] != 0) k++;
                    }
                }
                return k;
            }

            private boolean isFull(){
                return this.moveCount == this.board.length * this.board.length;
            }

            private boolean isWinningMove(byte column){
                int row = nextRow(board, column);
                if(row < 0) return false;
                board[row][column] = color;
                boolean winning = this.check() == color;
                board[row][column] = 0;
                return winning;
            }

            public byte check() {
                byte HEIGHT = (byte)this.board.length;
                byte WIDTH = (byte)this.board[0].length;
                byte EMPTY_SLOT = 0;
                for (int r = 0; r < HEIGHT; r++) { // iterate rows, bottom to top
                    for (int c = 0; c < WIDTH; c++) { // iterate columns, left to right
                        byte player = this.board[r][c];
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
        }

        static byte nextRow(byte[][] board, byte column){
            for (byte i = 0; i < board.length; i++) {
                if(board[i][column] != 0){
                    return (byte)(i - 1);
                }
            }
            return (byte)(board.length - 1);
        }

        static byte[][] cloneArray(byte[][] src) {
            int length = src.length;
            byte[][] target = new byte[length][src[0].length];
            for (byte i = 0; i < length; i++) {
                System.arraycopy(src[i], 0, target[i], 0, src[i].length);
            }
            return target;
        }

        private byte play(byte color, byte column, byte[][] board){
            byte row = nextRow(board, column);
            if(row < 0) return -1;
            board[row][column] = color;
            return row;
        }

        byte[] negamax(byte[][] oldBoard, byte curColor, int alpha, int beta/*, byte depth*/) {
            byte size = (byte)oldBoard.length;
            byte[] results = {(byte)((oldBoard.length) * Math.random()), 0};
            State board = new State(oldBoard, curColor);

            if(board.isFull()) // check for draw game
                return results;

            for(byte x = 0; x < size; x++) // check if current player can win next move
                if(color.equals(curColor) && board.isWinningMove(x)){
                    results[0] = x;
                    results[1] = (byte)((size*size - board.moveCount - 1)/2);
                    transTable.put(Arrays.deepHashCode(oldBoard), results);
                    return results;
                }

            int max = (size * size - 1 - board.moveCount)/2;	// upper bound of our score as we cannot win immediately
            if(transTable.containsKey(Arrays.deepHashCode(oldBoard))){
                results = transTable.get(Arrays.deepHashCode(oldBoard));
                max = results[1] + (-(size * size) / 2)+ 2; //(-(size * size) / 2)+ 2 ->minimo posible
            }
            if(beta > max) {
                beta = max;                     // there is no need to keep beta above our max possible score.
                if(alpha >= beta){
                    results[1] = (byte)beta;
                    transTable.put(Arrays.deepHashCode(oldBoard), results);
                    return results;
                }
            }

            /*if(depth == 0){
                for(byte x = 0; x < size; x++){
                    results[0] = x;
                    results[1] = (byte)((size*size - board.moveCount)/2);
                    transTable.put(Arrays.deepHashCode(oldBoard), results);
                }
                return results;
            }*/

            curColor = (byte)(-curColor);

            for(byte x = 0; x < size; x++) {
                byte row = play(curColor, x, oldBoard);// It's opponent turn in P2 position after current player plays x column.
                if(row > -1){
                    byte[] score = negamax(oldBoard, curColor, -beta, -alpha/*, (byte)(depth - 1)*/);
                    //deshacer jugada
                    oldBoard[row][x] = 0;
                    score[1] = (byte)(-score[1]);// If current player plays col x, his score will be the opposite of opponent's score after playing col x
                    if(score[1] >= beta) return score;
                    if(score[1] > alpha){
                        alpha = score[1];
                        results[0] = x;
                        results[1] = score[1]; // keep track of best possible score so far.
                    }
                }
            }
            transTable.put(Arrays.deepHashCode(oldBoard), results);
            return results;
        }

        @Override
        public Action compute(Percept p) {
            long time = (long)(200 * Math.random());
            try{
                Thread.sleep(time);
            }catch(Exception e){}
            if( p.getAttribute(FourInRow.TURN).equals(color) ){
                int n = Integer.parseInt((String)p.getAttribute(FourInRow.SIZE));
                byte[][] matrix = generateBoardMatrix(n, p);
                byte colorCode = 0;
                if(this.color.equals(FourInRow.WHITE))colorCode = -1;
                else if(this.color.equals(FourInRow.BLACK)) colorCode = 1;
                byte[] movement = negamax(matrix, colorCode, -(n * n) / 2, (n * n) / 2/*, (byte)255*/);
                int i = nextRow(matrix, (byte)movement[0]);
                int j = movement[0];
            /*boolean flag = (i==n-1) || !p.getAttribute((i+1)+":"+j).equals((String)FourInRow.SPACE);
            while( !flag ){
                i = (int)(n*Math.random());
                j = (int)(n*Math.random());
                flag = (i==n-1) || !p.getAttribute((i+1)+":"+j).equals((String)FourInRow.SPACE);
            }*/
                return new Action( i+":"+j+":"+color );
            }
            System.out.println("i skip turn");
            return new Action(FourInRow.PASS);
        }

        @Override
        public void init() {
        }

    }