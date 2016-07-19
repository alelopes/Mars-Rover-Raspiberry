/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.marsroverclienttest;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author User
 */
public class GpioCommander implements Runnable {
    // create gpio controller

    public void testGPIO() throws InterruptedException {
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);

        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);

        // turn off gpio pin #01
        while (true) {
            pin.low();
            Thread.sleep(5000);
            pin.high();
            Thread.sleep(5000);
        }
    }
    long time2;

    public void testSensor() throws InterruptedException {
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "MyLED", PinState.HIGH);

        final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_06, PinPullResistance.PULL_DOWN);

        // create and register gpio pin listener
        myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
                time2 = System.currentTimeMillis();
            }
        });
        pin.low();
        Thread.sleep(100);
        long time1 = System.currentTimeMillis();
        pin.high();

        while (true) {
            Thread.sleep(10);
        }
    }

    private Pin spiClk = RaspiPin.GPIO_14; // Pin #18, clock
    private Pin spiMiso = RaspiPin.GPIO_13; // Pin #23, data in.  MISO: Master In Slave Out
    private Pin spiMosi = RaspiPin.GPIO_12; // Pin #24, data out. MOSI: Master Out Slave In
    private Pin spiCs = RaspiPin.GPIO_06; // Pin #25, Chip Select

    private int ADC_CHANNEL = 0; // Between 0 and 7, 8 channels on the MCP3008

    private GpioPinDigitalInput misoInput = null;
    private GpioPinDigitalOutput mosiOutput = null;
    private GpioPinDigitalOutput clockOutput = null;
    private GpioPinDigitalOutput chipSelectOutput = null;

    public void testMCP3008() throws IOException {
        String command = "./mcp3008";

        Process proc = Runtime.getRuntime().exec(command);
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
        line = reader.readLine();
        System.out.print(line + "\n");
    }
//        GpioController gpio = GpioFactory.getInstance();
//        mosiOutput = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
//        clockOutput = gpio.provisionDigitalOutputPin(spiClk, "CLK", PinState.LOW);
//        chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS", PinState.LOW);
//        misoInput = gpio.provisionDigitalInputPin(spiMiso, "MISO");
//        int lastRead = 0;
//        int tolerance = 5;
//
//        boolean trimPotChanged = false;
//        int adc = readAdc();
//        System.out.println("ADC VALUE READ: " + adc);

//    private int readAdc() {
//        chipSelectOutput.high();
//
//        clockOutput.low();
//        chipSelectOutput.low();
//
//        int adccommand = ADC_CHANNEL;
//        adccommand |= 0x18; // 0x18: 00011000
//        adccommand <<= 3;
//        // Send 5 bits: 8 - 3. 8 input channels on the MCP3008.
//        for (int i = 0; i < 5; i++) //
//        {
//            if ((adccommand & 0x80) != 0x0) // 0x80 = 0&10000000
//            {
//                mosiOutput.high();
//            } else {
//                mosiOutput.low();
//            }
//            adccommand <<= 1;
//            clockOutput.high();
//            clockOutput.low();
//        }
//
//        int adcOut = 0;
//        for (int i = 0; i < 12; i++) // Read in one empty bit, one null bit and 10 ADC bits
//        {
//            clockOutput.high();
//            clockOutput.low();
//            adcOut <<= 1;
//
//            if (misoInput.isHigh()) {
//                //      System.out.println("    " + misoInput.getName() + " is high (i:" + i + ")");
//                // Shift one bit on the adcOut
//                adcOut |= 0x1;
//            }
//            if (DISPLAY_DIGIT) {
//                System.out.println("ADCOUT: 0x" + Integer.toString(adcOut, 16).toUpperCase()
//                        + ", 0&" + Integer.toString(adcOut, 2).toUpperCase());
//            }
//        }
//        chipSelectOutput.high();
//
//        adcOut >>= 1; // Drop first bit
//        return adcOut;
//    }
    @Override
    public void run() {

        String command = "sudo ./mcp3008";

        Process proc;
        while (true) {
            try {
                proc = Runtime.getRuntime().exec(command);

                BufferedReader reader
                        = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                String line = "";
                line = reader.readLine();
                System.out.print(line + "\n");
                line = "0," + line;
                doPostString(line);
                Thread.sleep(10000);

            } catch (IOException ex) {
                Logger.getLogger(GpioCommander.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(GpioCommander.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void doPostString(String valueToSend) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://45.55.210.200:8080/MarsRover-war/getDistanceTemperatureFromRaspi");

        StringEntity entity = new StringEntity(valueToSend);
        httppost.setEntity(entity);
        httppost.getRequestLine();
        httpclient.execute(httppost);
    }
}
