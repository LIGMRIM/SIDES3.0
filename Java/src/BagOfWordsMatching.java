import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import org.tartarus.snowball.ext.frenchStemmer;

public class BagOfWordsMatching {
	
	private Hashtable<String,String>  stopWords =new Hashtable<String,String>();
	private Hashtable<String,String>  queries =new Hashtable<String,String>();
	private Hashtable<String,Hashtable<String,Double>>  index =new Hashtable<String,Hashtable<String,Double>>();
	private Hashtable<String,Double> docs =new Hashtable<String,Double>();
	private Hashtable<String,Double> IDF =new Hashtable<String,Double>();
	private boolean applyLemm =false;
	private long collectionSize =0;
	double mu=1000;
	double lambda=0.7;

	public void addFileToIndex(String filePath){
		double dlength=0;
		File file =new File(filePath);
		ArrayList<String> parsingResult = readAndParseTextFile(filePath);
		Hashtable<String,Double> docIndex = new Hashtable<String,Double>();
		for(int i=0; i<parsingResult.size();i++){
			String word =parsingResult.get(i);
			
			if(!docIndex.containsKey(word))
				docIndex.put(word, 1.0);
			else{
				double  freq = docIndex.get(word);
				freq++;
				docIndex.remove(word);
				docIndex.put(word, freq);
			}
			
			
			if(!IDF.containsKey(word))
				IDF.put(word, 1.0);
			else{
				double  freq = IDF.get(word);
				freq++;
				IDF.remove(word);
				IDF.put(word, freq);
			}
			
			
			collectionSize++;
			dlength++;

		}

		this.docs.put(file.getName(), dlength);
		this.index.put(file.getName(), docIndex);
		
	}
	
	public void addFolderToIndex(String path){
		
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()&&listOfFiles[i].isHidden()==false) {
		    	  addFileToIndex(listOfFiles[i].getPath());
		      } 
		    }
	}
	
	
	public static String getLemm(String word){
		
		frenchStemmer fstemmer = new frenchStemmer();
		fstemmer.setCurrent(word);
		fstemmer.stem();
		return fstemmer.getCurrent();
		
	}
	
	public void loadStopWords(String filePath){
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))){
    	    String line = br.readLine();
     	    while (line != null) {
    	    	line =line.trim();
    	    	line= line.toLowerCase();
    	    	stopWords.put(line, line);
    	    	line = br.readLine();

    	    }
    	    br.close();
    	}catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public ArrayList<String> readAndParseTextFile(String filePath){
		
		ArrayList<String> output = new ArrayList<String>();
		
		try {
			File file = new File(filePath);

			BufferedReader in = new BufferedReader(
			   new InputStreamReader(
	                      new FileInputStream(file), "UTF-8"));

    	    String line = in.readLine();

			while (line != null) {
				String [] parts =  line.split("[ \\(\\)\\+\\=\\<\\>\\&\\?\\!\\:\\;\\,\\@\\\r\\\n\\\'\\’\\.\\/\\«\\»\\_\\-\\[\\]\\”\\“]+");
				for (int i = 0; i < parts.length; i++) {
					
					parts[i] = parts[i].toLowerCase().trim();
										
					if(!this.stopWords.containsKey(parts[i])&&parts[i].length()>2)
					{

						if(this.applyLemm)
							parts[i] =getLemm(parts[i]);
						output.add(parts[i]);					
					}
				}
	    	    line = in.readLine();


			}

	                in.close();
		    }
		    catch (UnsupportedEncodingException e)
		    {
				System.out.println(e.getMessage());
		    }
		    catch (IOException e)
		    {
				System.out.println(e.getMessage());
		    }
		    catch (Exception e)
		    {
				System.out.println(e.getMessage());
		    }
		return output;
		}
	
	
		private ArrayList<String> parseQuery(String query){
			
			ArrayList<String> output = new ArrayList<String>();
			String [] parts =  query.split("[ \\(\\)\\+\\=\\<\\>\\&\\?\\!\\:\\;\\,\\@\\\r\\\n\\\'\\’\\.\\/\\«\\»\\_\\-\\[\\]\\”\\“]+");
			for (int i = 0; i < parts.length; i++) {
				
				parts[i] = parts[i].toLowerCase().trim();
							
				if(!this.stopWords.containsKey(parts[i])&&parts[i].length()>2)
				{

					if(this.applyLemm)
						parts[i] =getLemm(parts[i]);
					output.add(parts[i]);					
				}
			}
			
			return output;
			
		}
		
	
		public Hashtable<String,String> loadQueries(String queryFile){
			
			Hashtable<String,String> output = new Hashtable<String,String>();
			try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(queryFile), "UTF-8"))){
	    	    String line = br.readLine();
	     	    while (line != null) {
	     	    	String [] parts = line.split("[,]");
	     	    	output.put(parts[0], parts[1]);
	    	    	line = br.readLine();
	    	    }
	    	    br.close();
	    	}catch (IOException e) {
				e.printStackTrace();
			}
			return output;
		}
		public void matchingSetOfQueries(String queryFile,String Trecfile, int model, int topK){
			this.queries = loadQueries(queryFile);
			Enumeration<String> queriesEnum = this.queries.keys();
			while(queriesEnum.hasMoreElements()){
				String qID = queriesEnum.nextElement();
				matchingOneQuery(qID,this.queries.get(qID),Trecfile,model,topK);
				
			}
		}

		//model 1 for JM
		//model 2 for DIR
		public void matchingOneQuery(String qID, String query, String Trecfile, int model, int topK ){
			ArrayList<String> parsingResult = this.parseQuery(query);
			try {
				FileWriter outFile = new FileWriter(Trecfile,true);
				PrintWriter out = new PrintWriter(outFile);
				ArrayList<ResultElement> res= new ArrayList<ResultElement>();
				Enumeration<String> docsEnum = this.index.keys();
				while(docsEnum.hasMoreElements()){
					String docName = docsEnum.nextElement();
					double RSV=0;
					if(model==1)
						RSV= calculateRSVJM(parsingResult, this.index.get(docName), docName);
					else if (model==2)
						RSV= calculateRSVDIR(parsingResult, this.index.get(docName), docName);

					if(!(new Double (RSV)).equals(Double.NaN))
			 			addInorder(res,qID,docName,RSV);
				}
				//System.out.println(res.size());
				for(int m=0; m<topK&m<res.size();m++){
					ResultElement current= res.get(m);
					out.println(current.getqID()+"\t Q\t"+current.getLoID().replace("sides_", "")+"\t"+m+"\t"+current.getScore()+"\t exp");
				}   
				out.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
			}

		    public void addInorder(ArrayList<ResultElement> res,String qID,String dID, double rsv){
				
				
				for(int i=0; i<res.size();i++){
					
					ResultElement current= res.get(i);
			
					if(current.getScore() < rsv){
					
						res.add(i,new ResultElement(qID,dID,rsv));
						return;
					}

				}
				res.add(new ResultElement(qID,dID,rsv));

			}
			
		public double calculateRSVDIR(ArrayList<String> q,Hashtable<String,Double> d, String docName){
			
			double res=0;
			double RSV =0;
			Double dlength =docs.get(docName);

			
			for (int i = 0; i < q.size(); i++) {
				String word= q.get(i);
				double pwd=0;
			
				
				Double tf= d.get(word);
				
				if(tf!=null)
					pwd+=tf.doubleValue()/dlength;
				
				Double temp=null;
				temp=IDF.get(word);

				if(pwd!=0&&temp!=null){
					res = (dlength/(dlength+mu))*pwd + (mu/(dlength+mu))*(temp.doubleValue()/this.collectionSize);
					if(res != 0)
						res = Math.log(res);
					
				}else{
					 if(temp!=null){
						 
					  res = (mu/(dlength+mu))*(temp.doubleValue()/this.collectionSize);
					  if(res != 0)
							res = Math.log(res);
					 }
					 else
						 res=0.0;

				}
				RSV+=res;
			}
			RSV+=q.size()*Math.log(mu/(dlength+mu));
			return RSV;
		}


		public double calculateRSVJM(ArrayList<String> q,Hashtable<String,Double> d, String docName){
			
			double res=0;
			double RSV =0;
			Double dlength =docs.get(docName);

			for (int i = 0; i < q.size(); i++) {
				String word= q.get(i);
				double pwd=0;
			
				
				Double tf= d.get(word);
				
				if(tf!=null)
					pwd+=tf/dlength;
				
				Double temp=null;
				temp=IDF.get(word);

				if(pwd!=0&&temp!=null){
					res = (1-lambda)*pwd + lambda*(temp.doubleValue()/this.collectionSize);
					if(res != 0)
						res = Math.log(res);
					
				}else{
					 if(temp!=null){
						 res = lambda*(temp.doubleValue()/this.collectionSize);
					  if(res != 0)
							res = Math.log(res);
					 }
					 else
						 res=0.0;

				}
				RSV+=res;
			}
			RSV+=q.size()*Math.log(lambda);
			return RSV;
		}

		private void matchingOneQuery(String qID, int model) {
			System.out.println(this.queries.size());
			ArrayList<String> parsingResult = this.parseQuery(this.queries.get(qID));
			ArrayList<ResultElement> res= new ArrayList<ResultElement>();
			Enumeration<String> docsEnum = this.index.keys();
			while(docsEnum.hasMoreElements()){
					String docName = docsEnum.nextElement();
					double RSV=0;
					if(model==1)
						RSV= calculateRSVJM(parsingResult, this.index.get(docName), docName);
					else if (model==2)
						RSV= calculateRSVDIR(parsingResult, this.index.get(docName), docName);

					if(!(new Double (RSV)).equals(Double.NaN))
			 			addInorder(res,qID,docName,RSV);
			}
				//System.out.println(res.size());
				for(int m=0; m<res.size();m++){
					ResultElement current= res.get(m);
					System.out.println(current.getqID()+"\t Q\t"+current.getLoID().replace("sides_", "")+"\t"+m+"\t"+current.getScore()+"\t exp");
			}
			
		}
		public ArrayList<ResultElement> matchingOneQuery(String qText) {
			ArrayList<String> parsingResult = this.parseQuery(qText);
			ArrayList<ResultElement> res= new ArrayList<ResultElement>();
			Enumeration<String> docsEnum = this.index.keys();
			while(docsEnum.hasMoreElements()){
					String docName = docsEnum.nextElement();
					double RSV=0;
					RSV= calculateRSVJM(parsingResult, this.index.get(docName), docName);

					if(!(new Double (RSV)).equals(Double.NaN))
			 			addInorder(res,"",docName.toLowerCase(),RSV);
			}
				//System.out.println(res.size());
				
			return res;
			
		}	
	
		public void activateLemm(){
			this.applyLemm =true;
		}
		
		public void deactivateLemm(){
			this.applyLemm =false;
		}
		
		public int getIndexSize(){
			return this.index.size();
			
		}
		public long getCollectionSize(){
			
			return this.collectionSize;
		}
		
		
	
	public static void main(String[] args) {
		
		BagOfWordsMatching indexer =new BagOfWordsMatching();
		indexer.activateLemm();
		indexer.loadStopWords("stopWords.txt");
		indexer.addFolderToIndex(".\\Datasets\\Dataset3\\LearningObjectiveAsBagsOfWords (236)\\");
		//System.out.println(indexer.getIndexSize());
		//System.out.println(indexer.getCollectionSize());
		
		indexer.matchingSetOfQueries(".\\Datasets\\Dataset3\\Questions_with_learning_objectives_filtered.csv", 
				".\\Classical_output_JM.txt", 1,10);
}

	

}


