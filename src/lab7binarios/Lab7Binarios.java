package lab7binarios;

import lab7binarios.GUI.MainGUI;
import javax.swing.SwingUtilities;

public class Lab7Binarios {


    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            MainGUI ventana = new MainGUI();
            ventana.setVisible(true);
            
        });
    }

}
