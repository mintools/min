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

    public static void migrate2() {
        List<Task> tasks = Task.find("from Task t order by t.sortOrder desc").fetch();

        long i = 1;
        for (Task task : tasks) {
            // change newlines to <br>
            task.title = task.title.replaceAll("<br.{0,}?>", "\n").trim();
            task.content = task.content.replaceAll("<br.{0,}?>", "\n").trim();

            // remove all other html tags
            task.title = task.title.replaceAll("\\<.*?>","");
            task.content = task.content.replaceAll("\\<.*?>","");

            task.save();
        }
    }
}
