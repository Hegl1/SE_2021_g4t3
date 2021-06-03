package at.qe.skeleton.bleclient;

import tinyb.BluetoothDevice;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import static org.mockito.Mockito.when;

public class ValueNotificationTest {

    @Test
    public void testFillListWithValues() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        ValueNotification valueNotification = new ValueNotification(dice);
        List<Integer> filledList = valueNotification.fillListWithValues(new ArrayList<>());
        Assert.assertEquals(12, filledList.size());
        for (int value : filledList) {
            Assert.assertTrue(value >= 0);
            Assert.assertTrue(value <= 11);
        }
    }

    @Test
    public void testGetMappedFacetValue() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        ValueNotification valueNotification = new ValueNotification(dice);
        Assert.assertNotNull(valueNotification);
        
        Assert.assertEquals(0, valueNotification.getFacetMapping().size());
        Assert.assertEquals(12, valueNotification.getAvailableFacetValues().size());

        int number = 14;
        int mappedFacetValue = valueNotification.getMappedFacetValue(number);

        Assert.assertEquals(1, valueNotification.getFacetMapping().size());
        Assert.assertEquals(11, valueNotification.getAvailableFacetValues().size());

        int mappedNumber = valueNotification.getFacetMapping().get(number);
        Assert.assertTrue(mappedNumber >= 0);
        Assert.assertTrue(mappedNumber <= 11);
    }

    @Test
    public void testGetMappedFacetValueNoMoreAvailableValues() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        ValueNotification valueNotification = new ValueNotification(dice);
        Assert.assertNotNull(valueNotification);
        
        Assert.assertEquals(0, valueNotification.getFacetMapping().size());
        Assert.assertEquals(12, valueNotification.getAvailableFacetValues().size());

        for (int i = 10; i <=21; i++) {
            valueNotification.getMappedFacetValue(i);
        }
        valueNotification.getMappedFacetValue(14);

        Assert.assertEquals(12, valueNotification.getFacetMapping().size());
        Assert.assertEquals(0, valueNotification.getAvailableFacetValues().size());

        int number = 42;
        int mappedNumber = valueNotification.getMappedFacetValue(number);
        Assert.assertEquals(number % 12, mappedNumber);
    }

    @Test
    public void testRun() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        ValueNotification valueNotification = new ValueNotification(dice);
        Assert.assertNotNull(valueNotification);

        Assert.assertEquals(0, valueNotification.getFacetMapping().size());
        Assert.assertEquals(12, valueNotification.getAvailableFacetValues().size());

        byte[] testData = { 0x11 };
        valueNotification.run(testData);

        Assert.assertEquals(1, valueNotification.getFacetMapping().size());
        Assert.assertEquals(11, valueNotification.getAvailableFacetValues().size());
    }

    @Test
    public void testRunFreshlyReconnected() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        ValueNotification valueNotification = new ValueNotification(dice);
        Assert.assertNotNull(valueNotification);

        Assert.assertEquals(0, valueNotification.getFacetMapping().size());
        Assert.assertEquals(12, valueNotification.getAvailableFacetValues().size());

        byte[] testData = { 0x11 };
        valueNotification.run(testData);

        Assert.assertEquals(1, valueNotification.getFacetMapping().size());
        Assert.assertEquals(11, valueNotification.getAvailableFacetValues().size());

        byte[] testData2 = { 0x12 };
        valueNotification.run(testData2);

        Assert.assertEquals(2, valueNotification.getFacetMapping().size());
        Assert.assertEquals(10, valueNotification.getAvailableFacetValues().size());

        dice.setFreshlyReconnected(true);
        valueNotification.run(testData2);

        Assert.assertEquals(1, valueNotification.getFacetMapping().size());
        Assert.assertEquals(11, valueNotification.getAvailableFacetValues().size());
        Assert.assertFalse(dice.isFreshlyReconnected());
    }

    private BluetoothDevice utilMockDevice() {
        BluetoothDevice mockTimeFlip = Mockito.mock(BluetoothDevice.class);
        when(mockTimeFlip.getName()).thenReturn("timeflip");
        when(mockTimeFlip.getAddress()).thenReturn("A8:A8:9F:B9:28:AD");
        when(mockTimeFlip.getRSSI()).thenReturn((short) -42);

        return mockTimeFlip;
    }
}
