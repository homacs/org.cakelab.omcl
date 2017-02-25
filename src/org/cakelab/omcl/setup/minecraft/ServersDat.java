package org.cakelab.omcl.setup.minecraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

public class ServersDat extends NBTFile {
	public static final String FILENAME = "servers.dat";
	private static final String PROPERTY_SERVERS = "servers";
	private static final String ROOT_NAME = "";
	

	public static ServersDat loadFromGamedir(File gamedir) throws IOException {
		File f = new File(gamedir, FILENAME);
		ServersDat result = new ServersDat();
		if (f.exists())result.loadFile(f);
		else result.setFile(f);
		return result;
	}

	
	private ServersDat() {
		Map<String, Tag> rootMembers = new HashMap<String, Tag>();
		rootMembers.put(PROPERTY_SERVERS, new ListTag(PROPERTY_SERVERS, CompoundTag.class, new ArrayList<Tag>()));
		this.root = new CompoundTag(ROOT_NAME, rootMembers);
	}
	

	
	private int findServer(String ip) {
		if (root.getValue() == null 
				|| !root.getValue().containsKey(PROPERTY_SERVERS)) {
				return -1;
		} else {
			ListTag serverList = (ListTag) root.getValue().get(PROPERTY_SERVERS);
			for (int i = 0; i < serverList.getValue().size(); i++) {
				Tag entry = serverList.getValue().get(i);
				CompoundTag serverInfo = (CompoundTag) entry;
				String serverIp = (String) serverInfo.getValue().get("ip").getValue();
				if (serverIp.equals(ip)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	
	public boolean containsServer(String ip) {
		return findServer(ip) >= 0;
	}



	public void addServer(String ip, String name, String icon) {
		Map<String, Tag> info = new HashMap<String, Tag>();
		info.put("ip", new StringTag("ip", ip));
		info.put("name", new StringTag("name", name));
		info.put("icon", new StringTag("icon", icon));
		addServer(info);
	}

	public void addServer(String ip, String name) {
		Map<String, Tag> info = new HashMap<String, Tag>();
		info.put("ip", new StringTag("ip", ip));
		info.put("name", new StringTag("name", name));
		addServer(info);
	}


	protected void addServer(Map<String, Tag> info) {
		CompoundTag serverInfo = new CompoundTag("server", info);
		ListTag serverList = (ListTag) root.getValue().get(PROPERTY_SERVERS);
		serverList.getValue().add(serverInfo);
		modified = true;
	}
	
	
	public void removeServer(String ip) {
		int i = findServer(ip);
		if (i >= 0) {
			ListTag serverList = (ListTag) root.getValue().get(PROPERTY_SERVERS);
			serverList.getValue().remove(i);
		}
	}

}
