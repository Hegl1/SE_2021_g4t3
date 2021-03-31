package at.qe.skeleton.bleclient;

import tinyb.*;

// with help from https://github.com/intel-iot-devkit/tinyb/blob/ac6d3082d06183c860eea97f451d5a92022348e0/examples/java/Notification.java#L66
class ValueNotification implements BluetoothNotification<byte[]> {

    public void run(byte[] data) {
            System.out.print("data = {");
            for (byte b : data) {
                System.out.print(String.format("%02x,", b));
            }
            System.out.println("}");
    }
}