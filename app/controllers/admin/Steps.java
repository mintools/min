package controllers.admin;

import controllers.CRUD;
import controllers.Secure;
import models.Requirement;
import models.Step;
import play.mvc.With;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
@With(Secure.class)
@CRUD.For(Step.class)
public class Steps extends CRUD {
}
