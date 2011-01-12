package controllers.admin;

import models.Task;
import play.mvc.Controller;

import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Jan 12, 2011
 */
public class Maintain extends Controller {
    public static void migrate1() {
        List<Task> tasks = Task.findAll();

        long largestId = tasks.size();

        for (Task task : tasks) {
            // change newlines to <br>
            task.content = task.content.replace("\n", "<br/>");

            // assign sortOrder
            if (task.sortOrder == null || task.sortOrder == 0) {
                task.sortOrder = largestId--;
            }

            task.save();
        }
    }
}
