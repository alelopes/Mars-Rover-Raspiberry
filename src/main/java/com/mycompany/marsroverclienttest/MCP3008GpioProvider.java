/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.marsroverclienttest;

import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import java.io.IOException;

public class MCP3008GpioProvider extends GpioProviderBase implements GpioProvider {

    public static final String NAME = "com.pi4j.gpio.extension.mcp.MCP3008GpioProvider";
    public static final String DESCRIPTION = "MCP3008 GPIO Provider";
    public static final int INVALID_VALUE = -1;

    private final SpiDevice spiDevice;

    /**
     * Create new instance of this MCP3008 provider.
     *
     * @param spiChannel spi channel the MCP3008 is connected to
     * @throws IOException if an error occurs during initialization of the
     * SpiDevice
     */
    public MCP3008GpioProvider(SpiChannel spiChannel) throws IOException {
        this.spiDevice = SpiFactory.getInstance(spiChannel);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double getValue(Pin pin) {
        // do not return, only let parent handle whether this pin is OK
        super.getValue(pin);
        return isInitiated() ? readAnalog(toCommand((short) pin.getAddress())) : INVALID_VALUE;
    }

    private short toCommand(short channel) {
        short command = (short) ((channel + 8) << 4);
        return command;
    }

    private boolean isInitiated() {
        return spiDevice != null;
    }

    private int readAnalog(short channelCommand) {
        // send 3 bytes command - "1", channel command and some extra byte 0
        // http://hertaville.com/2013/07/24/interfacing-an-spi-adc-mcp3008-chip-to-the-raspberry-pi-using-c
        short[] data = new short[]{1, channelCommand, 0};
        short[] result;
        try {
            result = spiDevice.write(data);
        } catch (IOException e) {
            return INVALID_VALUE;
        }

        // now take 8 and 9 bit from second byte (& with 0b11 and shift) and the whole last byte to form the value
        int analogValue = ((result[1] & 3) << 8) + result[2];
        return analogValue;
    }
}
