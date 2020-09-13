package nanoj.deviceControl.java.sequentialProtocol.tabs;

import nanoj.deviceControl.java.devices.ConnectedSubDevice;
import nanoj.deviceControl.java.devices.Device;
import nanoj.deviceControl.java.devices.DeviceManager;
import nanoj.deviceControl.java.devices.Syringe;
import nanoj.deviceControl.java.sequentialProtocol.FlowRateSlider;
import nanoj.deviceControl.java.sequentialProtocol.GUI;
import nanoj.deviceControl.java.sequentialProtocol.StopButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;

public class DirectControl extends JPanel implements Observer, ActionListener {
    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
    private DeviceManager deviceManager = DeviceManager.INSTANCE;
    private GUI gui;

    public String name = "Direct Device Control";

    private static final String TARGET = "target";
    
    JLabel deviceStatusLabel = new JLabel("Device status:");
    JLabel deviceStatus = new JLabel("Device not started.");
    JLabel deviceSelectionLabel = new JLabel("Select device to control: ");
    JComboBox deviceSelection;
    JLabel syringeLabel = new JLabel("Syringe");
    JComboBox syringeComboBox;
    JLabel rateLabel = new JLabel("Rate ("+ DeviceManager.FLOW_RATE_UNITS+")");
    FlowRateSlider rateSlider = new FlowRateSlider();
    JLabel targetLabel = new JLabel("Target Volume (" + DeviceManager.VOLUME_UNITS + ")");
    JTextField targetVolume = new JTextField(prefs.get(TARGET, "500"), 6);
    JLabel actionLabel = new JLabel("Action to Perform");
    JRadioButton infuse = new JRadioButton("Infuse", true);
    JRadioButton withdraw = new JRadioButton("Withdraw");
    JButton startDeviceButton = new JButton("Device!");
    StopButton stopDeviceButton;

    private boolean editing = false;

    public DirectControl(GUI gui) {
        super();
        this.gui = gui;

        ButtonGroup buttons = new ButtonGroup();

        syringeComboBox = new JComboBox(Syringe.getAllBrandedNames());
        deviceSelection = new JComboBox(new String[]{DeviceManager.NO_PUMP_CONNECTED});
        buttons.add(infuse);
        buttons.add(withdraw);

        stopDeviceButton = new StopButton(gui,deviceSelection);

        setLayout(new DirectControlLayout(this));

        deviceSelection.addActionListener(this);
        syringeComboBox.addActionListener(this);
        startDeviceButton.addActionListener(this);

        deviceManager.addObserver(this);
    }

    public void rememberPreferences() {
        prefs.put(TARGET, targetVolume.getText());
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg.equals(DeviceManager.NEW_PUMP_CONNECTED) ||
            arg.equals(DeviceManager.PUMP_DISCONNECTED)) {
            if (deviceManager.noDevicesConnected()) {
                deviceSelection.removeAllItems();
                deviceSelection.addItem(DeviceManager.NO_PUMP_CONNECTED);
            }
            else {
                editing = true;
                deviceSelection.removeAllItems();
                for (ConnectedSubDevice device: deviceManager.getConnectedDevicesList())
                    deviceSelection.addItem(device.getFullName());

                editing = false;
            }

            if (deviceManager.noDevicesConnected())
                return;

            rateSlider.setDeviceSelection(deviceSelection.getSelectedIndex());
            rateSlider.setSyringeDiameter(Syringe.values()[syringeComboBox.getSelectedIndex()].diameter);
        } else if (arg.equals(DeviceManager.NEW_STATUS_AVAILABLE)) {
            deviceStatus.setText(deviceManager.getStatus());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(syringeComboBox) && deviceManager.anyDevicesConnected())
            rateSlider.setSyringeDiameter(Syringe.values()[syringeComboBox.getSelectedIndex()].diameter);

        else if (e.getSource().equals(startDeviceButton)) {
            int index = deviceSelection.getSelectedIndex();
            prefs.put(TARGET, targetVolume.getText());
            if (gui.deviceManager.isConnected(index)) {
                try {
                    Device.Action action;
                    if (infuse.isSelected())
                        action = Device.Action.Infuse;
                    else action = Device.Action.Withdraw;
                    rateSlider.setDeviceSelection(index);
                    rateSlider.setSyringeDiameter(Syringe.values()[syringeComboBox.getSelectedIndex()].diameter);
                    double volume = Double.parseDouble(targetVolume.getText());
                    gui.log.message("" + gui.deviceManager.startPumping(
                            index,
                            Syringe.values()[syringeComboBox.getSelectedIndex()],
                            rateSlider.getCurrentFlowRate(),
                            volume,
                            action
                    ));
                    gui.log.message("Estimated pumping time: " + (volume/rateSlider.getCurrentFlowRate()) + " secs");
                } catch (Exception e1) {
                    gui.log.message("Error, problem with starting the device.");
                    e1.printStackTrace();
                }
            } else gui.log.message("Can't do anything until device is connected.");
        }

        else if (e.getSource().equals(deviceSelection)) {
            if (deviceSelection.getItemCount() >= 0 &&
                    deviceManager.anyDevicesConnected() &&
                    !editing)
                rateSlider.setDeviceSelection(deviceSelection.getSelectedIndex());
        }

    }


}
