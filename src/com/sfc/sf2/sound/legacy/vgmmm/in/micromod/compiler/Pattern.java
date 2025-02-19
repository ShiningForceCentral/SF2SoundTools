package com.sfc.sf2.sound.legacy.vgmmm.in.micromod.compiler;

public class Pattern implements Element {
	private Module parent;
	private Row child = new Row( this );
	private int patternIdx;

	public Pattern( Module parent ) {
		this.parent = parent;
	}
	
	public String getToken() {
		return "Pattern";
	}
	
	public Element getParent() {
		return parent;
	}
	
	public Element getSibling() {
		return null;
	}
	
	public Element getChild() {
		return child;
	}
	
	public void begin( String value ) {
		patternIdx = Parser.parseInteger( value );
		child.setRowIdx( 0 );
	}
	
	public void end() {
	}

	public String description() {
		return "\"Index\" (Pattern index, from 0 to 127.)";
	}

	public void setNote( int rowIdx, int channelIdx, com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Note note ) {
		parent.getPattern( patternIdx ).setNote( rowIdx, channelIdx, note );
	}

	public int getPatternIdx() {
		return patternIdx;
	}
}
