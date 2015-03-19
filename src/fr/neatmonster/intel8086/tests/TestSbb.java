package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSbb extends fr.neatmonster.intel8086.tests.Test {

    private void setBorrow() {
        int flags = getRegister("flags");
        flags |= 1;
        setRegister("flags", flags);
    }

    @Test
    public void test18() {
        setRegister("bl", 2);
        setRegister("bh", 1);
        setBorrow();
        execute(0x18, 0b11_111_011);
        assertEquals("SBB REG8,REG8", 0, getRegister("bl"));

        setMemory(0x42, 2);
        setRegister("bl", 1);
        setBorrow();
        execute(0x18, 0b00_011_110, 0x42, 0x00);
        assertEquals("SBB MEM8,REG8", 0, getMemory(0x42));
    }

    @Test
    public void test19() {
        setRegister("bl", 3);
        setRegister("bh", 4);
        setRegister("cl", 1);
        setRegister("ch", 2);
        setBorrow();
        execute(0x19, 0b11_001_011);
        assertEquals("SBB", 1, getRegister("bl"));
        assertEquals("SBB REG16,REG16", 2, getRegister("bh"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        setRegister("bl", 1);
        setRegister("bh", 2);
        setBorrow();
        execute(0x019, 0b00_011_110, 0x42, 0x00);
        assertEquals("SBB MEM16,REG16", 1, getMemory(0x42));
        assertEquals("SBB MEM16,REG16", 2, getMemory(0x43));
    }

    @Test
    public void test1A() {
        setRegister("bh", 2);
        setRegister("bl", 1);
        setBorrow();
        execute(0x1a, 0b11_111_011);
        assertEquals("SBB REG8,REG8", 0, getRegister("bh"));

        setRegister("bl", 2);
        setMemory(0x42, 1);
        setBorrow();
        execute(0x1a, 0b00_011_110, 0x42, 0x00);
        assertEquals("SBB REG8,MEM8", 0, getRegister("bl"));
    }

    @Test
    public void test1B() {
        setRegister("cl", 3);
        setRegister("ch", 4);
        setRegister("bl", 1);
        setRegister("bh", 2);
        setBorrow();
        execute(0x1b, 0b11_001_011);
        assertEquals("SBB REG16,REG16", 1, getRegister("cl"));
        assertEquals("SBB REG16,REG16", 2, getRegister("ch"));

        setRegister("bl", 3);
        setRegister("bh", 4);
        setMemory(0x42, 1);
        setMemory(0x43, 2);
        setBorrow();
        execute(0x1b, 0b00_011_110, 0x42, 0x00);
        assertEquals("SBB REG16,MEM16", 1, getRegister("bl"));
        assertEquals("SBB REG16,MEM16", 2, getRegister("bh"));
    }

    @Test
    public void test1C() {
        setRegister("al", 2);
        setBorrow();
        execute(0x1c, 1);
        assertEquals("SBB AL,IMMED8", 0, getRegister("al"));
    }

    @Test
    public void test1D() {
        setRegister("al", 3);
        setRegister("ah", 4);
        setBorrow();
        execute(0x1d, 1, 2);
        assertEquals("SBB AX,IMMED16", 1, getRegister("al"));
        assertEquals("SBB AX,IMMED16", 2, getRegister("ah"));
    }

    @Test
    public void test80() {
        setRegister("al", 2);
        setBorrow();
        execute(0x80, 0b11_011_000, 1);
        assertEquals("SBB REG8,IMMED8", 0, getRegister("al"));

        setMemory(0x42, 2);
        setBorrow();
        execute(0x80, 0b00_011_110, 0x42, 0x00, 1);
        assertEquals("SBB MEM8,IMMED8", 0, getMemory(0x42));
    }

    @Test
    public void test81() {
        setRegister("al", 3);
        setRegister("ah", 4);
        setBorrow();
        execute(0x81, 0b11_011_000, 1, 2);
        assertEquals("SBB REG16,IMMED16", 1, getRegister("al"));
        assertEquals("SBB REG16,IMMED16", 2, getRegister("ah"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        setBorrow();
        execute(0x81, 0b00_011_110, 0x42, 0x00, 1, 2);
        assertEquals("SBB MEM16,IMMED16", 1, getMemory(0x42));
        assertEquals("SBB MEM16,IMMED16", 2, getMemory(0x43));
    }

    @Test
    public void test82() {
        setRegister("al", 2);
        setBorrow();
        execute(0x82, 0b11_011_000, 1);
        assertEquals("SBB REG8,IMMED8", 0, getRegister("al"));

        setMemory(0x42, 2);
        setBorrow();
        execute(0x82, 0b00_011_110, 0x42, 0x00, 1);
        assertEquals("SBB MEM8,IMMED8", 0, getMemory(0x42));
    }

    @Test
    public void test83() {
        setRegister("al", 3);
        setRegister("ah", 4);
        setBorrow();
        execute(0x83, 0b11_011_000, 1);
        assertEquals("SBB REG16,IMMED8", 1, getRegister("al"));
        assertEquals("SBB REG16,IMMED8", 4, getRegister("ah"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        setBorrow();
        execute(0x83, 0b00_011_110, 0x42, 0x00, 1);
        assertEquals("SBB MEM16,IMMED8", 1, getMemory(0x42));
        assertEquals("SBB MEM16,IMMED8", 4, getMemory(0x43));
    }
}
