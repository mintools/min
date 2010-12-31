package controllers;

import models.Member;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
public class Members extends Controller {
    public static void show(Long memberId) {
        Member member = Member.findById(memberId);
        render(member);
    }
}
