package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestPush extends fr.neatmonster.intel8086.tests.Test {

    @Test
    public void test06() {
        setRegister("es", 513);
        execute(new int[] { 0x06 });
        final int sp = getRegister("sp");
        assertEquals("PUSH ES", 1, getMemory(sp));
        assertEquals("PUSH ES", 2, getMemory(sp + 1));
    }

    @Test
    public void test0E() {
        setRegister("cs", 1027);
        execute(new int[] { 0x0e });
        final int sp = getRegister("sp");
        assertEquals("PUSH CS", 3, getMemory(sp));
        assertEquals("PUSH CS", 4, getMemory(sp + 1));
    }

    @Test
    public void test16() {
        setRegister("ss", 1541);
        execute(new int[] { 0x16 });
        final int sp = getRegister("sp");
        assertEquals("PUSH SS", 5, getMemory(sp));
        assertEquals("PUSH SS", 6, getMemory(sp + 1));
    }

    @Test
    public void test1E() {
        setRegister("ds", 2055);
        execute(new int[] { 0x1e });
        final int sp = getRegister("sp");
        assertEquals("PUSH DS", 7, getMemory(sp));
        assertEquals("PUSH DS", 8, getMemory(sp + 1));
    }

    @Test
    public void test50() {
        setRegister("al", 1);
        setRegister("ah", 2);
        execute(new int[] { 0x50 });
        final int sp = getRegister("sp");
        assertEquals("PUSH AX", 1, getMemory(sp));
        assertEquals("PUSH AX", 2, getMemory(sp + 1));
    }

    @Test
    public void test51() {
        setRegister("cl", 3);
        setRegister("ch", 4);
        execute(new int[] { 0x51 });
        final int sp = getRegister("sp");
        assertEquals("PUSH CX", 3, getMemory(sp));
        assertEquals("PUSH CX", 4, getMemory(sp + 1));
    }

    @Test
    public void test52() {
        setRegister("dl", 5);
        setRegister("dh", 6);
        execute(new int[] { 0x52 });
        final int sp = getRegister("sp");
        assertEquals("PUSH DX", 5, getMemory(sp));
        assertEquals("PUSH DX", 6, getMemory(sp + 1));
    }

    @Test
    public void test53() {
        setRegister("bl", 7);
        setRegister("bh", 8);
        execute(new int[] { 0x53 });
        final int sp = getRegister("sp");
        assertEquals("PUSH BX", 7, getMemory(sp));
        assertEquals("PUSH BX", 8, getMemory(sp + 1));
    }

    @Test
    public void test54() {
        setRegister("sp", 513);
        execute(new int[] { 0x54 });
        final int sp = getRegister("sp");
        assertEquals("PUSH SP", 1, getMemory(sp));
        assertEquals("PUSH SP", 2, getMemory(sp + 1));
    }

    @Test
    public void test55() {
        setRegister("bp", 1027);
        execute(new int[] { 0x55 });
        final int sp = getRegister("sp");
        assertEquals("PUSH BP", 3, getMemory(sp));
        assertEquals("PUSH BP", 4, getMemory(sp + 1));
    }

    @Test
    public void test56() {
        setRegister("si", 1541);
        execute(new int[] { 0x56 });
        final int sp = getRegister("sp");
        assertEquals("PUSH SI", 5, getMemory(sp));
        assertEquals("PUSH SI", 6, getMemory(sp + 1));
    }

    @Test
    public void test57() {
        setRegister("di", 2055);
        execute(new int[] { 0x57 });
        final int sp = getRegister("sp");
        assertEquals("PUSH DI", 7, getMemory(sp));
        assertEquals("PUSH DI", 8, getMemory(sp + 1));
    }

    @Test
    public void testFF() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(new int[] { 0xff, 0b11_110_011 });
        int sp = getRegister("sp");
        assertEquals("PUSH REG16", 1, getMemory(sp));
        assertEquals("PUSH REG16", 2, getMemory(sp + 1));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        execute(new int[] { 0xff, 0b00_110_110, 0x42, 0x00 });
        sp = getRegister("sp");
        assertEquals("PUSH MEM16", 3, getMemory(sp));
        assertEquals("PUSH MEM16", 4, getMemory(sp + 1));
    }
}
