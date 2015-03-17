package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestXchg extends fr.neatmonster.intel8086.tests.Test {

    @Test
    public void test86() {
        setRegister("bl", 1);
        setRegister("cl", 2);
        execute(0x86, 0b11_011_001);
        assertEquals("XCHG REG8,REG8", 1, getRegister("cl"));
        assertEquals("XCHG REG8,REG8", 2, getRegister("bl"));

        setRegister("bl", 1);
        setMemory(0x42, 2);
        execute(0x86, 0b00_011_110, 0x42, 0x00);
        assertEquals("XCHG REG8,MEM8", 1, getMemory(0x42));
        assertEquals("XCHG REG8,MEM8", 2, getRegister("bl"));
    }

    @Test
    public void test87() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        setRegister("cl", 3);
        setRegister("ch", 4);
        execute(0x87, 0b11_011_001);
        assertEquals("XCHG REG16,REG16", 1, getRegister("cl"));
        assertEquals("XCHG REG16,REG16", 2, getRegister("ch"));
        assertEquals("XCHG REG16,REG16", 3, getRegister("bl"));
        assertEquals("XCHG REG16,REG16", 4, getRegister("bh"));

        setRegister("bl", 1);
        setRegister("bh", 2);
        setMemory(0x42, 3);
        setMemory(0x43, 4);
        execute(0x87, 0b00_011_110, 0x42, 0x00);
        assertEquals("XCHG REG16,MEM16", 1, getMemory(0x42));
        assertEquals("XCHG REG16,MEM16", 2, getMemory(0x43));
        assertEquals("XCHG REG16,MEM16", 3, getRegister("bl"));
        assertEquals("XCHG REG16,MEM16", 4, getRegister("bh"));
    }

    @Test
    public void test91() {
        setRegister("al", 1);
        setRegister("ah", 2);
        setRegister("cl", 3);
        setRegister("ch", 4);
        execute(0x91);
        assertEquals("XCHG AX,CX", 1, getRegister("cl"));
        assertEquals("XCHG AX,CX", 2, getRegister("ch"));
        assertEquals("XCHG AX,CX", 3, getRegister("al"));
        assertEquals("XCHG AX,CX", 4, getRegister("ah"));
    }

    @Test
    public void test92() {
        setRegister("al", 1);
        setRegister("ah", 2);
        setRegister("dl", 3);
        setRegister("dh", 4);
        execute(0x92);
        assertEquals("XCHG AX,DX", 1, getRegister("dl"));
        assertEquals("XCHG AX,DX", 2, getRegister("dh"));
        assertEquals("XCHG AX,DX", 3, getRegister("al"));
        assertEquals("XCHG AX,DX", 4, getRegister("ah"));
    }

    @Test
    public void test93() {
        setRegister("al", 1);
        setRegister("ah", 2);
        setRegister("bl", 3);
        setRegister("bh", 4);
        execute(0x93);
        assertEquals("XCHG AX,BX", 1, getRegister("bl"));
        assertEquals("XCHG AX,BX", 2, getRegister("bh"));
        assertEquals("XCHG AX,BX", 3, getRegister("al"));
        assertEquals("XCHG AX,BX", 4, getRegister("ah"));
    }

    @Test
    public void test94() {
        setRegister("al", 1);
        setRegister("ah", 2);
        setRegister("sp", 1027);
        execute(0x94);
        assertEquals("XCHG AX,SP", 513, getRegister("sp"));
        assertEquals("XCHG AX,SP", 3, getRegister("al"));
        assertEquals("XCHG AX,SP", 4, getRegister("ah"));
    }

    @Test
    public void test95() {
        setRegister("al", 1);
        setRegister("ah", 2);
        setRegister("bp", 1027);
        execute(0x95);
        assertEquals("XCHG AX,BP", 513, getRegister("bp"));
        assertEquals("XCHG AX,BP", 3, getRegister("al"));
        assertEquals("XCHG AX,BP", 4, getRegister("ah"));
    }

    @Test
    public void test96() {
        setRegister("al", 1);
        setRegister("ah", 2);
        setRegister("si", 1027);
        execute(0x96);
        assertEquals("XCHG AX,SI", 513, getRegister("si"));
        assertEquals("XCHG AX,SI", 3, getRegister("al"));
        assertEquals("XCHG AX,SI", 4, getRegister("ah"));
    }

    @Test
    public void test97() {
        setRegister("al", 1);
        setRegister("ah", 2);
        setRegister("di", 1027);
        execute(0x97);
        assertEquals("XCHG AX,DI", 513, getRegister("di"));
        assertEquals("XCHG AX,DI", 3, getRegister("al"));
        assertEquals("XCHG AX,DI", 4, getRegister("ah"));
    }
}
