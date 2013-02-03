package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ac.biu.nlp.nlp.general.BidirectionalMap;
import ac.biu.nlp.nlp.general.SimpleBidirectionalMap;
/**
 * 
 * @author Chaya Liebeskind
 * copy of ReponsaSys.utils.HebFileName
 *
 */
	public class TargetTerm2Id {
		
		private static BidirectionalMap<String,Integer> m_namesMap = null;
		
		public static void loadTargetTerm2IdMapping(File input) throws IOException {
			m_namesMap = new SimpleBidirectionalMap<String, Integer>();
			String encoding = FileUtils.getFileEncoding(input);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), encoding));
			String line = reader.readLine();
			int lineNum = 1;
			boolean testFormat = false;
			if(StringUtils.checkIfNumber(line.split("\t")[0]))
				testFormat = true;
			while(line!= null) {
				if(line.equals("@@@"))
					break;
				if(testFormat) {
					String num = line.split("\t")[0];
					m_namesMap.put(line.trim().substring(num.length()+1), Integer.parseInt(num));
				}
				else 
					m_namesMap.put(line.trim(), lineNum);
				line = reader.readLine();
				lineNum ++;
			}
		}
		
		public static int getIntDesc(String strDesc) {
			return m_namesMap.leftGet(strDesc);
		}
		
		public static String getStrDesc(int intDesc) {
			return m_namesMap.rightGet(intDesc);
		}
		
		public static void renameFiles(File dir){
			for(File file:dir.listFiles()) {
				String fileName = file.getName();
				String[] spfileName = fileName.split("_");
				String newName = getStrDesc(Integer.parseInt(spfileName[0]));
				newName = newName.replaceAll("\"", "_").replaceAll("\'", "");;
				int index = file.getPath().lastIndexOf((File.separator));
				String newPath = file.getPath().substring(0,index) + File.separator + newName + "_" + spfileName[1];
				file.renameTo(new File(newPath));
			}
		}
	}


