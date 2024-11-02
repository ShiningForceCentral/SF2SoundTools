/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.sound.convert.io.furnace;

/**
 *
 * @author Wiz
 */
public class FurnaceVolume {
    
    private byte value;
    
    public FurnaceVolume(int value){
        this.value=(byte)(0xFF&value);
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }
    
    public String produceClipboardOutput(){
        String clipboard="";
        clipboard+=String.format("%02x", value).toUpperCase();
        return clipboard;
    }
    
}
