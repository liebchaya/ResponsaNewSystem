package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MutualRankRatio {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		BigramsMutualRankRatio bMRR = new BigramsMutualRankRatio(new File("C:\\responsaOld2.cnt"));
		BigramsMutualRankRatio bMRR = new BigramsMutualRankRatio(new File("/home/ir/liebesc/NgramStatCount/modernJewish2.cnt"));
		TrigramsMutualRankRatio tMRR = new TrigramsMutualRankRatio(new File("/home/ir/liebesc/NgramStatCount/modernJewish3.cnt"));
		FourgramsMutualRankRatio fMRR = new FourgramsMutualRankRatio(new File("/home/ir/liebesc/NgramStatCount/modernJewish4.cnt"));
		
		BigramsMutualRankRatio bMRRold = new BigramsMutualRankRatio(new File("/home/ir/liebesc/NgramStatCount/responsaOld2.cnt"));
		TrigramsMutualRankRatio tMRRold = new TrigramsMutualRankRatio(new File("/home/ir/liebesc/NgramStatCount/responsaOld3.cnt"));
		FourgramsMutualRankRatio fMRRold = new FourgramsMutualRankRatio(new File("/home/ir/liebesc/NgramStatCount/responsaOld4.cnt"));
		
		File dir = new File("/home/ir/liebesc/ResponsaSys/output/modernJewishOnly/Surface_Surface/");
//		File f = new File("C:\\28_ModernCounts.txt");
		
		for (File f:dir.listFiles()) {
			System.out.println("File: " + f.getAbsolutePath());
			if (f.getName().endsWith("_ModernCounts.txt")) {
				BufferedReader reader = new BufferedReader(new FileReader(f));
//				BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\" + f.getName().replace("_ModernCounts.txt","_MWEOldScores.txt")));
				BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/" + f.getName().replace("_ModernCounts.txt","_MWEOldScores.txt")));
				String line = reader.readLine();
				while (line != null) {
					String ngram = line.split("\t")[0];
					int oldCount = Integer.parseInt(line.split("\t")[3]);
					String[] tokens = ngram.split(" ");
					double score = 0.0;
					if (oldCount > 0) {
						if (tokens.length == 2)
							if (bMRRold.exist(ngram))
								score = bMRRold.mutualRankRatio(ngram);
						if (tokens.length == 3)
							if (tMRRold.exist(ngram))
								score = tMRRold.mutualRankRatio(ngram);
						if (tokens.length == 4)
							if (fMRRold.exist(ngram))
								score = fMRRold.mutualRankRatio(ngram);
					} else {
						if (tokens.length == 2)
							if (bMRR.exist(ngram))
								score = bMRR.mutualRankRatio(ngram);
						if (tokens.length == 3)
							if (tMRR.exist(ngram))
								score = tMRR.mutualRankRatio(ngram);
						if (tokens.length == 4)
							if (fMRR.exist(ngram))
								score = fMRR.mutualRankRatio(ngram);
						}
					if (tokens.length > 1)
						writer.write(line + "\t" + score + "\n");
					line = reader.readLine();
				}
				reader.close();
				writer.close();
			}
		}
	}

}
