package controllers;

import com.mortennobel.imagescaling.ResampleOp;
import models.Attachment;
import models.Member;
import models.Task;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * User: soyoung
 * Date: Dec 31, 2010
 */
public class Members extends Controller {
    private static final String FILES_DIR = Play.configuration.getProperty("avatarStorage.location");

    public static void show(Long id) {
        Member member = Member.findById(id);
        render(member);
    }

    public static void create() {
        Member member = new Member();
        render(member);
    }

    public static void edit(Long id) {
        Member member = Member.findById(id);
        renderTemplate("Members/create.html", member);
    }

    public static void save(@Valid Member member, File avatar) throws Exception {
        if (Validation.hasErrors()) {
            renderTemplate("Members/create.html", member);
        } else {            
            if (avatar != null) {
                // make thumbnail
                File dir = new File(FILES_DIR);

                String filename = UUID.randomUUID().toString() + ".png";

                // read the image
                BufferedImage srcImage = ImageIO.read(avatar);

                // scale to thumbnail
                ResampleOp resampleOp = new ResampleOp(50, 50);
                BufferedImage icon = resampleOp.filter(srcImage, null);

                // write the thumbnail
                ImageIO.write(icon, "png", new File(dir, filename));

                member.avatarFilename = filename;
            }

            member.save();

            renderTemplate("Members/show.html", member);
        }
    }
}
