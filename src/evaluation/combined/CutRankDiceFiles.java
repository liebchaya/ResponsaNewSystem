package evaluation.combined;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CutRankDiceFiles {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File newDir = new File("C:\\Combined\\Combined");
		for(File f: newDir.listFiles()) {
			if (f.getName().endsWith(".txt")) {
				File comFile = new File(newDir+"\\"+f.getName().replace(".txt", ".comb"));
				BufferedReader reader = new BufferedReader(new FileReader(f));
				BufferedWriter writer = new BufferedWriter(new FileWriter(comFile));
				String line = reader.readLine();
				int num = 0;
				int rank = 1;
				while(line != null && num < 50){
					String word = line.split("\t")[0];
					Double score = Double.parseDouble(line.split("\t")[1]);
					if ((num+1) % 2 == 1){
						score = score / rank;
						rank ++;
					}
					num++;
					writer.write(word + "\t" + score + "\n");
					line = reader.readLine();
				}
				writer.close();
				reader.close();
			}
	}

}
}
