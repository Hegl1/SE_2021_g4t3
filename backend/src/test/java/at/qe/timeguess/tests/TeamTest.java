package at.qe.timeguess.tests;

import at.qe.timeguess.gamelogic.Team;
import at.qe.timeguess.model.User;
import at.qe.timeguess.model.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TeamTest {

    @Test
    public void testJoinTeam(){
        Team t = new Team();
        User u = new User();
        t.joinTeam(u);
        Assertions.assertEquals(1, t.getPlayers().size());
    }

    @Test
    public void testLeaveTeam(){
        Team t = new Team();
        User u = new User();
        t.joinTeam(u);
        Assertions.assertEquals(1, t.getPlayers().size());
        t.leaveTeam(u);
        Assertions.assertEquals(0, t.getPlayers().size());
    }

    @Test
    public void testIncrementScore(){
        Team t = new Team();
        t.incrementScore(3);
        Assertions.assertEquals(3, t.getScore());
    }

    @Test
    public void testDecrementScore(){
        Team t = new Team();
        t.incrementScore(6);
        t.decrementScore(3);
        Assertions.assertEquals(3, t.getScore());
        t.decrementScore(6);
        Assertions.assertEquals(0, t.getScore());
    }

    @Test
    public void testIncrementRightWrongExpressions(){
        Team t = new Team();
        t.incrementCorrectExpressions();
        Assertions.assertEquals(1,t.getNumberOfCorrectExpressions());
        t.incrementWrongExpressions();
        Assertions.assertEquals(1, t.getNumberOfWrongExpressions());
    }

    @Test
    public void testIncrementCurrentPlayer(){
        Team t = new Team();
        User u1 = new User("usr1", "passwd", UserRole.GAMEMANAGER);
        User u2 = new User("usr2", "passwd", UserRole.PLAYER);
        t.joinTeam(u1);
        t.joinTeam(u2);
        User currentUser = t.getCurrentPlayer();
        t.incrementCurrentPlayer();
        Assertions.assertNotEquals(currentUser, t.getCurrentPlayer());
    }

    @Test
    public void testIsInTeam(){
        Team t = new Team();
        User u1 = new User("usr1", "passwd", UserRole.GAMEMANAGER);
        User u2 = new User("usr2", "passwd", UserRole.PLAYER);
        t.joinTeam(u1);
        Assertions.assertTrue(t.isInTeam(u1));
        Assertions.assertFalse(t.isInTeam(u2));
    }

}
