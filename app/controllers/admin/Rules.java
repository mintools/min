package controllers.admin;

import controllers.CRUD;
import controllers.Secure;
import models.Member;
import models.Rule;
import play.mvc.With;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
@With(Secure.class)
@CRUD.For(Rule.class)
public class Rules extends CRUD {
}
