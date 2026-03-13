package lab7binarios;

import java.io.Serializable;

public class Track implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String nombre;
    private String artista;
    private String genre;
    private double duracion;
    private String filePath;
    private byte[] albumArt;
    private String albumType;
    
    public Track() {}
    
    public Track(int id, String nombre, String artista, String genre, double duracion, String filePath, byte[] albumArt, String albumType) {
        this.id = id;
        this.nombre = nombre;
        this.artista = artista;
        this.genre = genre;
        this.duracion = duracion;
        this.filePath = filePath;
        this.albumArt = albumArt;
        this.albumType = albumType;
    }
    
    public int getId(){
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    public String getnombre() { 
        return nombre;
    }
    public void setnombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getartista() { 
        return artista;
    }
    public void setartista(String artista) {
        this.artista = artista;
    }
    
    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public double getduracion() {
        return duracion;
    }
    public void setduracion(double duracion) {
        this.duracion = duracion;
    }
    
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public byte[] getalbumArt() {
        return albumArt;
    }
    public void setalbumArt(byte[] albumArt) {
        this.albumArt = albumArt;
    }
    
    public String getalbumType() {
        return albumType;
    }
    public void setalbumType(String albumType) {
        this.albumType = albumType;
    }
    
    public String getduracionFormatted() {
        int total = (int) duracion;
        return String.format("%d:%02d", total / 60, total % 60);
    }
    
    @Override
    public String toString() {
        return nombre + " — " + artista + " (" + getduracionFormatted() + ")";
    }
}