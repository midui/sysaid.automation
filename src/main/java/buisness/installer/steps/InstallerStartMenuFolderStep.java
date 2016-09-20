package buisness.installer.steps;

import com.core.base.LogManager;
import com.core.utils.AutoItAPI;

public class InstallerStartMenuFolderStep extends InstallerAbstractStep{

	private String nextButtonID = "1";
	
	@Override
	public void waitTo(String logInfo) {
		LogManager.info("Step: " + logInfo);
		AutoItAPI.waitWin("InstallShield Wizard" , "add program icons to the Program Folder");
		
	}
	
	@Override
	public void waitTo() {
		waitTo("start menu folder");
	}
	
	public void clickNext(){
		AutoItAPI.clickButton("InstallShield Wizard", "add program icons to the Program Folder", nextButtonID);
	}

}
