/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.marsroverclienttest;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author User
 */
public class SingleImageUnit implements Runnable {

    int threadNumber;
    Webcam webcam;

    public SingleImageUnit(int threadNumber, Webcam webcam) {
        this.threadNumber = threadNumber;
        this.webcam = webcam;
    }
    CloseableHttpClient httpclient;

    @Override
    public void run() {

        while (true) {
            httpclient = HttpClients.createDefault();

            if (!webcam.isOpen()) {

            } else if (webcam.isImageNew()) {
                try {
                    
                    BufferedImage newImage = getScaledImage(webcam.getImage(), 320, 240);
                    File newFile = null;
                    ImageIO.write(newImage, "jpg", newFile);
                    //ImageIO.write(webcam.getImage(), "jpg", new File("image" + threadNumber + ".jpg"));
                    doPost(newFile);
                } catch (IOException ex) {
                    Logger.getLogger(SingleImageUnit.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                httpclient.close();
            } catch (IOException ex) {
                Logger.getLogger(SingleImageUnit.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void doPost(File sendFile) throws IOException {

        HttpPost httppost = new HttpPost("http://45.55.210.200:8080/MarsRover-war/getReceiveFromRaspiInformation");
        File file = sendFile;
        FileEntity entity = new FileEntity(file, "image/jpeg");
        httppost.setEntity(entity);
        httppost.getRequestLine();
        httpclient.execute(httppost);
    }

    private BufferedImage getScaledImage(BufferedImage src, int w, int h) {
        int finalw = w;
        int finalh = h;
        double factor = 1.0d;
        if (src.getWidth() > src.getHeight()) {
            factor = ((double) src.getHeight() / (double) src.getWidth());
            finalh = (int) (finalw * factor);
        } else {
            factor = ((double) src.getWidth() / (double) src.getHeight());
            finalw = (int) (finalh * factor);
        }

        BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, finalw, finalh, null);
        g2.dispose();
        return resizedImg;
    }

}
