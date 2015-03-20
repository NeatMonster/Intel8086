package fr.neatmonster.intel8086.tests;

import java.lang.reflect.Field;

import org.junit.Before;

import fr.neatmonster.intel8086.Intel8086;

public class Test {
    protected Intel8086 cpu = new Intel8086();

    protected void execute(final int... bin) {
        final int[] code = new int[bin.length + 1];
        for (int i = 0; i < bin.length; i++)
            code[i] = bin[i];
        code[bin.length] = 0xc3;
        cpu.load(code);
        setRegister("ip", 0);
        cpu.execute();
    }

    protected int getMemory(final int addr) {
        try {
            final Field field = cpu.getClass().getDeclaredField("memory");
            field.setAccessible(true);
            return ((int[]) field.get(cpu))[addr];
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected int getRegister(final String name) {
        try {
            final Field field = cpu.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (int) field.get(cpu);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected void setMemory(final int addr, final int value) {
        try {
            final Field field = cpu.getClass().getDeclaredField("memory");
            field.setAccessible(true);
            final int[] memory = (int[]) field.get(cpu);
            memory[addr] = value;
            field.set(cpu, memory);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    protected void setRegister(final String name, final int value) {
        try {
            final Field field = cpu.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(cpu, value);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() {
        cpu = new Intel8086();
        cpu.reset();
    }
}
