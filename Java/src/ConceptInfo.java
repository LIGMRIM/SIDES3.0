import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class ConceptInfo {
	
	
		private String apiKey = "caa01549-2436-4eb6-a33c-f59bed75f216";
		private String id = "";
		private String version = "2017AB";
		RestTicketClient ticketClient = new RestTicketClient(apiKey);
		//get a ticket granting ticket for this session.
		private String tgt  = ticketClient.getTgt();

		private Hashtable<String,String> relations = new Hashtable<String,String>();
		private Hashtable<String,String> atoms = new Hashtable<String,String>();
		private Hashtable<String,String> definitions = new Hashtable<String,String>();
 		private ArrayList<String> semanticTypes =new ArrayList<String>();
 		ConceptLite concept ;

		
		
		public ArrayList<String> getSemanticTypes(){
			
			return semanticTypes;
		}
		
		public Hashtable<String,String> getRelations (){
			
			return relations;
		}
		
		public Hashtable<String,String> getAtoms (){
			
			return atoms;
		}

		public Hashtable<String,String> getDefinitions (){
		
			return definitions;
		}
		
		public String getID(){
			return id;
		}
		
		
		public ConceptInfo (String CID) throws Exception{
			this.id=CID;
			this.retrieveCui();
		}

		
		public ConceptLite getConcept(){
			
			return concept;
		}
		
		
		public String getContent(String urlToRead){
			
			if(urlToRead.equals("NONE"))
				return "";

			String result = getHttpReponse(urlToRead);
			return result;
		}
		
		void fillRelations(String relations) throws Exception{
			
			if(relations.equals(""))
				return;
			
			JSONObject obj = new JSONObject(relations);
			JSONArray arr = obj.getJSONArray("result");
			for (int i = 0; i < arr.length(); i++)
			{
				String relationLabel = arr.getJSONObject(i).getString("relationLabel");
				String relatedId = arr.getJSONObject(i).getString("relatedId").replace("https://uts-ws.nlm.nih.gov/rest/content/2017AB/CUI/", "");

				//System.out.println(relationLabel+"\t"+relatedId);
				
				this.relations.put(relatedId, relationLabel);
			}
		
		}
		
		
		void fillDefinitions(String definitions)throws Exception{
			
			if(definitions.equals(""))
				return;
			
			JSONObject obj = new JSONObject(definitions);
			JSONArray arr = obj.getJSONArray("result");
			for (int i = 0; i < arr.length(); i++)
			{
				String rootSource = arr.getJSONObject(i).getString("rootSource");
				String value = arr.getJSONObject(i).getString("value");

				//System.out.println(rootSource+"\t"+ value);
				this.definitions.put(rootSource, value);
				
			}
		

		}
		

		void fillAtoms(String atoms)throws Exception{
			
			if(atoms.equals(""))
				return;
			
			JSONObject obj = new JSONObject(atoms);
			JSONArray arr = obj.getJSONArray("result");
			for (int i = 0; i < arr.length(); i++)
			{
				String language = arr.getJSONObject(i).getString("language");
				String name = arr.getJSONObject(i).getString("name");

				//System.out.println(language+"\t"+ name);
				this.atoms.put(name, language);
				
			}
		}
		
		
	
		public String getHttpReponse(String urlToRead) {
			
			
			StringBuilder result = new StringBuilder();
		    try {
		    	
		    		urlToRead=urlToRead+"?pageSize=500&ticket="+ticketClient.getST(tgt);
		    		//??????//System.out.println(urlToRead);

		    	
		        	 URL url = new URL(urlToRead);
		             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		             conn.setRequestMethod("GET");
		             BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		             String line;
		             while ((line = rd.readLine()) != null) {
		                result.append(line);
		             }
		         rd.close();
		        } catch (Exception e) {
		            //e.printStackTrace();
		        }
		    
		    return result.toString();
			
		}
		
		
		
		public void extractSemanticTypes (String st){
            Pattern p = Pattern.compile("T[0-9]+");
            Matcher m = p.matcher(st);
        	while (m.find()) {
        		this.semanticTypes.add(m.group());
        	}
		}
		
		
		
		public void retrieveCui() throws Exception {
			    //if you omit the -Dversion parameter, use 'current' as the default.
			    version = System.getProperty("version") == null ? "current": System.getProperty("version");
			    
			    String urlToRead = "/rest/content/"+version+"/CUI/"+id;	    	
			    urlToRead = "https://uts-ws.nlm.nih.gov"+urlToRead;
			    
				String result = getHttpReponse(urlToRead);
				
				
				
				if(result.equals(""))
					return;
				
				
				ObjectMapper mapper = new ObjectMapper();

				LinkedHashMap<String,String>  map =   JsonPath.read(result,"$.result");
				
				Gson gson = new Gson();
				String json = gson.toJson(map,LinkedHashMap.class);
				
				//??????//System.out.println(json);

     			ConceptLite conceptLite = new ConceptLite(json);

				/*
			    Configuration config = Configuration.builder().mappingProvider(new JacksonMappingProvider()).build();
				ConceptLite conceptLite = JsonPath.using(config).parse(result).read("$.result",ConceptLite.class);
				*/
				
     			
     			
     			this.concept = conceptLite;
     			
	 			//??????//System.out.println(conceptLite.getUi()+": "+conceptLite.getName());
				//System.out.println("Semantic Type(s): "+ conceptLite.getSemanticTypes());
	 			extractSemanticTypes(conceptLite.getSemanticTypes().toString());
	 			//System.out.println("Number of Atoms: " + conceptLite.getAtomCount());
				
				
				//System.out.println("*********************Atoms***************");
				String atoms = this.getContent(conceptLite.getAtoms());
				this.fillAtoms(atoms);
				
				
				//System.out.println("*********************Definitions***************");
				String definitions = this.getContent(conceptLite.getDefinitions());
				this.fillDefinitions(definitions);
				
				//System.out.println("*********************Relations***************");
				String relations =this.getContent(conceptLite.getRelations());
				this.fillRelations(relations);
				//System.out.println("Highest Ranking Atom: "+this.getContent(conceptLite.getDefaultPreferredAtom()));
		}
		
		public static ArrayList<String> fillConceptsFromFile(String path) throws Exception{
			ArrayList<String> res = new ArrayList<String>();
			 File file = new File(path); 
			  
			  BufferedReader br = new BufferedReader(new FileReader(file)); 
			  String line; 
			  while ((line = br.readLine()) != null){ 
				  res.add(line.trim());
			  }

			return res;
		}
		public static void main(String[] args) throws Exception {
			/*RelationManager rm =new RelationManager();
			rm.fillAllSemanticTypes();
			rm.loadFile("C:\\Users\\mohan\\Desktop\\Post-doc\\Work\\Wiki-Sides\\Links\\Input for semantic types.txt");
			rm.countRelationsandSemanticTypes();
			rm.printALLInfo();
			rm.printSemanticTypes();*/
			ArrayList<String> Allconcepts = ConceptInfo.fillConceptsFromFile(args[0]);
			//System.out.println(Allconcepts.size());
			for(int i=0;i<Allconcepts.size();i++){
				String cui= Allconcepts.get(i);
				ConceptInfo ci =new ConceptInfo(cui);
				if(ci.getConcept()!=null){
					System.out.println("sides:UMLS_concept_"+cui);
					System.out.println("  rdf:type sides:UMLS_concept ;");
					for(int j=0; j<ci.getSemanticTypes().size();j++)
						System.out.println("  sides:has_for_semantic_type sides:UMLS_semantic_type_"+ci.getSemanticTypes().get(j)+" ;");
					
					System.out.println("  sides:umls_cui  \""+cui+"\" ;");

					if(ci.getDefinitions().get("MSH")!=null)
						System.out.println("  sides:umls_definition \""+ci.getDefinitions().get("MSH").replace('"', ' ')+"\"@en ;");
					System.out.println("  rdfs:label \""+ci.getConcept().getName().replace('"', ' ')+"\"@en ;");
					System.out.println(".");
				}
			}

		}
		
		
	}