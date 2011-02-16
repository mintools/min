package jobs;

import models.EmailSpec;
import models.Task;
import notifiers.Mails;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Feb 16, 2011
 */
@Every("1mn")
public class EmailJob extends Job {

    public void doJob() throws Exception {
        Logger.info("EmailJob checking for unsent email");
        List<EmailSpec> emails = EmailSpec.find("from EmailSpec e where e.sent <> true").fetch();

        for (Iterator<EmailSpec> iterator = emails.iterator(); iterator.hasNext();) {
            EmailSpec emailSpec = iterator.next();
            Task task = Task.findById(emailSpec.taskId);

            // send email
            Mails.taskChanged(task);

            emailSpec.sent = true;
            emailSpec.save();
        }
    }
}
