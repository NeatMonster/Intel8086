package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestCmp extends fr.neatmonster.intel8086.tests.Test {

    @Test
    public void test38() {
        setRegister("bl", 2);
        setRegister("bh", 1);
        execute(0x38, 0b11_111_011);
        assertEquals("CMP REG8,REG8", 2, getRegister("bl"));

        setMemory(0x42, 2);
        setRegister("bl", 1);
        execute(0x38, 0b00_011_110, 0x42, 0x00);
        assertEquals("CMP MEM8,REG8", 2, getMemory(0x42));
    }

    @Test
    public void test39() {
        setRegister("bl", 3);
        setRegister("bh", 4);
        setRegister("cl", 1);
        setRegister("ch", 2);
        execute(0x39, 0b11_001_011);
        assertEquals("CMP REG16,REG16", 3, getRegister("bl"));
        assertEquals("CMP REG16,REG16", 4, getRegister("bh"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0x39, 0b00_011_110, 0x42, 0x00);
        assertEquals("CMP MEM16,REG16", 3, getMemory(0x42));
        assertEquals("CMP MEM16,REG16", 4, getMemory(0x43));
    }

    @Test
    public void test3A() {
        setRegister("bh", 2);
        setRegister("bl", 1);
        execute(0x3a, 0b11_111_011);
        assertEquals("CMP REG8,REG8", 2, getRegister("bh"));

        setRegister("bl", 2);
        setMemory(0x42, 1);
        execute(0x3a, 0b00_011_110, 0x42, 0x00);
        assertEquals("CMP REG8,MEM8", 2, getRegister("bl"));
    }

    @Test
    public void test3B() {
        setRegister("cl", 3);
        setRegister("ch", 4);
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0x3b, 0b11_001_011);
        assertEquals("CMP REG16,REG16", 3, getRegister("cl"));
        assertEquals("CMP REG16,REG16", 4, getRegister("ch"));

        setRegister("bl", 3);
        setRegister("bh", 4);
        setMemory(0x42, 1);
        setMemory(0x43, 2);
        execute(0x3b, 0b00_011_110, 0x42, 0x00);
        assertEquals("CMP REG16,MEM16", 3, getRegister("bl"));
        assertEquals("CMP REG16,MEM16", 4, getRegister("bh"));
    }

    @Test
    public void test3C() {
        setRegister("al", 2);
        execute(0x3c, 1);
        assertEquals("CMP AL,IMMED8", 2, getRegister("al"));
    }

    @Test
    public void test3D() {
        setRegister("al", 3);
        setRegister("ah", 4);
        execute(0x3d, 1, 2);
        assertEquals("CMP AX,IMMED16", 3, getRegister("al"));
        assertEquals("CMP AX,IMMED16", 4, getRegister("ah"));
    }

    @Test
    public void test80() {
        setRegister("al", 2);
        execute(0x80, 0b11_111_000, 1);
        assertEquals("CMP REG8,IMMED8", 2, getRegister("al"));

        setMemory(0x42, 2);
        execute(0x80, 0b00_111_110, 0x42, 0x00, 1);
        assertEquals("CMP MEM8,IMMED8", 2, getMemory(0x42));
    }

    @Test
    public void test81() {
        setRegister("al", 3);
        setRegister("ah", 4);
        execute(0x81, 0b11_111_000, 1, 2);
        assertEquals("CMP REG16,IMMED16", 3, getRegister("al"));
        assertEquals("CMP REG16,IMMED16", 4, getRegister("ah"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        execute(0x81, 0b00_111_110, 0x42, 0x00, 1, 2);
        assertEquals("CMP MEM16,IMMED16", 3, getMemory(0x42));
        assertEquals("CMP MEM16,IMMED16", 4, getMemory(0x43));
    }

    @Test
    public void test82() {
        setRegister("al", 2);
        execute(0x82, 0b11_111_000, 1);
        assertEquals("CMP REG8,IMMED8", 2, getRegister("al"));

        setMemory(0x42, 2);
        execute(0x82, 0b00_111_110, 0x42, 0x00, 1);
        assertEquals("CMP MEM8,IMMED8", 2, getMemory(0x42));
    }

    @Test
    public void test83() {
        setRegister("al", 3);
        setRegister("ah", 4);
        execute(0x83, 0b11_111_000, 1);
        assertEquals("CMP REG16,IMMED8", 3, getRegister("al"));
        assertEquals("CMP REG16,IMMED8", 4, getRegister("ah"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        execute(0x83, 0b00_111_110, 0x42, 0x00, 1);
        assertEquals("CMP MEM16,IMMED8", 3, getMemory(0x42));
        assertEquals("CMP MEM16,IMMED8", 4, getMemory(0x43));
    }
}
