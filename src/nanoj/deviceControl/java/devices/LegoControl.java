package nanoj.deviceControl.java.devices;

import mmcorej.StrVector;
import nanoj.deviceControl.java.sequentialProtocol.GUI;

public class LegoControl extends Device {
    private GUI.Log log = GUI.Log.INSTANCE;
    private String comPort;
    private static final String SHIELD = "S";
    private static final String PUMP = "P";

    public LegoControl() {
        name = "NanoJ Lego Control Hub";
        timeOut = 2000;

        double max = 2.3;
        defaultRate = new double[]{4.699,max,max*0.25};
    }

    @Override
    public String connectToDevice(String comPort) throws Exception{
        String answer;

        this.comPort = comPort;
        //First, unload any potential leftovers of failed connections
        portName = comPort;
        StrVector devices = core.getLoadedDevices();
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).equals(portName)) {
                core.unloadDevice(portName);
            }
        }

        /*  code assuming the core object doesn't need to reconnect every time
        try {
            core.loadDevice(portName, "SerialManager", comPort);
            core.setProperty(portName, "AnswerTimeout", "" + timeOut);
            core.setProperty(portName, "BaudRate", "57600");
            core.setProperty(portName, "StopBits", "2");
            core.setProperty(portName, "Parity", "None");
            core.initializeDevice(portName);

            sendCommand("");
            answer = sendCommand("p");
            out(sendCommand("a").getBytes().length + "");
            out(sendCommand("p").getBytes().length + "");
        } catch (Exception e) {
            e.printStackTrace();
            return FAILED_TO_CONNECT;
        }
        */

        try {
            answer = sendCommand("p");
        } catch (Exception e) {
            e.printStackTrace();
            return FAILED_TO_CONNECT;
        }

        connected = true;
        int[] sDevices = new int[] {
                Integer.parseInt(answer.substring(0, answer.indexOf("."))),
                Integer.parseInt(answer.substring(answer.indexOf(".")+1/*,answer.length()-1*/))
        };
        subDevices = new String[sDevices[0]*sDevices[1]];
        int a = 0;
        for (int s = 0; s < sDevices[0]; s++)
            for (int p = 0; p < sDevices[1]; p++) {
                subDevices[a] = SHIELD + (s+1) + "," + PUMP + (p+1);
                a++;
            }

        for (String subDevice : subDevices)
            referenceRates.put(subDevice,defaultRate);

        return answer;
    }

    @Override
    public void setFlowRate(double givenFlowRate) throws Exception {
        flowRate = givenFlowRate;
        double[] maxMin = getMaxMin(currentSubDevice,syringeDiameter);

        if (flowRate > maxMin[0]) flowRate = maxMin[0];
        else if(flowRate < maxMin[1]) flowRate = maxMin[1];

        // The devices only accept values in the range of 0-255, so this calculates a ratio
        // and converts it to the proper range.

        int commandFlowInt = new Double((flowRate/maxMin[0])*255).intValue();

        String commandFlow = "";
        if (commandFlowInt < 10) {
            commandFlow = "00" + commandFlowInt;
        }
        else if (commandFlowInt < 100) {
            commandFlow = "0" + commandFlowInt;
        }
        else if (commandFlowInt < 256) {
            commandFlow = "" + commandFlowInt;
        }
        /* Device serial command: sxynnn = for device xy set speed nnn*/
        sendCommand("s" + parseSubDevice(currentSubDevice) + commandFlow);
    }

    @Override
    public void setTargetVolume(double target) { targetVolume = target; } //Target volume should be given in ul

    @Override
    public synchronized void startPumping(Action direction) throws Exception {
        // Target volume is in ul and flowrate in ul/sec but the arduino code
        // wants a duration in seconds. So we have to convert.

        int duration = (int) Math.round(targetVolume/flowRate);
        startPumping(duration,direction);
    }

    @Override
    public void startPumping(int duration, Action direction) throws Exception {
        int action;

        String target = "";

        if(duration < 10) {
            target = "0000" + duration;
        }
        else if(duration < 100) {
            target = "000" + duration;
        }
        else if(duration < 1000) {
            target = "00" + duration;
        }
        else if(duration < 10000) {
            target = "0" + duration;
        }
        if(direction.equals(Action.Infuse)) action = 1;
        else action = 2;

        /*
        rxydttttt - Start device xy in direction d for ttttt seconds
        d = 1 is forward, d = 2 is backwards
        */
        sendCommand("r" + parseSubDevice(currentSubDevice) + action + target);
    }

    @Override
    public void stopAllDevices() throws Exception {
        sendCommand("a");
    }

    @Override
    public synchronized void stopDevice() throws Exception {
        sendCommand("a" + parseSubDevice(currentSubDevice));
    }

    @Override
    public synchronized void stopDevice(String subDevice) throws Exception {
        sendCommand("a" + parseSubDevice(subDevice));
    }

    //TODO: Create a status getter that automatically parses the lego style reply.
    @Override
    public synchronized String getStatus() throws Exception {
        return "Device alive."/*sendCommand("g")*/;
    }

    @Override
    public String sendCommand(String command) throws Exception {
        /* code assuming the core object doesn't need to reconnect every time
        core.setSerialPortCommand(portName, command + ".", "\r");
        String answer = core.getSerialPortAnswer(portName, "\n");
        //CharVector answer =  core.readFromSerialPort(portName);
        out("Command sent to Lego device: " + command + ", heard: " + answer);
        return "" + answer;
        */

        //First, unload any potential leftovers of failed connections
        portName = comPort;
        StrVector devices = core.getLoadedDevices();
        for(int i = 0; i<devices.size(); i++) {
            if (devices.get(i).equals(portName)) {
                core.unloadDevice(portName);
            }
        }
        String result;
        try {

            core.loadDevice(portName, "SerialManager", comPort);
            core.setProperty(portName, "AnswerTimeout", "" + timeOut);
            core.setProperty(portName, "BaudRate", "57600");
            core.setProperty(portName, "StopBits", "2");
            core.setProperty(portName, "Parity", "None");
            core.initializeDevice(portName);

            core.setSerialPortCommand(portName, "", "\r");
            core.getSerialPortAnswer(portName, "\n");

            log.message("Command sent to Lego device: " + command);
            core.setSerialPortCommand(portName, command + ".", "\r");
            result = core.getSerialPortAnswer(portName, "\n");
        } catch (Exception e) {
            throw e;
        }
        result = result.substring(0, result.length()-1);

        String prefix = "Response from device: ";
        log.message(prefix + result);
        setStatus(prefix + result);

        return result;

    }

    private String parseSubDevice(String subDeviceString){
        int indexOfDevice = subDeviceString.indexOf(PUMP) + PUMP.length();
        String shield = Integer.parseInt(subDeviceString.substring(SHIELD.length(),SHIELD.length()+1)) + "";
        String device = Integer.parseInt(subDeviceString.substring(indexOfDevice,indexOfDevice+1)) + "";
        return shield + device;
    }

}
