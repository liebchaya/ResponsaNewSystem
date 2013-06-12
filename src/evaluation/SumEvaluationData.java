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
		/**/	File outputDir = new File("C:\\Combined\\CombinedPer");		
		/**/ 	String clusteringType = "clusters50_SUMSCORE_2";
		/**/ 	String evalFileName = "evalFinal.txt";
		BufferedWriter writerSum = new BufferedWriter(new FileWriter(outputDir + "\\" + clusteringType + "COM.txt"));
//		for( File f: outputDir.listFiles()) {
//			if (f.isDirectory()&& /* !f.getName().contains("COVER") && !*/ f.getName().contains("LIN")) {
		File f = new File("C:\\Combined\\CombinedPer");
				BufferedReader reader = new BufferedReader(new FileReader(f.getAbsolutePath() + "\\" + clusteringType + "\\" + evalFileName) );
				BufferedWriter writer = new BufferedWriter(new FileWriter(f.getAbsolutePath() + "\\" + clusteringType + "\\relativeRecall15GSOSum.txt"));
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
					if(spLine[3].equals("NaN"))
						sumR+=0;
					else
						sumR+=Double.parseDouble(spLine[3]);
					if(spLine[2].equals("NaN"))
						sumP+=0;
					else
						sumP+=Double.parseDouble(spLine[2]);
//					sumR+=Double.parseDouble(spLine[3]);
//					sumP+=Double.parseDouble(spLine[2]);
					if(spLine[4].equals("NaN"))
						sumF1+=0;
					else
						sumF1+=Double.parseDouble(spLine[4]);
					if(spLine[5].equals("NaN"))
						sumAP+=0;
					else
						sumAP+=Double.parseDouble(spLine[5]);
//					sumAP+=Double.parseDouble(spLine[5]);
					line = reader.readLine();
				}
				System.out.println("Recall sum: " +sumR);
				writer.write(sumP/(double)count+"\t"+sumR/(double)108+"\t"+sumF1/(double)108+"\t"+sumAP/(double)108+"\n");
				writerSum.write(f.getAbsolutePath() + "\t" + sumP/(double)count+"\t"+sumR/(double)108+"\t"+sumF1/(double)108+"\t"+sumAP/(double)108+"\n");
		//		writer.write(sumP/(double)count+"\t"+sumR/(double)count+"\t"+sumF1/(double)count+"\t"+sumAP/(double)count+"\n");
				writer.close();
				reader.close();
//				//bw.write("\n************************************\n");
//			}
//		}
		writerSum.close();
	}


	}


