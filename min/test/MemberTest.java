import models.Member;
import models.Task;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * User: soyoung
 * Date: Jan 10, 2011
 */
public class MemberTest extends UnitTest {
    @Before
    public void setup() {
        Fixtures.load("data.yml");
    }

    @Test
    public void testAddTaskInterest() {
        Member member = Member.findById(1l);
        Task task = Task.findById(1l);

        member.addInterest(task);

        assertSame(member.taskInterests.size(), 1);
    }

    @Test
    public void testRemoveTaskInterest() {
        Member member = Member.findById(1l);
        Task task = Task.findById(1l);

        member.removeInterest(task);

        assertSame(member.taskInterests.size(), 0);
    }
}
