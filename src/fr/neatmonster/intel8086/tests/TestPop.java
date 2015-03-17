package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestPop extends fr.neatmonster.intel8086.tests.Test {

    @Test
    public void test07() {
        final int sp = getRegister("sp");
        setMemory(sp, 1);
        setMemory(sp + 1, 2);
        execute(0x07);
        assertEquals("POP ES", 513, getRegister("es"));
    }

    @Test
    public void test0F() {
        final int sp = getRegister("sp");
        setMemory(sp, 3);
        setMemory(sp + 1, 4);
        execute(0x0f);
        assertEquals("POP CS", 1027, getRegister("cs"));
    }

    @Test
    public void test17() {
        final int sp = getRegister("sp");
        setMemory(sp, 5);
        setMemory(sp + 1, 6);
        execute(0x17);
        assertEquals("POP SS", 1541, getRegister("ss"));
    }

    @Test
    public void test1F() {
        final int sp = getRegister("sp");
        setMemory(sp, 7);
        setMemory(sp + 1, 8);
        execute(0x1f);
        assertEquals("POP DS", 2055, getRegister("ds"));
    }

    @Test
    public void test58() {
        final int sp = getRegister("sp");
        setMemory(sp, 1);
        setMemory(sp + 1, 2);
        execute(0x58);
        assertEquals("POP AX", 1, getRegister("al"));
        assertEquals("POP AX", 2, getRegister("ah"));
    }

    @Test
    public void test59() {
        final int sp = getRegister("sp");
        setMemory(sp, 3);
        setMemory(sp + 1, 4);
        execute(0x59);
        assertEquals("POP CX", 3, getRegister("cl"));
        assertEquals("POP CX", 4, getRegister("ch"));
    }

    @Test
    public void test5A() {
        final int sp = getRegister("sp");
        setMemory(sp, 5);
        setMemory(sp + 1, 6);
        execute(0x5a);
        assertEquals("POP DX", 5, getRegister("dl"));
        assertEquals("POP DX", 6, getRegister("dh"));
    }

    @Test
    public void test5B() {
        final int sp = getRegister("sp");
        setMemory(sp, 7);
        setMemory(sp + 1, 8);
        execute(0x5b);
        assertEquals("POP BX", 7, getRegister("bl"));
        assertEquals("POP BX", 8, getRegister("bh"));
    }

    @Test
    public void test5C() {
        final int sp = getRegister("sp");
        setMemory(sp, 1);
        setMemory(sp + 1, 2);
        execute(0x5c);
        assertEquals("POP SP", 513, getRegister("sp"));
    }

    @Test
    public void test5D() {
        final int sp = getRegister("sp");
        setMemory(sp, 3);
        setMemory(sp + 1, 4);
        execute(0x5d);
        assertEquals("POP BP", 1027, getRegister("bp"));
    }

    @Test
    public void test5E() {
        final int sp = getRegister("sp");
        setMemory(sp, 5);
        setMemory(sp + 1, 6);
        execute(0x5e);
        assertEquals("POP SI", 1541, getRegister("si"));
    }

    @Test
    public void test5F() {
        final int sp = getRegister("sp");
        setMemory(sp, 7);
        setMemory(sp + 1, 8);
        execute(0x5f);
        assertEquals("POP DI", 2055, getRegister("di"));
    }

    @Test
    public void test8F() {
        int sp = getRegister("sp");
        setMemory(sp, 1);
        setMemory(sp + 1, 2);
        execute(0x8f, 0b11_000_011);
        assertEquals("POP REG16", 1, getRegister("bl"));
        assertEquals("POP REG16", 2, getRegister("bh"));

        sp = getRegister("sp");
        setMemory(sp, 3);
        setMemory(sp + 1, 4);
        execute(0x8f, 0b00_000_110, 0x42, 0x00);
        assertEquals("POP MEM16", 3, getMemory(0x42));
        assertEquals("POP MEM16", 4, getMemory(0x43));
    }
}
