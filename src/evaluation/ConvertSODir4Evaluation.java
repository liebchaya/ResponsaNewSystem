package evaluation;

import java.io.File;
import java.io.IOException;

import utils.CopyFilesDir;

import ac.biu.nlp.nlp.general.file.FileUtils;

public class ConvertSODir4Evaluation {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/**/ String inputDirName = "C:\\SOStatisticsFolder";
		/**/ String outputDirName = "C:\\SONewStatisticsFolder";
		
		File inputDir = new File(inputDirName); 
		for (File conf:inputDir.listFiles()) {
			for (File method:conf.listFiles()) {
				if (method.isDirectory()) {
				for (File targetConf:method.listFiles()) {
					String finalConfName = targetConf.getName() + "_" + conf.getName() + "_" + method.getName();
					File finalConfDir = new File(outputDirName+"\\"+finalConfName);
					CopyFilesDir.copy(targetConf.getAbsoluteFile(), finalConfDir);
				}
			}
			}
		}
	}

}
