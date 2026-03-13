package lab7binarios.Almacenamiento;

import lab7binarios.Track;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistQueue {

    private static final String DATA_DIR  = "data";
    private static final String DATA_FILE = DATA_DIR + File.separator + "PLAYLIST.txt";
    private static final String IDX_FILE  = DATA_DIR + File.separator + "PLAYLIST.idx";

    private static class IndexEntry {
        int  id;
        long offset;
        int  length;

        IndexEntry(int id, long offset, int length) {
            this.id     = id;
            this.offset = offset;
            this.length = length;
        }
    }

    private IndexEntry[] entries;
    private int size;
    private static final int colaInicial = 10;

    public PlaylistQueue() {
        entries = new IndexEntry[colaInicial];
        size    = 0;
        new File(DATA_DIR).mkdirs();
        loadIndex();
    }

    public synchronized void addSong(Track track) throws IOException {
        byte[] bytes = serialize(track);
        long offset;

        try (RandomAccessFile raf = new RandomAccessFile(DATA_FILE, "rw")) {
            offset = raf.length();
            raf.seek(offset);
            raf.write(bytes);
        }

        addEntry(new IndexEntry(track.getId(), offset, bytes.length));
        saveIndex();
    }

    public synchronized void removeSong(int id) throws IOException {
        int pos = findById(id);
        if (pos == -1) return;

        for (int i = pos; i < size - 1; i++) {
            entries[i] = entries[i + 1];
        }
        entries[size - 1] = null;
        size--;

        saveIndex();
        compact();
    }

    public synchronized List<Track> loadAll() throws IOException {
        List<Track> songs = new ArrayList<>();
        if (!new File(DATA_FILE).exists()) return songs;

        try (RandomAccessFile raf = new RandomAccessFile(DATA_FILE, "r")) {
            for (int i = 0; i < size; i++) {
                IndexEntry entry = entries[i];
                byte[] bytes = new byte[entry.length];
                raf.seek(entry.offset);
                raf.readFully(bytes);
                songs.add(deserialize(bytes));
            }
        }
        return songs;
    }
    
    public synchronized Track loadSong(int id) throws IOException {
        int pos = findById(id);
        if (pos == -1) return null;

        IndexEntry entry = entries[pos];
        byte[] bytes = new byte[entry.length];

        try (RandomAccessFile raf = new RandomAccessFile(DATA_FILE, "r")) {
            raf.seek(entry.offset);
            raf.readFully(bytes);
        }
        return deserialize(bytes);
    }
    
    public synchronized int nextId() {
        int maxId = 0;
        for (int i = 0; i < size; i++) {
            if (entries[i].id > maxId) {
                maxId = entries[i].id;
            }
        }
        return maxId + 1;
    }
    
    private void addEntry(IndexEntry entry) {
        if (size == entries.length) {

            IndexEntry[] bigger = new IndexEntry[entries.length * 2];
            for (int i = 0; i < size; i++) {
                bigger[i] = entries[i];
            }
            entries = bigger;
        }
        entries[size] = entry;
        size++;
    }
    
    private int findById(int id) {
        for (int i = 0; i < size; i++) {
            if (entries[i].id == id) {
                return i;
            }
        }
        return -1;
    }
    
    private void loadIndex() {
        File f = new File(IDX_FILE);
        if (!f.exists()) return;

        try (DataInputStream dis = new DataInputStream( new BufferedInputStream(new FileInputStream(f)))) {
            
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                int  id     = dis.readInt();
                long offset = dis.readLong();
                int  length = dis.readInt();
                addEntry(new IndexEntry(id, offset, length));
            }
            
        } catch (IOException e) {
            System.err.println("[Storage] No se pudo cargar el índice: " + e.getMessage());
        }
    }
    
    private void saveIndex() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(IDX_FILE)))) {

            dos.writeInt(size);
            for (int i = 0; i < size; i++) {
                dos.writeInt(entries[i].id);
                dos.writeLong(entries[i].offset);
                dos.writeInt(entries[i].length);
            }
        }
    }
    
    private void compact() throws IOException {
        File dataFile = new File(DATA_FILE);
        File tmpFile  = new File(DATA_FILE + ".tmp");
        
        try (RandomAccessFile src = new RandomAccessFile(dataFile, "r");
             RandomAccessFile dst = new RandomAccessFile(tmpFile, "rw")) {

            for (int i = 0; i < size; i++) {
                IndexEntry entry = entries[i];

                byte[] bytes = new byte[entry.length];
                src.seek(entry.offset);
                src.readFully(bytes);

                long newOffset = dst.length();
                dst.seek(newOffset);
                dst.write(bytes);

                entries[i].offset = newOffset;
            }
        }
        
        Files.move(tmpFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        saveIndex();
    }
    
    private byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T deserialize(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Clase no encontrada", e);
        }
    }
}