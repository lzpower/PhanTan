package network;

import gui.Gui_Start;

import javax.swing.SwingUtilities;

public class Client {

    public static void main(String[] args) {
        String host = args.length > 0 && args[0] != null && !args[0].isBlank() ? args[0] : "Dell-6N85";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9090;
        String clientName = args.length > 2 && args[2] != null && !args[2].isBlank()
                ? args[2]
                : Gui_Client.defaultClientName();

        SwingUtilities.invokeLater(() -> new Gui_Start().setVisible(true));
    }
}