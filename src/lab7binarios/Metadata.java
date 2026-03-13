package lab7binarios;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

public class Metadata {
    
    public static Track extract(File file, int songId) {
        String title  = stripExtension(file.getName());
        String artist = "Artista Desconocido";
        String genre  = "Desconocido";
        double duration = getDuration(file);
        byte[] artData  = extractAlbumArt(file);
        String mime     = artData != null ? detectMime(artData) : null;

        return new Track (songId, title, artist, genre, duration, file.getAbsolutePath(), artData, mime);
    }
    
    public static double getDuration(File file) {
        
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
            AudioFormat fmt = ais.getFormat();
            long frames = ais.getFrameLength();
            if (frames > 0 && fmt.getFrameRate() > 0) {
                return frames / fmt.getFrameRate();
            }
            
        } catch (Exception ignored) {}
        return 0;
    }
    
    public static byte[] extractAlbumArt(File file) {
        
        try {
            
            byte[] raw = Files.readAllBytes(file.toPath());
            
            if (raw.length < 10) return null;
            
            if (raw[0] != 'I' || raw[1] != 'D' || raw[2] != '3') return null;
            
            int tagSize = ((raw[6] & 0x7F) << 21) | ((raw[7] & 0x7F) << 14) | ((raw[8] & 0x7F) << 7) | (raw[9] & 0x7F);
            
            tagSize += 10;
            
            int pos = 10;
            
            if ((raw[5] & 0x40) != 0) {
                int extSize = ((raw[10] & 0xFF) << 24) | ((raw[11] & 0xFF) << 16)
                        | ((raw[12] & 0xFF) << 8) | (raw[13] & 0xFF);
                pos += extSize;
            }
            
            while (pos + 10 < tagSize) {
                String frameId = new String(raw, pos, 4);
                int frameSize = ((raw[pos+4] & 0xFF) << 24) | ((raw[pos+5] & 0xFF) << 16)  | ((raw[pos+6] & 0xFF) << 8) | (raw[pos+7] & 0xFF);
                
                pos += 10;
                if (frameSize <= 0 || pos + frameSize > raw.length) break;
                
                if (frameId.equals("APIC")) {
                    
                    int encoding = raw[pos] & 0xFF;
                    int mimeEnd = pos + 1;
                    while (mimeEnd < pos + frameSize && raw[mimeEnd] != 0) mimeEnd++;
                    int dataStart = mimeEnd + 1 + 1;
                    
                    int nullSize = (encoding == 1 || encoding == 2) ? 2 : 1;
                    while (dataStart + nullSize <= pos + frameSize) {
                        if (nullSize == 1 && raw[dataStart] == 0) { dataStart++; break; }
                        if (nullSize == 2 && raw[dataStart] == 0 && raw[dataStart+1] == 0) { dataStart += 2; break; }
                        dataStart += nullSize;
                    }
                    
                    int imgLen = frameSize - (dataStart - pos);
                    if (imgLen > 0 && dataStart + imgLen <= raw.length) {
                        byte[] art = new byte[imgLen];
                        System.arraycopy(raw, dataStart, art, 0, imgLen);
                        return art;
                    }
                }
                pos += frameSize;
            }
        } catch (Exception ignored) {}
        return null;
    }
    
    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
    
    private static String detectMime(byte[] data) {
        if (data.length >= 3 && data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) return "imagen/jpeg";
        if (data.length >= 8 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G')  return "imagen/png";
        return "imagen/jpeg";
    }
}
