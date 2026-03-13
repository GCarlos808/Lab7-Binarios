package lab7binarios.GUI;


import lab7binarios.Track;
import lab7binarios.AudioPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class PlayerPanel extends JPanel {
    
    private static final Color BG      = new Color(18, 18, 22);
    private static final Color CARD_BG = new Color(28, 28, 34);
    private static final Color ACCENT  = new Color(255, 55, 200);
    private static final Color FG      = new Color(235, 235, 235);
    private static final Color FG_DIM  = new Color(150, 150, 160);
    private static final Color TRACK   = new Color(55, 55, 65);
    private static final Color FILL    = ACCENT;
    
    private final AudioPlayer player;
    private Track currentTrack;
    
    private JLabel artLabel;
    private JLabel titleLabel;
    private JLabel artistLabel;
    private JLabel genreLabel;
    private JLabel timeLabel;
    private JLabel durationLabel;
    private JSlider progressSlider;
    private JButton playPauseBtn;
    
    private static final String PLAY_ICON  = "▶";
    private static final String PAUSE_ICON = "⏸";
    private static final String STOP_ICON  = "⏹";
    
    private Runnable onPlay;
    private Runnable onPause;
    private Runnable onStop;
    
    public PlayerPanel(AudioPlayer player) {
        super(new BorderLayout());
        this.player = player;
        setBackground(BG);
        setBorder(new EmptyBorder(32, 32, 32, 32));
        buildUI();
    }
    
    private void buildUI() {
        
        artLabel = new JLabel();
        artLabel.setPreferredSize(new Dimension(260, 260));
        artLabel.setHorizontalAlignment(SwingConstants.CENTER);
        artLabel.setVerticalAlignment(SwingConstants.CENTER);
        artLabel.setBackground(CARD_BG);
        artLabel.setOpaque(true);
        artLabel.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60), 1));
        artLabel.setFont(new Font("Dialog", Font.PLAIN, 64));
        artLabel.setForeground(new Color(70, 70, 80));
        artLabel.setText("♪");
        artLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel artWrapper = new JPanel();
        artWrapper.setLayout(new BoxLayout(artWrapper, BoxLayout.Y_AXIS));
        artWrapper.setBackground(BG);
        artWrapper.add(Box.createVerticalStrut(10));
        artLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        artWrapper.add(artLabel);
        artWrapper.add(Box.createVerticalStrut(24));
        add(artWrapper, BorderLayout.NORTH);
        
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(BG);
        
        titleLabel  = centeredLabel("Ninguna canción seleccionada", new Font("Pro Display", Font.BOLD,  20), FG);
        artistLabel = centeredLabel("—", new Font("Pro Display", Font.PLAIN, 14), FG_DIM);
        genreLabel  = centeredLabel("", new Font("Pro Display", Font.PLAIN, 12), new Color(120, 0, 120));
        
        bottom.add(titleLabel);
        bottom.add(Box.createVerticalStrut(4));
        bottom.add(artistLabel);
        bottom.add(Box.createVerticalStrut(2));
        bottom.add(genreLabel);
        bottom.add(Box.createVerticalStrut(18));
        
        JPanel progressRow = new JPanel(new BorderLayout(8, 0));
        progressRow.setBackground(BG);
        progressRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        timeLabel     = new JLabel("0:00");
        durationLabel = new JLabel("0:00");
        styleTimeLabel(timeLabel);
        styleTimeLabel(durationLabel);
        
        progressSlider = new JSlider(0, 1000, 0);
        progressSlider.setBackground(BG);
        progressSlider.setForeground(ACCENT);
        progressSlider.setBorder(null);
        progressSlider.setUI(new AccentSliderUI(progressSlider));
        progressSlider.addChangeListener(e -> {
            
            if (progressSlider.getValueIsAdjusting()) {
                double fraction = progressSlider.getValue() / 1000.0;
                player.seek(fraction);
            }
        });
        
        progressRow.add(timeLabel,     BorderLayout.WEST);
        progressRow.add(progressSlider, BorderLayout.CENTER);
        progressRow.add(durationLabel,  BorderLayout.EAST);
        
        bottom.add(progressRow);
        
        bottom.add(Box.createVerticalStrut(20));
        
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        controls.setBackground(BG);
        
        JButton stopBtn = transportButton(STOP_ICON, false);
        stopBtn.addActionListener(e -> { if (onStop != null) onStop.run(); });
        
        playPauseBtn = transportButton(PLAY_ICON, true);
        playPauseBtn.addActionListener(e -> {
            if (player.getState() == AudioPlayer.State.PLAYING) {
                if (onPause != null) onPause.run();
            } else {
                if (onPlay != null) onPlay.run();
            }
        });
        
        controls.add(stopBtn);
        controls.add(playPauseBtn);
        bottom.add(controls);
        
        add(bottom, BorderLayout.CENTER);
    }
    
    public void setSong(Track song) {
        this.currentTrack = song;
        if (song == null) {
            artLabel.setIcon(null);
            artLabel.setText("♪");
            titleLabel.setText("No song selected");
            artistLabel.setText("—");
            genreLabel.setText("");
            durationLabel.setText("0:00");
            return;
        }
        titleLabel.setText(song.getNombre());
        artistLabel.setText(song.getArtista());
        genreLabel.setText(song.getGenre());
        durationLabel.setText(song.getDuracionFormatted());
        updateAlbumArt(song);
    }
    
    public void updateProgress(double positionSeconds) {
        if (currentTrack == null || progressSlider.getValueIsAdjusting())
            return;
        
        double dur = currentTrack.getDuracion();
        if (dur <= 0) return;
        int val = (int)((positionSeconds / dur) * 1000);
        progressSlider.setValue(Math.min(val, 1000));
        timeLabel.setText(formatTime(positionSeconds));
    }
    
    public void setPlayingState(boolean playing) {
        playPauseBtn.setText(playing ? PAUSE_ICON : PLAY_ICON);
    }
    
    public void resetProgress() {
        progressSlider.setValue(0);
        timeLabel.setText("0:00");
    }
    
    public void setOnPlay(Runnable r)  { this.onPlay  = r; }
    public void setOnPause(Runnable r) { this.onPause = r; }
    public void setOnStop(Runnable r)  { this.onStop  = r; }
    
    private void updateAlbumArt(Track song) {
        byte[] data = song.getAlbumArt();
        if (data != null && data.length > 0) {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                if (img != null) {
                    
                    BufferedImage round = makeRoundedImage(img, 260, 260, 18);
                    artLabel.setIcon(new ImageIcon(round));
                    artLabel.setText("");
                    return;
                }
            } catch (Exception ignored) {}
        }
        artLabel.setIcon(null);
        artLabel.setText("♪");
    }
    
    private BufferedImage makeRoundedImage(BufferedImage src, int w, int h, int arc) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        g2.drawImage(src.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        g2.dispose();
        return out;
    }
    
    private JLabel centeredLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        l.setFont(font);
        l.setForeground(color);
        return l;
    }
    
    private void styleTimeLabel(JLabel l) {
        l.setForeground(FG_DIM);
        l.setFont(new Font("SF Mono", Font.PLAIN, 11));
    }
    
    private JButton transportButton(String icon, boolean primary) {
        JButton b = new JButton(icon);
        b.setFont(new Font("Dialog", Font.PLAIN, primary ? 26 : 20));
        b.setForeground(primary ? ACCENT : FG_DIM);
        b.setBackground(BG);
        b.setBorder(null);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            
            @Override public void mouseEntered(MouseEvent e){
                b.setForeground(primary ? Color.WHITE : ACCENT);
            }
            
            @Override public void mouseExited(MouseEvent e)  {
                b.setForeground(primary ? ACCENT : FG_DIM);
                
            }
        });
        return b;
    }
    
    private static String formatTime(double seconds) {
        int s = (int) seconds;
        return String.format("%d:%02d", s / 60, s % 60);
    }
    
    private static class AccentSliderUI extends javax.swing.plaf.basic.BasicSliderUI {
        AccentSliderUI(JSlider s) { super(s); }
        
        @Override public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = trackRect;
            int cy = r.y + r.height / 2;
            
            g2.setColor(TRACK);
            g2.fillRoundRect(r.x, cy - 2, r.width, 4, 4, 4);
            
            int fillW = (int)((slider.getValue() / (double) slider.getMaximum()) * r.width);
            g2.setColor(FILL);
            g2.fillRoundRect(r.x, cy - 2, fillW, 4, 4, 4);
        }
        
        @Override public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ACCENT);
            Rectangle t = thumbRect;
            int d = Math.min(t.width, t.height) - 2;
            g2.fillOval(t.x + (t.width - d) / 2, t.y + (t.height - d) / 2, d, d);
        }
    }
}
