package evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SumEvaluationData {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/**/	File outputDir = new File("C:\\SONewStatisticsFolder");		
		/**/ 	String clusteringType = "clusters50_SUMSCORE_2";
		/**/ 	String evalFileName = "eval.txt";
		BufferedWriter writerSum = new BufferedWriter(new FileWriter(outputDir + "\\" + clusteringType + ".txt"));
		for( File f: outputDir.listFiles()) {
			if (f.isDirectory()) {
				BufferedReader reader = new BufferedReader(new FileReader(f.getAbsolutePath() + "\\" + clusteringType + "\\" + evalFileName) );
				BufferedWriter writer = new BufferedWriter(new FileWriter(f.getAbsolutePath() + "\\" + clusteringType + "\\relativeRecall15GSum.txt"));
				String line=reader.readLine();
				double count = 0, sumR = 0, sumP = 0, sumF1 = 0, sumAP = 0;
				while(line!=null){
					if(!line.contains("groupJudges")||line.contains("missing")||line.contains("בנקאי")||line.contains("מגל")||line.contains("הרפתקאות")||line.contains("קטטר")||line.contains("מסתמי הלב"))
					{
						line=reader.readLine();
						continue;
					}
					count++;
					String[] spLine = line.split("\t");
					System.out.println(line);
					sumR+=Double.parseDouble(spLine[3]);
					sumP+=Double.parseDouble(spLine[2]);
					if(spLine[4].equals("NaN"))
						sumF1+=0;
					else
						sumF1+=Double.parseDouble(spLine[4]);
					sumAP+=Double.parseDouble(spLine[5]);
					line = reader.readLine();
				}
				System.out.println("Recall sum: " +sumR);
				writer.write(sumP/(double)count+"\t"+sumR/(double)108+"\t"+sumF1/(double)108+"\t"+sumAP/(double)108+"\n");
				writerSum.write(f.getAbsolutePath() + "\t" + sumP/(double)count+"\t"+sumR/(double)108+"\t"+sumF1/(double)108+"\t"+sumAP/(double)108+"\n");
		//		writer.write(sumP/(double)count+"\t"+sumR/(double)count+"\t"+sumF1/(double)count+"\t"+sumAP/(double)count+"\n");
				writer.close();
				reader.close();
				//bw.write("\n************************************\n");
			}
		}
		writerSum.close();
	}


	}


