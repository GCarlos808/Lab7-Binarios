package lab7binarios.GUI;


import lab7binarios.Track;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;


public class AddDialog extends JDialog {
    
    private static final Color BG       = new Color(28, 28, 30);
    private static final Color PANEL_BG = new Color(38, 38, 42);
    private static final Color ACCENT   = new Color(255, 55, 200);
    private static final Color FG       = new Color(235, 235, 235);
    private static final Color FG_DIM   = new Color(160, 160, 170);
    
    private final Track song;
    private boolean confirmed = false;
    
    private JTextField nameField;
    private JTextField artistField;
    private JTextField genreField;
    private JLabel     artPreview;
    private byte[]     artData;
    private String     artMime;
    
    public AddDialog(Frame parent, Track preloaded) {
        super(parent, "Añadir Canción", true);
        this.song    = preloaded;
        this.artData = preloaded.getAlbumArt();
        this.artMime = preloaded.getAlbumType();
        buildGUI();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void buildGUI() {
        
        setBackground(BG);
        getRootPane().setBackground(BG);
        
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(24, 28, 20, 28));
        setContentPane(root);
        
        JLabel heading = new JLabel("Add to Playlist");
        heading.setForeground(FG);
        heading.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        root.add(heading, BorderLayout.NORTH);
        
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(BG);
        centre.setBorder(new EmptyBorder(16, 0, 16, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 0, 6, 12);
        c.anchor = GridBagConstraints.WEST;
        
        artPreview = new JLabel();
        artPreview.setPreferredSize(new Dimension(110, 110));
        artPreview.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1));
        artPreview.setBackground(PANEL_BG);
        artPreview.setOpaque(true);
        artPreview.setHorizontalAlignment(SwingConstants.CENTER);
        refreshArtPreview();
        
        JButton chooseArtBtn = styledButton("Choose Image");
        chooseArtBtn.setFont(new Font("SF Pro", Font.PLAIN, 11));
        chooseArtBtn.addActionListener(e -> chooseArt());
        
        JPanel artPanel = new JPanel(new BorderLayout(0, 6));
        artPanel.setBackground(BG);
        artPanel.add(artPreview, BorderLayout.CENTER);
        artPanel.add(chooseArtBtn, BorderLayout.SOUTH);
        
        c.gridx = 0; c.gridy = 0; c.gridheight = 4; c.insets = new Insets(0, 0, 0, 20);
        centre.add(artPanel, c);
        c.gridheight = 1;
        c.insets = new Insets(6, 0, 6, 0);
        
        c.gridx = 1; c.gridy = 0;
        centre.add(label("Nombre"), c);
        c.gridy = 1;
        nameField = inputField(song.getNombre());
        centre.add(nameField, c);
        
        c.gridy = 2;
        centre.add(label("Artista"), c);
        c.gridy = 3;
        artistField = inputField(song.getArtista());
        centre.add(artistField, c);
        
        c.gridy = 4;
        centre.add(label("Genéro"), c);
        c.gridy = 5;
        genreField = inputField(song.getGenre());
        centre.add(genreField, c);
        
        c.gridy = 6;
        centre.add(label("Duración"), c);
        c.gridy = 7;
        JLabel durLabel = new JLabel(song.getDuracionFormatted());
        durLabel.setForeground(FG_DIM);
        durLabel.setFont(new Font("Pro", Font.PLAIN, 13));
        centre.add(durLabel, c);
        
        c.gridy = 8;
        centre.add(label("File"), c);
        c.gridy = 9;
        JLabel pathLabel = new JLabel(truncate(song.getFilePath(), 40));
        pathLabel.setForeground(FG_DIM);
        pathLabel.setFont(new Font("Pro", Font.PLAIN, 11));
        centre.add(pathLabel, c);
        
        root.add(centre, BorderLayout.CENTER);
        
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(BG);
        
        JButton cancel = styledButton("Cancelar");
        cancel.addActionListener(e -> dispose());
        
        JButton add = accentButton("Añadir Canción");
        add.addActionListener(e -> {
            applyEdits();
            confirmed = true;
            dispose();
        });
        
        btnRow.add(cancel);
        btnRow.add(add);
        root.add(btnRow, BorderLayout.SOUTH);
    }
    
    private void applyEdits() {
        song.setNombre(nameField.getText().trim().isEmpty() ? "No identificado" : nameField.getText().trim());
        song.setArtista(artistField.getText().trim().isEmpty() ? "Desconocido" : artistField.getText().trim());
        song.setGenre(genreField.getText().trim().isEmpty() ? "Desconocido" : genreField.getText().trim());
        song.setAlbumArt(artData);
        song.setAlbumType(artMime);
    }
    
    private void chooseArt() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Imagenes", "jpg", "png"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                artData = Files.readAllBytes(fc.getSelectedFile().toPath());
                artMime = fc.getSelectedFile().getName().toLowerCase().endsWith(".png")
                        ? "imagen/png" : "imagen/jpeg";
                refreshArtPreview();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "No se logró cargar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void refreshArtPreview() {
        
        if (artData != null && artData.length > 0) {
            
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(artData));
                if (img != null) {
                    Image scaled = img.getScaledInstance(110, 110, Image.SCALE_SMOOTH);
                    artPreview.setIcon(new ImageIcon(scaled));
                    artPreview.setText("");
                    return;
                }
            } catch (Exception ignored) {}
        }
        
        artPreview.setIcon(null);
        artPreview.setText("-");
        artPreview.setFont(new Font("Dialog", Font.PLAIN, 36));
        artPreview.setForeground(new Color(100, 100, 110));
    }
    
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FG_DIM);
        l.setFont(new Font("Pro", Font.PLAIN, 11));
        return l;
    }
    
    private JTextField inputField(String value) {
        JTextField f = new JTextField(value, 22);
        f.setBackground(PANEL_BG);
        f.setForeground(FG);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Pro", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 80)),
                new EmptyBorder(5, 8, 5, 8)));
        return f;
    }
    
    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PANEL_BG);
        b.setForeground(FG);
        b.setFont(new Font("Pro", Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 80)),
                new EmptyBorder(7, 16, 7, 16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    
    private JButton accentButton(String text) {
        JButton b = styledButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Pro", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(7, 20, 7, 20));
        return b;
    }
    
    private String truncate(String s, int max) {
        return s.length() <= max ? s : "…" + s.substring(s.length() - max + 1);
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public Track getSong() {
        return song;
    }
    
}
