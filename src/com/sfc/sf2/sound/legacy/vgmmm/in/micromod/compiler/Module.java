
package com.sfc.sf2.sound.legacy.vgmmm.in.micromod.compiler;

public class Module implements Element {
	private com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Module module;
	private com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Macro[] macros = new com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Macro[ 100 ];
	private Channels child = new Channels( this );
	private java.io.File resourceDir;

	public Module( java.io.File resourceDir ) {
		this.resourceDir = resourceDir;
	}

	public String getToken() {
		return "Module";
	}
	
	public Element getParent() {
		return null;
	}
	
	public Element getSibling() {
		return null;
	}
	
	public Element getChild() {
		return child;
	}
	
	public void begin( String value ) {
		module = new com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Module();
		module.setSongName( value );
	}
	
	public void end() {
		/* Expand macros.*/
		int speed = 6;
		int numChannels = module.getNumChannels();
		com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Note note = new com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Note();
		com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Pattern[] patterns = new com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Pattern[ module.getSequenceLength() ];
		for( int sequenceIdx = 0; sequenceIdx < patterns.length; sequenceIdx++ ) {
			com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Pattern pattern = module.getPattern( module.getSequenceEntry( sequenceIdx ) );
			patterns[ sequenceIdx ] = new com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Pattern( numChannels, pattern );
		}
		for( int patternIdx = 0; patternIdx < patterns.length; patternIdx++ ) {
			com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Pattern pattern = patterns[ patternIdx ];
			for( int rowIdx = 0; rowIdx < com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Pattern.NUM_ROWS; rowIdx++ ) {
				for( int channelIdx = 0; channelIdx < numChannels; channelIdx++ ) {
					pattern.getNote( rowIdx, channelIdx, note );
					if( note.effect == 0xF && note.parameter < 0x20 ) {
						speed = note.parameter;
					}
				}
				for( int channelIdx = 0; channelIdx < numChannels; channelIdx++ ) {
					pattern.getNote( rowIdx, channelIdx, note );
					com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Macro macro = macros[ note.instrument ];
					if( macro != null ) {
						note.instrument = 0;
						pattern.setNote( rowIdx, channelIdx, note );
						macro.expand( module, patterns, patternIdx, rowIdx, channelIdx, speed );
						pattern.getNote( rowIdx, channelIdx, note );
					}
					module.getPattern( module.getSequenceEntry( patternIdx ) ).setNote( rowIdx, channelIdx, note );
				}
			}
		}
		System.out.println( getToken() + " end." );
	}

	public String description() {
		return "\"Title\" (Song title, maximum 20 characters.)";
	}

	public void setNumChannels( int numChannels ) {
		module.setNumChannels( numChannels );
	}

	public void setSequenceLength( int length ) {
		module.setSequenceLength( length );
	}

	public void setSequenceEntry( int seqIdx, int patIdx ) {
		module.setSequenceEntry( seqIdx, patIdx );
	}
	
	public com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Instrument getInstrument( int insIdx ) {
		return module.getInstrument( insIdx );
	}
	
	public void setMacro( int macroIdx, com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Macro macro ) {
		if( macroIdx > 0 && macroIdx < 100 ) macros[ macroIdx ] = macro;
	}
	
	public com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Pattern getPattern( int patIdx ) {
		return module.getPattern( patIdx );
	}
	
	public com.sfc.sf2.sound.legacy.vgmmm.in.micromod.Module getModule() {
		return module;
	}
	
	public java.io.InputStream getInputStream( String path ) throws java.io.IOException {
		return new java.io.FileInputStream( new java.io.File( resourceDir, path ) );
	}
}
