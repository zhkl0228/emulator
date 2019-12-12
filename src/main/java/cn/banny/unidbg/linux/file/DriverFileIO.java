package cn.banny.unidbg.linux.file;

import cn.banny.unidbg.Emulator;
import cn.banny.unidbg.file.AbstractFileIO;
import cn.banny.unidbg.file.FileIO;
import cn.banny.unidbg.pointer.UnicornPointer;
import com.sun.jna.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import unicorn.Unicorn;

public class DriverFileIO extends AbstractFileIO implements FileIO {

    private static final Log log = LogFactory.getLog(DriverFileIO.class);

    public static DriverFileIO create(Emulator emulator, int oflags, String pathname) {
        if ("/dev/urandom".equals(pathname) || "/dev/random".equals(pathname) || "/dev/srandom".equals(pathname)) {
            return new RandomFileIO(emulator, pathname);
        }
        if ("/dev/alarm".equals(pathname) || "/dev/null".equals(pathname)) {
            return new DriverFileIO(emulator, oflags, pathname);
        }
        if ("/dev/ashmem".equals(pathname)) {
            return new Ashmem(emulator, oflags, pathname);
        }
        return null;
    }

    private final Emulator emulator;
    private final String path;

    DriverFileIO(Emulator emulator, int oflags, String path) {
        super(oflags);
        this.emulator = emulator;
        this.path = path;
    }

    @Override
    public void close() {
    }

    @Override
    public int write(byte[] data) {
        throw new AbstractMethodError();
    }

    @Override
    public int read(Unicorn unicorn, Pointer buffer, int count) {
        throw new AbstractMethodError();
    }

    private static final int _IOC_NRBITS = 8;
    private static final int _IOC_TYPEBITS = 8;
    private static final int _IOC_SIZEBITS = 14;

    private enum AndroidAlarmType {
        ANDROID_ALARM_RTC_WAKEUP,
        ANDROID_ALARM_RTC,
        ANDROID_ALARM_ELAPSED_REALTIME_WAKEUP,
        ANDROID_ALARM_ELAPSED_REALTIME,
        ANDROID_ALARM_SYSTEMTIME,
        ANDROID_ALARM_TYPE_COUNT;
        static AndroidAlarmType valueOf(long type) {
            for (AndroidAlarmType alarmType : values()) {
                if (alarmType.ordinal() == type) {
                    return alarmType;
                }
            }
            throw new IllegalArgumentException("type=" + type);
        }
    }

    @Override
    public int ioctl(Emulator emulator, long request, long argp) {
        if ("/dev/alarm".equals(path)) {
            long ioc = request;
            long nr = ioc & 0xff;
            ioc >>= _IOC_NRBITS;
            long type = ioc & 0xff;
            ioc >>= _IOC_TYPEBITS;
            long size = ioc & 0x3fff;
            ioc >>= _IOC_SIZEBITS;
            long dir = ioc;
            if (type == 'a') {
                long c = nr & 0xf;
                type = nr >> 4;
                return androidAlarm(dir, c, AndroidAlarmType.valueOf(type), size, argp);
            }

            log.info("alarm ioctl request=0x" + Long.toHexString(request) + ", argp=0x" + Long.toHexString(argp) + ", nr=" + nr + ", type=" + type + ", size=" + size + ", dir=" + dir);
            return -1;
        }

        return super.ioctl(emulator, request, argp);
    }

    private static final int _IOC_WRITE = 1;
    private static final int _IOC_READ = 2;

    private static final int ANDROID_ALARM_GET_TIME = 4;

    private int androidAlarm(long dir, long c, AndroidAlarmType type, long size, long argp) {
        if (dir == _IOC_WRITE && c == ANDROID_ALARM_GET_TIME && type == AndroidAlarmType.ANDROID_ALARM_ELAPSED_REALTIME) {
            long offset = System.currentTimeMillis();
            long tv_sec = offset / 1000000000L;
            long tv_nsec = offset % 1000000000L;
            Pointer pointer = UnicornPointer.pointer(emulator, argp);
            if (pointer == null) {
                throw new IllegalArgumentException();
            }
            if (size == 8) {
                pointer.setInt(0, (int) tv_sec);
                pointer.setInt(4, (int) tv_nsec);
                return 0;
            } else if (size == 16) {
                pointer.setLong(0, tv_sec);
                pointer.setLong(8, tv_nsec);
                return 0;
            } else {
                throw new IllegalArgumentException("size=" + size);
            }
        }

        log.info("androidAlarm argp=0x" + Long.toHexString(argp) + ", c=" + c + ", type=" + type + ", size=" + size + ", dir=" + dir);
        return -1;
    }

    @Override
    public FileIO dup2() {
        throw new AbstractMethodError();
    }

    @Override
    public String toString() {
        return path;
    }
}
