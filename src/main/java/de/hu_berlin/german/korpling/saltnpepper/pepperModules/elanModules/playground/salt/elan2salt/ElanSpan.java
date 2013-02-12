package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.playground.salt.elan2salt;

import java.util.ArrayList;
import java.util.Collection;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;

public class ElanSpan {
	
	private ArrayList<ElanToken> ets;
	private long beginTime;
	private long endTime;
	private int beginChar;
	private int endChar;
	private String name;
	private String value;

	public ElanSpan(Collection<ElanToken> elantokens, String name, String value){
		this.ets = (ArrayList) elantokens;
		ArrayList<ElanToken> placeholder = (ArrayList<ElanToken>) elantokens;
		this.setBeginTime(placeholder.get(0).getBeginTime());
		this.setEndTime(placeholder.get(placeholder.size()-1).getEndTime());
		this.setBeginChar(placeholder.get(0).getEndChar());
		this.setEndChar(placeholder.get(placeholder.size()-1).getEndChar());
		this.setName(name);
		this.setValue(value);
	}
	
	public ElanToken getElanToken(int i){
		return ets.get(i);
	}
	
	public ArrayList<ElanToken> getElanTokens(){
		return this.ets;
	}
	
	public int size(){
		return ets.size();
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
