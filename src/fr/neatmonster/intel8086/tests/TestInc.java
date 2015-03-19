package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestInc extends fr.neatmonster.intel8086.tests.Test {

    @Test
    public void test40() {
        setRegister("al", 1);
        setRegister("ah", 2);
        execute(0x40);
        assertEquals("INC AX", 2, getRegister("al"));
        assertEquals("INC AX", 2, getRegister("ah"));
    }

    @Test
    public void test41() {
        setRegister("cl", 1);
        setRegister("ch", 2);
        execute(0x41);
        assertEquals("INC CX", 2, getRegister("cl"));
        assertEquals("INC CX", 2, getRegister("ch"));
    }

    @Test
    public void test42() {
        setRegister("dl", 1);
        setRegister("dh", 2);
        execute(0x42);
        assertEquals("INC DX", 2, getRegister("dl"));
        assertEquals("INC DX", 2, getRegister("dh"));
    }

    @Test
    public void test43() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0x43);
        assertEquals("INC BX", 2, getRegister("bl"));
        assertEquals("INC BX", 2, getRegister("bh"));
    }

    @Test
    public void test44() {
        setRegister("sp", 513);
        execute(0x44);
        assertEquals("INC SP", 514, getRegister("sp"));
    }

    @Test
    public void test45() {
        setRegister("bp", 513);
        execute(0x45);
        assertEquals("INC BP", 514, getRegister("bp"));
    }

    @Test
    public void test46() {
        setRegister("si", 513);
        execute(0x46);
        assertEquals("INC SI", 514, getRegister("si"));
    }

    @Test
    public void test47() {
        setRegister("di", 513);
        execute(0x47);
        assertEquals("INC DI", 514, getRegister("di"));
    }

    @Test
    public void testFE() {
        setRegister("bl", 1);
        execute(0xfe, 0b11_000_011);
        assertEquals("INC REG8", 2, getRegister("bl"));

        setMemory(0x42, 1);
        execute(0xfe, 0b00_000_110, 0x42, 0x00);
        assertEquals("INC MEM8", 2, getMemory(0x42));
    }

    @Test
    public void testFF() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0xff, 0b11_000_011);
        assertEquals("INC REG16", 2, getRegister("bl"));
        assertEquals("INC REG16", 2, getRegister("bh"));

        setMemory(0x42, 1);
        setMemory(0x43, 2);
        execute(0xff, 0b00_000_110, 0x42, 0x00);
        assertEquals("INC MEM16", 2, getMemory(0x42));
        assertEquals("INC MEM16", 2, getMemory(0x43));
    }
}
