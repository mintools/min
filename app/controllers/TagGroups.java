package controllers;

import models.Tag;
import models.TagGroup;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

import java.util.List;

/**
 * User: soyoung
 * Date: Jan 5, 2011
 */
public class TagGroups extends Controller {
    public static void create() {
        TagGroup tagGroup = new TagGroup();
        render(tagGroup);
    }

    public static void save(@Valid TagGroup tagGroup) {
        if (Validation.hasErrors()) {
            renderTemplate("TagGroups/create.html", tagGroup);
        } else {
            tagGroup.save();
            Tags.index();
        }
    }
}
