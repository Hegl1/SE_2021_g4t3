package at.qe.skeleton.bleclient;

import tinyb.*;

// with help from https://github.com/intel-iot-devkit/tinyb/blob/ac6d3082d06183c860eea97f451d5a92022348e0/examples/java/Notification.java#L66
class ValueNotification implements BluetoothNotification<byte[]> {
    private BackendCommunicator backendCommunicator;

    public ValueNotification(BackendCommunicator backendCommunicator) {
        super();
        this.backendCommunicator = backendCommunicator;
    }

    public void run(byte[] data) {
        int facetValue = Byte.toUnsignedInt(data[0]);
        System.out.println("facet = " + facetValue); 
        backendCommunicator.postDicePosition(facetValue);
    }
}