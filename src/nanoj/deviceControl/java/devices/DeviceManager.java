package nanoj.deviceControl.java.devices;

import mmcorej.CMMCore;
import java.util.*;
import nanoj.deviceControl.java.devices.ConnectedSubDevicesList.DeviceNotFoundException;

public class DeviceManager extends Observable implements Observer {
    private CMMCore serialManager;
    private LinkedHashMap<String, Device> availableDevices = new LinkedHashMap<String, Device>();
    private ConnectedSubDevicesList connectedSubDevices = new ConnectedSubDevicesList();
    private boolean alive = true;
    private String status;

    private long wait = 100;  // Wait time for status checker in milliSeconds

    public static final String FAILED_TO_CONNECT = "Failed to connect to device.";
    public static final String PORT_ALREADY_CONNECTED = "Port already in use.";
    public static final String NOT_AVAILABLE = "Device not available to manager.";
    public static final String NEW_STATUS_AVAILABLE = "New status available.";
    public static final String NO_PUMP_CONNECTED = "No device connected.";
    public static final String NEW_PUMP_CONNECTED = "New device has been connected.";
    public static final String PUMP_DISCONNECTED = "A device has been disconnected.";
    public static final String NO_PUMP_AVAILABLE = "No Device available!";

    public static final String VOLUME_UNITS = "ul";
    public static final String TIME_UNITS = "sec";
    public static final String FLOW_RATE_UNITS = VOLUME_UNITS + "/" + TIME_UNITS;

    public final static DeviceManager INSTANCE = new DeviceManager();

    /*This no-argument constructor is required for the singleton design pattern.
      Remember to loadPlugins.
      loadPlugins is called after construction since the DeviceManager class might be loaded in the class path and created
      before the application (in our case MicroManager) has loaded all plugin classes
      */
    private DeviceManager() {  }

    public void loadPlugins() {
        //Looks in the class path for all classes declaring themselves as services of the type "nanoj.(...).Device"

        ServiceLoader<Device> serviceLoader = ServiceLoader.load(Device.class);
        for (Device device : serviceLoader) {
            availableDevices.put(device.getDeviceName(), device);
        }

        Device dummy = new DummyControl();
        availableDevices.put(dummy.getName(), dummy);

        Device lego = new LegoControl();
        availableDevices.put(lego.getName(), lego);
    }

    public void setCore(CMMCore core) { serialManager = core; }

    public String connect(String device, String port) throws Exception {
        if (connectedSubDevices.connectedPorts().contains(port)) return PORT_ALREADY_CONNECTED;
        if (!availableDevices.containsKey(device)) return NOT_AVAILABLE;

        Device newDevice = availableDevices.get(device).getNewInstance();
        newDevice.setCore(serialManager);
        String answer = newDevice.connectToDevice(port);

        if (answer.equals(Device.FAILED_TO_CONNECT)) {
            return Device.FAILED_TO_CONNECT;
        }

        connectedSubDevices.addDevice(newDevice);

        newDevice.addObserver(this);

        setChanged();
        notifyObservers(NEW_PUMP_CONNECTED);
        return answer;
    }

    public synchronized String startPumping(int deviceIndex, int seconds, Device.Action direction) throws Exception {
        String answer;

        ConnectedSubDevice connectedSubDevice = connectedSubDevices.getConnectedSubDevice(deviceIndex);
        Device device = connectedSubDevice.device;

        device.setCurrentSubDevice(connectedSubDevice.subDevice);
        device.startPumping(seconds,direction);

        answer = direction.toString() + " for " + seconds;

        setChanged();
        notifyObservers(NEW_STATUS_AVAILABLE);
        return answer;
    }

    public synchronized String startPumping(int deviceIndex, Syringe syringe, double flowRate,
                               double targetVolume, Device.Action action) throws Exception {
        String answer;

        ConnectedSubDevice connectedSubDevice = connectedSubDevices.getConnectedSubDevice(deviceIndex);
        Device device = connectedSubDevice.device;

        device.setUnitsOfVolume(VOLUME_UNITS);
        device.setUnitsOfTime(TIME_UNITS);
        device.setCurrentSubDevice(connectedSubDevice.subDevice);
        device.setSyringeDiameter(syringe.diameter);
        device.setFlowRate(flowRate);
        device.setTargetVolume(targetVolume);
        device.startPumping(action);

        answer = action.toString() + " " + targetVolume+" "+device.getUnitsOfVolume()+ " at " +flowRate+" "
                + device.getUnitsOfFlowRate() + " with " + syringe.getVolumeWUnits() + " syringe.";

        setChanged();
        notifyObservers(NEW_STATUS_AVAILABLE);
        return answer;
    }

    public synchronized void stopAllDevices() throws Exception {
        for (Device device: connectedSubDevices.getAllConnectedDevices())
            device.stopAllDevices();
    }

    public synchronized String stopPumping(int deviceIndex) throws Exception {
        String subDevice = connectedSubDevices.getConnectedSubDevice(deviceIndex).subDevice;
        connectedSubDevices.getConnectedSubDevice(deviceIndex).device.stopDevice(subDevice);
        setChanged();
        notifyObservers(NEW_STATUS_AVAILABLE);
        return connectedSubDevices.getConnectedSubDevice(deviceIndex).getFullName();
    }

    public synchronized void stopPumping(String deviceName, String subDevice, String port) throws Exception {
        connectedSubDevices.getConnectedSubDevice(deviceName,subDevice,port).device.stopDevice();
        setChanged();
        notifyObservers(NEW_STATUS_AVAILABLE);
    }

    public synchronized boolean disconnect(String port) throws Exception {
        boolean success = false;
        Device device = null;
        for (ConnectedSubDevice subDevice: connectedSubDevices)
            if (subDevice.port.equals(port)) {
                device = subDevice.device;
                success = subDevice.device.disconnect();
                break;
            }

        if (success)
            connectedSubDevices.removeDevice(device);

        setChanged();
        notifyObservers(PUMP_DISCONNECTED);
        return success;
    }

    public synchronized boolean disconnect(int index) throws Exception {
        boolean success = connectedSubDevices.getConnectedSubDevice(index).device.disconnect();
        String name = connectedSubDevices.getConnectedSubDevice(index).name;
        String port = connectedSubDevices.getConnectedSubDevice(index).port;
        if (success)
            connectedSubDevices.removeDevice(name,port);

        setChanged();
        notifyObservers(PUMP_DISCONNECTED);
        return success;
    }

    public String[] getAvailableDevicesList() {
        if (availableDevices.size() > 0) {
            return availableDevices.keySet().toArray(new String[availableDevices.size()]);
        }
        else return new String[]{NO_PUMP_AVAILABLE};
    }

    public ConnectedSubDevicesList getConnectedDevicesList() {
        return connectedSubDevices;
    }

    public Device getDeviceOnPort(String port) {
        Device device = null;
        for (ConnectedSubDevice subDevice: connectedSubDevices)
            if (subDevice.port.equals(port)) {
                device = subDevice.device;
                break;
            }

        return device;
    }

    public String getDeviceNameOnPort(String port) {
        String device = "Not found";
        for (ConnectedSubDevice subDevice: connectedSubDevices)
            if (subDevice.port.equals(port)) {
                device = subDevice.name;
                break;
            }

        return device;
    }

    public synchronized boolean isConnected(int deviceIndex) {
        return connectedSubDevices != null &&
                deviceIndex < connectedSubDevices.size() &&
                connectedSubDevices.getConnectedSubDevice(deviceIndex).device.isConnected();
    }

    public synchronized boolean isConnected(String deviceName, String port, boolean fullName) {
        boolean isIt = false;
        if (fullName) {
            for (String device: getAllFullNames())
                if (device.equals(deviceName)) {
                    isIt = true;
                    break;
                }
        }
        else isIt = connectedSubDevices != null && connectedSubDevices.getDevice(deviceName, port).isConnected();

        return isIt;
    }

    public double[] getMaxMin(int deviceIndex, double diameter) throws DeviceNotFoundException {
        String name = connectedSubDevices.getDeviceName(deviceIndex);
        String port = connectedSubDevices.getDevicePort(deviceIndex);
        String subDevice = connectedSubDevices.getDeviceSubName(deviceIndex);
        return connectedSubDevices.getDevice(name,port).getMaxMin(subDevice,diameter);
    }

    private synchronized void getStatusFromDevice(int deviceIndex) throws Exception {
        if (isConnected(deviceIndex))
            status = connectedSubDevices.getConnectedSubDevice(deviceIndex).device.getStatus();
    }

    public String getStatus() {
        return status;
    }

    public boolean anyDevicesConnected() {
        return connectedSubDevices.anyDevicesConnected();
    }

    public boolean noDevicesConnected() {
        return connectedSubDevices.noDevicesConnected();
    }

    public String[] getAllFullNames() {
        return connectedSubDevices.getAllFullNames();
    }

    public void updateReferenceRate(int deviceIndex, double[] newRate) {
        connectedSubDevices.getConnectedSubDevice(deviceIndex).device.updateReferenceRate(
                connectedSubDevices.getConnectedSubDevice(deviceIndex).subDevice,
                newRate);
    }

    @Override
    public void update(Observable o, Object arg) {
        status = (String) arg;
    }
}
