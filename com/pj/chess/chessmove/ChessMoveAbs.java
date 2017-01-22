package com.pj.chess.chessmove;

import static com.pj.chess.ChessConstant.*;

import java.util.ArrayList;
import java.util.List;

import com.pj.chess.BitBoard;
import com.pj.chess.ChessConstant;
import com.pj.chess.Tools;
import com.pj.chess.chessparam.ChessParam;
import com.pj.chess.evaluate.EvaluateCompute;
import com.pj.chess.movelist.MoveNodeList;
import com.pj.chess.zobrist.TranspositionTable;

/**
 * @author Administrator
 *
 */
public abstract class ChessMoveAbs {

 

//	public int[]  board;
//	public int[] allChess;
//	public int[] chessParam.boardBitRow;
//	public int[] chessParam.boardBitCol;
	public int tranHit,killerHit;
	
	protected MoveNodeList generalMoveList;
	
	protected MoveNodeList goodMoveList;
	
	protected MoveNodeList  repeatMoveList;
	
	protected BitBoard oppAttackSite;
//	public int play;
//	boolean isEatKing=false;
	protected ChessParam chessParam ;

	protected TranspositionTable tranTable;
	
	protected int[] board;
	
	protected int[] allChess;
	
	protected EvaluateCompute evaluateCompute;
	
	public ChessMoveAbs(){
	
	}
	public ChessMoveAbs(ChessParam chessParam,TranspositionTable tranTable,EvaluateCompute evaluateCompute){
		this.tranTable=tranTable;
		this.chessParam=chessParam;
		this.board=this.chessParam.board;
		this.allChess=this.chessParam.allChess;
		this.evaluateCompute=evaluateCompute;
	}
	
	/**�����ƶ��ı����̺�λ����
	 * @param moveNode
	 */
	public void moveOperate(MoveNode moveNode){

		int srcSite=moveNode.srcSite;
		int destSite=moveNode.destSite;
		int srcChess = moveNode.srcChess;
		int destChess = moveNode.destChess;
		if(board[srcSite]==-1){
			return ;
		}
		
		/*******����ÿ������Ԥ��*******/
		int srcChessRole = chessRoles[board[srcSite]];
		int srcPlay;
		if((chessPlay[BLACKPLAYSIGN] & srcChess)!=0){ //ԭ����Ϊ�ڷ�		
			srcPlay=BLACKPLAYSIGN;
		}else{ //ԭ����Ϊ�췽
			srcPlay=REDPLAYSIGN;
		}
		//����������λ�÷���Ԥ��
		chessParam.baseScore[srcPlay]-=evaluateCompute.chessAttachScore(srcChessRole, srcSite);
		chessParam.baseScore[srcPlay]+=evaluateCompute.chessAttachScore(srcChessRole, destSite);
		//��ȫ�ֱ�λ����
		chessParam.maskBoardChesses.assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardChesses.assignOr(MaskChesses[destSite]);
		//�޸����ӷ�λ����
		chessParam.maskBoardPersonalChesses[srcPlay].assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardPersonalChesses[srcPlay].assignXor(MaskChesses[destSite]);
		//�޸Ĵ˽�ɫ��λ����
		chessParam.maskBoardPersonalRoleChesses[srcChessRole].assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardPersonalRoleChesses[srcChessRole].assignXor(MaskChesses[destSite]);
		
		//�г���
		if(destChess!=NOTHING){
			int destPlay=1-srcPlay;
			int destChessRole = chessRoles[board[destSite]];
			chessParam.baseScore[destPlay]-=evaluateCompute.chessBaseScore[destChess];
			chessParam.baseScore[destPlay]-=evaluateCompute.chessAttachScore(destChessRole, destSite);
			//������������
			chessParam.reduceChessesNum(destChessRole);
			//�޸ı��Է���λ����
			chessParam.maskBoardPersonalChesses[destPlay].assignXor(MaskChesses[destSite]);
			//�޸ı��Է��Ľ�ɫλ����
			chessParam.maskBoardPersonalRoleChesses[destChessRole].assignXor(MaskChesses[destSite]);
		}
		setBoard(srcSite,NOTHING);
		setBoard(destSite,srcChess);
		setChess(srcChess,destSite);
		setChess(destChess,NOTHING);	
			
		
		int srcRow = boardRow[srcSite];
		int srcCol = boardCol[srcSite];
		int destRow = boardRow[destSite];
		int destCol = boardCol[destSite];
//		System.out.println("ԭλ��:��"+srcRow+"��"+srcCol+"��");
//		System.out.println("Ŀ��λ��:��"+destRow+"��"+destCol+"��");
//		System.out.println("�޸�֮ǰrow��ϵ\t"+Integer.toBinaryString(boardBitRow[destRow]));
		chessParam.boardBitRow[destRow]|=(1<<(8-destCol));
//		System.out.println("�޸�֮��row��ϵ\t"+Integer.toBinaryString(chessParam.boardBitRow[destRow]));
		chessParam.boardBitCol[destCol]|=(1<<(9-destRow));
		
//		System.out.println("�޸�֮ǰrow��ϵ\t"+Integer.toBinaryString(chessParam.boardBitRow[srcRow]));
		chessParam.boardBitRow[srcRow]^=(1<<(8-srcCol));
//		System.out.println("�޸�֮��row��ϵ\t"+Integer.toBinaryString(chessParam.boardBitRow[srcRow]));
		chessParam.boardBitCol[srcCol]^=(1<<(9-srcRow));
		
//		System.out.println("�ƶ�����ǰZobrist��"+TranspositionTable.boardZobrist);
		tranTable.moveOperate(moveNode);
//		System.out.println("�ƶ����Ӻ�Zobrist��"+TranspositionTable.boardZobrist);
		
	}
	/**���������ƶ��ı����̺�λ����
	 * @param moveNode 
	 */
	public void unMoveOperate(MoveNode moveNode){
		int srcSite=moveNode.destSite;
		int srcChess =moveNode.destChess;
		int destSite=moveNode.srcSite;
		int destChess = moveNode.srcChess;
		//��ϵ��ԭ 
		setBoard(srcSite,srcChess);
		setBoard(destSite,destChess);
		setChess(srcChess,srcSite);
		setChess(destChess,destSite);		
		
		int destPlay; 
		int destChessRole = chessRoles[board[destSite]];
		if((chessPlay[BLACKPLAYSIGN] & destChess)!=0){ //ԭ����Ϊ�ڷ�			
			destPlay=BLACKPLAYSIGN;
		}else{ //ԭ����Ϊ�췽			
			destPlay=REDPLAYSIGN; 
		}
		//���ӷ���Ԥ��
		chessParam.baseScore[destPlay]-=evaluateCompute.chessAttachScore(destChessRole, srcSite);
		chessParam.baseScore[destPlay]+=evaluateCompute.chessAttachScore(destChessRole, destSite);
		//��ȫ�ֱ�λ����(��Ŀ��λ�û�ԭ)
		chessParam.maskBoardChesses.assignXor(MaskChesses[destSite]);
		//�޸����ӷ�λ����
		chessParam.maskBoardPersonalChesses[destPlay].assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardPersonalChesses[destPlay].assignXor(MaskChesses[destSite]);
		//�޸����ӷ���ɫλ����
		chessParam.maskBoardPersonalRoleChesses[destChessRole].assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardPersonalRoleChesses[destChessRole].assignXor(MaskChesses[destSite]);
		
		//��һ������������
		if(srcChess!=NOTHING){
			int srcChessRole = chessRoles[board[srcSite]];
			int srcPlay=1-destPlay;
			chessParam.baseScore[srcPlay] += evaluateCompute.chessBaseScore[srcChess];
			chessParam.baseScore[srcPlay] += evaluateCompute.chessAttachScore(srcChessRole, srcSite);
			//��ԭ��������
			chessParam.increaseChessesNum(srcChessRole);
			//��������һ����������λ�����ϻ�ԭ
			chessParam.maskBoardPersonalChesses[srcPlay].assignXor(MaskChesses[srcSite]);
			//��������һ���������ڽ�ɫλ�����ϻ�ԭ
			chessParam.maskBoardPersonalRoleChesses[srcChessRole].assignXor(MaskChesses[srcSite]);
		}else{
			//û�������޸�ȫ��λ����  ��ΪҪ��ԭ����Ҫ������������
			chessParam.maskBoardChesses.assignXor(MaskChesses[srcSite]);
		}
		 
		
		
		int srcRow = boardRow[srcSite];
		int srcCol = boardCol[srcSite];
		int destRow = boardRow[destSite];
		int destCol = boardCol[destSite];
		
		chessParam.boardBitRow[destRow]|=(1<<(8-destCol));
		chessParam.boardBitCol[destCol]|=(1<<(9-destRow)); 
		if(srcChess==NOTHING){
			chessParam.boardBitRow[srcRow]^=(1<<(8-srcCol)); 
			chessParam.boardBitCol[srcCol]^=(1<<(9-srcRow));
		}else{
			chessParam.boardBitRow[srcRow]|=(1<<(8-srcCol)); 
			chessParam.boardBitCol[srcCol]|=(1<<(9-srcRow));
		}
//		System.out.println("��ԭ����ǰZobrist��"+TranspositionTable.boardZobrist);
		tranTable.unMoveOperate(moveNode);
//		System.out.println("��ԭ���Ӻ�Zobrist��"+TranspositionTable.boardZobrist);
		
		
	}
	private void setBoard(int site,int chess){
		if(site!=NOTHING){
			board[site]=chess;
		}
	}
	private void setChess(int chess,int site){
		if(chess!=NOTHING){
			allChess[chess]=site;
		}
	}

	
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 2:34:27 PM
	 * ���ܣ��ŷ��������ж�
	 *@param play
	 *@param moveNode
	 *@return
	*/
	public boolean legalMove(int play,MoveNode moveNode){
		if(moveNode==null)
			return false;
		int srcChess = chessParam.board[moveNode.srcSite];
		int destChess = chessParam.board[moveNode.destSite];
		//ԭ���Ӳ�Ϊ�ѷ����ӻ�Ŀ������Ϊ�ѷ�����
		if((chessPlay[play] & srcChess)==0){
			return false;
		}
		if(destChess!=NOTHING && (chessPlay[play] & destChess)!=0){
			return false;
		}
		if(srcChess!=moveNode.srcChess || destChess!=moveNode.destChess){
			return false;
		}
		int srcSite=moveNode.srcSite;
		int destSite=moveNode.destSite;
		BitBoard bitBoard=null;
		switch (chessRoles[srcChess]) {
		case REDCHARIOT:
		case BLACKCHARIOT:
			int row=chessParam.boardBitRow[boardRow[srcSite]];
			int col=chessParam.boardBitCol[boardCol[srcSite]];
			if(destChess!=NOTHING){ //�ǳ����߷�
				//ȡ�������ܹ�������λ��
				bitBoard=BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[srcSite][row], ChariotBitBoardOfAttackCol[srcSite][col]);
			}else{
				bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			}
			break;
		case REDKNIGHT:
		case BLACKKNIGHT:
			//ȡ���������ȵ�λ��
			BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[srcSite],chessParam.maskBoardChesses);
			bitBoard=new BitBoard(KnightBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfKnight()]);
			break;
		case REDGUN:
		case BLACKGUN:
			row=chessParam.boardBitRow[boardRow[srcSite]];
			col=chessParam.boardBitCol[boardCol[srcSite]];
			//ȡ�������ܹ�������λ��
			if(destChess!=NOTHING){ //�ǳ����߷�
				bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[srcSite][row], GunBitBoardOfAttackCol[srcSite][col]);
			}else{
				bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			}
			break;
		case REDELEPHANT:
		case BLACKELEPHANT:
			//ȡ���������۵�λ��
			legBoard=BitBoard.assignAndToNew(ElephanLegBitBoards[srcSite],chessParam.maskBoardChesses);
			bitBoard=new BitBoard(ElephanBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfElephant()]);
			break;
		case REDKING:
		case BLACKKING:
			//�������ŷ�
			bitBoard=new BitBoard(KingBitBoard[srcSite]);
			break;
		case REDGUARD:
		case BLACKGUARD:
			bitBoard=new BitBoard(GuardBitBoard[srcSite]);
			break;
		case REDSOLDIER:
		case BLACKSOLDIER:
			bitBoard=new BitBoard(SoldiersBitBoard[play][srcSite]);
			break;
		default :
			System.out.println("û���������:"+srcSite);
		}
		bitBoard.assignAnd(MaskChesses[destSite]);
		if(!bitBoard.isEmpty()){
			return true;
		}else{
			return false;
		}
		
	}

	private static int[][] knights=new int[][]{{35,36},{19,20}};
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 5:36:05 PM
	 * ���ܣ� �����ж�
	 *@param play
	 *@return
	*/
	public boolean checked(int play){
		int opponentPlay=1-play;
		//�Է���������
		if(chessParam.allChess[chessPlay[opponentPlay]]==NOTHING){
			return false;
		} 
		int kingSite = chessParam.allChess[chessPlay[play]];
		
		int row=chessParam.boardBitRow[boardRow[kingSite]];
		int col=chessParam.boardBitCol[boardCol[kingSite]];
		
		//������
		BitBoard bitBoard = BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[kingSite][row], ChariotBitBoardOfAttackCol[kingSite][col]);
		bitBoard.assignAnd(chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.CHARIOT));
		if(!bitBoard.isEmpty()){
			return true;
		}
		//������
		if(!BitBoard.assignAndToNew(ChariotBitBoardOfAttackCol[kingSite][col],MaskChesses[chessParam.allChess[chessPlay[1-play]]]).isEmpty()){
			return true;
		}
		
		//�ڽ���
		bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[kingSite][row], GunBitBoardOfAttackCol[kingSite][col]);
		bitBoard.assignAnd(chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.GUN));
		if(!bitBoard.isEmpty()){
			return true;
		}
		//������
		//ȡ���������Ǳ������ߵ���λ��
		bitBoard =new BitBoard(KnightBitBoards[kingSite]);
		int opponentKnight1 = knights[play][0],opponentKnight2 = knights[play][1];;
		int knight1Site=chessParam.allChess[opponentKnight1],knight2Site=chessParam.allChess[opponentKnight2];
		//�����Ǳ��ȿ��Ƿ��ܽ���
		if(!BitBoard.assignAndToNew(chessParam.getBitBoardByPlayRole(opponentPlay,ChessConstant.KNIGHT),bitBoard).isEmpty()){
			//��1
			if(knight1Site!=NOTHING && !BitBoard.assignAndToNew(MaskChesses[knight1Site],bitBoard).isEmpty()){
				BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[knight1Site],chessParam.maskBoardChesses);
				//����1�ܹ�������λ���Ƿ��н�
				if(!BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[knight1Site][legBoard.checkSumOfKnight()],MaskChesses[kingSite]).isEmpty()){
					return true;
				}
			}
			//��2
			if(knight2Site!=NOTHING && !BitBoard.assignAndToNew(MaskChesses[knight2Site],bitBoard).isEmpty()){
				BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[knight2Site],chessParam.maskBoardChesses);
				//����1�ܹ�������λ���Ƿ��н�
				if(!BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[knight2Site][legBoard.checkSumOfKnight()],MaskChesses[kingSite]).isEmpty()){
					return true;
				}
			}
		}
		//���������ж�
		if(!BitBoard.assignAndToNew(KingCheckedSoldierBitBoards[kingSite],chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.SOLDIER)).isEmpty()){
			return true;
		}
		return false;
		
	}
	/**
	 *@author pengjiu 
	 *@date:Sep 20, 2011 2:13:54 PM
	 * ���ܣ� ���������ӽ���
	 *@param play
	 *@return
	*/
	public int chkNum(int play){
		int opponentPlay=1-play;
		int chkNum=0;
		//�Է���������
		if(chessParam.allChess[chessPlay[opponentPlay]]==NOTHING){
			return chkNum;
		} 
		int kingSite = chessParam.allChess[chessPlay[play]];
		
		int row=chessParam.boardBitRow[boardRow[kingSite]];
		int col=chessParam.boardBitCol[boardCol[kingSite]];
		
		//������
		BitBoard bitBoard = BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[kingSite][row], ChariotBitBoardOfAttackCol[kingSite][col]);
		bitBoard.assignAnd(chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.CHARIOT));
		if(!bitBoard.isEmpty()){ 
			chkNum++;
		}
		
		//�ڽ���
		bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[kingSite][row], GunBitBoardOfAttackCol[kingSite][col]);
		bitBoard.assignAnd(chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.GUN));
		if(!bitBoard.isEmpty()){ 
			chkNum++;
		}
		//������
		//ȡ���������Ǳ������ߵ���λ��
		bitBoard =new BitBoard(KnightBitBoards[kingSite]);
		int opponentKnight1 = knights[play][0],opponentKnight2 = knights[play][1];;
		int knight1Site=chessParam.allChess[opponentKnight1],knight2Site=chessParam.allChess[opponentKnight2];
		//�����Ǳ��ȿ��Ƿ��ܽ���
		if(!BitBoard.assignAndToNew(chessParam.getBitBoardByPlayRole(opponentPlay,ChessConstant.KNIGHT),bitBoard).isEmpty()){
			//��1
			if(knight1Site!=NOTHING && !BitBoard.assignAndToNew(MaskChesses[knight1Site],bitBoard).isEmpty()){
				BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[knight1Site],chessParam.maskBoardChesses);
				//����1�ܹ�������λ���Ƿ��н�
				if(!BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[knight1Site][legBoard.checkSumOfKnight()],MaskChesses[kingSite]).isEmpty()){
					chkNum++;
				}
			}
			//��2
			if(knight2Site!=NOTHING && !BitBoard.assignAndToNew(MaskChesses[knight2Site],bitBoard).isEmpty()){
				BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[knight2Site],chessParam.maskBoardChesses);
				//����1�ܹ�������λ���Ƿ��н�
				if(!BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[knight2Site][legBoard.checkSumOfKnight()],MaskChesses[kingSite]).isEmpty()){
					chkNum++;
				}
			}
		}
		//���������ж�
		if(!BitBoard.assignAndToNew(KingCheckedSoldierBitBoards[kingSite],chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.SOLDIER)).isEmpty()){
			chkNum++;
		}
		return chkNum;
	}
	public void setMoveNodeList(MoveNodeList generalMoveList,MoveNodeList goodMoveList,MoveNodeList repeatMoveList,BitBoard oppAttackSite){
		this.generalMoveList=generalMoveList;
		this.goodMoveList=goodMoveList;
		this.repeatMoveList=repeatMoveList;
		this.oppAttackSite=oppAttackSite;
	}
	public MoveNodeList getGeneralMoveList(){
		return generalMoveList;
	}
	public MoveNodeList getGoodMoveList(){
		return goodMoveList;
	}
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 11:01:45 AM
	 * ���ܣ��������г����ŷ� note��(�������ӵļ�ֵ����һ��ֵʱ������Ϊ�����ŷ���)
	 *@param play
	 *@param dumpMoveList
	*/
	public void genEatMoveList(int play){
		int begin=chessPlay[play];
		int end=begin+16;
		for(int i=begin+1;i<end;i++){
			int chessSite = allChess[i];
			if(chessSite!=NOTHING){
				 this.chessEatMove(chessRoles[i], chessSite, play);
			}
		}
		this.chessEatMove(chessRoles[begin], allChess[begin], play);
	 }
	
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 11:01:19 AM
	 * ���ܣ����ɲ������ŷ��б�
	 *@param play ���
	 *@param dumpMoveList  
	*/
	public void genNopMoveList(int play){ 
		int begin=chessPlay[play];
		int end=begin+16;
		for(int i=begin+1;i<end;i++){
			int chessSite = allChess[i];
			if(chessSite!=NOTHING){
				 this.chessNopMove(chessRoles[i], chessSite, play);
			}
		}
		this.chessNopMove(chessRoles[begin], allChess[begin], play);
	 }
	//note:[play][��ʿ��]
	private static int[][] checkMateFristUseMove= new int[][]{
			{23,24,25,26,16},
			{39,40,41,42,32} };
	//note:[play][��������] 
	private static int[][] checkMateSecondlyUseMove= new int[][]{
			{17,18,19,20,21,22,27,28,29,30,31},
			{33,34,35,36,37,38,43,44,45,46,47} };
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 11:00:38 AM
	 * ���ܣ�	���Ƚ⽫�ŷ�,�ձ齫�����ᱻ���� note:��ʿ�� 
	 *@param play
	 *@param dumpMoveList
	*/
	/*public void genFristMoveListCheckMate(int play ){ 
		int[] move=checkMateFristUseMove[play];
		for(int i:move){
			int chessSite = allChess[i];
			if(chessSite!=NOTHING){
				 this.chessAllMove(chessRoles[i], chessSite, play);
			}
		} 
	 }*/

	/**���������ܹ�����λ��
	 * @param play
	 */
	public BitBoard getOppAttackSite(int play ) {
		BitBoard oppAttack=new BitBoard();
		int begin=chessPlay[1-play];
		int end=begin+16;
		for(int i=begin;i<end;i++){
			int chessSite = allChess[i];
			if(chessSite!=NOTHING){
				oppAttack.assignOr(this.chessAttackSite(chessRoles[i], chessSite, 1-play));
			}
		}
		return oppAttack;
	}
	public BitBoard chessAttackSite(int chessRole, int srcSite, int play) {
		BitBoard bitBoard=null;
		switch (chessRole) {
		case REDCHARIOT:
		case BLACKCHARIOT:
			int row=chessParam.boardBitRow[boardRow[srcSite]];
			int col=chessParam.boardBitCol[boardCol[srcSite]];
			//ȡ�������ܹ�������λ��
			bitBoard=BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[srcSite][row], ChariotBitBoardOfAttackCol[srcSite][col]);			
			bitBoard.assignXor(BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]));
			break;
		case REDKNIGHT:
		case BLACKKNIGHT:
			//ȡ���������ȵ�λ��
			BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[srcSite],chessParam.maskBoardChesses);
			bitBoard=new BitBoard(KnightBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfKnight()]);
			break;
		case REDGUN:
		case BLACKGUN:
			row=chessParam.boardBitRow[boardRow[srcSite]];
			col=chessParam.boardBitCol[boardCol[srcSite]];
			//ȡ�������ܹ�������λ��
			bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[srcSite][row], GunBitBoardOfAttackCol[srcSite][col]);
			//���ߵ���λ��
//			bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			//��α����λ��
			bitBoard.assignXor(BitBoard.assignXorToNew(GunBitBoardOfFakeAttackRow[srcSite][row], GunBitBoardOfFakeAttackCol[srcSite][col]));
			break;
		case REDELEPHANT:
		case BLACKELEPHANT:
			//ȡ���������۵�λ��
			legBoard=BitBoard.assignAndToNew(ElephanLegBitBoards[srcSite],chessParam.maskBoardChesses);
			bitBoard=new BitBoard(ElephanBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfElephant()]);
			break;
		case REDKING:
		case BLACKKING:
			//�������ŷ�
			bitBoard=new BitBoard(KingBitBoard[srcSite]);
			break;
		case REDGUARD:
		case BLACKGUARD:
			bitBoard=new BitBoard(GuardBitBoard[srcSite]);
			break;
		case REDSOLDIER:
		case BLACKSOLDIER:
			bitBoard=new BitBoard(SoldiersBitBoard[play][srcSite]);
			break;
		default :
			System.out.println("û���������:"+srcSite);
		}
		return bitBoard;
	}
	public void chessEatMove(int chessRole, int srcSite, int play) {
		BitBoard bitBoard=null;
		switch (chessRole) {
		case REDCHARIOT:
		case BLACKCHARIOT:
			int row=chessParam.boardBitRow[boardRow[srcSite]];
			int col=chessParam.boardBitCol[boardCol[srcSite]];
			
			//ȡ�������ܹ�������λ��
			bitBoard=BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[srcSite][row], ChariotBitBoardOfAttackCol[srcSite][col]);
			//ȡ���������ĶԷ�����
			bitBoard.assignAnd(chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDKNIGHT:
		case BLACKKNIGHT:
			//ȡ���������ȵ�λ��
			BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[srcSite],chessParam.maskBoardChesses);
			//ȡ���ܹ������Է�����λ��
			bitBoard=BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfKnight()],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDGUN:
		case BLACKGUN:
			row=chessParam.boardBitRow[boardRow[srcSite]];
			col=chessParam.boardBitCol[boardCol[srcSite]];
			//ȡ�������ܹ�������λ��
			bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[srcSite][row], GunBitBoardOfAttackCol[srcSite][col]);
			//ȡ���������ĶԷ�����
			bitBoard.assignAnd(chessParam.maskBoardPersonalChesses[1-play]);
			
			
			
			break;
		case REDELEPHANT:
		case BLACKELEPHANT:
			//ȡ���������۵�λ��
			legBoard=BitBoard.assignAndToNew(ElephanLegBitBoards[srcSite],chessParam.maskBoardChesses);
			//ȡ���ܹ������Է�����λ��
			bitBoard=BitBoard.assignAndToNew(ElephanBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfElephant()],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDKING:
		case BLACKKING:
			//�������ŷ�
			bitBoard=BitBoard.assignAndToNew(KingBitBoard[srcSite],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDGUARD:
		case BLACKGUARD:
			bitBoard=BitBoard.assignAndToNew(GuardBitBoard[srcSite],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDSOLDIER:
		case BLACKSOLDIER:
			bitBoard=BitBoard.assignAndToNew(SoldiersBitBoard[play][srcSite],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		default :
			System.out.println("û���������:"+srcSite);
		}
		int destSite;
		while((destSite = bitBoard.MSB(play))!=-1){
			savePlayChess(srcSite, destSite, play);
			//ȥ���ձ��������
			bitBoard.assignXor(MaskChesses[destSite]);
		}
	}
	public void chessNopMove(int chessRole, int srcSite, int play) {
		BitBoard bitBoard=null;
		switch (chessRole) {
		case REDCHARIOT:
		case BLACKCHARIOT:
			int row=chessParam.boardBitRow[boardRow[srcSite]];
			int col=chessParam.boardBitCol[boardCol[srcSite]];
			//ȡ���������ߵ���λ��
			bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			break;
		case REDKNIGHT:
		case BLACKKNIGHT:
			//ȡ���������ȵ�λ��
			BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[srcSite],chessParam.maskBoardChesses);
			//�����ŵ���λ��
			BitBoard attackBoard=KnightBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfKnight()];
			//�ߵ���λ�������ӵ�
			bitBoard=BitBoard.assignAndToNew(attackBoard,chessParam.maskBoardChesses);
			//�ó��ߵ���λ��û�����ӵ�
			bitBoard.assignXor(attackBoard);
			break;
		case REDGUN:
		case BLACKGUN:
			row=chessParam.boardBitRow[boardRow[srcSite]];
			col=chessParam.boardBitCol[boardCol[srcSite]];
			bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			break;
		case REDELEPHANT:
		case BLACKELEPHANT:
			//ȡ���������۵�λ��
			legBoard=BitBoard.assignAndToNew(ElephanLegBitBoards[srcSite],chessParam.maskBoardChesses);
			//�����ŵ���λ��
			attackBoard=ElephanBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfElephant()];
			//�ߵ���λ�������ӵ�
			bitBoard=BitBoard.assignAndToNew(attackBoard,chessParam.maskBoardChesses);
			//�ó��ߵ���λ��û�����ӵ�
			bitBoard.assignXor(attackBoard);
			break;
		case REDKING:
		case BLACKKING:
			//���ŵ���λ�������ӵ�
			bitBoard=BitBoard.assignAndToNew(KingBitBoard[srcSite],chessParam.maskBoardChesses);
			//���ŵ���λ��û�����ӵ�
			bitBoard.assignXor(KingBitBoard[srcSite]);
			break;
		case REDGUARD:
		case BLACKGUARD:
			//�ߵ�λ��������
			bitBoard=BitBoard.assignAndToNew(GuardBitBoard[srcSite],chessParam.maskBoardChesses);
			//ȡ��û�����ӵ�
			bitBoard.assignXor(GuardBitBoard[srcSite]);
			break;
		case REDSOLDIER:
		case BLACKSOLDIER:
			//������
			bitBoard=BitBoard.assignAndToNew(SoldiersBitBoard[play][srcSite],chessParam.maskBoardChesses);
			//ȡ��������
			bitBoard.assignXor(SoldiersBitBoard[play][srcSite]);
			break;
		default :
			System.out.println("û���������:"+srcSite);
		}
		int destSite;
		while((destSite = bitBoard.MSB(play))!=-1){
			savePlayChess(srcSite, destSite, play);
			//ȥ���ձ��������
			bitBoard.assignXor(MaskChesses[destSite]);
		}
	}
	
	/**
	 *@author pengjiu 
	 *@date:Aug 10, 2011 12:40:22 PM
	 * ���ܣ���⹥���������Ƿ�Ϊ��������
	 *@param srcChess
	 *@param destChess
	 *@param play
	*/
	public boolean isOpponentCheck(int destChess,int play){
		//�����ӷ���
		if(destChess==NOTHING){
			return true;
		//Ϊ��������	
		}
		return ((chessPlay[play] & destChess)==0);
	}
	public abstract void savePlayChess(int srcSite,int destSite,int play);
}