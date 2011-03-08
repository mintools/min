import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

/**
 * Job that is executed on statup
 */
@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() throws Exception {
        // add some dummy data if using in-memory database
        if ("mem".equalsIgnoreCase(Play.configuration.getProperty("db"))) {
            Fixtures.deleteAll();
            Fixtures.load("../data/test.yml");
        }
    }
}