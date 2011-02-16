package notifiers;

import models.Member;
import models.Task;
import play.Play;
import play.mvc.Mailer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Mails extends Mailer {

    public static void taskChanged(Task task) throws Exception {

        String host = Play.configuration.getProperty("hostname");
        List<Member> interestedParties = task.getInteresteds();

        if (!interestedParties.isEmpty()) {
            setSubject("Min: %s", task.title);
    
            for (Iterator<Member> i = interestedParties.iterator(); i.hasNext();) {
                Member member = i.next();

                if (member.email == null) {
                    throw new Exception("no email found for " + member.username);
                }
                addRecipient(member.email);
            }

            Map changeMap = task.retrieveRevisions();

            if (task.owner.email == null) {
                throw new Exception("no email found for " + task.owner.username);
            }
            setFrom(task.owner.email);

            send(task, changeMap, host);
        }
    }
}