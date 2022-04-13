import Systems.File;
import Webhook.WebhookListener;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args){
        File.setUp();
        WebhookListener.main(args);
    }
}

