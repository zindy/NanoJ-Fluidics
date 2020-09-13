package nanoj.deviceControl.java.devices;

public interface DeviceInterface {

    Device getNewInstance() throws Exception;
    String connectToDevice(String comPort) throws Exception;
    boolean disconnect() throws Exception;
    void setSyringeDiameter(double diameter) throws Exception;
    void setFlowRate (double flowRate) throws Exception; //Flowrate is always equal to the defaults in Device Abstract class
    void setTargetVolume(double target) throws Exception;
    void startPumping(Device.Action direction) throws Exception; //direction = true, infuse; else, withdraw
    void startPumping(int seconds, Device.Action direction) throws Exception;
    void stopDevice() throws Exception; //Stops either a single device device or (on hub devices) the current device.
    void stopDevice(String subDevice) throws Exception; //Stops a specific device on hub-devices.
    void stopAllDevices() throws Exception; // For device hub type devices, this method stops all devices.
    double[] getMaxMin(String subDevice,double diameter); // Get Maximum and minimum flow rates for a given syringe diameter
    String[] getSubDevices();
    String sendCommand(String command) throws Exception;
    String getStatus() throws Exception;
    String getDeviceName();
    boolean isConnected();

}
