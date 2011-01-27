package notifiers;

import models.Task;
import play.mvc.Mailer;

public class Mails extends Mailer {

    public static void taskAssigned(Task task) {
        setSubject("Task %s has been assigned to you", task.id);
        // todo: should be assigned to instead of owner
        addRecipient(task.owner.email);
        setFrom("SOY <soyabean@gmail.com>");
        send(task);
    }
}