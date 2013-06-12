package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import obj.Pair;

import utils.StringUtils;

public class OldTargetTermExp {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String confDir = "/home/ir/liebesc/ResponsaSys/output/baseline/Surface_Surface";
		String newClusterDir = confDir + "/clusters50_SUMSCORE_2";
		String targetTermPrev = "/home/ir/liebesc/ResponsaSys/input/targetTerms_withoutWiki.txt";
		String outputFile = "/home/ir/liebesc/ResponsaSys/input/tries/targetTerms_oldExpShmulik100MWEBaseline.txt";
		
		/*String confDir = "C:/ResponsaSys/output/expAfterwikiBaseLine/Surface_Surface";
		String newClusterDir = confDir + "/clusters50_SUMSCORE_2";
		String targetTermPrev = "C:/ResponsaSys/input/targetTerms_expAfterWiki.txt";
		String outputFile = "C:/ResponsaSys/input/tries/targetTerms_oldExp.txt";*/
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		
		// read the suitable input file
		HashMap<Integer,String> queries = new HashMap<Integer,String>();
		BufferedReader reader = new BufferedReader(new FileReader(targetTermPrev));
		String line = reader.readLine();
		while (line != null){
			int index = line.indexOf("\t");
			queries.put(Integer.parseInt(line.substring(0,index)),line.substring(index+1));
			line = reader.readLine();
		}
		reader.close();
		
		File mainDir = new File(confDir);
		for(File f:mainDir.listFiles()){
			if(f.getAbsolutePath().endsWith("_NoModern.filter")){
				int queryId = Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_")));
				if (queries.containsKey(queryId)){
					String queryLine = queryId + "\t" + queries.get(queryId);
					HashMap<String, String> data = loadFileToHash(f);
					File newClustersFile = new File(newClusterDir+"/"+queryId+"_NoModern.clustersNew");
					reader = new BufferedReader(new FileReader(newClustersFile));
					int counter=0;
					line = reader.readLine();
					while (line != null && counter<2){
						String clusterString = line.split("\t")[0];
						HashSet<String> cluster = StringUtils.convertStringToSet(clusterString);
						System.out.println(clusterString + " " + cluster.size());
						
						if (cluster.size()>=2){
							
							HashMap<String,Pair<Integer,Integer>> longPartCluster = new HashMap<String, Pair<Integer,Integer>>();
							// expression desired length
							int maxLen = 0;
							for(String cls:cluster){
								if(cls.split(" ").length>maxLen)
									maxLen = cls.split(" ").length;
							}
							// data on expressions with the desired length
							for(String cls:cluster){
								if(cls.split(" ").length==maxLen){
									String[] dataExp = data.get(cls).split("\t");
									if (Double.parseDouble(dataExp[7])> 0)
										longPartCluster.put(cls, new Pair<Integer, Integer>((int)Double.parseDouble(dataExp[1]),Integer.parseInt(dataExp[2])));
								}
							}
							for(String cls:longPartCluster.keySet()){
								if(longPartCluster.get(cls).value()<100)
									queryLine = queryLine + "\t" + cls;
							}
							counter++;
							
						
						
//						HashMap<String,Pair<Integer,Integer>> longPartCluster = new HashMap<String, Pair<Integer,Integer>>();
//						// expression desired length
//						int maxLen = 0;
//						for(String cls:cluster){
//							if(cls.split(" ").length>maxLen)
//								maxLen = cls.split(" ").length;
//						}
//						// data on expressions with the desired length
//						for(String cls:cluster){
//							if(cls.split(" ").length==maxLen){
//								String[] dataExp = data.get(cls).split("\t");
//								if (Double.parseDouble(dataExp[7])> 0)
//									longPartCluster.put(cls, new Pair<Integer, Integer>((int)Double.parseDouble(dataExp[1]),Integer.parseInt(dataExp[2])));
//							}
//						}
//						// data processing for filtering
//						int oldCorpusFreq = 0;
//						boolean bMoreThenOnce = false;
//						for(String cls:longPartCluster.keySet()){
//							oldCorpusFreq += longPartCluster.get(cls).value();
//							if (longPartCluster.get(cls).key()>1)
//								bMoreThenOnce = true;
//						}
//						if (oldCorpusFreq < 50 && bMoreThenOnce){
//							for(String cls:longPartCluster.keySet()){
//								queryLine = queryLine + "\t" + cls;
//							}
//						}
					}
//					counter++;
					line = reader.readLine();
				}
				reader.close();
				writer.write(queryLine+"\n");
			}
		}
		}
		writer.close();
	}
	
	private static HashMap<String,String> loadFileToHash(File f) throws IOException {
		// read the suitable input file
		HashMap<String,String> data = new HashMap<String,String>();
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String line = reader.readLine();
		while (line != null){
			int index = line.indexOf("\t");
			data.put(line.substring(0,index),line.substring(index+1));
			line = reader.readLine();
		}
		reader.close();
		return data;
	}

}
