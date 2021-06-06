package at.qe.timeguess.tests;

import at.qe.timeguess.gamelogic.Dice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DiceMappingTest {

    @Test
    public void testDefaultMapping(){
        Dice testDice = new Dice();
        Assertions.assertEquals(2, testDice.getPoints(1));
        Assertions.assertEquals(120,testDice.getDurationInSeconds(1));
        Assertions.assertEquals("Speak", testDice.getActivity(1));
    }

    @Test
    public void testCustomMapping(){
        int[] pointsMapping = new int[12];
        int[] durationMapping = new int[12];
        String[] activityMapping = new String[12];
        for (int i = 0; i < 12; ++i){
            pointsMapping[i] = 1;
            durationMapping[i] = 1;
            activityMapping[i] = "TEST";
        }

        Dice testDice = new Dice(pointsMapping, activityMapping, durationMapping);
        Assertions.assertEquals(1, testDice.getPoints(1));
        Assertions.assertEquals(1,testDice.getDurationInSeconds(1));
        Assertions.assertEquals("TEST", testDice.getActivity(1));
    }
}
