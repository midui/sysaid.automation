package buisness.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import buisness.db.DBInstaller;
import buisness.modules.SysAid.InstallType;

import com.core.base.LogManager;
import com.core.base.TestManager;
import com.core.utils.AutoItAPI;
import com.core.utils.SystemUtils;

public class SysAidServer {
	
	//private static boolean upgradeProcess = false;
	private static String fromVersion;
	
	private static boolean useDefaultExe = false;
	public static String server_ver = "unknown";
	private static String server_build = "unknown";
	public static String exeName;
	public static String exePath;
	private static String activationFilePath = "c:\\SA\\activation.xml";
	
	private static List<String> filesList;
	private static String serverPath = "C:\\Program Files\\SysAidServer";
	private static String tomcatPath = "C:\\Program Files\\SysAidServer\\tomcat";
	private static String webInfPath = "C:\\Program Files\\SysAidServer\\root\\WEB-INF";
	private static String log4jPath = "C:\\Program Files\\SysAidServer\\root\\WEB-INF\\log4j.properties";
	
	private static String accountConfPath = "C:\\Program Files\\SysAidServer\\root\\WEB-INF\\conf\\accountConf.xml";
	private static String upgradeToNewReportsPath = "C:\\Program Files\\SysAidServer\\root\\WEB-INF\\logs\\upgradeToNewReports.log";
	private static String qschedulerLogPath = "C:\\Program Files\\SysAidServer\\root\\WEB-INF\\logs\\q-scheduler.log";
	
	static{
		exeName = "SysAidServer64_default.exe";
		initFiles();
	}
	
	
	public static void initInstaller(){
		
		String upgradeFrom = System.getProperty("upgradeFrom"); //16.1.25.b28
		if(upgradeFrom != null && !upgradeFrom.equals("")){
			LogManager.debug("bese version : " + upgradeFrom);
			System.setProperty("upgradeFrom", "");//next time it will init to current version (upgrade process)
			fromVersion = server_ver = upgradeFrom.substring(0,upgradeFrom.lastIndexOf("."));
			server_build = upgradeFrom.substring(upgradeFrom.indexOf("b")+1);
		}else{
			initCurrentVersion();
		}
	
		initExeEnv(false);
		
		LogManager.assertTrue(SystemUtils.Files.isFileExist(exePath), "verify exe exists: " + exePath);
	}
	
	
	
	public static void initUpgrade(){
		initCurrentVersion();
		
		initExeEnv(true);
		
		/*if(!useDefaultExe){
			LogManager.info(String.format("SysAid Upgrade Version:%s , Build: %s",server_ver,server_build));
			exeName = String.format("SysAidServerPatch64_%s_b%s.exe",server_ver.replace(".", "_"),server_build);
		}
		String srcPath= String.format("\\\\fs01.ilient-hq.local\\sysaidarchive\\IT\\%s\\b%s\\PaidUpgrades\\%s",server_ver,server_build,exeName);
		//Check OS Bit
		if(SystemUtils.OS.is32Bit()){
			exeName = exeName.replace("64", "");
			srcPath = srcPath.replace("64", "");
		}
		
		if(useDefaultExe){
			exePath = "C:\\SA\\debug\\" + exeName;
			activationFilePath = "c:\\SA\\debug\\activation.xml";
		}else{
			exePath = "C:\\SA\\" + exeName;
			SystemUtils.Files.copyFile(srcPath, exePath); //copy exe file to SA folder from network
		}
		*/
		LogManager.assertTrue(SystemUtils.Files.isFileExist(exePath), "verify exe exists: " + exePath);
	}
	
	
	

	private static void initExeEnv(boolean isUpgraseProcess) {
		String srcPath = "";
		if(!useDefaultExe){
			LogManager.info(String.format("SysAid Version:%s , Build: %s",server_ver,server_build));
			exeName = String.format("SysAidServer64_%s_b%s.exe",server_ver.replace(".", "_"),server_build);
			
			String path = "\\\\fs01.ilient-hq.local\\sysaidarchive\\IT";
			File versionFile = SystemUtils.Files.getFile(path, server_ver);
			File buildFile = SystemUtils.Files.getFile(versionFile.getAbsolutePath(), server_build);
			
			srcPath= String.format("%s\\installations\\%s",buildFile.getAbsoluteFile(),exeName);
		}
		
		
		if(isUpgraseProcess){
			exeName = exeName.replace("64", "Patch64");
			srcPath = srcPath.replace("64", "Patch64").replace("installations", "PaidUpgrades");
		}
		
		//Check OS Bit
		if(SystemUtils.OS.is32Bit()){
			exeName = exeName.replace("64", "");
			srcPath = srcPath.replace("64", "");
		}
		
		if(useDefaultExe){
			exePath = "C:\\SA\\debug\\" + exeName;
			activationFilePath = "c:\\SA\\debug\\activation.xml";
		}else{
			exePath = "C:\\SA\\" + exeName;
			SystemUtils.Files.copyFile(srcPath, exePath); //copy exe file to SA folder from network
		}
	}

	
	/**
	 * update current version  install process
	 */
	private static void initCurrentVersion() {
		server_ver = System.getProperty("version");
		server_build = System.getProperty("build");
		if(server_ver == null || server_ver.equals("unknown")){
			LogManager.warn("sysaid version didn't defined via system properties! install default exe");
			useDefaultExe = true;
		}
		else if(server_build == null || server_build.equals("unknown")){
			LogManager.warn("sysaid build version didn't defined via system properties! install default exe");
			useDefaultExe = true;
		}
	}
	
	
	
	
	
	
	
	
	/*public static void initInstaller(){
		initCurrentVersion();
	
		initExeEnv();
		
		LogManager.assertTrue(SystemUtils.Files.isFileExist(exePath), "verify exe exists: " + exePath);
	}*/

	private static void initFiles() {
		filesList = new ArrayList<String>();
		//add
		filesList.add(serverPath);
		filesList.add(tomcatPath);
		filesList.add(webInfPath);
	}
	
	
	
	public static String getActivationFilePath(){
			return activationFilePath;
	}
	
	
	/**
	 *  Test #2 Verification - SysAid Server installation
	 */
	public static void verifyInstallation(){
		LogManager.bold("SysAid Server : Verify Installation");
		changeLog4J();
		verifyServices();
		verifyProcesses();
		verifyDesktopIcon();
		verifyDirectories();
		verifyConfigurationFiles();
		//verifySysAidLog();
		verifyQschedulerLog();
		verifyLoginBrowser();
		verifyupgradeToNewReports();
	}
	
	
	
	public static void verifyUpgradeProcess(){ //TODO : might be in dedicate class?
		LogManager.debug("Verify Upgrade Process");
		AutoItAPI.waitWin("Upgrade", "", 30);
		AutoItAPI.verifyWinActivate("Upgrade", true);
	}
	
	
	//Verify Browser opened with 2 tabs
	public static void verifyLoginBrowser(){
		LogManager.debug("Verify Browser opened with 2 tabs ..");
		SystemUtils.Processes.verify("chrome.exe", true);
		if(AutoItAPI.softWait("SysAid Help Tree", "", 5)){
			// Browser is focus on 'SysAid Help Tree' - Tab
			AutoItAPI.activateWindow("SysAid Help Tree","");
			AutoItAPI.verifyWinActivate("SysAid Help Tree", true);
			TestManager.sleep(1000);
			SystemUtils.Keyboard.switchBrowserTab();
			AutoItAPI.softWait("SysAid Help Desk Software", "", 30);
			AutoItAPI.verifyWinActivate("SysAid Help Desk Software",true);
		}else{
			// Browser is focus on 'SysAid Help Desk Software' - Tab
			AutoItAPI.softWait("SysAid Help Desk Software", "", 30);
			AutoItAPI.activateWindow("SysAid Help Desk Software","");
			AutoItAPI.verifyWinActivate("SysAid Help Desk Software", true);
			TestManager.sleep(1000);
			SystemUtils.Keyboard.switchBrowserTab();
			AutoItAPI.softWait("SysAid Help Tree", "", 5);
			AutoItAPI.verifyWinActivate("SysAid Help Tree",true);
		}
		SystemUtils.Processes.killProcess("chrome.exe");
		
		
	}
	 
	private static void verifyQschedulerLog() {
		SystemUtils.Files.scanByLine(qschedulerLogPath, "Error", "Exception");
	}

	private static void verifyupgradeToNewReports() {
		LogManager.debug("Verify upgradeToNewReports.log ..");
		BufferedReader br = null;
		try {
			//wait for file
			if(!SystemUtils.Files.waitForFile(upgradeToNewReportsPath, 180 *1000, 5 * 1000))
				throw new Exception("File is missing");
			br = new BufferedReader(new InputStreamReader(new FileInputStream(upgradeToNewReportsPath)));
			String line;
			Stack<String> stack = new Stack<String>();
			while ((line = br.readLine()) != null) {
				LogManager.debug("Line = " + line);
				if(line.contains("ERROR")){
					LogManager.assertSoft(false, "upgradeToNewReports.log file contains error : " + line);
				}else if(line.contains("Start")){
					stack.push("Start");
				}else if(line.contains("End")){
					if(stack.isEmpty()){
						LogManager.assertSoft(false, "Incorrect Start -End Order. see debug log");
						break;
					}else{
						stack.pop();
					}
				}
			}
			// after reading all lines , stack should be empty
			LogManager.verify(stack.isEmpty(), "Verify upgradeToNewReports.log - correct Start-End tags order");
		} catch (Exception e) {
			LogManager.error("Verify upgradeToNewReports.log - Error : " + e.getMessage());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception e) {
			}
		}
	}

	

	//Verify configuration files in C:\Program Files\SysAidServer\root\WEB-INF\conf 
	public static void verifyConfigurationFiles() {
		// verify sysaid.ver file:
		LogManager.debug("Verify configurations file : sysaid.ver..");
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("C:\\Program Files\\SysAidServer\\root\\WEB-INF\\conf\\sysaid.ver"));
			String rdsVer = properties.getProperty("rdsVersion");
			String ver = properties.getProperty("version");
			String clientVersion = properties.getProperty("clientVersion");
			LogManager.assertSoft(rdsVer.equals(server_ver), String.format("Incorrect Server Version! Expected : %s , Actual: %s" ,server_ver , rdsVer ));
			LogManager.assertSoft(clientVersion.equals(server_ver), String.format("Incorrect Server Version! Expected : %s , Actual: %s" ,server_ver , clientVersion ));
			LogManager.assertSoft(ver.equals("v"+server_ver), String.format("Incorrect Server Version! Expected : %s , Actual: %s" ,server_ver , ver ));
		
		}catch (Exception e) {
			LogManager.error("Verify configurations file - Error : " + e.getMessage());
		}
		
		//verify serverconf.xml file exist.. // TODO: Should check content instead on DBInstaller class! (Discussion with Alex)
		SystemUtils.Files.verifyExist(DBInstaller.serverConfPath, true);
		
		// Verify accountConf file exist , add TODO TBD (content)
		SystemUtils.Files.verifyExist(accountConfPath, true);
		
	}

	/**
	 * Test# 11 , #12
	 */
	public static void verifyDB(){
		LogManager.bold("Verification - MSSQL embedded");
		if(SysAid.getInstallType() == InstallType.TYPICAL)
			DBInstaller.verifyMsSqlEmbedded();
		DBInstaller.verifyTablesCount();
		DBInstaller.verifyTableContents();
	}
	
	
	
	public static void verifyProcesses(){ 
		SystemUtils.Processes.verify("Wrapper.exe", true); //TODO : Should be an enum  ?Is a RDS Process?
	}
	
	public static void verifyServices(){
		SystemUtils.Services.verify("SysAid Server", true); //TODO : Should be an enum
	}
	
	public static void verifyDesktopIcon(){
		SystemUtils.Files.verifyExist("SysAid Login.lnk", SystemUtils.Files.getPublicDesktopPath(), true); //TODO : Should be an enum
	}
	
	public static void verifyDirectories(){
		for (String file : filesList) {
			SystemUtils.Files.verifyExist(file, true);
		}
	}
	
	public static void changeLog4J(){
		SystemUtils.Files.replaceLine(log4jPath, "log4j.logger.com.ilient=INFO, sysaidLogFile", "log4j.logger.com.ilient=DEBUG, sysaidLogFile");
	}
	
	

}
