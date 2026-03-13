package lab7binarios.GUI;

import lab7binarios.Track;
import lab7binarios.AudioPlayer;
import lab7binarios.Metadata;
import lab7binarios.Almacenamiento.PlaylistQueue;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class MainGUI extends JFrame {
    
    private static final Color BG_DARK  = new Color(16, 16, 20);
    private static final Color BG_PANEL = new Color(22, 22, 28);
    private static final Color ACCENT   = new Color(255, 55, 200);
    private static final Color FG       = new Color(235, 235, 235);
    private static final Color DIVIDER  = new Color(40, 40, 50);
    
    private final PlaylistQueue storage = new PlaylistQueue();
    private final AudioPlayer   player  = new AudioPlayer();
    private Track selectedSong;
    
    private final PlaylistPanel playlistPanel = new PlaylistPanel();
    private final PlayerPanel   playerPanel;
    
    public MainGUI() {
        super("Music Player");
        playerPanel = new PlayerPanel(player);
        initLookAndFeel();
        buildGUI();
        events();
        loadPersistedPlaylist();
    }
    
    private void initLookAndFeel() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        UIManager.put("OptionPane.background",        BG_PANEL);
        UIManager.put("Panel.background",             BG_PANEL);
        UIManager.put("OptionPane.messageForeground", FG);
    }
    
    private void buildGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(BG_DARK);
        titleBar.setBorder(new EmptyBorder(12, 20, 10, 20));
        
        JLabel appTitle = new JLabel("MC Player");
        appTitle.setFont(new Font("Pro Display", Font.BOLD, 18));
        appTitle.setForeground(FG);
        titleBar.add(appTitle, BorderLayout.WEST);
        
        JButton addBtn = accentButton("+ Agregar Canción");
        addBtn.addActionListener(e -> addSong());
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setBackground(BG_DARK);
        rightButtons.add(addBtn);
        titleBar.add(rightButtons, BorderLayout.EAST);
        
        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        sep.setBackground(DIVIDER);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, playlistPanel, playerPanel);
        split.setDividerLocation(360);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(DIVIDER);
        split.setContinuousLayout(true);
        
        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setBackground(BG_DARK);
        topArea.add(titleBar, BorderLayout.CENTER);
        topArea.add(sep, BorderLayout.SOUTH);
        
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(topArea, BorderLayout.NORTH);
        root.add(split,   BorderLayout.CENTER);
        setContentPane(root);
    }
    
    private void events() {
        
        playlistPanel.setOnSelect(song -> {
            selectedSong = song;
            playerPanel.setSong(song);
            playlistPanel.highlight(song);
            player.play(song);
            playerPanel.setPlayingState(true);
        });
        
        playlistPanel.setOnRemove(this::removeSong);
        
        playerPanel.setOnPlay(() -> {
            if (selectedSong == null) return;
            player.play(selectedSong);
            playerPanel.setPlayingState(true);
        });
        playerPanel.setOnPause(() -> {
            player.pause();
            playerPanel.setPlayingState(false);
        });
        playerPanel.setOnStop(() -> {
            player.stop();
            playerPanel.setPlayingState(false);
            playerPanel.resetProgress();
        });
        
        player.setProgressCallback(pos ->
                SwingUtilities.invokeLater(() -> playerPanel.updateProgress(pos)));
        
        player.setOnEndCallback(() ->
                SwingUtilities.invokeLater(() -> {
                    playerPanel.setPlayingState(false);
                    playerPanel.resetProgress();
                }));
        
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { player.stop(); }
        });
    }
    
    private void addSong() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Archivos de Audio", "wav", "mp3"));
        fc.setMultiSelectionEnabled(false);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        File file = fc.getSelectedFile();
        int  id   = storage.nextId();
        Track song = Metadata.extract(file, id);
        
        AddDialog dlg = new AddDialog(this, song);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;
        
        song = dlg.getSong();
        try {
            storage.addSong(song);
            playlistPanel.addSong(song);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se logró guardar la pista: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeSong(Track track) {
        int choice = JOptionPane.showConfirmDialog(this,
                "¿Quitar \"" + track.getNombre() + "\" de la playlist?",
                "¿Quitar Canción?", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        
        if (player.getCurrentSong() != null
                && player.getCurrentSong().getId() == track.getId()) {
            player.stop();
            playerPanel.setPlayingState(false);
            playerPanel.resetProgress();
            playerPanel.setSong(null);
            selectedSong = null;
        }
        
        try {
            storage.removeSong(track.getId());
            playlistPanel.removeSong(track);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se logró quitar la canción: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadPersistedPlaylist() {
        try {
            List<Track> songs = storage.loadAll();
            playlistPanel.setSongs(songs);
        } catch (Exception ex) {
            System.err.println("[Player] Playlist no encontrada: " + ex.getMessage());
        }
    }
    
    private JButton accentButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Pro", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(7, 18, 7, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
