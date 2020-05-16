package com.github.unidbg.android;

import com.github.unidbg.Emulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.ARMSyscallHandler;
import com.github.unidbg.linux.android.AndroidARMEmulator;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.struct.Dirent;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.memory.SvcMemory;
import com.github.unidbg.unix.UnixSyscallHandler;
import com.sun.jna.Pointer;

import java.io.File;
import java.io.IOException;

public class AndroidTest {

    public static void main(String[] args) throws IOException {
        new AndroidTest().test();
    }

    private final Emulator<?> emulator;
    private final Module module;

    private static class MyARMSyscallHandler extends ARMSyscallHandler {
        private MyARMSyscallHandler(SvcMemory svcMemory) {
            super(svcMemory);
        }
        @Override
        protected int fork(Emulator<?> emulator) {
            return emulator.getPid();
        }
    }

    private AndroidTest() throws IOException {
        File executable = new File("src/test/native/android/libs/armeabi-v7a/test");
        emulator = new AndroidARMEmulator(executable.getName(), new File("target/rootfs")) {
            @Override
            protected UnixSyscallHandler<AndroidFileIO> createSyscallHandler(SvcMemory svcMemory) {
                return new MyARMSyscallHandler(svcMemory);
            }
        };
        Memory memory = emulator.getMemory();
        emulator.getSyscallHandler().setVerbose(false);
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);

        module = emulator.loadLibrary(executable);

        {
            Pointer pointer = memory.allocateStack(0x100);
            System.out.println(new Dirent(pointer));
        }
    }

    private void test() {
//        Logger.getLogger("com.github.unidbg.linux.ARMSyscallHandler").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.unix.UnixSyscallHandler").setLevel(Level.DEBUG);
        System.err.println("exit code: " + module.callEntry(emulator));
    }

}
