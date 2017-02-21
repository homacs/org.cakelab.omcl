package org.cakelab.omcl.plugins.minecraft;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import org.cakelab.omcl.plugins.ServicesStubBase;
import org.cakelab.omcl.plugins.StubException;
import org.cakelab.omcl.plugins.interfaces.ServicesListener;


public class BootstrapServicesStub extends ServicesStubBase {
	private static final String SERVICES_PACKAGE = "org.cakelab.omcl.plugins.minecraft.bootstrap";
	private static final String SERVICES_CLASS = SERVICES_PACKAGE + ".BootstrapServices";
	private Object stubInstance;
	private Method method_initWorkDir;
	
	private BootstrapServicesStub(URLClassLoader classLoader, Object stubInstance, Method method_initWorkDir) {
		super(classLoader);
		this.stubInstance = stubInstance;
		this.method_initWorkDir = method_initWorkDir;
	}
	
	
	
	public static BootstrapServicesStub create(File bootstrapJar, ServicesListener listener) throws StubException, IOException {
		
		final String className = SERVICES_CLASS;
		final String methodName_initWorkDir = "initWorkDir";
		
		Object stubInstance;
		Method method_initWorkDir;
		URLClassLoader classLoader = null;
		if (bootstrapJar.exists()) {
			try {
				classLoader = createURLClassLoader(BootstrapServicesStub.class, bootstrapJar, new String[]{SERVICES_PACKAGE});
				enterPluginContext(classLoader);
				
				Class<?> launcherClass = classLoader.loadClass(className);
				
				Constructor<?> constructor = launcherClass.getConstructor(ServicesListener.class);
				
				stubInstance = constructor.newInstance(listener);
				
				
				// method void initWorkDir(File workDir) 
				method_initWorkDir = launcherClass.getDeclaredMethod (methodName_initWorkDir, File.class);
			} catch (Throwable t) {
				throw new StubException("jar " + bootstrapJar.getName() + " incompatible", t);
			} finally {
				leavePluginContext(classLoader);
			}
		} else {
			// bail
			throw new IOException("Could not load Minecraft.jar at given location '" + bootstrapJar + "'");
		}		
		
		
		return new BootstrapServicesStub(classLoader, stubInstance, method_initWorkDir);
	}
	
	
	
	public void initWorkDir(File workDir) throws StubException {
		enterPluginContext();
		try {
			this.method_initWorkDir.invoke(stubInstance, workDir);
		} catch (Throwable e) {
			throw new StubException("failed to invoke " + this.method_initWorkDir.getName(), e);
		} finally {
			leavePluginContext();
		}
	}
	

	
	public static void main (String [] args) {
		// Testing
		try {
			BootstrapServicesStub stub = create(new File("/home/homac/workspace-MCLauncher/MinecraftWrapper/libs/Minecraft.jar"), null);
			stub.initWorkDir(new File(System.getProperty("user.home"), ".minecraft"));
		} catch (StubException | IOException e) {
			// testing
			e.printStackTrace();
		}
	}
	
	
}
