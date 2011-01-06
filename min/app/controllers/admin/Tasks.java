package controllers.admin;

import controllers.CRUD;
import controllers.Secure;
import models.Task;
import play.mvc.With;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
@With(Secure.class)
@CRUD.For(Task.class)
public class Tasks extends CRUD {
}
