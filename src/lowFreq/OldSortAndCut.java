package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class OldSortAndCut {
	
	public class Item implements Comparable<Item> {
	    private String m_s;
	    private Double m_double1;
	    private Double m_double2;
	    
	    public Item(String s, Double double1, Double double2){
	    	m_s = s;
	    	m_double1 = double1;
	    	m_double2 = double2;
	    }

	    @Override
	    public int compareTo(Item o) {
	        int compare = o.m_double2.compareTo(m_double2);
	        return compare != 0 ? compare : o.m_double1.compareTo(m_double1);
//	    	return o.m_double2.compareTo(m_double2);
	    }
	}
	
	public List<Item> oldTermLoader(File input) throws IOException{
		LinkedList<Item> content = new LinkedList<Item>();
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line = reader.readLine();
		line = reader.readLine();
		while(line != null){
			String[] tokens = line.split("\t");
			Item i = new Item(tokens[0],Double.parseDouble(tokens[1]),Double.parseDouble(tokens[8]));
			content.add(i);
			line = reader.readLine();
		}
		reader.close();
		Collections.sort(content); 
		return content;
	}

	public void sortFilterOldFiles(File input, File output) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		List<Item> l = oldTermLoader(input);
		for(Item i:l)
			writer.write(i.m_s + "\t" + i.m_double1 + "\t" + i.m_double2+ "\n");
		writer.close();
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File inputDir = new File("/home/ir/liebesc/ResponsaSys/output/qeWikiAfterWiki/Surface_Surface");
		for(File f:inputDir.listFiles())
		{
			if (f.getAbsolutePath().endsWith("_NoModern.filter")) {
				File output = new File(f.getAbsolutePath().replace(".filter", ".mweFilter"));
				OldSortAndCut sorter = new OldSortAndCut();
				sorter.sortFilterOldFiles(f, output);
			}
		}
		
	}

}
