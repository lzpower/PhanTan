package network;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import gui.Gui_Chinh;
import gui.Gui_Start;

import javax.swing.*;
import java.awt.*;
import javax.swing.UIManager;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import java.awt.*;

public class Client {

    public static void main(String[] args) {
        String host = args.length > 0 && args[0] != null && !args[0].isBlank() ? args[0] : ClientSession.getHost();
        int port = args.length > 1 ? Integer.parseInt(args[1]) : ClientSession.getPort();
        String clientName = args.length > 2 && args[2] != null && !args[2].isBlank()
            ? args[2]
            : ClientSession.defaultClientName();

        ClientSession.configureRemote(host, port, clientName);

        System.setProperty("sun.java2d.uiScale", "1.0");
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.put("TitlePane.menuBarEmbedded", false);
                    UIManager.setLookAndFeel(new FlatMacLightLaf());
                    UIManager.put("MenuBar.foreground", Color.white);
                    UIManager.put("defaultFont", new Font("Segoe UI Semibold", Font.PLAIN, 14));
                    UIManager.put("TextComponent.arc", 12);
                    UIManager.put("Button.arc", 12);
                    new Gui_Start().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}