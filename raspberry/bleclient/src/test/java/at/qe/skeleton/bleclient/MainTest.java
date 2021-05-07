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

public class MainTest {
    private FindDevicesManagerTest findDevicesManagerTest = new FindDevicesManagerTest();

    @Test
    public void testFindBluetoothDevices() throws InterruptedException {
        BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
        List<BluetoothDevice> mockDevices = findDevicesManagerTest.utilMockDevices();
        when(bluetoothManager.getDevices()).thenReturn(mockDevices);

        FindDevicesManager findDevicesManager = new FindDevicesManager("TimeFlip");
        Assert.assertNotNull(findDevicesManager);

        
    }
}
