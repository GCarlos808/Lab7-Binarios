package lab7binarios;

import lab7binarios.Track;

import javax.sound.sampled.*;
import java.io.*;
import java.util.function.Consumer;

public class AudioPlayer {
    
    public enum State { STOPPED, PLAYING, PAUSED }
    
    private Clip clip;
    private State state = State.STOPPED;
    private long pausePosition = 0;
    private Track currentSong;
    
    private Consumer<Double> progressCallback;
    private Runnable onEndCallback;
    
    private Thread progressThread;
    
    public void setProgressCallback(Consumer<Double> cb){
        this.progressCallback = cb;
    }
    public void setOnEndCallback(Runnable cb){
        this.onEndCallback = cb;
    }
    
    public synchronized void play(Track track) {
        if (track == null) return;
        
        if (state == State.PAUSED && currentSong != null
                && currentSong.getId() == track.getId()) {
            resume();
            return;
        }
        
        stop();
        
        currentSong = track;
        
        try {
            File audioFile = new File(track.getFilePath());
            if (!audioFile.exists()) {
                System.err.println("[Player] Archivo no encontrado: " + track.getFilePath());
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat baseFormat = ais.getFormat();
            
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);

            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(targetFormat, ais);
            clip = AudioSystem.getClip();
            clip.open(pcmStream);
            clip.setMicrosecondPosition(pausePosition);
            clip.start();
            state = State.PLAYING;
            pausePosition = 0;
            
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP
                        && state == State.PLAYING
                        && clip.getMicrosecondPosition() >= clip.getMicrosecondLength() - 50_000) {
                    state = State.STOPPED;
                    if (onEndCallback != null) onEndCallback.run();
                }
            });
            
            startProgressThread();
            
        } catch (Exception e) {
            System.err.println("[Player] Error de Playback: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public synchronized void pause() {
        if (state != State.PLAYING || clip == null) return;
        pausePosition = clip.getMicrosecondPosition();
        clip.stop();
        state = State.PAUSED;
        stopProgressThread();
    }
    
    public synchronized void resume() {
        if (state != State.PAUSED || clip == null) return;
        clip.setMicrosecondPosition(pausePosition);
        clip.start();
        state = State.PLAYING;
        startProgressThread();
    }
    
    public synchronized void stop() {
        stopProgressThread();
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        pausePosition = 0;
        state = State.STOPPED;
    }
    
    public synchronized void seek(double fraction) {
        if (clip == null) return;
        long pos = (long)(clip.getMicrosecondLength() * fraction);
        clip.setMicrosecondPosition(pos);
        if (state == State.PAUSED) pausePosition = pos;
    }
    
    public State  getState()       { return state; }
    public Track   getCurrentSong() { return currentSong; }
    
    public double getPositionSeconds() {
        if (clip == null) return 0;
        return clip.getMicrosecondPosition() / 1_000_000.0;
    }
    
    private void startProgressThread() {
        stopProgressThread();
        progressThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (progressCallback != null && clip != null) {
                    progressCallback.accept(getPositionSeconds());
                }
                try { Thread.sleep(500); } catch (InterruptedException e) { break; }
            }
        });
        progressThread.setDaemon(true);
        progressThread.start();
    }
    
    private void stopProgressThread() {
        if (progressThread != null) {
            progressThread.interrupt();
            progressThread = null;
        }
    }
}