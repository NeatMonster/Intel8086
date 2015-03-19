package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestDec extends fr.neatmonster.intel8086.tests.Test {

    @Test
    public void test48() {
        setRegister("al", 1);
        setRegister("ah", 2);
        execute(0x48);
        assertEquals("DEC AX", 0, getRegister("al"));
        assertEquals("DEC AX", 2, getRegister("ah"));
    }

    @Test
    public void test49() {
        setRegister("cl", 1);
        setRegister("ch", 2);
        execute(0x49);
        assertEquals("DEC CX", 0, getRegister("cl"));
        assertEquals("DEC CX", 2, getRegister("ch"));
    }

    @Test
    public void test4A() {
        setRegister("dl", 1);
        setRegister("dh", 2);
        execute(0x4a);
        assertEquals("DEC DX", 0, getRegister("dl"));
        assertEquals("DEC DX", 2, getRegister("dh"));
    }

    @Test
    public void test4B() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0x4b);
        assertEquals("DEC BX", 0, getRegister("bl"));
        assertEquals("DEC BX", 2, getRegister("bh"));
    }

    @Test
    public void test4C() {
        setRegister("sp", 513);
        execute(0x4c);
        assertEquals("DEC SP", 512, getRegister("sp"));
    }

    @Test
    public void test4D() {
        setRegister("bp", 513);
        execute(0x4d);
        assertEquals("DEC BP", 512, getRegister("bp"));
    }

    @Test
    public void test4E() {
        setRegister("si", 513);
        execute(0x4e);
        assertEquals("DEC SI", 512, getRegister("si"));
    }

    @Test
    public void test4F() {
        setRegister("di", 513);
        execute(0x4f);
        assertEquals("DEC DI", 512, getRegister("di"));
    }

    @Test
    public void testFE() {
        setRegister("bl", 1);
        execute(0xfe, 0b11_001_011);
        assertEquals("DEC REG8", 0, getRegister("bl"));

        setMemory(0x42, 1);
        execute(0xfe, 0b00_001_110, 0x42, 0x00);
        assertEquals("DEC MEM8", 0, getMemory(0x42));
    }

    @Test
    public void testFF() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0xff, 0b11_001_011);
        assertEquals("DEC REG16", 0, getRegister("bl"));
        assertEquals("DEC REG16", 2, getRegister("bh"));

        setMemory(0x42, 1);
        setMemory(0x43, 2);
        execute(0xff, 0b00_001_110, 0x42, 0x00);
        assertEquals("DEC MEM16", 0, getMemory(0x42));
        assertEquals("DEC MEM16", 2, getMemory(0x43));
    }
}
