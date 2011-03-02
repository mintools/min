package controllers;

import models.Requirement;
import models.Step;
import play.mvc.Controller;

/**
 * User: soyoung
 * Date: Feb 28, 2011
 */
public class Steps extends Controller {
    public static void show(Long id) throws Exception {
        Step step = Step.findById(id);

        notFoundIfNull(step);

        Requirement requirement = step.requirement;

        Requirement stepParent = step.parent;

        renderTemplate("Requirements/show.html", requirement, stepParent);
    }
}
