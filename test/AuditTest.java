import models.Task;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.junit.Test;
import play.db.jpa.JPA;
import play.test.UnitTest;

import java.util.List;

/**
 * User: soyoung
 * Date: Feb 7, 2011
 */
public class AuditTest extends UnitTest {
    @Test
    public void testQuery() {

    }
}
