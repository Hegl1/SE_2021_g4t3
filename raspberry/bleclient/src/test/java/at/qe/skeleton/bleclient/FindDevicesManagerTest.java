package at.qe.skeleton.bleclient;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

public class FindDevicesManagerTest {

    @Test
    public void testFindDevices() throws InterruptedException {
        BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
        List<BluetoothDevice> mockDevices = utilMockDevices();
        when(bluetoothManager.getDevices()).thenReturn(mockDevices);

        FindDevicesManager findDevicesManager = new FindDevicesManager("TimeFlip");
        Assert.assertNotNull(findDevicesManager);

        boolean findDevicesSuccess = findDevicesManager.findDevices(bluetoothManager);
        Assert.assertTrue(findDevicesSuccess);

        Set<BluetoothDevice> devices = findDevicesManager.getFoundDevices();
        Assert.assertNotNull(devices);
        Assert.assertEquals(2, devices.size());

        for (BluetoothDevice device : devices) {
            if (device.getName().equals("timeflip")) {
                Assert.assertEquals("A8:A8:9F:B9:28:AD", device.getAddress());
                Assert.assertEquals(-20, device.getRSSI());
            } else if (device.getName().equals("TimeFlip2")) {
                Assert.assertEquals("B8:A8:9F:B9:28:AD", device.getAddress());
                Assert.assertEquals(-44, device.getRSSI());
            } else {
                Assert.fail("Unexpected device " + device.getName());
            }
        }
    }

    @Test
    public void testFindDevicesNoSearchDeviceNameFilter() throws InterruptedException {
        BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
        List<BluetoothDevice> mockDevices = utilMockDevices();
        when(bluetoothManager.getDevices()).thenReturn(mockDevices);

        FindDevicesManager findDevicesManager = new FindDevicesManager("");
        Assert.assertNotNull(findDevicesManager);
        Assert.assertTrue(findDevicesManager.findDevices(bluetoothManager));
        Set<BluetoothDevice> devices = findDevicesManager.getFoundDevices();
        Assert.assertNotNull(devices);
        Assert.assertEquals(3, devices.size());
    }

    @Test
    public void testFindDevicesNull() throws InterruptedException {
        BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
        when(bluetoothManager.getDevices()).thenReturn(null);

        FindDevicesManager findDevicesManager = new FindDevicesManager("TimeFlip");
        Assert.assertNotNull(findDevicesManager);
        Assert.assertFalse(findDevicesManager.findDevices(bluetoothManager));
    }

    @Test
    public void testFindDevicesFoundAfterSomeTime() throws InterruptedException {
        BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
        List<BluetoothDevice> mockDevices = utilMockDevices();
        when(bluetoothManager.getDevices())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(mockDevices);

        FindDevicesManager findDevicesManager = new FindDevicesManager("TimeFlip");
        Assert.assertNotNull(findDevicesManager);
        Assert.assertTrue(findDevicesManager.findDevices(bluetoothManager));
    }

    @Test
    public void testFindDevicesFailed() throws InterruptedException {
        BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
        List<BluetoothDevice> mockDevices = utilMockDevices();
        when(bluetoothManager.getDevices()).thenReturn(mockDevices);

        FindDevicesManager findDevicesManager1 = new FindDevicesManager("headphones1");
        Assert.assertNotNull(findDevicesManager1);
        Assert.assertFalse(findDevicesManager1.findDevices(bluetoothManager));
        Assert.assertNotNull(findDevicesManager1.getFoundDevices());

        FindDevicesManager findDevicesManager2 = new FindDevicesManager("timeflip3");
        Assert.assertNotNull(findDevicesManager2);
        Assert.assertFalse(findDevicesManager2.findDevices(bluetoothManager));
        Assert.assertNotNull(findDevicesManager2.getFoundDevices());

        FindDevicesManager findDevicesManager3 = new FindDevicesManager("timeflip4");
        Assert.assertNotNull(findDevicesManager3);
        Assert.assertFalse(findDevicesManager3.findDevices(bluetoothManager));
        Assert.assertNotNull(findDevicesManager3.getFoundDevices());
    }

    private List<BluetoothDevice> utilMockDevices() {
        List<BluetoothDevice> mockDevices = new ArrayList<>();

        BluetoothDevice mockTimeFlip1 = Mockito.mock(BluetoothDevice.class);
        when(mockTimeFlip1.getName()).thenReturn("timeflip");
        when(mockTimeFlip1.getAddress()).thenReturn("A8:A8:9F:B9:28:AD");
        when(mockTimeFlip1.getRSSI()).thenReturn((short) -20);
        mockDevices.add(mockTimeFlip1);

        BluetoothDevice mockTimeFlip2 = Mockito.mock(BluetoothDevice.class);
        when(mockTimeFlip2.getName()).thenReturn("TimeFlip2");
        when(mockTimeFlip2.getAddress()).thenReturn("B8:A8:9F:B9:28:AD");
        when(mockTimeFlip2.getRSSI()).thenReturn((short) -44);
        mockDevices.add(mockTimeFlip2);

        BluetoothDevice mockTimeFlip3 = Mockito.mock(BluetoothDevice.class);
        when(mockTimeFlip3.getName()).thenReturn("timeflip3");
        when(mockTimeFlip3.getAddress()).thenReturn("C8:A8:9F:B9:28:AD");
        when(mockTimeFlip3.getRSSI()).thenReturn((short) -91);
        mockDevices.add(mockTimeFlip3);

        BluetoothDevice mockTimeFlip4 = Mockito.mock(BluetoothDevice.class);
        when(mockTimeFlip4.getName()).thenReturn("timeflip4");
        when(mockTimeFlip4.getAddress()).thenReturn("D8:A8:9F:B9:28:AD");
        when(mockTimeFlip4.getRSSI()).thenReturn((short) 0);
        mockDevices.add(mockTimeFlip4);

        BluetoothDevice mockHeadphones = Mockito.mock(BluetoothDevice.class);
        when(mockHeadphones.getName()).thenReturn("headphones");
        when(mockHeadphones.getAddress()).thenReturn("D8:A8:9F:B9:28:AD");
        when(mockHeadphones.getRSSI()).thenReturn((short) -35);
        mockDevices.add(mockHeadphones);

        return mockDevices;
    }
}
