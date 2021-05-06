package at.qe.skeleton.bleclient;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattService;
import tinyb.BluetoothGattCharacteristic;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;

public class ConnectionThreadTest {

    @Test
    public void testReconnectDevice() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        Assert.assertNotNull(dice);
        ConnectionThread connectionThread = new ConnectionThread(dice);
        Assert.assertNotNull(connectionThread);

        boolean success = connectionThread.reconnectDevice(mockDevice);
        Assert.assertTrue(success);
    }

    @Test
    public void testRun() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        Assert.assertNotNull(dice);
        ConnectionThread connectionThread = new ConnectionThread(dice);
        Assert.assertNotNull(connectionThread);

        try {
            connectionThread.run();
        } catch (RuntimeException e) {}  
    }

    private BluetoothDevice utilMockDevice() {
        BluetoothDevice mockTimeFlip = Mockito.mock(BluetoothDevice.class);
        when(mockTimeFlip.getName()).thenReturn("timeflip");
        when(mockTimeFlip.getAddress()).thenReturn("A8:A8:9F:B9:28:AD");
        when(mockTimeFlip.getRSSI()).thenReturn((short) -42);

        when(mockTimeFlip.connect())
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException())
            .thenReturn(true);
        when(mockTimeFlip.getConnected())
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false)
            .thenReturn(true)
            .thenThrow(new RuntimeException());

        return mockTimeFlip;
    }
}