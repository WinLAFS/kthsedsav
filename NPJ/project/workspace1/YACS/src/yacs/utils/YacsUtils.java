package yacs.utils;

public class YacsUtils {
	
	public static void ignorantSleep( long millis ){
		try{
			Thread.sleep( millis );
		}
		catch( Exception e ){
			// ignore all errors
		}
	}
	
	public static String nfsRelativeToNfsAbsolute( String nfsRelative ){
		
		String os = System.getProperty("os.name");
		String nfsAbsoluteBase = getNFSBasePath();
		
		if( os.indexOf("Windows") != -1 ){
			String winMod = nfsRelative.replace('/', '\\');
			return nfsAbsoluteBase + winMod;
		}
		else {
			return nfsAbsoluteBase + nfsRelative;
		}
	}
	public static String nfsAbsoluteToNfsRelative( String localPath ){
		
		String os = System.getProperty("os.name");
		String nfsAbsoluteBase = getNFSBasePath();
		
		String pathMod = localPath.substring(nfsAbsoluteBase.length(),localPath.length());
		
		if( os.indexOf("Windows") != -1 ){
			pathMod = pathMod.replace('\\', '/');
		}
		
		return pathMod;
	}
	
	
	public static String getNFSBasePath(){
		String nfsAbsoluteBase = System.getProperty("yacs.nfs.base");
		
		if( nfsAbsoluteBase == null || nfsAbsoluteBase.length() == 0 ){
			throw new IllegalArgumentException("Setting: yacs.nfs.base is missing!");
		}
		
		return nfsAbsoluteBase;
	}

}
