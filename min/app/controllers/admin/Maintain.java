package controllers.admin;

import models.Task;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Jan 12, 2011
 */
public class Maintain extends Controller {
    public static void migrate1() {
        List<Task> tasks = Task.find("from Task t order by t.sortOrder desc").fetch();

        long i = 1;
        for (Task task : tasks) {
            // change newlines to <br>
            task.content = task.content.replace("\n", "<br/>");

            // assign sortOrder

            task.sortOrder = i++;
            task.save();
        }
    }
}
