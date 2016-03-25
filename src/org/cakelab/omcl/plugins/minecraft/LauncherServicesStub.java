package org.cakelab.omcl.plugins.minecraft;




/*
 * DO NOT IMPORT ANYTHING FROM MOJANG HERE!
 * 
 * (that was the purpose of the whole project!)
 * 
 * 
 */


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import org.cakelab.omcl.plugins.ServicesStubBase;
import org.cakelab.omcl.plugins.StubException;
import org.cakelab.omcl.plugins.interfaces.ServicesListener;






public class LauncherServicesStub extends ServicesStubBase {
	private static final String SERVICES_PACKAGE = "org.cakelab.omcl.plugins.minecraft.launcher";
	private static final String SERVICES_CLASS = SERVICES_PACKAGE + ".LauncherServices";
	

	private Method methodInstallVersion;
	private Object instance;
	private Method method_launchSelectedProfile;
	
	// TODO: remove this workaround. It was introduced to temporary solve class loading issues
	/* 
	 * To remove this, we need to make the system forget 
	 * about all classes that have been loaded in the context of
	 * this plugin. Otherwise there exist classes loaded 
	 * from a different classloader earlier which results in
	 * IllegalAccessExceptions in privileged class loading.
	 */
	private static LauncherServicesStub INSTANCE = null;

	public LauncherServicesStub(ClassLoader classLoader, Object instance, Method methodInstallVersion, Method method_launchSelectedProfile) {
		super(classLoader);
		this.instance = instance;
		this.methodInstallVersion = methodInstallVersion;
		this.method_launchSelectedProfile = method_launchSelectedProfile;
	}

	public static LauncherServicesStub create(File launcherJar, ServicesListener listener) throws IOException, StubException {
		// TODO: md5 check on launcher.jar when a call fails
		if (INSTANCE != null) return INSTANCE;
		
		
		final String className = SERVICES_CLASS;
		final String methodName_installVersion = "installVersion";
		final String methodName_launchSelectedProfile = "launchSelectedProfile";
		
		Object stubInstance;
		Method methodInstallVersion;
		Method method_launchSelectedProfile;
		URLClassLoader classLoader = null;
		if (launcherJar.exists()) {
			try {
				classLoader = createURLClassLoader(launcherJar, new String[]{SERVICES_PACKAGE});
				enterPluginContext(classLoader);
				
				Class<?> launcherClass = classLoader.loadClass(className);
				
				// default constructor LauncherService()
				Constructor<?> constructor = launcherClass.getConstructor(ServicesListener.class);
				
				// invoke constructor
				stubInstance = constructor.newInstance(listener);
				
				// method void installVersion(String version, File workDir)
				methodInstallVersion = launcherClass.getDeclaredMethod (methodName_installVersion, String.class, File.class);
				method_launchSelectedProfile = launcherClass.getDeclaredMethod (methodName_launchSelectedProfile, File.class);
			} catch (Throwable t) {
				throw new StubException("jar " + launcherJar.getName() + " incompatible", t);
			} finally {
				leavePluginContext(classLoader);
			}
		} else {
			// bail
			throw new IOException("Could not load launcher.jar at given location '" + launcherJar + "'");
		}		
		
		
		INSTANCE = new LauncherServicesStub(classLoader, stubInstance, methodInstallVersion, method_launchSelectedProfile);
		return INSTANCE;
	}

	
	public boolean installVersion(String version, File workDir) throws StubException {
		enterPluginContext();
		boolean result;
		try {
			result  = (boolean)this.methodInstallVersion.invoke(this.instance, version, workDir);
		} catch (Throwable e) {
			while (e instanceof StubException || e instanceof InvocationTargetException) e = e.getCause();
			throw new StubException("failed to install minecraft version " + version + " in directory " + workDir.getPath(), e);
		} finally {
			leavePluginContext();
		}
		return result;
	}

	public boolean launchSelectedProfile(File workDir) throws StubException {
		enterPluginContext();
		boolean result;
		try {
			result = (boolean)this.method_launchSelectedProfile.invoke(this.instance, workDir);
		} catch (Throwable e) {
			while (e instanceof StubException || e instanceof InvocationTargetException) e = e.getCause();
			throw new StubException("failed to invoke " + this.method_launchSelectedProfile.getName(), e);
		} finally {
			leavePluginContext();
		}
		return result;
	}

	public static void main(String [] args) {
		try {
			File workDir = new File(System.getProperty("user.home"), ".minecraft");
			File jarFile = new File(workDir, "launcher.jar");
			LauncherServicesStub launcher = create(jarFile, null);
			launcher.launchSelectedProfile(workDir);
		} catch (IOException | StubException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
