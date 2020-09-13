package nanoj.deviceControl.java.devices;

public class ConnectedSubDevice {
    public final Device device;
    public final String name;
    public final String port;
    public final String subDevice;

    public ConnectedSubDevice(Device device, String subDevice) {
        this.device = device;
        this.name = device.name;
        this.port = device.portName;
        this.subDevice = subDevice;
    }

    public String getFullName() {
        return subDevice + ", " + port;
    }

    public String[] asConnectionArray() {
        return new String[]{name,subDevice,port};
    }

    public String[] asCalibrationArray() {
        return new String[]{name,subDevice,port,
                ""+device.referenceRates.get(subDevice)[0],
                ""+device.referenceRates.get(subDevice)[1],
                ""+device.referenceRates.get(subDevice)[2]};
    }
}
