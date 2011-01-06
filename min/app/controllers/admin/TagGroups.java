package controllers.admin;

import controllers.CRUD;
import controllers.Secure;
import models.TagGroup;
import play.mvc.With;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
@With(Secure.class)
@CRUD.For(TagGroup.class)
public class TagGroups extends CRUD {
}
