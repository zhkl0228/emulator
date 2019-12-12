package cn.banny.unidbg.linux.android;

import cn.banny.unidbg.Emulator;
import cn.banny.unidbg.arm.Arm64Hook;
import cn.banny.unidbg.arm.ArmHook;
import cn.banny.unidbg.arm.HookStatus;
import cn.banny.unidbg.arm.context.RegisterContext;
import cn.banny.unidbg.hook.HookListener;
import cn.banny.unidbg.memory.SvcMemory;
import com.sun.jna.Pointer;
import unicorn.UnicornException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SystemPropertyHook implements HookListener {

    private static final int PROP_VALUE_MAX = 92;

    private final Emulator emulator;

    public SystemPropertyHook(Emulator emulator) {
        this.emulator = emulator;
    }

    @Override
    public long hook(SvcMemory svcMemory, String libraryName, String symbolName, final long old) {
        if ("libc.so".equals(libraryName) && "__system_property_get".equals(symbolName)) {
            if (emulator.is64Bit()) {
                return svcMemory.registerSvc(new Arm64Hook() {
                    @Override
                    protected HookStatus hook(Emulator emulator) {
                        return __system_property_get(old);
                    }
                }).peer;
            } else {
                return svcMemory.registerSvc(new ArmHook() {
                    @Override
                    protected HookStatus hook(Emulator emulator) {
                        return __system_property_get(old);
                    }
                }).peer;
            }
        }
        return 0;
    }

    private HookStatus __system_property_get(long old) {
        if (propertyProvider != null) {
            RegisterContext context = emulator.getContext();
            Pointer pointer = context.getPointerArg(0);
            String key = pointer.getString(0);
            String value = propertyProvider.getProperty(key);
            if (value != null) {
                byte[] data = value.getBytes(StandardCharsets.UTF_8);
                if (data.length >= PROP_VALUE_MAX) {
                    throw new UnicornException("invalid property value length: key=" + key + ", value=" + value);
                }

                context.getPointerArg(1).write(0, Arrays.copyOf(data, data.length + 1), 0, data.length + 1);
                return HookStatus.LR(emulator, value.length());
            }
        }

        return HookStatus.RET(emulator, old);
    }

    private SystemPropertyProvider propertyProvider;

    public void setPropertyProvider(SystemPropertyProvider propertyProvider) {
        this.propertyProvider = propertyProvider;
    }

}
