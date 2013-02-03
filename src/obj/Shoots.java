package obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;



public class Shoots {
	public static HashMap<String,Shoot> shoots = new HashMap<String, Shoot>();
	
	private  String shootsfile ;
	private  String shootsDir ;
	
	public Shoots(String sf,String sd){
		shootsfile = sf;
		shootsDir = sd;
	}
	
	public void loadShoots() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader( new File(shootsfile)));
		String line = br.readLine();
		while (line!=null){
			//System.out.println(line);
			String[] lineArr = line.split("\t");
			Shoot shoot = new Shoot(lineArr[0],lineArr[1],lineArr[2],shootsDir,lineArr[3],lineArr[4],lineArr[5],lineArr[6]);
			System.out.println(shoot.toString());
			shoots.put(lineArr[0],shoot);
			line = br.readLine();
		}
	}
	
	public Shoot getShootById(String id){
		if (id.startsWith("000"))
			return shoots.get(id);
		System.out.println(id+"- id is not legal");
		return null;
	}
	
	public Shoot getShootByDir(String dir){
		if (!dir.startsWith(shootsDir)){
				System.out.println(dir+" - directory is not in corpus!!");
				return null;	
		}
		String[] arr = dir.split("\\\\");
		int n = arr.length;
		String dirName = arr[n-1];
		String id = dirName.substring(0, 6);
		return shoots.get(id);
	}
	
	public Shoot getShootByFile(String filename){
		File f = new File(filename);		
		
		if (!filename.startsWith(shootsDir)){
			System.out.println(filename +" - is not in responsa corpus !!!");
			System.out.println(filename + " - is unknown file");
			return null;
		}
			
		if (f.isDirectory()){
			System.out.println(filename = " - is directory and not a file!!");
			return null;
		}
		File p = f.getParentFile();
		File d = new File(shootsDir);
		File temp = f;
		while(!p.equals(d)){
			temp = p;
			p = p.getParentFile();	
		}
		//here, temp is the shoot folder of the given file
		String tempStr = temp.getName();
		String tempId = tempStr.substring(0, 6);
		return getShootById(tempId);
		
	}
	 
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		Shoots shoots = new Shoots();
//		shoots.loadShoots();
//		System.out.println("shoots were loaded");
//		System.out.println("id=000025, shoot=\n"+shoots.getShootById("000025").toString());
//		System.out.println(shoots.getShootByDir("C:\\Responsa\\ResponsaCorpus\\000010amownt_shmowal@@@showt-amownt-shmowal").toString());
//		System.out.println(shoots.getShootByFile("C:\\Responsa\\ResponsaCorpus\\000019bit_mrdki\\000001chlk_a@@@showt-bit-mrdki-a\\000014td00025062285000000simn_tow.txt").toString());

	}

}
