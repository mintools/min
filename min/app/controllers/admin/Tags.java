package controllers.admin;

import controllers.CRUD;
import controllers.Secure;
import models.Tag;
import play.mvc.With;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
@With(Secure.class)
@CRUD.For(Tag.class)
public class Tags extends CRUD {
}
