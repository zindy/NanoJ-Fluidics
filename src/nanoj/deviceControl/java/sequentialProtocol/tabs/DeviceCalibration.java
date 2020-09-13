package nanoj.deviceControl.java.sequentialProtocol.tabs;

import nanoj.deviceControl.java.devices.ConnectedSubDevice;
import nanoj.deviceControl.java.devices.Device;
import nanoj.deviceControl.java.devices.DeviceManager;
import nanoj.deviceControl.java.sequentialProtocol.GUI;
import nanoj.deviceControl.java.sequentialProtocol.StopButton;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;

public class DeviceCalibration extends JPanel implements Observer, TableModelListener, ActionListener {
    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
    private DeviceManager deviceManager = DeviceManager.INSTANCE;
    private GUI gui;
    public String name = "Device Calibration";

    JLabel tableLabel;
    private JTable table;
    private CalibrationTable tableModel;
    JScrollPane tableScrollPane;

    JButton loadCalibration = new JButton("Load Previous Calibration from file");
    JButton saveCalibration = new JButton("Save Current Calibration to file");
    JButton resetCalibration = new JButton("Reset Calibration");

    JComboBox deviceList;
    JLabel timeToDeviceLabel = new JLabel("Time to device (seconds)");
    JTextField timeToDevice = new JTextField("10");
    JButton calibrateButton = new JButton("Start pumping");
    StopButton stopButton;

    private static final String CAL = "Cal";
    private static final String SAVE_LOCATION = "location";

    private int NAME = 0;
    private int SUB_PUMP = 1;
    private int PORT = 2;
    private int DIAMETER = 3;
    private int MAX_FLOW_RATE = 4;
    private int MIN_FLOW_RATE = 5;

    private boolean editing = false;

    public DeviceCalibration(GUI gui) {
        super();

        this.gui = gui;

        deviceList = new JComboBox(new String[]{DeviceManager.NO_PUMP_CONNECTED});
        stopButton = new StopButton(gui,deviceList);

        tableLabel = new JLabel("Currently connected devices. Diameter is in mm. Flow rates are in ul/sec.");
        tableModel = new CalibrationTable();
        table = new JTable(tableModel);
        tableScrollPane = new JScrollPane(table);

        loadCalibration.addActionListener(this);
        saveCalibration.addActionListener(this);
        resetCalibration.addActionListener(this);
        calibrateButton.addActionListener(this);

        deviceManager.addObserver(this);
        tableModel.addTableModelListener(this);

        setLayout(new DeviceCalibrationLayout(this));
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg.equals(DeviceManager.NEW_PUMP_CONNECTED) || arg.equals(DeviceManager.PUMP_DISCONNECTED)) {
            editing = true;

            deviceList.removeAllItems();
            if (deviceManager.noDevicesConnected()) {
                deviceList.addItem(DeviceManager.NO_PUMP_CONNECTED);
            }

            tableModel.setRowCount(0);

            int index = 0;
            for (ConnectedSubDevice subDevice: deviceManager.getConnectedDevicesList()) {
                deviceList.addItem(subDevice.getFullName());
                tableModel.addRow(subDevice.asCalibrationArray());

                String key = subDevice.name + subDevice.subDevice + subDevice.port;

                double diameter = prefs.getDouble(CAL+DIAMETER+key,
                        Double.parseDouble(subDevice.asCalibrationArray()[DIAMETER]));

                tableModel.setValueAt(""+diameter,index,DIAMETER);

                double maxFlowRate = prefs.getDouble(CAL+MAX_FLOW_RATE+key,
                        Double.parseDouble(subDevice.asCalibrationArray()[MAX_FLOW_RATE]));

                tableModel.setValueAt(""+maxFlowRate,index,MAX_FLOW_RATE);

                double minFlowRate = prefs.getDouble(CAL+MIN_FLOW_RATE+key,
                        Double.parseDouble(subDevice.asCalibrationArray()[MIN_FLOW_RATE]));

                tableModel.setValueAt(""+minFlowRate,index,MIN_FLOW_RATE);

                deviceManager.updateReferenceRate(index,new double[]{diameter,maxFlowRate,minFlowRate});

                index++;
            }

            editing = false;
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE || e.getType() == TableModelEvent.INSERT ) {
            if (!editing) {
                double diameter;
                double maxFlowRate;
                double minFlowRate;

                diameter = Double.parseDouble((String) tableModel.getValueAt(e.getFirstRow(),DIAMETER));
                maxFlowRate = Double.parseDouble((String) tableModel.getValueAt(e.getFirstRow(),MAX_FLOW_RATE));
                minFlowRate = Double.parseDouble((String) tableModel.getValueAt(e.getFirstRow(),MIN_FLOW_RATE));

                String name = (String) tableModel.getValueAt(e.getFirstRow(),NAME);
                String subDevice = (String) tableModel.getValueAt(e.getFirstRow(),SUB_PUMP);
                String port = (String) tableModel.getValueAt(e.getFirstRow(),PORT);

                deviceManager.updateReferenceRate(e.getFirstRow(),new double[] {diameter,maxFlowRate,minFlowRate});

                prefs.putDouble(CAL+DIAMETER+name+subDevice+port,diameter);
                prefs.putDouble(CAL+MAX_FLOW_RATE+name+subDevice+port,maxFlowRate);
                prefs.putDouble(CAL+MIN_FLOW_RATE+name+subDevice+port,minFlowRate);

                gui.log.message("Updated calibration of " + name + ", " + subDevice + " on port " + port + " to: " +
                        diameter + ", " + maxFlowRate + ", " + minFlowRate);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(loadCalibration)) {
            // Create .nsc file chooser
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("NanoJ SeqLab Calibration file", "nsc"));
            chooser.setDialogTitle("Choose Calibration file to load");

            // Get working directory from preferences
            chooser.setCurrentDirectory(new File(prefs.get(SAVE_LOCATION, System.getProperty("user.home"))));

            // Get save location from user
            int returnVal = chooser.showOpenDialog(loadCalibration);

            // If successful, load protocol
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // Save location in preferences so it is loaded next time the software is loaded
                prefs.put(SAVE_LOCATION,chooser.getSelectedFile().getParent());

                try {
                    // Create file opener
                    FileInputStream fileIn = new FileInputStream(chooser.getSelectedFile().getAbsolutePath());
                    ObjectInputStream in = new ObjectInputStream(fileIn);

                    ArrayList<String[]> input = (ArrayList<String[]>) in.readObject();

                    editing = true;
                    int index = 0;
                    int matches = 0;
                    for (ConnectedSubDevice subDevice: deviceManager.getConnectedDevicesList()) {
                        for (String[] entry: input) {
                            if (entry[NAME].equals(subDevice.name) &&
                                    entry[SUB_PUMP].equals(subDevice.subDevice) &&
                                    entry[PORT].equals(subDevice.port)) {
                                tableModel.setValueAt(entry[DIAMETER],index,DIAMETER);
                                tableModel.setValueAt(entry[MAX_FLOW_RATE],index,MAX_FLOW_RATE);
                                tableModel.setValueAt(entry[MIN_FLOW_RATE],index,MIN_FLOW_RATE);
                                matches++;
                            }
                        }
                        index ++;
                    }
                    editing = false;

                    if (matches == 0) {
                        gui.log.message("WARNING: Loaded calibration file doesn't match any connected device.");
                    } else if (matches < index) {
                        gui.log.message("WARNING: Loaded calibration only matched a few of the connected device.");
                    } else if (matches == index) {
                        gui.log.message("Loaded calibration file.");
                    } else if (matches > index) {
                        gui.log.message("WARNING: Calibration matches more than the number of connected devices?.");
                    }

                    // Close file
                    in.close();
                    fileIn.close();

                } catch (FileNotFoundException f) {
                    gui.log.message("Error, File Not Found.");
                    f.printStackTrace();
                } catch (IOException i) {
                    gui.log.message("Error, can not read from location.");
                    i.printStackTrace();
                } catch (ClassNotFoundException c) {
                    gui.log.message("Error, File type is incorrect.");
                    c.printStackTrace();
                }
            }
        }

        else if (e.getSource().equals(saveCalibration)) {
            // Get working directory from preferences
            File dir = new File(prefs.get(SAVE_LOCATION, System.getProperty("user.home")));

            // Create .nsp file chooser
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("NanoJ SeqLab Calibration file", "nsc"));
            chooser.setDialogTitle("Choose where to save calibration file");
            chooser.setCurrentDirectory(dir);

            // Get save location from user
            int returnVal = chooser.showSaveDialog(saveCalibration);

            // If successful, save protocol
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // Save location in preferences so it is loaded next time the software is loaded
                prefs.put(SAVE_LOCATION,chooser.getSelectedFile().getParent());

                // Make sure file has only one .nsc termination
                if (!chooser.getSelectedFile().getAbsolutePath().endsWith(".nsc")) {
                    dir = new File(chooser.getSelectedFile() + ".nsc");
                } else dir = chooser.getSelectedFile();

                try {
                    // Open file streams
                    FileOutputStream fileOut = new FileOutputStream(dir);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);

                    // Create output file information
                    ArrayList<String[]> output = new ArrayList<String[]>();

                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String name = (String) tableModel.getValueAt(i,NAME);
                        String subDevice = (String) tableModel.getValueAt(i,SUB_PUMP);
                        String port = (String) tableModel.getValueAt(i,PORT);
                        String diameter = (String) tableModel.getValueAt(i,DIAMETER);
                        String max = (String) tableModel.getValueAt(i,MAX_FLOW_RATE);
                        String min = (String) tableModel.getValueAt(i,MIN_FLOW_RATE);

                        output.add(new String[]{name,subDevice,port,diameter,max,min});
                    }

                    //Write to file
                    out.writeObject(output);
                    out.close();
                    fileOut.close();

                } catch (FileNotFoundException f) {
                    gui.log.message("Error, file not found.");
                    f.printStackTrace();
                } catch (IOException i) {
                    gui.log.message("Error, can not write to target location.");
                    i.printStackTrace();
                }
                gui.log.message("Saved calibration.");
            }
        }

        else if (e.getSource().equals(resetCalibration)) {
            editing = true;

            int index = 0;

            for (ConnectedSubDevice subDevice: deviceManager.getConnectedDevicesList()) {
                String key = subDevice.name+subDevice.subDevice+subDevice.port;

                double diameter = subDevice.device.getDefaultRate()[0];
                tableModel.setValueAt("" + diameter,index,DIAMETER);
                prefs.putDouble(CAL+DIAMETER+key,diameter);

                double maxFlowRate = subDevice.device.getDefaultRate()[1];
                tableModel.setValueAt("" + maxFlowRate,index,MAX_FLOW_RATE);
                prefs.putDouble(CAL+MAX_FLOW_RATE+key,maxFlowRate);

                double minFlowRate = subDevice.device.getDefaultRate()[2];
                tableModel.setValueAt("" + minFlowRate,index,MIN_FLOW_RATE);
                prefs.putDouble(CAL+MIN_FLOW_RATE+key,minFlowRate);

                deviceManager.updateReferenceRate(index,new double[]{diameter,maxFlowRate,minFlowRate});

                index++;
            }

            editing = false;

            gui.log.message("Reset calibration of all devices to default values.");

        }

        else if (e.getSource().equals(calibrateButton)) {
            int index = deviceList.getSelectedIndex();
            if (!deviceManager.isConnected(index)){
                gui.log.message("Can't do anything until device is connected.");
                return;
            }

            if (index >= 0 && index < deviceManager.getConnectedDevicesList().size()) {
                try {
                    deviceManager.startPumping(
                            index,
                            Integer.parseInt(timeToDevice.getText()),
                            Device.Action.Infuse);
                } catch (Exception e1) {
                    gui.log.message("Error while starting the device.");
                    e1.printStackTrace();
                }
            }
        }
    }

    private class CalibrationTable extends DefaultTableModel {

        protected CalibrationTable() {
            super(new String[][]{{DeviceManager.NO_PUMP_CONNECTED,"","","",""}},
                    new String[]{"Device","Sub-Device","COM port","Ref. Diam.","Max Ref. Rate","Min Ref. Rate"});
        }

        public boolean isCellEditable(int row, int column){
            if (column > 2)
                return true;
            else return false;
        }

    }

}
