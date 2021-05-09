package at.qe.skeleton.bleclient;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ValueNotificationTest {

    @Test
    public void testFillListWithValues() {
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        ValueNotification valueNotification = new ValueNotification(backendCommunicator);
        List<Integer> filledList = valueNotification.fillListWithValues(new ArrayList<>());
        Assert.assertEquals(12, filledList.size());
        for (int value : filledList) {
            Assert.assertTrue(value >= 0);
            Assert.assertTrue(value <= 11);
        }
    }

    @Test
    public void testGetMappedFacetValue() {
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        ValueNotification valueNotification = new ValueNotification(backendCommunicator);
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
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        ValueNotification valueNotification = new ValueNotification(backendCommunicator);
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
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        ValueNotification valueNotification = new ValueNotification(backendCommunicator);
        Assert.assertNotNull(valueNotification);

        Assert.assertEquals(0, valueNotification.getFacetMapping().size());
        Assert.assertEquals(12, valueNotification.getAvailableFacetValues().size());

        byte[] testData = { 0x11 };
        valueNotification.run(testData);

        Assert.assertEquals(1, valueNotification.getFacetMapping().size());
        Assert.assertEquals(11, valueNotification.getAvailableFacetValues().size());
    }
}
