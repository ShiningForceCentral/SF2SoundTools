/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.sound.convert.io.furnace.file.section;

import com.sfc.sf2.sound.convert.io.furnace.file.FurnaceFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Wiz
 */
public class InstrumentBlock {
    
    private String blockId = "INS2";
    private int blockSize = 0;
    private short formatVersion = 219;
    private short instrumentType = 0;
    private byte[] rawData = null;
    private Feature[] features = null;

    public InstrumentBlock(byte[] data, int instrumentPointer) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.position(instrumentPointer);  
        String blockId = getString(bb, 4);
        blockSize = bb.getInt();      
        formatVersion = bb.getShort();
        instrumentType = bb.getShort();
        rawData = getByteArray(bb, blockSize-2-2);
    }

    private byte[] getByteArray(ByteBuffer bb, int length){
        return FurnaceFile.getByteArray(bb, length);
    }

    private int[] getIntArray(ByteBuffer bb, int length){
        return FurnaceFile.getIntArray(bb, length);
    }

    private float[] getFloatArray(ByteBuffer bb, int length){
        return FurnaceFile.getFloatArray(bb, length);
    }

    private String getString(ByteBuffer bb){
        return FurnaceFile.getString(bb);
    }

    private String getString(ByteBuffer bb, int length){
        return FurnaceFile.getString(bb, length);
    }
    
    private int findStringLength(ByteBuffer bb, int cursor){
        return FurnaceFile.findStringLength(bb, cursor);
    }

    private String[] getStringArray(ByteBuffer bb, int length){
        return FurnaceFile.getStringArray(bb, length);
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public int getSize() {
        return blockSize;
    }

    public void setSize(int size) {
        this.blockSize = size;
    }

    public short getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(short formatVersion) {
        this.formatVersion = formatVersion;
    }

    public short getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(short instrumentType) {
        this.instrumentType = instrumentType;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public Feature[] getFeatures() {
        return features;
    }

    public void setFeatures(Feature[] features) {
        this.features = features;
    }
    
    public byte[] toByteArray(){
        ByteBuffer bb = ByteBuffer.allocate(findLength());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.position(0);
        bb.put(blockId.getBytes(StandardCharsets.UTF_8));
        bb.putInt(blockSize);
        bb.putShort(formatVersion);
        bb.putShort(instrumentType);
        bb.put(rawData);
        return bb.array();
    }
    
    public int findLength(){
        return 4+4+2+2+rawData.length;
    }
    
}
