package pl.edu.mimuw.cloudatlas.interpreter.fetcher;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import pl.edu.mimuw.cloudatlas.cloudatlasRMI.MachineDescriptionFetcher;
import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Fetcher {

    private String ZMIName = null;

    // in milliseconds
    private Integer feedInterval = 2000;
    private Integer averagingPeriod = 5000;

    private Double averageCPULoad = 0.0;
    private Integer cycleCount = 0;
    private Timer fetchingTimer = new Timer();
    private Timer averagingTimer = new Timer();
    MachineDescriptionFetcher fetcher;

    public static void main(String[] args) {
        Fetcher dataFetcher = new Fetcher("/uw/violet07");
        dataFetcher.startFetching();
    }

    public Fetcher(String ZMIName) {
        this(ZMIName, new File("fetcher.ini"));
        this.connectToAgent();
    }

    public Fetcher(String ZMIName, File iniFile) {
        try {
            Ini ini = new Ini(iniFile);
            java.util.prefs.Preferences prefs = new IniPreferences(ini);
            this.feedInterval = prefs.node("options").getInt("feedInterval", 2000);
            this.averagingPeriod = prefs.node("options").getInt("averagingPeriod", 5000);
            this.ZMIName = ZMIName;
            connectToAgent();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void connectToAgent() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry();
            this.fetcher = (MachineDescriptionFetcher) registry.lookup(MachineDescriptionFetcher.class.getName());
        } catch (Exception e) {
            System.err.println("Could not connect to agent:");
            e.printStackTrace();
        }
    }

    public Fetcher(String ZMIName, Integer feedInterval, Integer averagingPeriod) {
        this.ZMIName = ZMIName;
        this.feedInterval = feedInterval;
    }

    public void startFetching() {
        fetchingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    fetcher.updateZMIAttributes(ZMIName, readData());
                } catch (RemoteException e) {
                    System.out.println(e.getMessage());
                }
            }
        }, 0, feedInterval);
        averagingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                countAverageCPULoad();
            }
        }, 0, averagingPeriod);
    }

    public Map<String, Object> readData() {
        cycleCount++;
        System.out.println(cycleCount);
        try {
            averageCPULoad += CPULoad() / cycleCount;
            System.out.println(averageCPULoad);
        } catch (Exception e) {
            System.out.println("CPULoad exception");
        }
        System.out.println(">>>>>>Data fetched<<<<<<");

        HashMap attributeMap = new HashMap<String, Object>();
        attributeMap.put("cpu_load", averageCPULoad);
        attributeMap.put("free_ram", this.freeRAM());
        attributeMap.put("total_ram", this.totalRAM());
        attributeMap.put("free_disk", this.freeDiskSpace());
        attributeMap.put("total_disk", this.totalDiskSpace());
        attributeMap.put("free_swap", this.freeSwap());
        attributeMap.put("total_swap", this.totalSwap());
        attributeMap.put("num_processess", this.activeProcessesCount());
        attributeMap.put("num_cores", this.CPUCoreCount());
        attributeMap.put("kernel_version", this.kernelVersion());
        attributeMap.put("logged_users", this.loggedUsersCount());
        attributeMap.put("dns_names", this.DNSNames());

        return attributeMap;
    }

    public void countAverageCPULoad() {
        System.out.println("average counted");
    }

    public Double CPULoad() throws Exception {
        return (((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad() * 10);
    }

    public Integer CPUCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public Long freeDiskSpace() {
        return new File("/").getFreeSpace();
    }

    public Long totalDiskSpace() {
        return new File("/").getTotalSpace();
    }

    public Long freeRAM() { //MB
        return ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
    }

    public Long totalRAM() { //MB
        return ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
    }

    public Long freeSwap() { //MB
        return ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreeSwapSpaceSize();
    }

    public Long totalSwap() { //MB
        return ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalSwapSpaceSize();
    }

    public Integer activeProcessesCount() {
        try {
            String line;
            Process p = Runtime.getRuntime().exec("ps -e");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            int processCount = 0;
            while ((line = input.readLine()) != null) {
                processCount++;
            }
            input.close();
            return processCount;
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    public String kernelVersion() {
        return System.getProperty("os.version");
    }

    public Integer loggedUsersCount() {
        return 1;
    }

    public ArrayList<String> DNSNames() {
        return new ArrayList<String>(Arrays.asList("a", "b", "c"));
    }
}
