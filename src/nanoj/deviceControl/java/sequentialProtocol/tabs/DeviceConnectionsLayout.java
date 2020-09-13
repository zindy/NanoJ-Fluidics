package nanoj.deviceControl.java.sequentialProtocol.tabs;

import nanoj.deviceControl.java.sequentialProtocol.GUI;

import javax.swing.*;
import java.awt.*;

class DeviceConnectionsLayout extends GroupLayout {
    DeviceConnectionsLayout(Container host) {
        super(host);
        DeviceConnections panel = (DeviceConnections) host;

        setAutoCreateGaps(true);
        setAutoCreateContainerGaps(true);

        setVerticalGroup(
                createSequentialGroup()
                        .addGroup(createParallelGroup()
                                .addComponent(panel.deviceListLabel)
                                .addComponent(panel.availableDevicesList, GroupLayout.PREFERRED_SIZE, GUI.rowHeight, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(createParallelGroup()
                                .addComponent(panel.connectLabel)
                                .addComponent(panel.portsList, GroupLayout.PREFERRED_SIZE, GUI.rowHeight, GroupLayout.PREFERRED_SIZE)
                                .addComponent(panel.connectButton)
                                .addComponent(panel.disconnectButton)
                        )
                        .addGroup(createParallelGroup().addComponent(panel.connectedDevicesLabel))
                        .addGroup(createParallelGroup().addComponent(panel.connectedDevicesListPane))
                        .addGroup(createParallelGroup().addComponent(panel.version))
        );

        setHorizontalGroup(
                createParallelGroup()
                        .addGroup(createSequentialGroup()
                                .addGroup(createParallelGroup()
                                        .addComponent(panel.deviceListLabel, GroupLayout.Alignment.TRAILING)
                                        .addComponent(panel.connectLabel, GroupLayout.Alignment.TRAILING)
                                )
                                .addGroup(createParallelGroup()
                                        .addComponent(panel.availableDevicesList, GroupLayout.PREFERRED_SIZE, GUI.sizeSecondColumn, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(panel.portsList, GroupLayout.PREFERRED_SIZE, GUI.sizeSecondColumn, GroupLayout.PREFERRED_SIZE)
                                )
                                .addGroup(createSequentialGroup()
                                        .addComponent(panel.connectButton)
                                        .addComponent(panel.disconnectButton)
                                )
                        )
                        .addGroup(createParallelGroup().addComponent(panel.connectedDevicesLabel))
                        .addGroup(createParallelGroup().addComponent(panel.connectedDevicesListPane))
                        .addGroup(createParallelGroup().addComponent(panel.version))
        );
    }
}
