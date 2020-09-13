package nanoj.deviceControl.java.sequentialProtocol.tabs;

import nanoj.deviceControl.java.sequentialProtocol.GUI;

import javax.swing.*;
import java.awt.*;

public class DeviceCalibrationLayout extends GroupLayout {

    public DeviceCalibrationLayout(Container host) {
        super(host);

        DeviceCalibration panel = (DeviceCalibration) host;

        setAutoCreateGaps(true);
        setAutoCreateContainerGaps(true);

        setVerticalGroup(
                createSequentialGroup()
                        .addGroup(createParallelGroup()
                                .addComponent(panel.loadCalibration)
                                .addComponent(panel.saveCalibration)
                                .addComponent(panel.resetCalibration)
                        )
                        .addGroup(createParallelGroup()
                                .addComponent(panel.deviceList)
                                .addComponent(panel.timeToDeviceLabel)
                                .addComponent(panel.timeToDevice)
                                .addComponent(panel.calibrateButton)
                                .addComponent(panel.stopButton)
                        )
                        .addGroup(createParallelGroup().addComponent(panel.tableLabel))
                        .addGroup(createParallelGroup().addComponent(panel.tableScrollPane))
        );

        setHorizontalGroup(
                createParallelGroup()
                        .addGroup(createSequentialGroup()
                                .addComponent(panel.loadCalibration)
                                .addComponent(panel.saveCalibration)
                                .addComponent(panel.resetCalibration)
                        )
                        .addGroup(createSequentialGroup()
                                .addComponent(panel.deviceList)
                                .addComponent(panel.timeToDeviceLabel)
                                .addComponent(panel.timeToDevice)
                                .addComponent(panel.calibrateButton)
                                .addComponent(panel.stopButton)
                        )
                        .addGroup(createParallelGroup().addComponent(panel.tableLabel))
                        .addGroup(createParallelGroup().addComponent(panel.tableScrollPane))
        );
    }
}
