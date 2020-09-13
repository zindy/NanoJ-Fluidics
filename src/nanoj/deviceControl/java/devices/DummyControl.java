package nanoj.deviceControl.java.devices;

import nanoj.deviceControl.java.sequentialProtocol.GUI;

public final class DummyControl extends Device implements DeviceInterface {
    private GUI.Log log;
    private boolean demo = false;

    public DummyControl() {
        log = GUI.INSTANCE.log;

        if (demo) {
            name = "NanoJ Lego Control Hub ";
            subDevices = new String[]{
                    "S1,P1",
                    "S1,P2",
                    "S1,P3",
                    "S1,P4",
                    "S2,P1",
                    "S2,P2",
                    "S2,P3",
                    "S2,P4",
                    "S3,P1"};
        } else {
            name = "Virtual device";
            subDevices = new String[]{"Sub 1", "Sub 2", "Sub 3"};
        }

        for (String subDevice: subDevices)
            referenceRates.put(subDevice,new double[]{4,1,0.25});
    }

    private String currentSubDevice() {
        return portName + ", " + getCurrentSubDevice() + ": ";
    }

    void message(String text) {
        log.message("Virtual device says: " + text);
    }

    @Override
    public String connectToDevice(String comPort) throws Exception {
        connected = true;
        portName = comPort;
        core.loadDevice(portName, "SerialManager", comPort);
        setStatus("Connected to " + portName);
        return status;
    }

    @Override
    public void setSyringeDiameter(double diameter) throws Exception{
        setStatus(currentSubDevice() + "Set Syringe Diameter to " + diameter);
    }

    @Override
    public void setFlowRate(double flowRate) throws Exception {
        setStatus(currentSubDevice() + "Set Flow Rate to " + flowRate + " ul/s");
        message(status);
    }

    @Override
    public void setTargetVolume(double target) throws Exception{
        setStatus(currentSubDevice() + "Set Syringe Volume to " + target + " ul");
        message(status);
    }

    @Override
    public void startPumping(Action direction) throws Exception {
        String action;
        if(direction.equals(Action.Infuse)) action = "push.";
        else action = "withdraw.";
        message("Reference diameter for sub device is: " + referenceRates.get(currentSubDevice)[0]);
        message("Reference max rate for sub device is: " + referenceRates.get(currentSubDevice)[1]);
        message("Reference min rate for sub device is: " + referenceRates.get(currentSubDevice)[2]);
        setStatus(currentSubDevice() + " told to " + direction);
        message(status);
    }

    @Override
    public void startPumping(int seconds, Action direction) throws Exception {
        String action;
        if(direction.equals(Action.Infuse)) action = "push";
        else action = "withdraw";
        message("Reference diameter for sub device is: " + referenceRates.get(currentSubDevice)[0]);
        message("Reference max rate for sub device is: " + referenceRates.get(currentSubDevice)[1]);
        message("Reference min rate for sub device is: " + referenceRates.get(currentSubDevice)[2]);
        setStatus(currentSubDevice() + " told to " + action + " for " + seconds + " seconds.");
        message(status);
    }

    @Override
    public void stopAllDevices() throws Exception {
        setStatus("Stopped ALL the devices.");
        message(status);
    }

    @Override
    public void stopDevice() {
        setStatus("Stopped current device: " + currentSubDevice);
        message(status);
    }

    @Override
    public void stopDevice(String subDevice) throws Exception {
        setStatus("Stopped device: " + subDevice);
        message(status);
    }

    @Override
    public String sendCommand(String command) throws Exception { return null; }

    @Override
    public String getStatus() { return status; }

}
