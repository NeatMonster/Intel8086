package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestAdd extends fr.neatmonster.intel8086.tests.Test {

    @Test
    public void test00() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0x00, 0b11_111_011);
        assertEquals("ADD REG8,REG8", 3, getRegister("bl"));

        setRegister("bl", 1);
        setMemory(0x42, 2);
        execute(0x00, 0b00_011_110, 0x42, 0x00);
        assertEquals("ADD MEM8,REG8", 3, getMemory(0x42));
    }

    @Test
    public void test01() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        setRegister("cl", 3);
        setRegister("ch", 4);
        execute(0x01, 0b11_001_011);
        assertEquals("ADD REG16,REG16", 4, getRegister("bl"));
        assertEquals("ADD REG16,REG16", 6, getRegister("bh"));

        setRegister("bl", 1);
        setRegister("bh", 2);
        setMemory(0x42, 3);
        setMemory(0x43, 4);
        execute(0x01, 0b00_011_110, 0x42, 0x00);
        assertEquals("ADD MEM16,REG16", 4, getMemory(0x42));
        assertEquals("ADD MEM16,REG16", 6, getMemory(0x43));
    }

    @Test
    public void test02() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(0x02, 0b11_111_011);
        assertEquals("ADD REG8,REG8", 3, getRegister("bh"));

        setRegister("bl", 1);
        setMemory(0x42, 2);
        execute(0x02, 0b00_011_110, 0x42, 0x00);
        assertEquals("ADD REG8,MEM8", 3, getRegister("bl"));
    }

    @Test
    public void test03() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        setRegister("cl", 3);
        setRegister("ch", 4);
        execute(0x03, 0b11_001_011);
        assertEquals("ADD REG16,REG16", 4, getRegister("cl"));
        assertEquals("ADD REG16,REG16", 6, getRegister("ch"));

        setRegister("bl", 1);
        setRegister("bh", 2);
        setMemory(0x42, 3);
        setMemory(0x43, 4);
        execute(0x03, 0b00_011_110, 0x42, 0x00);
        assertEquals("ADD REG16,MEM16", 4, getRegister("bl"));
        assertEquals("ADD REG16,MEM16", 6, getRegister("bh"));
    }

    @Test
    public void test04() {
        setRegister("al", 1);
        execute(0x04, 2);
        assertEquals("ADD AL,IMMED8", 3, getRegister("al"));
    }

    @Test
    public void test05() {
        setRegister("al", 1);
        setRegister("ah", 2);
        execute(0x05, 3, 4);
        assertEquals("ADD AX,IMMED16", 4, getRegister("al"));
        assertEquals("ADD AX,IMMED16", 6, getRegister("ah"));
    }

    @Test
    public void test80() {
        setRegister("al", 1);
        execute(0x80, 0b11_000_000, 2);
        assertEquals("ADD REG8,IMMED8", 3, getRegister("al"));

        setMemory(0x42, 1);
        execute(0x80, 0b00_000_110, 0x42, 0x00, 2);
        assertEquals("ADD MEM8,IMMED8", 3, getMemory(0x42));
    }

    @Test
    public void test81() {
        setRegister("al", 1);
        setRegister("ah", 2);
        execute(0x81, 0b11_000_000, 3, 4);
        assertEquals("ADD REG16,IMMED16", 4, getRegister("al"));
        assertEquals("ADD REG16,IMMED16", 6, getRegister("ah"));

        setMemory(0x42, 1);
        setMemory(0x43, 2);
        execute(0x81, 0b00_000_110, 0x42, 0x00, 3, 4);
        assertEquals("ADD MEM16,IMMED16", 4, getMemory(0x42));
        assertEquals("ADD MEM16,IMMED16", 6, getMemory(0x43));
    }

    @Test
    public void test82() {
        setRegister("al", 1);
        execute(0x82, 0b11_000_000, 2);
        assertEquals("ADD REG8,IMMED8", 3, getRegister("al"));

        setMemory(0x42, 1);
        execute(0x82, 0b00_000_110, 0x42, 0x00, 2);
        assertEquals("ADD MEM8,IMMED8", 3, getMemory(0x42));
    }

    @Test
    public void test83() {
        setRegister("al", 1);
        setRegister("ah", 2);
        execute(0x83, 0b11_000_000, 3);
        assertEquals("ADD REG16,IMMED8", 4, getRegister("al"));
        assertEquals("ADD REG16,IMMED8", 2, getRegister("ah"));

        setMemory(0x42, 1);
        setMemory(0x43, 2);
        execute(0x83, 0b00_000_110, 0x42, 0x00, 3);
        assertEquals("ADD MEM16,IMMED8", 4, getMemory(0x42));
        assertEquals("ADD MEM16,IMMED8", 2, getMemory(0x43));
    }
}
