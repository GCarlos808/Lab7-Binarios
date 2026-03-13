package lab7binarios.GUI;

import lab7binarios.Track;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;

public class PlaylistPanel extends JPanel {
    
    private static final Color BG         = new Color(22, 22, 26);
    private static final Color ROW_HOVER  = new Color(45, 45, 52);
    private static final Color ROW_SEL    = new Color(80, 0, 80);
    private static final Color ACCENT     = new Color(255, 55, 200);
    private static final Color FG         = new Color(235, 235, 235);
    private static final Color FG_DIM     = new Color(150, 150, 160);
    
    private final DefaultListModel<Track> listModel = new DefaultListModel<>();
    private final JList<Track> songList;
    
    private Consumer<Track>    onSelect;
    private Consumer<Track>    onRemove;
    
    public PlaylistPanel() {
        super(new BorderLayout());
        setBackground(BG);
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(18, 16, 10, 16));
        JLabel title = new JLabel("Playlist");
        title.setForeground(FG);
        title.setFont(new Font("SF Pro Display", Font.BOLD, 16));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
        
        songList = new JList<>(listModel);
        songList.setBackground(BG);
        songList.setForeground(FG);
        songList.setSelectionBackground(ROW_SEL);
        songList.setSelectionForeground(Color.WHITE);
        songList.setFont(new Font("SF Pro", Font.PLAIN, 13));
        songList.setCellRenderer(new SongCellRenderer());
        songList.setFixedCellHeight(60);
        songList.setBorder(null);
        
        songList.addMouseListener(new MouseAdapter() {
            
            @Override public void mouseClicked(MouseEvent e) {
                
                int idx = songList.locationToIndex(e.getPoint());
                if (idx < 0)
                    return;
                
                Track s = listModel.get(idx);
                
                if (e.getClickCount() == 2 && onSelect != null) {
                    onSelect.accept(s);
                }
            }
        });
        
        JScrollPane scroll = new JScrollPane(songList);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        add(scroll, BorderLayout.CENTER);
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        footer.setBackground(BG);
        JButton removeBtn = new JButton("Quitar Canción");
        styleButton(removeBtn);
        removeBtn.addActionListener(e -> {
            Track s = songList.getSelectedValue();
            if (s != null && onRemove != null) onRemove.accept(s);
        });
        footer.add(removeBtn);
        add(footer, BorderLayout.SOUTH);
    }
    
    public void setSongs(List<Track> songs) {
        listModel.clear();
        songs.forEach(listModel::addElement);
    }
    
    public void addSong(Track song) {
        listModel.addElement(song);
    }
    
    public void removeSong(Track song) {
        listModel.removeElement(song);
    }
    
    
    public void highlight(Track song) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getId() == song.getId()) {
                songList.setSelectedIndex(i);
                songList.ensureIndexIsVisible(i);
                return;
            }
        }
    }
    
    public void setOnSelect(Consumer<Track> cb) { this.onSelect = cb; }
    public void setOnRemove(Consumer<Track> cb) { this.onRemove = cb; }
    
    private void styleButton(JButton b) {
        b.setBackground(new Color(50, 0, 50));
        b.setForeground(ACCENT);
        b.setFont(new Font("SF Pro", Font.PLAIN, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(80, 0, 80)),new EmptyBorder(5, 12, 5, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    private static class SongCellRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Track song = (Track) value;
            JPanel row = new JPanel(new BorderLayout(10, 0));
            
            row.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(40, 40, 46)), new EmptyBorder(6, 14, 6, 14)));
            row.setBackground(isSelected ? ROW_SEL : (index % 2 == 0 ? BG : new Color(28, 28, 33)));
            
            JLabel thumb = new JLabel(String.valueOf(index + 1));
            thumb.setForeground(new Color(100, 100, 110));
            thumb.setFont(new Font("SF Pro", Font.PLAIN, 12));
            thumb.setPreferredSize(new Dimension(24, 48));
            thumb.setHorizontalAlignment(SwingConstants.CENTER);
            row.add(thumb, BorderLayout.WEST);
            
            JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
            info.setOpaque(false);
            JLabel titleLbl = new JLabel(song.getNombre());
            titleLbl.setForeground(isSelected ? Color.WHITE : FG);
            titleLbl.setFont(new Font("Pro", Font.BOLD, 13));
            JLabel artistLbl = new JLabel(song.getArtista() + "  ·  " + song.getGenre());
            artistLbl.setForeground(isSelected ? new Color(220, 180, 220) : FG_DIM);
            artistLbl.setFont(new Font("Pro", Font.PLAIN, 11));
            info.add(titleLbl);
            info.add(artistLbl);
            row.add(info, BorderLayout.CENTER);
            
            JLabel dur = new JLabel(song.getDuracionFormatted());
            dur.setForeground(isSelected ? new Color(255, 200, 255) : FG_DIM);
            dur.setFont(new Font("SF Pro", Font.PLAIN, 12));
            dur.setPreferredSize(new Dimension(36, 48));
            dur.setHorizontalAlignment(SwingConstants.CENTER);
            row.add(dur, BorderLayout.EAST);
            
            return row;
        }
    }
    
    private static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        
        @Override protected void configureScrollBarColors() {
            thumbColor= new Color(80, 0, 80);
            trackColor= new Color(30, 30, 35);
        }
        
        @Override
        protected JButton createDecreaseButton(int o) {
            return zeroButton();
        }
        
        @Override protected JButton createIncreaseButton(int o) {
            return zeroButton();
        }
        
        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }
}
