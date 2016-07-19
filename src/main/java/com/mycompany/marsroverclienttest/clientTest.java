/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.marsroverclienttest;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.swing.text.html.parser.Entity;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author User
 */
public class clientTest {

    static {
        Webcam.setDriver(new V4l4jDriver()); // this is important
    }

    CloseableHttpClient httpclient = HttpClients.createDefault();

    static ByteBuffer byteBuffer;

    public static void main(String[] args) throws IOException, InterruptedException {
//        HttpClient httpclient = new DefaultHttpClient();
//        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
//
        System.out.println("Creating image...");
        //WebcamPanel panel = new WebcamPanel(Webcam.getDefault());
        Webcam webcam = Webcam.getDefault();

        webcam.open();
        
        if (args.length == 0) {

            SingleImageUnit newImage[];

            for (int j = 0; j < 4; j++) {
                Thread t = new Thread(new SingleImageUnit(j, webcam));
                t.start();
            }
            Thread t1 = new Thread(new ModeCar());
            t1.start();

        }
        if (args.length == 1) {
//            for (int i = 0; i < 100; i++) {
//        int w = panel.getWidth();
//        int h = panel.getHeight();
//        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
//        File newFile = new File("image1.jpg");
//        newFile.createNewFile();
//
//        ImageIO.write(bi, "jpg", newFile);
//                webcam.getImageBytes(byteBuffer);
//                new clientTest().doPostStream(byteBuffer);
//            }

            SingleImageUnit newImage[];

            for (int j = 0; j < 4; j++) {
                Thread t = new Thread(new SingleImageUnit(j, webcam));
                t.start();
            }
            Thread t1 = new Thread(new ModeCar());
            t1.start();
            Thread t2 = new Thread(new GpioCommander());
            t2.start();
        }
        if (args.length == 2) {
            new clientTest().testGPIO();
        }
        if (args.length == 3) {
            System.out.println("Starting car...");
            Thread t1 = new Thread(new ModeCar());
            t1.start();
        }

        if (args.length == 4) {
            for (int i = 0; i < 100; i++) {
                if (webcam.isImageNew()) {
//            new clientTest().doPostString(args[0] + "," + args[1]);
                    ImageIO.write(webcam.getImage(), "jpg", new File("image1.jpg"));
                    Thread.sleep(500);
                    new clientTest().doPost();
                }
            }
        }
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

    public void doPost() throws IOException {

        HttpPost httppost = new HttpPost("http://45.55.210.200:8080/MarsRover-war/getReceiveFromRaspiInformation");
        File file = new File("image1.jpg");
        FileEntity entity = new FileEntity(file, "image/jpeg");
        httppost.setEntity(entity);
        httppost.getRequestLine();
        httpclient.execute(httppost);

    }

    public void doPostString(String valueToSend) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://45.55.210.200:8080/MarsRover-war/getDistanceTemperatureFromRaspi");

        StringEntity entity = new StringEntity(valueToSend);
        httppost.setEntity(entity);
        httppost.getRequestLine();
        httpclient.execute(httppost);
    }

    public String doGet() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet("http://45.55.210.200:8080/MarsRover-war/updateButtonServlet");
        CloseableHttpResponse response = httpclient.execute(httpget);

        System.out.println(response.getStatusLine().getStatusCode());

        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    byte[] newByte = {(byte) instream.read()};
                    String newValue = new String(newByte);
                    return newValue;
                } finally {
                    instream.close();
                }
            }
        } finally {
            response.close();
        }
        return "0";
    }

    private void doPostStream(ByteBuffer newImage) throws IOException {
        HttpPost httppost = new HttpPost("http://45.55.210.200:8080/MarsRover-war/getReceiveFromRaspiInformation");
//newImage.

        InputStreamEntity entity = new InputStreamEntity(new ByteBufferBackedInputStream(newImage));
        httppost.setEntity(entity);
        httppost.getRequestLine();
        httpclient.execute(httppost);
    }

    public void testGPIO() throws InterruptedException {
        // create gpio controller
        GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);

        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);

        System.out.println("--> GPIO state should be: ON");

        Thread.sleep(5000);

        // turn off gpio pin #01
        while (true) {
            pin.low();
            System.out.println("--> GPIO state should be: OFF");

            Thread.sleep(5000);
            pin.high();
            System.out.println("ON");
            Thread.sleep(5000);

        }
    }
}

class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;

    ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public synchronized int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get();
    }

    public synchronized int read(byte[] bytes, int off, int len) throws IOException {
        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }

}
