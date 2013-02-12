package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.playground.salt.elan2salt;

public class ElanToken {
	
	private String tok;
	private long beginTime;
	private long endTime;
	private int beginChar;
	private int endChar;
	
	public ElanToken(String tok, long beginTime, long endTime, int beginChar, int endChar){
		setTok(tok);
		setBeginTime(beginTime);
		setEndTime(endTime);
		setBeginChar(beginChar);
		setEndChar(endChar);
	}

	public String getTok() {
		return tok;
	}

	public void setTok(String tok) {
		this.tok = tok;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getBeginChar() {
		return beginChar;
	}

	public void setBeginChar(int beginChar) {
		this.beginChar = beginChar;
	}

	public int getEndChar() {
		return endChar;
	}

	public void setEndChar(int endChar) {
		this.endChar = endChar;
	}
	
	public String toString(){
		return tok + ", " + beginTime + ", " + endTime + ", " + beginChar + ", " + endChar;
	}

}
