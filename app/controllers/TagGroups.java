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
        render();
    }

    public static void save(String name) {
        TagGroup group = TagGroup.find("byName", name).first();

        if (group == null) {
            group = new TagGroup();
            group.name = name;
            group.save();
        }

        Tags.index();
    }
}
