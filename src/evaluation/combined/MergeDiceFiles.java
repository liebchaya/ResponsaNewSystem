package evaluation.combined;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import utils.StringUtils;

import ac.biu.nlp.nlp.general.file.FileUtils;

public class MergeDiceFiles {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		String foDir = "C:\\Combined\\Best_Surface";
		String newDir = "C:\\Combined\\Combined";
		File soDir = new File("C:\\Combined\\All_Surface_Surface_LIN");
		for(File f: soDir.listFiles()) {
			if (f.getName().endsWith(".txt")) {
				File foFile = new File(foDir+"\\"+f.getName().replace(".txt", "_Dice.txt"));
				if(!foFile.exists())
					FileUtils.copyFile(f, new File(newDir+"\\"+f.getName()));
				else {
					mergeFiles(f,foFile,newDir);
				}
			}
		}
	}
	
	private static void mergeFiles(File f1, File f2, String outpuFolder) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(outpuFolder+ "\\" + f1.getName()));
		ArrayList<BufferedReader> readersList = new ArrayList<BufferedReader>();
		BufferedReader reader = new BufferedReader(new FileReader(f1));
		readersList.add(reader);
		reader = new BufferedReader(new FileReader(f2));
		readersList.add(reader);
		
		int activeCounter = readersList.size();
		while (activeCounter > 0) {
			activeCounter = readersList.size();
			for(int i=0; i<readersList.size(); i++ ){
				String line = readersList.get(i).readLine();
				if(line == null)
					activeCounter --;
				else {
					writer.write(line+"\n");
				}
			}
		}
	    for(BufferedReader r:readersList)
	    	r.close();
	    writer.close();
	}

}
