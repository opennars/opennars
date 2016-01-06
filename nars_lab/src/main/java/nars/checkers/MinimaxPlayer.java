//package checkers;
//
///* Computer.java : The computer player
// * Copyright (C) 1998-2002  Paulo Pinto
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation; either
// * version 2 of the License, or (at your option) any later version.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this library; if not, write to the
// * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
// * Boston, MA 02111-1307, USA.
// */
//
//public class MinimaxPlayer extends Player
//{
//
//	/**
//	 * 
//	 * @param name
//	 */
//	public MinimaxPlayer(String name)
//	{
//		super(name);
//	}
//	
//	@Override
//	public void takeTurn()
//	{
//		super.takeTurn();
//		
//		// yet to be done quite important however
//	}
//        
//
//
//
//  /**
//   * Cor usada pelo computador
//   */
//  private int color;
//
//
//  /**
//   * Profundidade maxima para o minimax
//   */
//  private static final int maxDepth = 2;
//
//  /**
//   * Peso das casas do tabuleiro
//   */
//
//  private static final int tableWeight[] = { 4, 4, 4, 4,
//                                             4, 3, 3, 3,
//                                             3, 2, 2, 4,
//                                             4, 2, 1, 3,
//                                             3, 1, 2, 4,
//                                             4, 2, 2, 3,
//                                             3, 3, 3, 4,
//                                             4, 4, 4, 4};
//  
//  
//  
//
//  /**
//   * Efectua uma jogada.
//   */
//  public void play () {
//    try {
//      List moves = minimax (currentBoard);
//      
//      if (!moves.isEmpty ())
//        currentBoard.move (moves);
//    }
//    catch (BadMoveException bad) {
//      bad.printStackTrace ();
//      System.exit (-1);
//    }
//  }
//
//  /**
//   * Muda o tabuleiro associado
//   */
//  public void setBoard (CheckersBoard board) {
//    currentBoard = board;
//  }
//
//  /**
//   * Indica se a jogada nao e' nula
//   */
//  private boolean mayPlay (List moves) {
//    return !moves.isEmpty () && !((List) moves.peek_head ()).isEmpty ();
//  }
//  
//
//  /**
//   * Implementa o algoritmo minimax
//   */
//  private List minimax (CheckersBoard board) throws BadMoveException {
//    List sucessors;
//    List move, bestMove = null;
//    CheckersBoard nextBoard;
//    int value, maxValue = Integer.MIN_VALUE;
//
//    sucessors = board.legalMoves ();
//    while (mayPlay (sucessors)) {
//      move =  (List) sucessors.pop_front ();
//      nextBoard = (CheckersBoard) board.clone ();
//
//      Debug.println ("******************************************************************");
//      nextBoard.move (move);
//      value = minMove (nextBoard, 1, maxValue, Integer.MAX_VALUE);
//
//      if (value > maxValue) {
//        Debug.println ("Max value : " + value + " at depth : 0");
//        maxValue = value;
//        bestMove = move;
//      }
//    }
//
//    Debug.println ("Move value selected : " + maxValue + " at depth : 0");
//
//    return bestMove;
//  }
//  
//  /**
//   * Implementa a avaliacao da jogada do ponto de vista do jogador MAX
//   */
//  private int maxMove (CheckersBoard board, int depth, int alpha, int beta) 
//   throws BadMoveException {
//    if (cutOffTest (board, depth))
//      return eval (board);
//
//
//    List sucessors;
//    List move;
//    CheckersBoard nextBoard;
//    int value;
//
//    Debug.println ("Max node at depth : " + depth + " with alpha : " + alpha + 
//                        " beta : " + beta);
//
//    sucessors = board.legalMoves ();
//    while (mayPlay (sucessors)) {
//      move = (List) sucessors.pop_front ();
//      nextBoard = (CheckersBoard) board.clone ();
//      nextBoard.move (move);
//      value = minMove (nextBoard, depth + 1, alpha, beta);
//
//      if (value > alpha) {
//        alpha = value;
//        Debug.println ("Max value : " + value + " at depth : " + depth);
//      }
//
//      if (alpha > beta) {
//        Debug.println ("Max value with prunning : " + beta + " at depth : " + depth);
//        Debug.println (sucessors.length () + " sucessors left");
//        return beta;
//      }
//  
//    }
//
//    Debug.println ("Max value selected : " + alpha + " at depth : " + depth);
//    return alpha;
//  }
//
//  /**
//   * Implementa a avaliacao da jogada do ponto de vista do jogador MIN
//   */
//  private int minMove (CheckersBoard board, int depth, int alpha, int beta)
//   throws BadMoveException {
//    if (cutOffTest (board, depth))
//      return eval (board);
//
//
//    List sucessors;
//    List move;
//    CheckersBoard nextBoard;
//    int value;
//
//    Debug.println ("Min node at depth : " + depth + " with alpha : " + alpha + 
//                        " beta : " + beta);
//    
//    sucessors = (List) board.legalMoves ();
//    while (mayPlay (sucessors)) {
//      move = (List) sucessors.pop_front ();
//      nextBoard = (CheckersBoard) board.clone ();
//      nextBoard.move (move);
//      value = maxMove (nextBoard, depth + 1, alpha, beta);
//
//      if (value < beta) {
//        beta = value;
//        Debug.println ("Min value : " + value + " at depth : " + depth);
//      }
//
//      if (beta < alpha) {
//        Debug.println ("Min value with prunning : " + alpha + " at depth : " + depth);
//        Debug.println (sucessors.length () + " sucessors left");
//        return alpha;
//      }
//    }
//
//    Debug.println ("Min value selected : " + beta + " at depth : " + depth);
//    return beta;
//  }
//
//   /**
//    * Devolve a forca do jogador corrente.
//    */
//   private int eval (CheckersBoard board) {
//      int colorKing;
//      int enemy, enemyKing;
//      
//      if (color == CheckersBoard.WHITE) {
//        colorKing = CheckersBoard.WHITE_KING;
//        enemy = CheckersBoard.BLACK;
//        enemyKing = CheckersBoard.BLACK_KING;
//      }
//      else {
//        colorKing = CheckersBoard.BLACK_KING;
//        enemy = CheckersBoard.WHITE;
//        enemyKing = CheckersBoard.WHITE_KING;
//      }
//      
//      int colorForce = 0;
//      int enemyForce = 0;
//      int piece;
//
//      try {
//        for (int i = 0; i < 32;  i++) {
//          piece = board.getPiece (i);
//        
//	  if (piece != CheckersBoard.EMPTY)
//	     if (piece == color || piece == colorKing)
//	       colorForce += calculateValue (piece, i);
//	     else
//	       enemyForce += calculateValue (piece, i);
//        }
//      }
//      catch (BadCoordException bad) {
//        bad.printStackTrace ();
//        System.exit (-1);
//      }
//
//      return colorForce - enemyForce;
//   }
//
//   /**
//    * Calcula a forca de uma peca
//    */
//   private int calculateValue (int piece, int pos) {
//      int value;
//      
//      if (piece == CheckersBoard.WHITE ) //Peca simples
//	if (pos >= 4 && pos <= 7)
//          value = 7;
//        else
//          value = 5;
//      else if (piece != CheckersBoard.BLACK) //Peca simples
//	if (pos >= 24 && pos <= 27)
//          value = 7;
//        else
//          value = 5;
//      else // dama
//	value = 10;
//
//      return value * tableWeight[pos];
//   }
//
//
//  /**
//   * Indica se se pode cortar a arvore
//   */
//  private boolean cutOffTest (CheckersBoard board, int depth) {
//    return depth > maxDepth || board.hasEnded ();
//  }
//  
//}
//
//
//
//
//
//
//
//
//        
//        
// }
