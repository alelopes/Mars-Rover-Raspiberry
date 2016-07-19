/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.marsroverclienttest;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author User
 */
public class ModeCar implements Runnable {

    private Pin pin02 = RaspiPin.GPIO_02; // Pin #02, clock
    private Pin pin07 = RaspiPin.GPIO_03; // Pin #07, data in.  MISO: Master In Slave Out
    private Pin pin15 = RaspiPin.GPIO_01; // Pin #15, data out. MOSI: Master Out Slave In
    private Pin pin10 = RaspiPin.GPIO_04; // Pin #10, Chip Select
    GpioController gpio;

    // provision gpio pin #01 as an output pin and turn on
    GpioPinDigitalOutput pin02_;
    GpioPinDigitalOutput pin07_;
    GpioPinDigitalOutput pin15_;
    GpioPinDigitalOutput pin10_;

    // set shutdown state for this pin
    @Override
    public void run() {
        gpio = GpioFactory.getInstance();
        pin02_ = gpio.provisionDigitalOutputPin(pin02, "MyLED1", PinState.HIGH);
        pin07_ = gpio.provisionDigitalOutputPin(pin07, "MyLED2", PinState.HIGH);
        pin15_ = gpio.provisionDigitalOutputPin(pin15, "MyLED3", PinState.HIGH);
        pin10_ = gpio.provisionDigitalOutputPin(pin10, "MyLED4", PinState.HIGH);

        pin02_.setShutdownOptions(true, PinState.LOW);
        pin07_.setShutdownOptions(true, PinState.LOW);
        pin15_.setShutdownOptions(true, PinState.LOW);
        pin10_.setShutdownOptions(true, PinState.LOW);
        pin02_.low();
        pin07_.low();
        pin15_.low();
        pin10_.low();

        while (true) {
            try {
                Thread.sleep(1000);

                String value = doGet();
                int valueInt;
                if (value.equals("W")) {
                    System.out.println("W");
                    valueInt = 0;
                    foward();
                } else if (value.equals("A")) {
                    System.out.println("A");
                    valueInt = 1;
                    back();
                } else if (value.equals("S")) {
                    System.out.println("S");
                    valueInt = 2;
                    right();
                } else if (value.equals("D")) {
                    System.out.println("D");
                    valueInt = 3;
                    left();
                } else {
                    System.out.println("0");
                    valueInt = 4;
                    stop();
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(ModeCar.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ModeCar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void stop() {
        //System.out.println("STOP");
        pin02_.low();
        pin07_.low();
        pin15_.low();
        pin10_.low();

    }

    public void right() {
        //System.out.println("RIGHT");
        pin02_.low();
        pin07_.high();
        pin15_.high();
        pin10_.low();
    }

    public void left() {
        //System.out.println("LEFT");
        pin02_.high();
        pin07_.low();
        pin15_.low();
        pin10_.high();
    }

    public void foward() {
        //System.out.println("FOWARD");
        pin02_.low();
        pin07_.high();
        pin15_.low();
        pin10_.high();
    }

    public void back() {
        //System.out.println("BACK");
        pin02_.high();
        pin07_.low();
        pin15_.high();
        pin10_.low();
    }

    public String doGet() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet("http://45.55.210.200:8080/MarsRover-war/updateButtonServlet");
        CloseableHttpResponse response = httpclient.execute(httpget);

        //System.out.println(response.getStatusLine().getStatusCode());
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    byte[] newByte = {(byte) instream.read()};
                    String value = new String(newByte);
                    //System.out.println("Inside:" + value);
                    return value;
                } finally {
                    instream.close();
                }
            }
        } finally {
            response.close();
        }
        return "0";
    }
}
