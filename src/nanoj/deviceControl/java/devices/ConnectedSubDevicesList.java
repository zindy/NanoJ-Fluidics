package nanoj.deviceControl.java.devices;

import java.util.ArrayList;
import java.util.Iterator;

public class ConnectedSubDevicesList implements Iterable<ConnectedSubDevice> {
    private ArrayList<ConnectedSubDevice> list = new ArrayList<ConnectedSubDevice>();
    private ArrayList<Device> connectedDevices = new ArrayList<Device>();

    private static final String OUT_OF_BOUNDS = "SubDevice index doesn't exist in list.";

    public boolean notPresent(int index) {
        return index >= list.size() || index < 0;
    }

    public void addDevice(Device device) {
        for (String subDevice: device.subDevices)
            list.add(new ConnectedSubDevice(device, subDevice));
        connectedDevices.add(device);
    }

    public void removeDevice(String deviceName,String port) {
        ArrayList<ConnectedSubDevice> devicesToRemove = new ArrayList<ConnectedSubDevice>();
        for (ConnectedSubDevice device: list)
            if (device.name.equals(deviceName) && device.port.equals(port))
                devicesToRemove.add(device);

        for (ConnectedSubDevice deviceToRemove: devicesToRemove)
            list.remove(deviceToRemove);

        for (ConnectedSubDevice device: list)
            if (device.name.equals(deviceName) && device.port.equals(port)) {
                connectedDevices.remove(device.device);
                break;
            }
    }

    public void removeDevice(Device device) {
        ArrayList<ConnectedSubDevice> found = new ArrayList<ConnectedSubDevice>();
        for (ConnectedSubDevice subDevice: list)
            if (subDevice.device.equals(device))
                found.add(subDevice);

        for (ConnectedSubDevice foundDevice : found)
            list.remove(foundDevice);

        connectedDevices.remove(found.get(0).device);
    }

    public ConnectedSubDevice getConnectedSubDevice(String name, String subDevice, String port) throws DeviceNotFoundException {
        ConnectedSubDevice result = null;
        for (ConnectedSubDevice connectedSubDevice : list)
            if (connectedSubDevice.name.equals(name) &&
                connectedSubDevice.subDevice.equals(subDevice) &&
                connectedSubDevice.port.equals(port))
            {
                result = connectedSubDevice;
                break;
            }

        if (result == null)
            throw new DeviceNotFoundException();
        else return result;
    }

    public Device getDevice(String deviceName, String port) throws DeviceNotFoundException {
        Device device = null;
        for (ConnectedSubDevice subDevice: list)
            if (subDevice.name.equals(deviceName) && subDevice.port.equals(port)) {
                device = subDevice.device;
                break;
            }

        if (device == null)
            throw new DeviceNotFoundException();
        else return device;
    }

    public boolean anyDevicesConnected() {
        return !list.isEmpty();
    }

    public boolean noDevicesConnected() {
        return list.isEmpty();
    }

    public ConnectedSubDevice getConnectedSubDevice(int index) throws IndexOutOfBoundsException {
        if (notPresent(index))
            throw new IndexOutOfBoundsException(OUT_OF_BOUNDS);
        return list.get(index);
    }

    public String getFullName(int index) throws IndexOutOfBoundsException {
        if (!notPresent(index))
            throw new IndexOutOfBoundsException(OUT_OF_BOUNDS);
        return list.get(index).getFullName();
    }

    protected ArrayList<Device> getAllConnectedDevices() {
        return connectedDevices;
    }

    public String[] getAllFullNames() {
        String array[] = new String[list.size()];
        if (list.isEmpty())
            return new String[]{};
        else {
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i).getFullName();
            }
            return array;
        }
    }

    public String getDeviceName(int index) throws IndexOutOfBoundsException {
        if (notPresent(index))
            throw new IndexOutOfBoundsException(OUT_OF_BOUNDS);
        return list.get(index).name;
    }

    public String getDeviceSubName(int index) throws IndexOutOfBoundsException {
        if (notPresent(index))
            throw new IndexOutOfBoundsException(OUT_OF_BOUNDS);
        return list.get(index).subDevice;
    }

    public String getDevicePort(int index) throws IndexOutOfBoundsException {
        if (notPresent(index))
            throw new IndexOutOfBoundsException(OUT_OF_BOUNDS);
        return list.get(index).port;
    }

    public int size() {
        return list.size();
    }

    public ArrayList<String> connectedPorts() {
        ArrayList<String> ports = new ArrayList<String>();
        for (ConnectedSubDevice device: list) {
            if (!ports.contains(device.port))
                ports.add(device.port);
        }
        return ports;
    }

    @Override
    public Iterator<ConnectedSubDevice> iterator() {
        return list.iterator();
    }

    public static class DeviceNotFoundException extends RuntimeException {
        public DeviceNotFoundException() { super();}
        public DeviceNotFoundException(String message) { super(message);}
    }
}
