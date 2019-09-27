import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Question {
	private String qID;
	private String qBody;
	private String qLOID;
	
	
	
	static Hashtable<String,Double> IDF =new Hashtable<String,Double>();
	static Hashtable<String,Double> docs =new Hashtable<String,Double>();
	static long collectionSize =0;
	static double lambda=0.3;

	private static Hashtable<String,String> stopWords;


	private ArrayList<Proposal> qCorrectProposals;
	public ArrayList<Proposal> getqCorrectProposals() {
		return qCorrectProposals;
	}

	public void setqCorrectProposals(ArrayList<Proposal> qCorrectProposals) {
		this.qCorrectProposals = qCorrectProposals;
	}

	private ArrayList<Proposal> qWrongProposals;

	
	private Hashtable<String,Double> fullAnnotations;
	public Hashtable<String, Double> getFullAnnotations() {
		return fullAnnotations;
	}

	public void setFullAnnotations(Hashtable<String, Double> fullAnnotations) {
		this.fullAnnotations = fullAnnotations;
	}

	private Hashtable<String,Double> qBodyAnnotations;
	private Hashtable<String,Double> qCorrectProposalsAnnotations;
	private Hashtable<String,Double> qWrongProposalsAnnotations;

	
	private Annotator at = new Annotator();
    static final ObjectMapper mapper = new ObjectMapper();
	//Weight Body
	private double weightBody = 3;
	
	//Weight Proposals
	private double weightCorrectProposals = 2;
	private double weightWrongProposals = 1;

	
	
	public Question(){
		this.setqID("");
		this.setqBody("");
		this.setqWrongProposals(new ArrayList<Proposal>());
		this.setqCorrectProposals(new ArrayList<Proposal>());
		fullAnnotations = new Hashtable<String,Double>();
		qBodyAnnotations  = new Hashtable<String,Double>();
		qCorrectProposalsAnnotations = new Hashtable<String,Double>();
		qWrongProposalsAnnotations = new Hashtable<String,Double>();
	}
	
	public Question(String qID,String qBody, ArrayList<Proposal> qCorrectProposals, ArrayList<Proposal> qWrongProposals){
		this.setqID(qID);
		this.setqBody(qBody);
		this.setqCorrectProposals(qCorrectProposals);
		this.setqWrongProposals(qWrongProposals);
		fullAnnotations = new Hashtable<String,Double>();
		qBodyAnnotations  = new Hashtable<String,Double>();
		qCorrectProposalsAnnotations = new Hashtable<String,Double>();
		qWrongProposalsAnnotations = new Hashtable<String,Double>();

	}
	
    public void fillAllAnnotations() throws Exception{
    	if(weightBody!=0)
    		fillBodyAnnotations();
		if(weightCorrectProposals!=0)
			fillCorrectProposalsAnnotations();
		if(weightWrongProposals!=0)
			fillWrongProposalsAnnotations();
   		fuseAnnoations();
		
	}
    
	private void fuseAnnoations() {
	this.fullAnnotations =new Hashtable<String, Double>();
		
		Enumeration<String> enumConcepts1 = this.qBodyAnnotations.keys();
	   	while(enumConcepts1.hasMoreElements()){
	    		String id = enumConcepts1.nextElement();
	    		double freq =  this.weightBody * this.qBodyAnnotations.get(id).doubleValue();
    		 	this.fullAnnotations.put(id,freq);
	   	}
	   	
	   	Enumeration<String> enumConcepts2 = this.qWrongProposalsAnnotations.keys();
	   	while(enumConcepts2.hasMoreElements()){
	    		String id = enumConcepts2.nextElement();
	    		double freq =  this.weightWrongProposals * this.qWrongProposalsAnnotations.get(id).doubleValue();
	    		if(this.fullAnnotations.containsKey(id))
       		 		{
	    				double oldFreq = this.fullAnnotations.get(id).doubleValue();   		
	    				oldFreq+=freq;
	    				this.fullAnnotations.remove(id);
	    				this.fullAnnotations.put(id,oldFreq);
  			

       		 		}else
       		 			this.fullAnnotations.put(id,freq);

	   		}
	   	Enumeration<String> enumConcepts3 = this.qCorrectProposalsAnnotations.keys();
	   	while(enumConcepts3.hasMoreElements()){
	    		String id = enumConcepts3.nextElement();
	    		double freq =  this.weightCorrectProposals * this.qCorrectProposalsAnnotations.get(id).doubleValue();
	    		if(this.fullAnnotations.containsKey(id))
       		 		{
	    				double oldFreq = this.fullAnnotations.get(id).doubleValue();   		
	    				oldFreq+=freq;
	    				this.fullAnnotations.remove(id);
	    				this.fullAnnotations.put(id,oldFreq);
  			

       		 		}else
       		 			this.fullAnnotations.put(id,freq);

	   		}
	}

	private void fillCorrectProposalsAnnotations() throws Exception {
		for(Proposal prop : qCorrectProposals){
	    	 String text = prop.getProposalText();
	    	 text =at.filterText(text);
  	    	 if(text.length()>0){
  	    		 JsonNode annotations = jsonToNode(at.sendPost(text));
  	  		     extractAnnotations(annotations,this.qCorrectProposalsAnnotations);
  	    	   }
  	        }		
	}
	
	private void fillWrongProposalsAnnotations() throws Exception {
		for(Proposal prop : qWrongProposals){
	    	 String text = prop.getProposalText();
	    	 text =at.filterText(text);
  	    	 if(text.length()>0){
  	    		 JsonNode annotations = jsonToNode(at.sendPost(text));
  	  		     extractAnnotations(annotations,this.qWrongProposalsAnnotations);
  	    	   }
  	        }		
	}

	private void fillBodyAnnotations() throws Exception {
		JsonNode annotations = jsonToNode(at.sendPost(this.qBody));
		extractAnnotations(annotations,qBodyAnnotations);
		//System.out.println(annotations);		
	}
	
	
private void extractAnnotations(JsonNode annotations , Hashtable<String , Double> result){
		
		try{
			if(annotations.size()==0)
			 	return;			
			
			 for (JsonNode annotation : annotations) {
				 		for(int i=0; i< annotation.get("annotations").size();i++){
				 			String id = annotation.get("annotatedClass").get("@id").asText();
				 			String text = annotation.get("annotations").get(i).get("text").asText().toLowerCase();
				 			String pref = annotation.get("annotatedClass").get("prefLabel").asText().toLowerCase();
				 			String st ="";
				 			if(annotation.get("annotatedClass").get("semanticType")!=null)
				 				for (int k=0;k<annotation.get("annotatedClass").get("semanticType").size();k++)
				 					st +=annotation.get("annotatedClass").get("semanticType").get(k).asText()+",";
			 				//String from = annotation.get("annotations").get(i).get("from").asText();
			 				//String to  = annotation.get("annotations").get(i).get("to").asText();

				 			String idx = id+"|"+pref+"|"+text+"|"+st;
				 			//System.out.println(idx);
				 			if(result.containsKey(idx))
		            		 {
		            			double CFreq = result.get(idx).doubleValue();   		
		            			CFreq+=1;
		            			result.remove(idx);
		            			result.put(idx,CFreq);
		            			

		            		 }else{
                    			result.put(idx,new Double(1.0));

		            		 }
		            		 
				 			
				 		}
				 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

public void printFullAnnotations(String filePath){
	Writer writer = null;
	try {
		String[] parts = this.qID.split("[#]");
        writer = new BufferedWriter(new OutputStreamWriter(
	          new FileOutputStream(filePath+parts[0]), "utf-8"));
	    Enumeration<String> enumConceptsfull = this.fullAnnotations.keys();
	   	while(enumConceptsfull.hasMoreElements()){
	    		String id = enumConceptsfull.nextElement();
	    		writer.write(id+"\t"+fullAnnotations.get(id)+"\n");
	   	}
		} catch (IOException ex) {
	    // Report
		} finally {
			try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
		
}

public static Hashtable<String,String> loadStopWords(String filePath){
	stopWords = new Hashtable<String,String>();
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
	return stopWords;
	
}


public static String cleanText (String text){
	String output ="";
	String [] parts =  text.split("[ \\(\\)\\+\\=\\<\\>\\&\\?\\!\\:\\;\\,\\@\\\r\\\n\\\'\\’\\.\\/\\«\\»\\_\\-\\[\\]\\”\\“]+");
	for (int i = 0; i < parts.length; i++) {
		parts[i] = parts[i].toLowerCase().trim();
		if(!stopWords.containsKey(parts[i])&&parts[i].length()>2)
			output+=parts[i]+"	";
	
	}
	return output;
}


public static String cleanTextwithStem (String text){
	String output ="";
	String [] parts =  text.split("[ \\(\\)\\+\\=\\<\\>\\&\\?\\!\\:\\;\\,\\@\\\r\\\n\\\'\\’\\.\\/\\«\\»\\_\\-\\[\\]\\”\\“]+");
	for (int i = 0; i < parts.length; i++) {
		parts[i] = parts[i].toLowerCase().trim();
		if(!stopWords.containsKey(parts[i])&&parts[i].length()>3){
			String word = parts[i];
			word = BagOfWordsMatching.getLemm(word);
			output+=normalize(word)+" ";
		}
	
	}
	return output;
}

public static String normalize(String source) {
	String tab00c0 = "AAAAAAACEEEEIIII" +
		    "DNOOOOO\u00d7\u00d8UUUUYI\u00df" +
		    "aaaaaaaceeeeiiii" +
		    "\u00f0nooooo\u00f7\u00f8uuuuy\u00fey" +
		    "AaAaAaCcCcCcCcDd" +
		    "DdEeEeEeEeEeGgGg" +
		    "GgGgHhHhIiIiIiIi" +
		    "IiJjJjKkkLlLlLlL" +
		    "lLlNnNnNnnNnOoOo" +
		    "OoOoRrRrRrSsSsSs" +
		    "SsTtTtTtUuUuUuUu" +
		    "UuUuWwYyYZzZzZzF";
	  char[] vysl = new char[source.length()];
	    char one;
	    for (int i = 0; i < source.length(); i++) {
	        one = source.charAt(i);
	        if (one >= '\u00c0' && one <= '\u017f') {
	            one = tab00c0.charAt((int) one - '\u00c0');
	        }
	        vysl[i] = one;
	    }
	    
	    return new String(vysl);
 

}


public static void printCSVQuestions(String file) {
	Hashtable<String, Question> questions =new Hashtable<String, Question>();
	try {
		BufferedReader br = new BufferedReader(new FileReader(file));
	    String line = br.readLine();

	    while (line != null) {
	        
	    	//qID --> 0 
	    	//qBody --> 1
	    	//qLOID --> 2
	    	//qProposalID-> 3
	    	//qProposalText -> 4
	    	//qProposalStatus -> 5
	    	String parts[] = line.split("[|]");
	    	if(parts.length==6){
		        String lobj = parts[2];
		        Question qTemp = questions.get(parts[0]);
		        Proposal pTemp;
		        if(parts[5].equals("0")){
	        		 //pTemp = new Proposal(parts[3], cleanText(parts[4]), false);
	        		 pTemp = new Proposal(parts[3], cleanText(parts[4]), false);

	        	}else {
	        		 //pTemp = new Proposal(parts[3], cleanText(parts[4]), true);
	        		 pTemp = new Proposal(parts[3], parts[4], true);

	        	}
		    
		        
		        if(qTemp==null){
		        	qTemp =new Question();
		        	qTemp.setqID(parts[0]);
//		        	qTemp.setqBody(cleanText(parts[1]));
		        	qTemp.setqBody(parts[1]);

		        	qTemp.setqLOID(lobj);
		        	if(pTemp.isProposalStatus())
		        		qTemp.qCorrectProposals.add(pTemp);
		        	else
		        		qTemp.qWrongProposals.add(pTemp);
		        	questions.put(qTemp.getqID(), qTemp);
		        	
		        }else{
		        	if(pTemp.isProposalStatus()&&!qTemp.qCorrectProposals.contains(pTemp))
		        		qTemp.qCorrectProposals.add(pTemp);
		        	else if(!qTemp.qWrongProposals.contains(pTemp))
		        		qTemp.qWrongProposals.add(pTemp);
		        	
		        	if(!qTemp.getqLOID().contains(lobj))
		        		qTemp.setqLOID(qTemp.getqLOID()+"|"+lobj);

		        }
	    	}
	    	line = br.readLine();
	    }
	 br.close();   
	}catch (Exception e) {
		e.printStackTrace();

	}
	
	
	//print quetstions
	
	/*
	Enumeration<String> qs =  questions.keys();
	while (qs.hasMoreElements()) {
		String qID =  qs.nextElement();
		Question q =questions.get(qID);
		System.out.println(qID+"\t"+q.getqLOID());
	}*/	
    //System.out.println(questions.size());

	int count_several_sps_qs=0;
	Enumeration<String> qs =  questions.keys();
	while (qs.hasMoreElements()) {
		String qID =  qs.nextElement();
		
		Question q =questions.get(qID);
		
		String[] sps =q.getqLOID().split("[|]");
		//System.out.println(sps.length);
		if(sps.length>=1){
			count_several_sps_qs++;
		String text =q.getqBody()+" ";
		for (int i = 0; i < q.getqCorrectProposals().size(); i++) {
			text+=q.getqCorrectProposals().get(i).getProposalText()+" ";
		}
		for (int i = 0; i < q.getqWrongProposals().size(); i++) {
			text+=q.getqWrongProposals().get(i).getProposalText()+" ";
		}
		for (int m = 0; m < sps.length; m++) {
			System.out.println("\""+qID+"\",\""+text.replaceAll("\t", " ")+"\",\""+convertSubLearningObjective(sps[m])+"\"");
			//System.out.println(qID+"\t"+q.getqLOID());
		}
	 }
	}
	
	//System.out.println(count_several_sps_qs);
	
}

public static Hashtable<String, Question> loadQuestions(String file){
		
		Hashtable<String, Question> questions =new Hashtable<String, Question>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
		    String line = br.readLine();

		    while (line != null) {
		        
		    	//qID --> 0 
		    	//qBody --> 1
		    	//qLOID --> 2
		    	//qProposalID-> 3
		    	//qProposalText -> 4
		    	//qProposalStatus -> 5
		    	String parts[] = line.split("[|]");
		    	if(parts.length==6){
			        String lobj = convertSubLearningObjective(parts[2].substring(parts[2].indexOf('#')+1, parts[2].length()));
	
			    	//System.out.println(parts[0].substring(parts[0].indexOf('#')+1, parts[0].length())+"\t0\t"+lobj+"\t1");
	
			        Question qTemp = questions.get(parts[0]);
			        Proposal pTemp;
			        if(parts[5].equals("0")){
		        		 pTemp = new Proposal(parts[3], parts[4], false);
		        	}else {
		        		 pTemp = new Proposal(parts[3], parts[4], true);
		        	}
			    
			        
			        if(qTemp==null){
			        	qTemp =new Question();
			        	qTemp.setqID(parts[0]);
			        	qTemp.setqBody(parts[1]);
			        	qTemp.setqLOID(lobj);
			        	if(pTemp.isProposalStatus())
			        		qTemp.qCorrectProposals.add(pTemp);
			        	else
			        		qTemp.qWrongProposals.add(pTemp);
			        	questions.put(qTemp.getqID(), qTemp);
			        	
			        }else{
			        	if(pTemp.isProposalStatus())
			        		qTemp.qCorrectProposals.add(pTemp);
			        	else
			        		qTemp.qWrongProposals.add(pTemp);
			        	
			        	if(!qTemp.getqLOID().contains(lobj))
			        		qTemp.setqLOID(qTemp.getqLOID()+"|"+lobj);
	
			        	
			        }
		    	}
		    	line = br.readLine();
		    }
		 br.close();   
		}catch (Exception e) {
			e.getStackTrace();
	
		}
		
		return questions;
	}

public static String convertSubLearningObjective(String lo) {

	if(lo.contains("sub")){
    	String parts[] = lo.split("[_]");
    	return parts[0]+"_"+parts[2]+"_"+parts[3];
	}
	else
		return lo;
}

public static Hashtable<String, Hashtable<String,Double>> loadAnnotatedQuestions(String path){

	Hashtable<String, Hashtable<String,Double>> questions = new Hashtable<String, Hashtable<String,Double>>();
	
	try {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
			  BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(listOfFiles[i]), "UTF8"));
			  String line = br.readLine();
			  
			  Hashtable<String,Double> temp = new Hashtable<String,Double>();
			  while (line != null) {
				  String parts[] = line.split("[|,]");
				  String CID =  parts[1];
				  double freq = Double.parseDouble(parts[parts.length-1].trim());
				  if(freq!=0)
					  temp.put(CID, freq);
				  line = br.readLine();
			  }
			  questions.put(listOfFiles[i].getName(), temp);
			  br.close();  
		  }
		}
	}catch (Exception e) {
		e.getStackTrace();

	}
	return questions;
}

public static Hashtable<String, Hashtable<String,Double>> loadAnnotatedLearningObjective(String path){

	Hashtable<String, Hashtable<String,Double>> learningObjectives = new Hashtable<String, Hashtable<String,Double>>();
	
	try {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
    		  BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(listOfFiles[i]), "UTF8"));
			  String line = br.readLine();
			  Hashtable<String,Double> temp = new Hashtable<String,Double>();
			  while (line != null) {
				  String parts[] = line.split("[,]");
				  if(parts.length>=5){
 
					  String CID =  parts[0];
					  double weight = Double.parseDouble(parts[2]);
					  if(!IDF.containsKey(CID))
							IDF.put(CID, 1.0);
						else{
							double  freq = IDF.get(CID);
							freq++;
							IDF.remove(CID);
							IDF.put(CID, freq);
						}
					  if(weight!=0)
						  temp.put(CID, weight);
				  }
				  line = br.readLine();
			  }
			  if(temp.size()>20){
				  learningObjectives.put(listOfFiles[i].getName(), temp);
				  double length =  Question.getQuestionLength(learningObjectives,listOfFiles[i].getName());
				  collectionSize+=length;
				  docs.put(listOfFiles[i].getName(),length);
			  }
			  br.close();  
		  }
		}
	}catch (Exception e) {
		e.printStackTrace();

	}
	return learningObjectives;
}



public static double calculateRSVJM (double tf, String  docName,String word){
	double res=0;
	Double dlength =docs.get(docName);
	double pwd=0;
	if(tf!=0)
		pwd+=tf/dlength;
		Double temp=null;
		temp=IDF.get(word);
		if(pwd!=0&&temp!=null){
			res = (1-lambda)*pwd + lambda*(temp.doubleValue()/collectionSize);
			if(res != 0)
				res = Math.log(res);
		}else{
			 if(temp!=null){
				 res = lambda*(temp.doubleValue()/collectionSize);
			  if(res != 0)
					res = Math.log(res);
			 }
			 else
				 res=0.0;
		}
		
	return res;
}


public static void MatchBM25(String qID, Hashtable<String,Double> question, Hashtable<String, Hashtable<String,Double>> learningObjectives){
	ArrayList<ResultElement> results =new ArrayList<ResultElement>();
	Enumeration<String> lOKeys = learningObjectives.keys();
	while(lOKeys.hasMoreElements()){
		String loKey =lOKeys.nextElement();
		double score=0;
		Enumeration<String> qCIDs = question.keys();
		while(qCIDs.hasMoreElements()){
			String qCID = qCIDs.nextElement();
			Double qFreq = question.get(qCID);
			Double dFreq = learningObjectives.get(loKey).get(qCID);
			if(dFreq!=null){
				score += qFreq*dFreq;
			}
		}
		if(score!=0){
			ResultElement res = new ResultElement(qID,loKey,score);
			results.add(res);
		}
	}
	Collections.sort(results);
	for (int i = 0; i < results.size()&&i<10; i++) {
		ResultElement res = results.get(i);
		System.out.println(res.getqID()+"\t QQ \t"+res.getLoID()+"\t"+(i+1)+"\t"+res.getScore()+"\t NOEXP \t");
		
	}
	
}


public static ArrayList<ResultElement> listOfMatchForQuestion(Hashtable<String,Double> question, Hashtable<String, Hashtable<String,Double>> learningObjectives){
	ArrayList<ResultElement> results =new ArrayList<ResultElement>();
	Enumeration<String> lOKeys = learningObjectives.keys();
	while(lOKeys.hasMoreElements()){
		String loKey =lOKeys.nextElement();
		//System.out.println(loKey);

		double score=0;
		Enumeration<String> qCIDs = question.keys();

		while(qCIDs.hasMoreElements()){
			String qCID = qCIDs.nextElement();
			//System.out.println(qCID);
			
			 String parts[] = qCID.split("[|,]");
			 String CID =  parts[1];

			Double qFreq = question.get(qCID);
			Double dFreq = learningObjectives.get(loKey).get(CID);
			if(dFreq!=null){
				score += qFreq*dFreq;
			}
		}

		if(score!=0){
			ResultElement res = new ResultElement("",loKey.toLowerCase(),score);
			results.add(res);
		}
	}
	Collections.sort(results);
	return results;
}
	
	private JsonNode jsonToNode(String json) {
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

	public String getqID() {
		return qID;
	}

	public void setqID(String qID) {
		this.qID = qID;
	}

	public String getqBody() {
		return qBody;
	}

	public void setqBody(String qBody) {
		this.qBody = qBody;
	}

	public ArrayList<Proposal> getqWrongProposals() {
		return qWrongProposals;
	}

	public void setqWrongProposals(ArrayList<Proposal> qWrongProposals) {
		this.qWrongProposals = qWrongProposals;
	}
	
	public String getqLOID() {
		return qLOID;
	}

	public void setqLOID(String qLOID) {
		this.qLOID = qLOID;
	}
	
	public static void filterQrel(String path, Hashtable<String, Hashtable<String,Double>> learningObjectives){
		try {
			Hashtable<String,String> qrels =new Hashtable<String,String>();
			BufferedReader br = new BufferedReader(new FileReader(path));
		    String line = br.readLine();

		    while (line != null) {
		     String [] parts = line.split("[\t]");
		     if(learningObjectives.containsKey("sides_"+parts[2]))
		    	 qrels.put(parts[0]+"\t"+parts[2],line);
		     line = br.readLine();
		    }
		 br.close();
		 Enumeration<String> qKeys = qrels.keys();
		 while(qKeys.hasMoreElements()){
			 String qKey = qKeys.nextElement();
			 System.out.println(qrels.get(qKey));
		 }
			 
		 
		}catch (Exception e) {
			e.printStackTrace();
	
		}
	}
	
	public static void countNumberOfLearningForEachQuestion(String path){
		try {
			Hashtable<String,Integer> countLOsInQrels =new Hashtable<String,Integer>();
			BufferedReader br = new BufferedReader(new FileReader(path));
		    String line = br.readLine();
		    while (line != null) {
		     String [] parts = line.split("[\t]");
		     Integer temp = countLOsInQrels.get(parts[0]);
		     if(temp==null)
		    	 countLOsInQrels.put(parts[0], 1);
		     else{
		    	 temp++;
		    	 countLOsInQrels.replace(parts[0], temp);
		     }
		     line = br.readLine();
		    }
		 br.close();
		 Enumeration<String> qKeys = countLOsInQrels.keys();
		 while(qKeys.hasMoreElements()){
			 String qKey = qKeys.nextElement();
			 System.out.println(qKey+"\t"+countLOsInQrels.get(qKey));
		 }
			 
		 
		}catch (Exception e) {
			e.getStackTrace();
	
		}
	}
	
	public static void printQuestion(Hashtable<String, Hashtable<String,Double>> questions , String qID){
		
		Hashtable<String,Double> q = questions.get(qID);
		Enumeration<String> qkeys = q.keys();
		while (qkeys.hasMoreElements()) {
			String cID =  qkeys.nextElement();
			System.out.println(cID + "\t"+q.get(cID));
			
		}
		
		
	}
	
	
	public static double getQuestionLength(Hashtable<String, Hashtable<String,Double>> questions , String qID){
		double length =0;
		Hashtable<String,Double> q = questions.get(qID);
		Enumeration<String> qkeys = q.keys();
		while (qkeys.hasMoreElements()) {
			String cID =  qkeys.nextElement();
			length+=q.get(cID);
			
		}
		return length;
		
		
	}
	
	public static void printFullQrel(String QuestionsPath){
		Hashtable<String, Question> questions =new Hashtable<String, Question>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(QuestionsPath));
		    String line = br.readLine();

		    while (line != null) {
		        
		    	//qID --> 0 
		    	//qBody --> 1
		    	//qLOID --> 2
		    	//qProposalID-> 3
		    	//qProposalText -> 4
		    	//qProposalStatus -> 5
		    	String parts[] = line.split("[|]");
		    	if(parts.length==6){
		    		System.out.println(parts[0]+"\t0\t"+convertSubLearningObjective(parts[2])+"\t1");
		    	}
			    line = br.readLine();
		    }
		 br.close();   
		}
		catch (Exception e) {
			e.getStackTrace();
	
		}
		
		
	}
	
	
	public static void cleanQuestions(String filePath, String targetpath){
		Hashtable<String, Question> questions = Question.loadQuestions(filePath);
		System.out.println(questions.size());
		File folder = new File (targetpath);
		for (int i = 0; i < folder.listFiles().length; i++) {
			//System.out.println(i);
			String fileName = folder.listFiles()[i].getName();
			if(!questions.containsKey(fileName)){
				//folder.listFiles()[i].delete();
				System.out.println(folder.listFiles()[i].getAbsolutePath());
			}
		}
		
	}
	
	public static void cleanQuestions(String filePath){
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
		    String line = br.readLine();

		    while (line != null) {
		    		File file = new File (line);
		    		System.out.println(line);
		    		file.delete();
		    	  	line= br.readLine();
		    }
		    
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void removeEmptyLines(String inputFile, String outputFile){
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "Cp1252"))){
			 Writer writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(outputFile), "utf-8"));
		    String line = br.readLine();

		    while (line != null) {
		    	if(line.length()>10)
		    		writer.write(line+"\n");
		    	line = br.readLine();
		    }
		    br.close();
		    writer.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	
}

