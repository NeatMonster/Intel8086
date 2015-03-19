package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSub extends fr.neatmonster.intel8086.tests.Test {

    @Test
    public void test28() {
        setRegister("bl", 2);
        setRegister("bh", 1);
        execute(0x28, 0b11_111_011);
        assertEquals("SUB REG8,REG8", 1, getRegister("bl"));

        setMemory(0x42, 2);
        setRegister("bl", 1);
        execute(0x28, 0b00_011_110, 0x42, 0x00);
        assertEquals("SUB MEM8,REG8", 1, getMemory(0x42));
    }

    @Test
    public void test29() {
        setRegister("bl", 3);
        setRegister("bh", 4);
        setRegister("cl", 1);
        setRegister("ch", 2);
        execute(0x29, 0b11_001_011);
        assertEquals("SUB REG16,REG16", 2, getRegister("bl"));
        assertEquals("SUB REG16,REG16", 2, getRegister("bh"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0x29, 0b00_011_110, 0x42, 0x00);
        assertEquals("SUB MEM16,REG16", 2, getMemory(0x42));
        assertEquals("SUB MEM16,REG16", 2, getMemory(0x43));
    }

    @Test
    public void test2A() {
        setRegister("bh", 2);
        setRegister("bl", 1);
        execute(0x2a, 0b11_111_011);
        assertEquals("SUB REG8,REG8", 1, getRegister("bh"));

        setRegister("bl", 2);
        setMemory(0x42, 1);
        execute(0x2a, 0b00_011_110, 0x42, 0x00);
        assertEquals("SUB REG8,MEM8", 1, getRegister("bl"));
    }

    @Test
    public void test2B() {
        setRegister("cl", 3);
        setRegister("ch", 4);
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0x2b, 0b11_001_011);
        assertEquals("SUB REG16,REG16", 2, getRegister("cl"));
        assertEquals("SUB REG16,REG16", 2, getRegister("ch"));

        setRegister("bl", 3);
        setRegister("bh", 4);
        setMemory(0x42, 1);
        setMemory(0x43, 2);
        execute(0x2b, 0b00_011_110, 0x42, 0x00);
        assertEquals("SUB REG16,MEM16", 2, getRegister("bl"));
        assertEquals("SUB REG16,MEM16", 2, getRegister("bh"));
    }

    @Test
    public void test2C() {
        setRegister("al", 2);
        execute(0x2c, 1);
        assertEquals("SUB AL,IMMED8", 1, getRegister("al"));
    }

    @Test
    public void test2D() {
        setRegister("al", 3);
        setRegister("ah", 4);
        execute(0x2d, 1, 2);
        assertEquals("SUB AX,IMMED16", 2, getRegister("al"));
        assertEquals("SUB AX,IMMED16", 2, getRegister("ah"));
    }

    @Test
    public void test80() {
        setRegister("al", 2);
        execute(0x80, 0b11_101_000, 1);
        assertEquals("SUB REG8,IMMED8", 1, getRegister("al"));

        setMemory(0x42, 2);
        execute(0x80, 0b00_101_110, 0x42, 0x00, 1);
        assertEquals("SUB MEM8,IMMED8", 1, getMemory(0x42));
    }

    @Test
    public void test81() {
        setRegister("al", 3);
        setRegister("ah", 4);
        execute(0x81, 0b11_101_000, 1, 2);
        assertEquals("SUB REG16,IMMED16", 2, getRegister("al"));
        assertEquals("SUB REG16,IMMED16", 2, getRegister("ah"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        execute(0x81, 0b00_101_110, 0x42, 0x00, 1, 2);
        assertEquals("SUB MEM16,IMMED16", 2, getMemory(0x42));
        assertEquals("SUB MEM16,IMMED16", 2, getMemory(0x43));
    }

    @Test
    public void test82() {
        setRegister("al", 2);
        execute(0x82, 0b11_101_000, 1);
        assertEquals("SUB REG8,IMMED8", 1, getRegister("al"));

        setMemory(0x42, 2);
        execute(0x82, 0b00_101_110, 0x42, 0x00, 1);
        assertEquals("SUB MEM8,IMMED8", 1, getMemory(0x42));
    }

    @Test
    public void test83() {
        setRegister("al", 3);
        setRegister("ah", 4);
        execute(0x83, 0b11_101_000, 1);
        assertEquals("SUB REG16,IMMED8", 2, getRegister("al"));
        assertEquals("SUB REG16,IMMED8", 4, getRegister("ah"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        execute(0x83, 0b00_101_110, 0x42, 0x00, 1);
        assertEquals("SUB MEM16,IMMED8", 2, getMemory(0x42));
        assertEquals("SUB MEM16,IMMED8", 4, getMemory(0x43));
    }
}
