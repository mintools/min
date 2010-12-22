import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() throws Exception {

        // setup database
        Fixtures.deleteAll();
        Fixtures.load("../data/test.yml");
    }
}
