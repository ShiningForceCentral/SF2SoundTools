/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.sound.convert.io.furnace;

import com.sfc.sf2.sound.convert.io.cube.CubeChannel;
import com.sfc.sf2.sound.convert.io.cube.CubeCommand;
import com.sfc.sf2.sound.convert.io.cube.MusicEntry;
import com.sfc.sf2.sound.convert.io.cube.command.Inst;
import com.sfc.sf2.sound.convert.io.cube.command.MainLoopStart;
import com.sfc.sf2.sound.convert.io.cube.command.Note;
import com.sfc.sf2.sound.convert.io.cube.command.NoteL;
import com.sfc.sf2.sound.convert.io.cube.command.PsgInst;
import com.sfc.sf2.sound.convert.io.cube.command.PsgNote;
import com.sfc.sf2.sound.convert.io.cube.command.PsgNoteL;
import com.sfc.sf2.sound.convert.io.cube.command.SetRelease;
import com.sfc.sf2.sound.convert.io.cube.command.Shifting;
import com.sfc.sf2.sound.convert.io.cube.command.Stereo;
import com.sfc.sf2.sound.convert.io.cube.command.Sustain;
import com.sfc.sf2.sound.convert.io.cube.command.Vibrato;
import com.sfc.sf2.sound.convert.io.cube.command.Vol;
import com.sfc.sf2.sound.convert.io.cube.command.Wait;
import com.sfc.sf2.sound.convert.io.cube.command.WaitL;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Wiz
 */
public class FurnacePattern {
    
    private static final int TYPE_FM = 0;
    private static final int TYPE_DAC = 1;
    private static final int TYPE_PSGTONE = 2;
    private static final int TYPE_PSGNOISE = 3;
    
    private static final int MAX_CHANNELS_SIZE=10;
    
    private FurnaceChannel[] channels = new FurnaceChannel[MAX_CHANNELS_SIZE];

    public FurnaceChannel[] getChannels() {
        return channels;
    }
    
    public FurnacePattern(MusicEntry me, boolean introOnly, boolean mainLoopOnly){
        for(int i=0;i<channels.length;i++){
            channels[i] = new FurnaceChannel();
            List<FurnaceRow> rowList = null;
            if(i<5){
                rowList = convertCubeChannel(me.getChannels()[i], TYPE_FM, introOnly, mainLoopOnly);
            }else if(i==5){
                if(!me.isYm6InDacMode()){
                    rowList = convertCubeChannel(me.getChannels()[i], TYPE_FM, introOnly, mainLoopOnly);
                }else{
                    rowList = convertCubeChannel(me.getChannels()[i], TYPE_DAC, introOnly, mainLoopOnly);
                }
            }else if(i<9){
                rowList = convertCubeChannel(me.getChannels()[i], TYPE_PSGTONE, introOnly, mainLoopOnly);
            }else{
                rowList = convertCubeChannel(me.getChannels()[i], TYPE_PSGNOISE, introOnly, mainLoopOnly);
            }
            FurnaceRow[] rows = new FurnaceRow[rowList.size()];
            for(int j=0;j<rows.length;j++){
                rows[j]=rowList.get(j);
            }
            channels[i].setRows(rows);
        }
        
        /*
        if(mainLoopOnly){
            repeatMainLoopToMaxLength();
        }
        
        fillChannelsToMaxLength();
        */
    }
    
    public List<FurnaceRow> convertCubeChannel(CubeChannel cch, int channelType, boolean introOnly, boolean mainLoopOnly){
        List<FurnaceRow> rowList = new ArrayList();
        CubeCommand[] ccs = cch.getCcs();
        int playLength = 0;
        int playCounter = 0;
        int release = 0;
        int releaseCounter = 0;
        int vibrato = -1;
        int vibratoCounter = 0;
        int currentInstrument = 0;
        int currentVolume = 0;
        int detune = -1;
        int panning = -1;
        boolean released = false;
        boolean vibratoTriggered = false;
        boolean mainLoopStarted = false;
        FurnaceRow currentRow = new FurnaceRow();
        for(int i=0;i<ccs.length;i++){
            CubeCommand cc = ccs[i];
            if(cc instanceof MainLoopStart){
                if(introOnly){
                    break;
                }else{
                    mainLoopStarted = true;
                }
            } else if(cc instanceof Stereo){
                Stereo s = (Stereo) cc;
                switch(0xFF&s.getValue()){
                    case 0x80:
                        panning = 0x00;
                        break;
                    case 0x40:
                        panning = 0xFF;
                        break;
                    default:
                        panning = 0x80;
                        break;
                }
            } else if(cc instanceof Shifting){
                Shifting s = (Shifting) cc;
                detune = ((s.getValue()&0x30)>>4)+3;
            } else if(cc instanceof Vibrato){
                Vibrato v = (Vibrato) cc;
                vibrato = (v.getValue()&0xF)*2;
            } else if(cc instanceof SetRelease){
                SetRelease sr = (SetRelease) cc;
                release = sr.getValue();
            } else if(cc instanceof Sustain){
                release = 0;
            } else if(cc instanceof Vol){
                Vol v = (Vol) cc;
                currentVolume = v.getValue();
            } else if(cc instanceof Inst){
                Inst inst = (Inst) cc;
                currentInstrument = inst.getValue();
            }else if(cc instanceof PsgInst){
                PsgInst inst = (PsgInst) cc;
                currentInstrument = (0xF0&inst.getValue())>>4;
                currentVolume = (0x0F&inst.getValue())/2;
            }else if((cc instanceof Note || cc instanceof NoteL)
                    && (  (!introOnly && !mainLoopOnly)
                       || (introOnly && !mainLoopStarted)
                       || (mainLoopOnly && mainLoopStarted)
                       )
                    ){
                if(cc instanceof NoteL){
                    NoteL n = (NoteL) cc;
                    playLength = 0xFF & n.getLength();
                    currentRow.setNote(new FurnaceNote(FurnacePitch.valueFromCubeValue(n.getNote().getValue()-12).getValue()));
                }else{
                    Note n = (Note) cc;
                    currentRow.setNote(new FurnaceNote(FurnacePitch.valueFromCubeValue(n.getNote().getValue()-12).getValue()));
                }
                currentRow.setInstrument(new FurnaceInstrument(currentInstrument));
                currentRow.setVolume(new FurnaceVolume(currentVolume*8));
                currentRow.getEffectList().add(new FurnaceEffect(0x04,0x00));
                if(detune>=0){
                    currentRow.getEffectList().add(new FurnaceEffect(0x53,0x00+detune));
                    detune=-1;
                }
                if(panning>=0){
                    currentRow.getEffectList().add(new FurnaceEffect(0x80,panning));
                    panning=-1;
                }
                playCounter = 0;
                releaseCounter = 0;
                vibratoCounter = 0;
                rowList.add(currentRow);
                currentRow = new FurnaceRow();
                playCounter++;
                releaseCounter++;
                vibratoCounter++;
                while(playCounter<playLength){
                    if(!vibratoTriggered && vibrato!=-1){
                        if(vibratoCounter>=(vibrato)){
                            currentRow.getEffectList().add(new FurnaceEffect(0x04,0x22));
                            vibratoTriggered = true;
                            vibratoCounter = 0;
                        } else{
                            vibratoCounter++;
                        }
                    }
                    if(releaseCounter>=(playLength-release)){
                        currentRow.setNote(new FurnaceNote(0xFF));
                        rowList.add(currentRow);
                        currentRow = new FurnaceRow();
                        releaseCounter=0;
                        playCounter++;
                        released = true;
                    }else{
                        rowList.add(currentRow);
                        currentRow = new FurnaceRow();
                        playCounter++;
                        if(!released){
                            releaseCounter++;
                        }
                    }
                }
                playCounter=0;
                released = false;
                vibratoTriggered = false;
            }else if((cc instanceof PsgNote || cc instanceof PsgNoteL)
                    && (  (!introOnly && !mainLoopOnly)
                       || (introOnly && !mainLoopStarted)
                       || (mainLoopOnly && mainLoopStarted)
                       )
                    ){
                if(cc instanceof PsgNoteL){
                    PsgNoteL n = (PsgNoteL) cc;
                    playLength = 0xFF & n.getLength();
                    currentRow.setNote(new FurnaceNote(FurnacePitch.valueFromCubeValue(n.getNote().getValue()-12).getValue()));
                }else{
                    PsgNote n = (PsgNote) cc;
                    currentRow.setNote(new FurnaceNote(FurnacePitch.valueFromCubeValue(n.getNote().getValue()-12).getValue()));
                }
                currentRow.setInstrument(new FurnaceInstrument(currentInstrument));
                currentRow.setVolume(new FurnaceVolume(currentVolume*8));
                currentRow.getEffectList().add(new FurnaceEffect(0x04,0x00));
                if(detune>=0){
                    currentRow.getEffectList().add(new FurnaceEffect(0x53,0x00+detune));
                    detune=-1;
                }
                playCounter = 0;
                releaseCounter = 0;
                vibratoCounter = 0;
                rowList.add(currentRow);
                currentRow = new FurnaceRow();
                playCounter++;
                releaseCounter++;
                vibratoCounter++;
                while(playCounter<playLength){
                    if(!vibratoTriggered && vibrato!=-1){
                        if(vibratoCounter>=(vibrato)){
                            currentRow.getEffectList().add(new FurnaceEffect(0x04,0x22));
                            vibratoTriggered = true;
                            vibratoCounter = 0;
                        } else{
                            vibratoCounter++;
                        }
                    }
                    if(releaseCounter>=(playLength-release)){
                        currentRow.setNote(new FurnaceNote(0xFE));
                        rowList.add(currentRow);
                        currentRow = new FurnaceRow();
                        releaseCounter=0;
                        playCounter++;
                        released = true;
                    }else{
                        rowList.add(currentRow);
                        currentRow = new FurnaceRow();
                        playCounter++;
                        if(!released){
                            releaseCounter++;
                        }
                    }
                }
                playCounter=0;
                released = false;
                vibratoTriggered = false;
            }else if((cc instanceof Wait || cc instanceof WaitL)
                    && (  (!introOnly && !mainLoopOnly)
                       || (introOnly && !mainLoopStarted)
                       || (mainLoopOnly && mainLoopStarted)
                       )
                    ){
                if(cc instanceof WaitL){
                    WaitL n = (WaitL) cc;
                    playLength = 0xFF & n.getValue();
                }
                playCounter = 0;
                rowList.add(currentRow);
                currentRow = new FurnaceRow();
                playCounter++;
                while(playCounter<playLength){
                    rowList.add(currentRow);
                    currentRow = new FurnaceRow();
                    playCounter++;
                }
                playCounter=0;
            }else {
                System.out.println("com.sfc.sf2.sound.convert.io.furnace.FurnacePattern.convertFmCubeChannel() - Ignoring command "+i+" : "+cc.produceAsmOutput());
            }
        }
        if(introOnly){
            currentRow.getEffectList().add(new FurnaceEffect(0x0D,0x00));
        }
        return rowList;
    }
    
    public void fillChannelsToMaxLength(){
        int maxLength=0;
        for (int i=0;i<channels.length;i++){
            if(maxLength<channels[i].getRows().length){
                maxLength = channels[i].getRows().length;
            }
        }
        for (int i=0;i<channels.length;i++){
            FurnaceRow[] rows = channels[i].getRows();
            if(rows.length<maxLength){
                FurnaceRow[] newRows = new FurnaceRow[maxLength];
                System.arraycopy(rows, 0, newRows, 0, rows.length);
                for(int j=0;(rows.length+j)<maxLength;j++){
                    FurnaceRow voidRow = new FurnaceRow();
                    newRows[rows.length+j] = voidRow;
                }
                channels[i].setRows(newRows);
            }
        }
    }
    
    public void repeatMainLoopToMaxLength(){
        int maxLength=0;
        for (int i=0;i<channels.length;i++){
            if(maxLength<channels[i].getRows().length){
                maxLength = channels[i].getRows().length;
            }
        }
        for (int i=0;i<channels.length;i++){
            if(channels[i].getRows()!=null && channels[i].getRows().length>0){
                channels[i].setRows(repeat(channels[i].getRows(),maxLength));
            }
        }
    }
    
    public static <T> T[] repeat(T[] arr, int newLength) {
        T[] dup = Arrays.copyOf(arr, newLength);
        for (int last = arr.length; last != 0 && last < newLength; last <<= 1) {
            System.arraycopy(dup, 0, dup, last, Math.min(last << 1, newLength) - last);
        }
        return dup;
    }
    
    public FurnacePattern(FurnacePattern intro, FurnacePattern mainLoop){
        FurnaceChannel[] introChannels = intro.getChannels();
        FurnaceChannel[] mainLoopChannels = mainLoop.getChannels();
        int maxLength=0;
        for (int i=0;i<MAX_CHANNELS_SIZE;i++){
            FurnaceRow[] introRows = introChannels[i].getRows();
            FurnaceRow[] mainLoopRows = mainLoopChannels[i].getRows();
            if(maxLength<(introRows.length+mainLoopRows.length)){
                maxLength = introRows.length+mainLoopRows.length;
            }
        }
        System.out.println("maxLength : "+maxLength);
        for (int i=0;i<MAX_CHANNELS_SIZE;i++){
            FurnaceRow[] introRows = introChannels[i].getRows();
            FurnaceRow[] mainLoopRows = mainLoopChannels[i].getRows();
            int targetMainLoopLength = maxLength - introRows.length;
            if(mainLoopRows!=null && mainLoopRows.length>0){
               mainLoopRows = repeat(mainLoopRows,targetMainLoopLength);
            }            
            mainLoop.getChannels()[i].setRows(mainLoopRows);
        }
        for (int i=0;i<MAX_CHANNELS_SIZE;i++){
            FurnaceRow[] introRows = introChannels[i].getRows();
            FurnaceRow[] mainLoopRows = mainLoopChannels[i].getRows();
            FurnaceRow[] rows = concatenate(introRows, mainLoopRows);
            FurnaceChannel fc = new FurnaceChannel();
            fc.setRows(rows);
            channels[i] = fc;
        }
        fillChannelsToMaxLength();
    }
    
    public <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
