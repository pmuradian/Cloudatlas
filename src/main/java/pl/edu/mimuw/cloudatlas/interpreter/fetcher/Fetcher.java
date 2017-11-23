package pl.edu.mimuw.cloudatlas.interpreter.fetcher;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;

public class Fetcher {
    public Double CPULoad() throws Exception {
        MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
        ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

        if (list.isEmpty())     return Double.NaN;

        Attribute att = (Attribute)list.get(0);
        Double value  = (Double)att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0)      return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        return new Double((value * 1000) / 10.0);
    }

    public Float freeDiskSpace() {
        return new Float(0);
    }

    public Float totalDiskSpace() {
        return new Float(0);
    }

    public Float freeRAM() {
        return new Float(0);
    }

    public Float totalRAM() {
        return new Float(0);
    }

    public Float freeSwap() {
        return new Float(0);
    }

    public Float totalSwap() {
        return new Float(0);
    }

    public Integer activeProcessesCount() {
        return new Integer(0);
    }

    public Integer CPUCoreCount() {
        return new Integer(0);
    }

    public Double kernelVersion() {
        return new Double(0);
    }

    public Integer loggedUsersCount() {
        return new Integer(0);
    }

    public ArrayList<String> DNSNames() {
        return new ArrayList<String>(Arrays.asList("a", "b", "c"));
    }
}
