package org.cakelab.omcl.setup.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;

public class NBTFile {
	
	private File file;
	protected boolean modified = true;
	protected CompoundTag root;


	public void loadFile(File file) throws IOException {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			load(in);
			this.file = file;
			this.modified = false;
		} finally {
			if (in != null) in.close();
		}
	}
	

	public void save(File f) throws IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);
			store(out);
			this.file = f;
			modified = false;
		} finally {
			if (out != null) out.close();
		}
	}
	
	public void save() throws IOException {
		save(file);
	}

	public void setFile(File f) {
		this.file = f;
		modified = true;
	}
	
	public boolean isModified() {
		return modified;
	}
	
	
	public void load(InputStream in) throws IOException {
		// we are not supposed to close the stream here
		@SuppressWarnings("resource")
		NBTInputStream nbtIn = new NBTInputStream(in);
		//
		// this reads the entire file and returns the 
		// root of the NBT tree.
		//
		root = (CompoundTag)nbtIn.readTag();
	}

	public void store(OutputStream out) throws IOException {
		// we are not supposed to close the stream here
		@SuppressWarnings("resource")
		NBTOutputStream nbtOut = new NBTOutputStream(out);
		nbtOut.writeTag(root);
	}

}
