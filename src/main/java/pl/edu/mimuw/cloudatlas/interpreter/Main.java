/**
 * Copyright (c) 2014, University of Warsaw
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.edu.mimuw.cloudatlas.interpreter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.modules.communication.CommunicationClient;
import pl.edu.mimuw.cloudatlas.modules.communication.CommunicationServer;

public class Main {
	public static ZMI root;
	private static ZMI node;
	private static HashMap<String, Timer> installedQueryTimers = new HashMap<>();
	private static Timer gossipTimer = new Timer();
	private static GossipType gossipType = GossipType.RandomSameProbability;
	private static Long gossipPeriod = 5000l;
	private static java.util.prefs.Preferences prefs;
	
	public static void main(String[] args) {

		try {
			root = createTestHierarchy();
		} catch (ParseException | UnknownHostException e) {
			e.printStackTrace();
		}

		String filepath = "tests/query.in";
		if (args.length > 1) {
			filepath = args[1];
		}

		CommunicationServer server = new CommunicationServer();
		server.start(9876);
		setupFromConfigurationFile("config.ini");
		startGossiping(gossipType);
	}

	public static void initializeHierarchy() throws Exception {
		root = createTestHierarchy();
		CommunicationServer server = new CommunicationServer();
		server.start(9876);
		setupFromConfigurationFile("config.ini");
		startGossiping(gossipType);
	}

	public static void startGossiping(GossipType type) {
		// Random selection probability for all levels
		gossipTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				GossipLevelGenerator generator = new GossipLevelGenerator(type, node.getNodeDepth());
				ValueContact contact = selectContact(root.getZMIWithLevel(generator.next()));
				CommunicationClient client = new CommunicationClient();
				String name = contact.getName().getSingletonName();
				String ip = prefs.node("ip_addresses").get(name, "localhost");
				boolean isConnected = client.connectTo(ip);
				if (isConnected) {
					client.sendZMI(node);
				}
			}
		}, 0, gossipPeriod);
	}

	public static ValueContact selectContact(ArrayList<ZMI> zmis) {
		ArrayList<ValueContact> contacts = new ArrayList();
		for (ZMI zmi: zmis) {
			ValueSet zmiContacts = (ValueSet) zmi.getAttributes().getOrNull("contacts");
			if (zmiContacts != null) {
				for (Value contact : zmiContacts.getValue())
					if (contact != null) {
						String contactName = ((ValueContact)contact).getName().getName();
						String zmiName = ((ValueString)node.getAttributes().get("owner")).getValue();
						if (!contactName.equals(zmiName)) {
							contacts.add((ValueContact) contact);
						}
					}
			}
		}

		if (contacts.size() == 0) {
			try {
				contacts = getFallbackContacts();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		Integer randomIndex = (new Random()).nextInt(contacts.size());
		randomIndex = randomIndex > 0 ? randomIndex - 1: randomIndex;
		return contacts.get(randomIndex);
	}

	private static ArrayList<ValueContact> getFallbackContacts() throws UnknownHostException {
		ValueContact violet07Contact = createContact("/uw/violet07", (byte)10, (byte)1, (byte)1, (byte)10);
		ValueContact khaki13Contact = createContact("/uw/khaki13", (byte)10, (byte)1, (byte)1, (byte)38);
		ValueContact khaki31Contact = createContact("/uw/khaki31", (byte)10, (byte)1, (byte)1, (byte)39);
		ValueContact whatever01Contact = createContact("/uw/whatever01", (byte)82, (byte)111, (byte)52, (byte)56);
		ValueContact whatever02Contact = createContact("/uw/whatever02", (byte)82, (byte)111, (byte)52, (byte)57);

		List list = Arrays.asList(new Value[] {
				violet07Contact, khaki13Contact, khaki31Contact, whatever01Contact, whatever02Contact
		});

		ArrayList<ValueContact> contacts = new ArrayList<>(list);
		int i = 0;
		int removeIndex = 0;
		for (ValueContact contact: contacts) {
			String contactName = contact.getName().getName();
			String zmiName = ((ValueString)node.getAttributes().get("owner")).getValue();
			if (contactName.equals(zmiName)) {
				removeIndex = i;
			}
			i++;
		}
		contacts.remove(removeIndex);

		return new ArrayList(list);
	}

	private static void setupFromConfigurationFile(String path) {
		File iniFile = new File(path);
		File gossipIni = new File("gossip.ini");
		Ini ini = null;
		Ini gossip = null;
		try {
			ini = new Ini(iniFile);
			gossip = new Ini(gossipIni);
		} catch (IOException e) {
			e.printStackTrace();
		}
		prefs = new IniPreferences(ini);
		String nodeName = prefs.node("current_node").get("node", "violet07");
		node = root.sonForPath("/uw/" + nodeName);
		IniPreferences gossipPrefs = new IniPreferences(gossip);
		gossipType = GossipType.fromString(gossipPrefs.node("gossip_option").get("option", "RSP"));
		gossipPeriod = gossipPrefs.node("gossip_option").getLong("interval", 5000);
	}

	// Will start interpreter with the queries in file, will install queries to the root node
	private static void initializeFromFile(String path) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
			for (String line: lines) {

				String[] splitString = line.split(":");
				if (splitString.length != 2) {
					System.out.println("Input error");
				}
				else {
					installQuery(root, splitString[0], splitString[1].split(";"));
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static PathName getPathName(ZMI zmi) {
		String name = ((ValueString)zmi.getAttributes().get("owner")).getValue();
		PathName pathName = new PathName(name);
		return pathName;
	}

	public static void updateZMIAttributes(String zmiPath, Map<String, Object> attributeMap) {
		ZMI zmi = root.sonForPath(zmiPath);
		if (zmi != null) {
			zmi.getAttributes().addOrChange("cpu_load", new ValueDouble((Double) attributeMap.get("cpu_load")));
			zmi.getAttributes().addOrChange("free_ram", new ValueInt((Long) attributeMap.get("free_ram")));
			zmi.getAttributes().addOrChange("total_ram", new ValueInt((Long) attributeMap.get("total_ram")));
			zmi.getAttributes().addOrChange("free_disk", new ValueInt((Long) attributeMap.get("free_disk")));
			zmi.getAttributes().addOrChange("total_disk", new ValueInt((Long) attributeMap.get("total_disk")));
			zmi.getAttributes().addOrChange("free_swap", new ValueInt((Long) attributeMap.get("free_swap")));
			zmi.getAttributes().addOrChange("total_swap", new ValueInt((Long) attributeMap.get("total_swap")));
			zmi.getAttributes().addOrChange("num_processes", new ValueInt((Long) attributeMap.get("num_processes")));
			zmi.getAttributes().addOrChange("kernel_version", new ValueString((String) attributeMap.get("kernel_version")));
			zmi.getAttributes().addOrChange("logged_users", new ValueInt((Long) attributeMap.get("free_swap")));
			Iterator<String> i = ((List<String>) attributeMap.get("dns_names")).iterator();
			ValueList dnsNames = new ValueList(TypePrimitive.STRING);
			while (i.hasNext()) {
				dnsNames.add(new ValueString(i.next()));
			}
			zmi.getAttributes().addOrChange("dns_names", dnsNames);
		}

		System.out.println("Attributes for " + zmiPath + " were updated by Fetcher");
	}

	public static void updateZMIAttributes(ZMI newZMI) {
		ZMI zmi = root.sonForPath(getPathName(newZMI).getName());
		if (zmi != null) {
			AttributesMap attributesMap = newZMI.getAttributes();
			for (Map.Entry<Attribute, Value> entry: attributesMap) {
				List<String> queries = new ArrayList<>();
				if (entry.getValue().getType().getPrimaryType() == Type.PrimaryType.LIST) {
					ValueList list = (ValueList) entry.getValue();
					if (list.getValue().size() > 0 && list.getValue().get(0).getClass() == ValueQuery.class) {
						for (Value str: list.getValue()) {
							queries.add(((ValueString)str).getValue());
						}
						// Uninstall active queries before installing new updated ZMI queries
						uninstallQuery(zmi, entry.getKey().getName());

						installQuery(zmi, entry.getKey().getName(), queries.toArray(new String[queries.size()]));
					}
				}
				else {
					zmi.getAttributes().addOrChange(entry.getKey(), entry.getValue());
				}
			}
		}

		System.out.println("Attributes were updated by Gossiping");
	}
	
	public static HashMap<String, Value> executeQueries(ZMI zmi, String query) throws Exception {
		if(!zmi.getSons().isEmpty()) {
			HashMap attributeMap = new HashMap<Attribute, Value>();
			for(ZMI son : zmi.getSons()) {
				HashMap<String, Value> sonResult = executeQueries(son, query);
				if (sonResult != null) {
					attributeMap.putAll(sonResult);
				}
			}

			Interpreter interpreter = new Interpreter(zmi);
			Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
			try {
				List<QueryResult> result = interpreter.interpretProgram((new parser(lex)).pProgram());
				PathName zone = getPathName(zmi);
				for(QueryResult r : result) {
					System.out.println(zone + ": " + r);
					attributeMap.put(zone + ": " + r.getName(), r.getValue());
					zmi.getAttributes().addOrChange(r.getName(), r.getValue());
				}
				return attributeMap;
			} catch(InterpreterException exception) {
				System.out.println(exception.getMessage());
				attributeMap.clear();
				attributeMap.put("Error in ZMI " + getPathName(zmi) + ":", new ValueString(exception.getMessage()));
				return attributeMap;
			}
		}
		return null;
	}

	public static void installQuery(ZMI zmi, String attributeName, String[] queries) {
		ValueList queryValues = new ValueList(TypePrimitive.STRING);
		for (String query: queries) {
			try {
				queryValues.add(new ValueQuery(query));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		zmi.getAttributes().addOrChange(attributeName, queryValues);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (String query: queries) {
					try {
						executeQueries(zmi, query);
					} catch (Exception e) {

					}
				}
			}
		}, 0, 4000);
		installedQueryTimers.put(attributeName, timer);
	}

	public static String uninstallQuery(ZMI zmi, String attributeName) {
		Timer timer = installedQueryTimers.get(attributeName);
		if (timer == null) {
			return "No such attribute";
		}
		timer.cancel();
		installedQueryTimers.remove(attributeName);
		zmi.removeAttribute(attributeName);
		System.out.println("Query uninstalled");
		return "Attribute " + attributeName + " was removed";
	}

	public static ZMI printZMIAttributes(ZMI zmi) {
		zmi.printAttributes(System.out);
		return zmi;
	}
	
	private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
			throws UnknownHostException {
		return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[] {
			ip1, ip2, ip3, ip4
		}));
	}
	
	private static ZMI createTestHierarchy() throws ParseException, UnknownHostException {
		ValueContact violet07Contact = createContact("/uw/violet07", (byte)10, (byte)1, (byte)1, (byte)10);
		ValueContact khaki13Contact = createContact("/uw/khaki13", (byte)10, (byte)1, (byte)1, (byte)38);
		ValueContact khaki31Contact = createContact("/uw/khaki31", (byte)10, (byte)1, (byte)1, (byte)39);
		ValueContact whatever01Contact = createContact("/uw/whatever01", (byte)82, (byte)111, (byte)52, (byte)56);
		ValueContact whatever02Contact = createContact("/uw/whatever02", (byte)82, (byte)111, (byte)52, (byte)57);
		
		List<Value> list;
		
		root = new ZMI();
		root.getAttributes().add("level", new ValueInt(0l));
		root.getAttributes().add("name", new ValueString(null));
		root.getAttributes().add("owner", new ValueString(null));
		
		ZMI uw = new ZMI(root);
		root.addSon(uw);
		uw.getAttributes().add("level", new ValueInt(1l));
		uw.getAttributes().add("name", new ValueString("uw"));
		uw.getAttributes().add("owner", new ValueString("/uw"));
		
		ZMI pjwstk = new ZMI(root);
		root.addSon(pjwstk);
		pjwstk.getAttributes().add("level", new ValueInt(1l));
		pjwstk.getAttributes().add("name", new ValueString("pjwstk"));
		pjwstk.getAttributes().add("owner", new ValueString("/pjwstk"));
		
		ZMI violet07 = new ZMI(uw);
		uw.addSon(violet07);
		violet07.getAttributes().add("level", new ValueInt(2l));
		violet07.getAttributes().add("name", new ValueString("violet07"));
		violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
		violet07.getAttributes().add("timestamp", new ValueTime("2012/11/09 18:00:00.000"));
		list = Arrays.asList(new Value[] {
			khaki31Contact, whatever01Contact
		});
		violet07.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			violet07Contact,
		});
		violet07.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("creation", new ValueTime("2011/11/09 20:8:13.123"));
		violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
		violet07.getAttributes().add("num_cores", new ValueInt(3l));
		violet07.getAttributes().add("num_processes", new ValueInt(131l));
		violet07.getAttributes().add("has_ups", new ValueBoolean(null));
		list = Arrays.asList(new Value[] {
			new ValueString("tola"), new ValueString("tosia"),
		});
		violet07.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		violet07.getAttributes().add("expiry", new ValueDuration(13l, 12l, 0l, 0l, 0l));
		
		ZMI khaki31 = new ZMI(uw);
		uw.addSon(khaki31);
		khaki31.getAttributes().add("level", new ValueInt(2l));
		khaki31.getAttributes().add("name", new ValueString("khaki31"));
		khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
		khaki31.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:03:00.000"));
		list = Arrays.asList(new Value[] {
			violet07Contact, whatever02Contact,
		});
		khaki31.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			khaki31Contact
		});
		khaki31.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("creation", new ValueTime("2011/11/09 20:12:13.123"));
		khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
		khaki31.getAttributes().add("num_processes", new ValueInt(131l));
		khaki31.getAttributes().add("num_cores", new ValueInt(3l));
		khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
		list = Arrays.asList(new Value[] {
			new ValueString("agatka"), new ValueString("beatka"), new ValueString("celina"),
		});
		khaki31.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki31.getAttributes().add("expiry", new ValueDuration(-13l, -11l, 0l, 0l, 0l));
		
		ZMI khaki13 = new ZMI(uw);
		uw.addSon(khaki13);
		khaki13.getAttributes().add("level", new ValueInt(2l));
		khaki13.getAttributes().add("name", new ValueString("khaki13"));
		khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
		khaki13.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:03:00.000"));
		list = Arrays.asList(new Value[] {});
		khaki13.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			khaki13Contact,
		});
		khaki13.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("creation", new ValueTime((Long)null));
		khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		khaki13.getAttributes().add("num_cores", new ValueInt(null));
		khaki13.getAttributes().add("num_processes", new ValueInt(107l));
		khaki13.getAttributes().add("has_ups", new ValueBoolean(true));
		list = Arrays.asList(new Value[] {});
		khaki13.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki13.getAttributes().add("expiry", new ValueDuration((Long)null));
		
		ZMI whatever01 = new ZMI(pjwstk);
		pjwstk.addSon(whatever01);
		whatever01.getAttributes().add("level", new ValueInt(2l));
		whatever01.getAttributes().add("name", new ValueString("whatever01"));
		whatever01.getAttributes().add("owner", new ValueString("/uw/whatever01"));
		whatever01.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:12:00.000"));
		list = Arrays.asList(new Value[] {
			violet07Contact, whatever02Contact,
		});
		whatever01.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			whatever01Contact,
		});
		whatever01.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("creation", new ValueTime("2012/10/18 07:03:00.000"));
		whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		whatever01.getAttributes().add("num_cores", new ValueInt(7l));
		whatever01.getAttributes().add("num_processes", new ValueInt(215l));
		list = Arrays.asList(new Value[] {
			new ValueString("rewrite")
		});
		whatever01.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));
		
		ZMI whatever02 = new ZMI(pjwstk);
		pjwstk.addSon(whatever02);
		whatever02.getAttributes().add("level", new ValueInt(2l));
		whatever02.getAttributes().add("name", new ValueString("whatever02"));
		whatever02.getAttributes().add("owner", new ValueString("/uw/whatever02"));
		whatever02.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:13:00.000"));
		list = Arrays.asList(new Value[] {
			khaki31Contact, whatever01Contact,
		});
		whatever02.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			whatever02Contact,
		});
		whatever02.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("creation", new ValueTime("2012/10/18 07:04:00.000"));
		whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
		whatever02.getAttributes().add("num_cores", new ValueInt(13l));
		whatever02.getAttributes().add("num_processes", new ValueInt(222l));
		list = Arrays.asList(new Value[] {
			new ValueString("odbc")
		});
		whatever02.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));
		
		return root;
	}
}
