package nanoj.deviceControl.java.sequentialProtocol.tabs;

import gnu.io.NRSerialPort;
import nanoj.deviceControl.java.devices.ConnectedSubDevice;
import nanoj.deviceControl.java.devices.Device;
import nanoj.deviceControl.java.devices.DeviceManager;
import nanoj.deviceControl.java.sequentialProtocol.GUI;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.prefs.Preferences;

public class DeviceConnections extends JPanel {
    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
    private DeviceManager deviceManager = DeviceManager.INSTANCE;
    private GUI gui;
    public String name = "Device Connections";

    private static final String VIRTUAL_PUMP = "Virtual Device";
    private static final String VIRTUAL_PORT = "Virtual Port";
    private static final String PORT = "com";
    private static final String PUMP = "device";

    JComboBox availableDevicesList;

    JLabel version = new JLabel("NanoJ Sequential Labelling version: 1.2.5");
    JLabel deviceListLabel = new JLabel("Device type");
    JLabel connectLabel = new JLabel("Serial port");
    JComboBox portsList;
    JButton connectButton = new JButton("Connect");
    JButton disconnectButton = new JButton("Disconnect");
    JLabel connectedDevicesLabel = new JLabel("List of currently connected devices");
    private JTable connectedDevicesTable;
    private ConnectionsTable connectedDevicesTableModel;
    JScrollPane connectedDevicesListPane;

    public DeviceConnections(GUI gui) {
        super();
        this.gui = gui;

        connectedDevicesTableModel = new ConnectionsTable();
        connectedDevicesTable = new JTable(connectedDevicesTableModel);
        connectedDevicesListPane = new JScrollPane(connectedDevicesTable);

        availableDevicesList = new JComboBox(deviceManager.getAvailableDevicesList());
        availableDevicesList.setSelectedItem(prefs.get(PUMP, VIRTUAL_PUMP));

        portsList = new JComboBox(new Vector(NRSerialPort.getAvailableSerialPorts()));
        portsList.addItem(VIRTUAL_PORT);
        portsList.setSelectedItem(prefs.get(PORT, VIRTUAL_PORT));

        setLayout( new DeviceConnectionsLayout(this));

        connectButton.addActionListener(new DeviceConnections.Connect());
        disconnectButton.addActionListener(new DeviceConnections.Disconnect());

    }

    public void rememberSettings() {
        prefs.put(PORT, (String) portsList.getSelectedItem());
        prefs.put(PUMP, (String) availableDevicesList.getSelectedItem());
    }

    private class Connect implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String port = (String) portsList.getSelectedItem();
            prefs.put(PORT, port);
            try {
                String isItConnected = deviceManager.connect((String) availableDevicesList.getSelectedItem(), port);
                if (isItConnected.equals(DeviceManager.PORT_ALREADY_CONNECTED)) {
                    gui.log.message("Tried to connect to " +
                            availableDevicesList.getSelectedItem() + " on port " +
                            port + ", but that port is already in use!");
                    return;
                } else if (isItConnected.equals(Device.FAILED_TO_CONNECT)) {
                    gui.log.message("Tried to connect to " +
                            availableDevicesList.getSelectedItem() + " on port " +
                            port + ", but there was an error!");
                    return;
                }

                connectedDevicesTableModel.setRowCount(0);

                for (ConnectedSubDevice subDevice: deviceManager.getConnectedDevicesList())
                    connectedDevicesTableModel.addRow(subDevice.asConnectionArray());

                gui.log.message("Connected to " + deviceManager.getAvailableDevicesList()[availableDevicesList.getSelectedIndex()] +
                        " on port " + port);
            } catch (Exception e1) {
                gui.log.message("Error, can not connect, check core log.");
                e1.printStackTrace();
            }
        }
    }

    private class Disconnect implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean success = false;
            String port = "Undefined";
            String name = "Undefined";
            try {
                port = (String) portsList.getSelectedItem();
                name = deviceManager.getDeviceNameOnPort(port);
                success = deviceManager.disconnect(port);
            } catch (Exception e1) {
                gui.log.message("Error, failed to disconnect properly.");
                e1.printStackTrace();
            }
            if (success) {
                connectedDevicesTableModel.setRowCount(0);

                for (ConnectedSubDevice subDevice: deviceManager.getConnectedDevicesList())
                    connectedDevicesTableModel.addRow(subDevice.asConnectionArray());

                gui.log.message("Disconnected from " + name + " on port " + port + ".");
            }
        }
    }

    public class ConnectionsTable extends DefaultTableModel {

        protected ConnectionsTable() {
            super(
                    new String[][]{{DeviceManager.NO_PUMP_CONNECTED,"",""}},
                    new String[]{"Device","Sub-Device","COM port"});
        }

        public boolean isCellEditable(int row, int column){
            return false;
        }

    }

}
