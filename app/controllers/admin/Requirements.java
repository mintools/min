package controllers.admin;

import controllers.CRUD;
import controllers.Secure;
import models.Requirement;
import models.Tag;
import play.mvc.With;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
@With(Secure.class)
@CRUD.For(Requirement.class)
public class Requirements extends CRUD {
}
