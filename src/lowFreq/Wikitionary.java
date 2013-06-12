package lowFreq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import obj.Pair;


public class Wikitionary {
	
	public Wikitionary(File wikiFile) throws IOException{
		m_wikiRel = new HashMap<String, HashMap<String,Integer>>();
		m_wikiRevRel = new HashMap<String, HashMap<String,Integer>>();
		BufferedReader reader = new BufferedReader(new FileReader(wikiFile));
		String line = reader.readLine();
		while (line != null){
			String [] tokens = line.split("\t");
			if (!m_wikiRel.containsKey(tokens[0])){
				HashMap<String,Integer> relsMap = new HashMap<String,Integer>();
				relsMap.put(tokens[1], Integer.parseInt(tokens[2]));
				m_wikiRel.put(tokens[0], relsMap);
			}
			else
				m_wikiRel.get(tokens[0]).put(tokens[1], Integer.parseInt(tokens[2]));
			
			if (!m_wikiRevRel.containsKey(tokens[1])){
				HashMap<String,Integer> relsMap = new HashMap<String,Integer>();
				relsMap.put(tokens[0], Integer.parseInt(tokens[2]));
				m_wikiRevRel.put(tokens[1], relsMap);
			}
			else
				m_wikiRevRel.get(tokens[1]).put(tokens[0], Integer.parseInt(tokens[2]));
			line = reader.readLine();
		}
	}
	public LinkedList<Pair<String, Integer>> getRel(String targetTerm, boolean bIncludeRev, int maxLevel){
		LinkedList<Pair<String,Integer>> relList = new LinkedList<Pair<String,Integer>>();
		if(m_wikiRel.containsKey(targetTerm)){
			HashMap<String, Integer> rels = m_wikiRel.get(targetTerm);
			for(String rel:rels.keySet())
				if(rels.get(rel)<=maxLevel)
					relList.add(new Pair<String,Integer>(rel,rels.get(rel)));
		}
		if (bIncludeRev) {
			if(m_wikiRevRel.containsKey(targetTerm)){
				HashMap<String, Integer> rels = m_wikiRevRel.get(targetTerm);
				for(String rel:rels.keySet())
					if(rels.get(rel)<=maxLevel)
						relList.add(new Pair<String,Integer>(rel,rels.get(rel)));
			}
		}
		return relList;
	}
	
	public HashSet<String> getRelSet(String targetTerm, boolean bIncludeRev, int maxLevel){
		HashSet<String> relSet = new HashSet<String>();
		if(m_wikiRel.containsKey(targetTerm)){
			HashMap<String, Integer> rels = m_wikiRel.get(targetTerm);
			for(String rel:rels.keySet())
				if(rels.get(rel)<=maxLevel)
					relSet.add(rel);
		}
		if (bIncludeRev) {
			if(m_wikiRevRel.containsKey(targetTerm)){
				HashMap<String, Integer> rels = m_wikiRevRel.get(targetTerm);
				for(String rel:rels.keySet())
					if(rels.get(rel)<=maxLevel)
						relSet.add(rel);
			}
		}
		return relSet;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		BufferedReader reader = new BufferedReader(new FileReader("C:\\ResponsaSys\\input\\targetTerms_orig.txt"));
//		String line = reader.readLine();
		Wikitionary wiki = new Wikitionary(new File("C:\\wikiRel"));
//		HashSet<String> relations = new HashSet<String>();
//		while(line != null){
//			LinkedList<Pair<String, Integer>> relList = wiki.getRel(line.split("\t")[1], true, 1);
//			if (relList.size() >0 ){
//				System.out.println(line.split("\t")[1]);
//				for(Pair<String, Integer> pair:relList){
//					System.out.println(pair.key() + " " + pair.value());
//					relations.add(pair.key());
//				}
//				System.out.println("More relations");
//				for(String rel:relations){
//					LinkedList<Pair<String, Integer>> relList2 = wiki.getRel(rel, true, 1);
//					if (relList2.size() >0 ){
//						for(Pair<String, Integer> pair:relList2){
//							if(!relations.contains(pair.key()))
//								System.out.println(pair.key() + " " + pair.value());
//						}
//					}
//				}
//			}
//			relations.clear();
//			System.out.println(wiki.getRelSet(line.split("\t")[1], true, 1));
			System.out.println(wiki.getRelSet("הרמת כוסית", true, 1));
//			line = reader.readLine();
//		}
	}
		
	
	HashMap<String,HashMap<String,Integer>> m_wikiRel = null;
	HashMap<String,HashMap<String,Integer>> m_wikiRevRel = null;
}
