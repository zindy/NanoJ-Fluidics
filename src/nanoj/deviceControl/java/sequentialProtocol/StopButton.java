package nanoj.deviceControl.java.sequentialProtocol;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StopButton extends JButton implements ActionListener {
    private JComboBox deviceSelection = null;
    private GUI gui;
    private int device = -1;

    public StopButton(GUI gui) {
        super("Stop Device!");
        this.gui = gui;
        this.deviceSelection = null;

        addActionListener(this);
    }

    public StopButton(GUI gui, JComboBox deviceSelection) {
        super("Stop Device!");
        this.gui = gui;
        this.deviceSelection = deviceSelection;

        addActionListener(this);
    }

    public void setCurrentDevice(int currentDevice) {
        this.device = currentDevice;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (deviceSelection != null)
                device = deviceSelection.getSelectedIndex();

            if (!gui.deviceManager.isConnected(device)) {
                gui.log.message("Can't do anything until device is connected.");
                return;
            }

            gui.deviceManager.stopPumping(device);
            gui.log.message("Told device " + gui.deviceManager.getAllFullNames()[device] + " to stop!");

        } catch (Exception e1) {
            gui.log.message("Error, did not properly stop device.");
            e1.printStackTrace();
        }

    }
}
