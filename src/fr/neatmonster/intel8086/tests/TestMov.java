package fr.neatmonster.intel8086.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestMov extends fr.neatmonster.intel8086.tests.Test {

    @Test
    public void test88() {
        setRegister("bl", 1);
        execute(new int[] { 0x88, 0b11_011_111 });
        assertEquals("MOV REG8,REG8", 1, getRegister("bh"));

        setRegister("bl", 1);
        execute(new int[] { 0x88, 0b00_011_110, 0x42, 0x00 });
        assertEquals("MOV MEM8,REG8", 1, getMemory(0x42));
    }

    @Test
    public void test89() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(new int[] { 0x89, 0b11_011_001 });
        assertEquals("MOV REG16,REG16", 1, getRegister("cl"));
        assertEquals("MOV REG16,REG16", 2, getRegister("ch"));

        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(new int[] { 0x89, 0b00_011_110, 0x42, 0x00 });
        assertEquals("MOV MEM16,REG16", 1, getMemory(0x42));
        assertEquals("MOV MEM16,REG16", 2, getMemory(0x43));
    }

    @Test
    public void test8A() {
        setRegister("bh", 1);
        execute(new int[] { 0x8a, 0b11_011_111 });
        assertEquals("MOV REG8,REG8", 1, getRegister("bl"));

        setMemory(0x42, 1);
        execute(new int[] { 0x88, 0b00_011_110, 0x42, 0x00 });
        assertEquals("MOV REG8,MEM8", 1, getRegister("bl"));
    }

    @Test
    public void test8B() {
        setRegister("cl", 1);
        setRegister("ch", 2);
        execute(new int[] { 0x8b, 0b11_011_001 });
        assertEquals("MOV REG16,REG16", 1, getRegister("bl"));
        assertEquals("MOV REG16,REG16", 2, getRegister("bh"));

        setMemory(0x42, 1);
        setMemory(0x43, 2);
        execute(new int[] { 0x8b, 0b00_011_110, 0x42, 0x00 });
        assertEquals("MOV REG16,MEM16", 1, getRegister("bl"));
        assertEquals("MOV REG16,MEM16", 2, getRegister("bh"));
    }

    @Test
    public void test8C() {
        setRegister("ds", 1541);
        execute(new int[] { 0x8c, 0b11_011_011 });
        assertEquals("MOV REG16,SEGREG", 5, getRegister("bl"));
        assertEquals("MOV REG16,SEGREG", 6, getRegister("bh"));

        setRegister("ds", 2055);
        execute(new int[] { 0x8c, 0b00_011_110, 0x42, 0x00 });
        assertEquals("MOV MEM16,SEGREG", 7, getMemory(0x42));
        assertEquals("MOV MEM16,SEGREG", 8, getMemory(0x43));
    }

    @Test
    public void test8E() {
        setRegister("bl", 1);
        setRegister("bh", 2);
        execute(new int[] { 0x8e, 0b11_011_011 });
        assertEquals("MOV SEGREG,REG16", 513, getRegister("ds"));

        setMemory(0x42, 3);
        setMemory(0x43, 4);
        execute(new int[] { 0x8e, 0b00_011_110, 0x42, 0x00 });
        assertEquals("MOV SEGREG,MEM16", 1027, getRegister("ds"));
    }

    @Test
    public void testA0() {
        setMemory(0x42, 1);
        execute(new int[] { 0xa0, 0x42, 0x00 });
        assertEquals("MOV AL,MEM8", 1, getRegister("al"));
    }

    @Test
    public void testA1() {
        setMemory(0x42, 1);
        setMemory(0x43, 2);
        execute(new int[] { 0xa1, 0x42, 0x00 });
        assertEquals("MOV AX,MEM16", 1, getRegister("al"));
        assertEquals("MOV AX,MEM16", 2, getRegister("ah"));
    }

    @Test
    public void testA2() {
        setRegister("al", 1);
        execute(new int[] { 0xa2, 0x42, 0x00 });
        assertEquals("MOV MEM8,AL", 1, getMemory(0x42));
    }

    @Test
    public void testA3() {
        setRegister("al", 1);
        setRegister("ah", 2);
        execute(new int[] { 0xa3, 0x42, 0x00 });
        assertEquals("MOV MEM16,AX", 1, getMemory(0x42));
        assertEquals("MOV MEM16,AX", 2, getMemory(0x43));
    }

    @Test
    public void testB0() {
        execute(new int[] { 0xb0, 1 });
        assertEquals("MOV AL,IMMED8", 1, getRegister("al"));
    }

    @Test
    public void testB1() {
        execute(new int[] { 0xb1, 2 });
        assertEquals("MOV CL,IMMED8", 2, getRegister("cl"));
    }

    @Test
    public void testB2() {
        execute(new int[] { 0xb2, 3 });
        assertEquals("MOV DL,IMMED8", 3, getRegister("dl"));
    }

    @Test
    public void testB3() {
        execute(new int[] { 0xb3, 4 });
        assertEquals("MOV BL,IMMED8", 4, getRegister("bl"));
    }

    @Test
    public void testB4() {
        execute(new int[] { 0xb4, 5 });
        assertEquals("MOV AH,IMMED8", 5, getRegister("ah"));
    }

    @Test
    public void testB5() {
        execute(new int[] { 0xb5, 6 });
        assertEquals("MOV CH,IMMED8", 6, getRegister("ch"));
    }

    @Test
    public void testB6() {
        execute(new int[] { 0xb6, 7 });
        assertEquals("MOV DH,IMMED8", 7, getRegister("dh"));
    }

    @Test
    public void testB7() {
        execute(new int[] { 0xb7, 8 });
        assertEquals("MOV BH,IMMED8", 8, getRegister("bh"));
    }

    @Test
    public void testB8() {
        execute(new int[] { 0xb8, 1, 2 });
        assertEquals("MOV AX,IMMED16", 1, getRegister("al"));
        assertEquals("MOV AX,IMMED16", 2, getRegister("ah"));
    }

    @Test
    public void testB9() {
        execute(new int[] { 0xb9, 3, 4 });
        assertEquals("MOV CX,IMMED16", 3, getRegister("cl"));
        assertEquals("MOV CX,IMMED16", 4, getRegister("ch"));
    }

    @Test
    public void testBA() {
        execute(new int[] { 0xba, 5, 6 });
        assertEquals("MOV DX,IMMED16", 5, getRegister("dl"));
        assertEquals("MOV DX,IMMED16", 6, getRegister("dh"));
    }

    @Test
    public void testBB() {
        execute(new int[] { 0xbb, 7, 8 });
        assertEquals("MOV BX,IMMED16", 7, getRegister("bl"));
        assertEquals("MOV BX,IMMED16", 8, getRegister("bh"));
    }

    @Test
    public void testBC() {
        execute(new int[] { 0xbc, 1, 2 });
        assertEquals("MOV SP,IMMED16", 513, getRegister("sp"));
    }

    @Test
    public void testBD() {
        execute(new int[] { 0xbd, 3, 4 });
        assertEquals("MOV BP,IMMED16", 1027, getRegister("bp"));
    }

    @Test
    public void testBE() {
        execute(new int[] { 0xbe, 5, 6 });
        assertEquals("MOV SI,IMMED16", 1541, getRegister("si"));
    }

    @Test
    public void testBF() {
        execute(new int[] { 0xbf, 7, 8 });
        assertEquals("MOV DI,IMMED16", 2055, getRegister("di"));
    }

    @Test
    public void testC6() {
        execute(new int[] { 0xc6, 0b11_000_011, 1 });
        assertEquals("MOV REG8,IMMED8", 1, getRegister("bl"));

        execute(new int[] { 0xc6, 0b00_000_110, 0x42, 0x00, 1 });
        assertEquals("MOV MEM8,IMMED8", 1, getMemory(0x42));
    }

    @Test
    public void testC7() {
        execute(new int[] { 0xc7, 0b11_000_011, 1, 2 });
        assertEquals("MOV REG16,IMMED16", 1, getRegister("bl"));
        assertEquals("MOV REG16,IMMED16", 2, getRegister("bh"));

        execute(new int[] { 0xc7, 0b00_000_110, 0x42, 0x00, 1, 2 });
        assertEquals("MOV MEM16,IMMED16", 1, getMemory(0x42));
        assertEquals("MOV MEM16,IMMED16", 2, getMemory(0x43));
    }
}
