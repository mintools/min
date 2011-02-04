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
}
