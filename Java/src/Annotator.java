import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Annotator {

    static final String REST_URL = "http://data.stageportal.lirmm.fr/annotator?";
    
    //static final String REST_URL = "http://bioportal.lirmm.fr/";
    static final String API_KEY = "22522d5c-c4fe-45fc-afc6-d43e2e613169";
    static final ObjectMapper mapper = new ObjectMapper();
    
    static Hashtable<String,Integer> conceptFreqs =new Hashtable<String,Integer>();
    static Hashtable<String,String> conceptLabels =new Hashtable<String,String>();
    static Hashtable<String,String> conceptTypes =new Hashtable<String,String>();

    
    Hashtable<String,Hashtable<String,Integer>> Paragraphs =  new Hashtable<String,Hashtable<String,Integer>>();
	private Hashtable<String,String>  allSemanticTypes=  new Hashtable<String,String>();


    int num=0;
    
    
    public static void main(String[] args) throws Exception {
        
        Annotator at = new Annotator();
        at.fillAllSemanticTypes();
        at.parseAndAnnotateFullText("C:\\Users\\mohan\\Desktop\\Post-doc\\Work\\Wiki-Sides\\Links\\One-link-Item 330.txt",
        "C:\\Users\\mohan\\Desktop\\Post-doc\\Work\\Wiki-Sides\\Links\\output_One-link-Item 330.txt");
        
        //at.parseAndAnnotateString("Prise en charge hospitali�re", "C:\\Users\\mohan\\Desktop\\Post-doc\\Work\\Wiki-Sides\\Links\\test.txt");        
        
        
        
        //at.printParagpathInfo();
        //System.out.println(at.number_without_annotations);
        at.extractConceptsFromTextFile2("C:\\Users\\mohan\\Desktop\\Post-doc\\Work\\Wiki-Sides\\Links\\output_One-link-Item 330.txt");
        //System.out.println("*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*");
        at.printConceptFrequencies();
     
    }
    
    public void fillAllSemanticTypes(){
		allSemanticTypes.put("T052", "Activity");
		allSemanticTypes.put("T053", "Behavior");
		allSemanticTypes.put("T056", "Daily or Recreational Activity");
		allSemanticTypes.put("T051", "Event");
		allSemanticTypes.put("T064", "Governmental or Regulatory Activity");
		allSemanticTypes.put("T055", "Individual Behavior");
		allSemanticTypes.put("T066", "Machine Activity");
		allSemanticTypes.put("T057", "Occupational Activity");
		allSemanticTypes.put("T054", "Social Behavior");
		allSemanticTypes.put("T017", "Anatomical Structure");
		allSemanticTypes.put("T029", "Body Location or Region");
		allSemanticTypes.put("T023", "Body Part, Organ, or Organ Component");
		allSemanticTypes.put("T030", "Body Space or Junction");
		allSemanticTypes.put("T031", "Body Substance");
		allSemanticTypes.put("T022", "Body System");
		allSemanticTypes.put("T025", "Cell");
		allSemanticTypes.put("T026", "Cell Component");
		allSemanticTypes.put("T018", "Embryonic Structure");
		allSemanticTypes.put("T021", "Fully Formed Anatomical Structure");
		allSemanticTypes.put("T024", "Tissue");
		allSemanticTypes.put("T116", "Amino Acid, Peptide, or Protein");
		allSemanticTypes.put("T195", "Antibiotic");
		allSemanticTypes.put("T123", "Biologically Active Substance");
		allSemanticTypes.put("T122", "Biomedical or Dental Material");
		allSemanticTypes.put("T118", "Carbohydrate");
		allSemanticTypes.put("T103", "Chemical");
		allSemanticTypes.put("T120", "Chemical Viewed Functionally");
		allSemanticTypes.put("T104", "Chemical Viewed Structurally");
		allSemanticTypes.put("T200", "Clinical Drug");
		allSemanticTypes.put("T111", "Eicosanoid");
		allSemanticTypes.put("T196", "Element, Ion, or Isotope");
		allSemanticTypes.put("T126", "Enzyme");
		allSemanticTypes.put("T131", "Hazardous or Poisonous Substance");
		allSemanticTypes.put("T125", "Hormone");
		allSemanticTypes.put("T129", "Immunologic Factor");
		allSemanticTypes.put("T130", "Indicator, Reagent, or Diagnostic Aid");
		allSemanticTypes.put("T197", "Inorganic Chemical");
		allSemanticTypes.put("T119", "Lipid");
		allSemanticTypes.put("T124", "Neuroreactive Substance or Biogenic Amine");
		allSemanticTypes.put("T114", "Nucleic Acid, Nucleoside, or Nucleotide");
		allSemanticTypes.put("T109", "Organic Chemical");
		allSemanticTypes.put("T115", "Organophosphorus Compound");
		allSemanticTypes.put("T121", "Pharmacologic Substance");
		allSemanticTypes.put("T192", "Receptor");
		allSemanticTypes.put("T110", "Steroid");
		allSemanticTypes.put("T127", "Vitamin");
		allSemanticTypes.put("T185", "Classification");
		allSemanticTypes.put("T077", "Conceptual Entity");
		allSemanticTypes.put("T169", "Functional Concept");
		allSemanticTypes.put("T102", "Group Attribute");
		allSemanticTypes.put("T078", "Idea or Concept");
		allSemanticTypes.put("T170", "Intellectual Product");
		allSemanticTypes.put("T171", "Language");
		allSemanticTypes.put("T080", "Qualitative Concept");
		allSemanticTypes.put("T081", "Quantitative Concept");
		allSemanticTypes.put("T089", "Regulation or Law");
		allSemanticTypes.put("T082", "Spatial Concept");
		allSemanticTypes.put("T079", "Temporal Concept");
		allSemanticTypes.put("T203", "Drug Delivery Device");
		allSemanticTypes.put("T074", "Medical Device");
		allSemanticTypes.put("T075", "Research Device");
		allSemanticTypes.put("T020", "Acquired Abnormality");
		allSemanticTypes.put("T190", "Anatomical Abnormality");
		allSemanticTypes.put("T049", "Cell or Molecular Dysfunction");
		allSemanticTypes.put("T019", "Congenital Abnormality");
		allSemanticTypes.put("T047", "Disease or Syndrome");
		allSemanticTypes.put("T050", "Experimental Model of Disease");
		allSemanticTypes.put("T033", "Finding");
		allSemanticTypes.put("T037", "Injury or Poisoning");
		allSemanticTypes.put("T048", "Mental or Behavioral Dysfunction");
		allSemanticTypes.put("T191", "Neoplastic Process");
		allSemanticTypes.put("T046", "Pathologic Function");
		allSemanticTypes.put("T184", "Sign or Symptom");
		allSemanticTypes.put("T087", "Amino Acid Sequence");
		allSemanticTypes.put("T088", "Carbohydrate Sequence");
		allSemanticTypes.put("T028", "Gene or Genome");
		allSemanticTypes.put("T085", "Molecular Sequence");
		allSemanticTypes.put("T086", "Nucleotide Sequence");
		allSemanticTypes.put("T083", "Geographic Area");
		allSemanticTypes.put("T100", "Age Group");
		allSemanticTypes.put("T011", "Amphibian");
		allSemanticTypes.put("T008", "Animal");
		allSemanticTypes.put("T194", "Archaeon");
		allSemanticTypes.put("T007", "Bacterium");
		allSemanticTypes.put("T012", "Bird");
		allSemanticTypes.put("T204", "Eukaryote");
		allSemanticTypes.put("T099", "Family Group");
		allSemanticTypes.put("T013", "Fish");
		allSemanticTypes.put("T004", "Fungus");
		allSemanticTypes.put("T096", "Group");
		allSemanticTypes.put("T016", "Human");
		allSemanticTypes.put("T015", "Mammal");
		allSemanticTypes.put("T001", "Organism");
		allSemanticTypes.put("T101", "Patient or Disabled Group");
		allSemanticTypes.put("T002", "Plant");
		allSemanticTypes.put("T098", "Population Group");
		allSemanticTypes.put("T097", "Professional or Occupational Group");
		allSemanticTypes.put("T014", "Reptile");
		allSemanticTypes.put("T010", "Vertebrate");
		allSemanticTypes.put("T005", "Virus");
		allSemanticTypes.put("T071", "Entity");
		allSemanticTypes.put("T168", "Food");
		allSemanticTypes.put("T073", "Manufactured Object");
		allSemanticTypes.put("T072", "Physical Object");
		allSemanticTypes.put("T167", "Substance");
		allSemanticTypes.put("T091", "Biomedical Occupation or Discipline");
		allSemanticTypes.put("T090", "Occupation or Discipline");
		allSemanticTypes.put("T093", "Health Care Related Organization");
		allSemanticTypes.put("T092", "Organization");
		allSemanticTypes.put("T094", "Professional Society");
		allSemanticTypes.put("T095", "Self-help or Relief Organization");
		allSemanticTypes.put("T038", "Biologic Function");
		allSemanticTypes.put("T069", "Environmental Effect of Humans");
		allSemanticTypes.put("T068", "Human-caused Phenomenon or Process");
		allSemanticTypes.put("T034", "Laboratory or Test Result");
		allSemanticTypes.put("T070", "Natural Phenomenon or Process");
		allSemanticTypes.put("T067", "Phenomenon or Process");
		allSemanticTypes.put("T043", "Cell Function");
		allSemanticTypes.put("T201", "Clinical Attribute");
		allSemanticTypes.put("T045", "Genetic Function");
		allSemanticTypes.put("T041", "Mental Process");
		allSemanticTypes.put("T044", "Molecular Function");
		allSemanticTypes.put("T032", "Organism Attribute");
		allSemanticTypes.put("T040", "Organism Function");
		allSemanticTypes.put("T042", "Organ or Tissue Function");
		allSemanticTypes.put("T039", "Physiologic Function");
		allSemanticTypes.put("T060", "Diagnostic Procedure");
		allSemanticTypes.put("T065", "Educational Activity");
		allSemanticTypes.put("T058", "Health Care Activity");
		allSemanticTypes.put("T059", "Laboratory Procedure");
		allSemanticTypes.put("T063", "Molecular Biology Research Technique");
		allSemanticTypes.put("T062", "Research Activity");
		allSemanticTypes.put("T061", "Therapeutic or Preventive Procedure");
		
	}
	
  
    
    public void extractConceptsFromTextFile(String file){
    	
    	try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    StringBuilder sb = new StringBuilder();
    	    String line = br.readLine();
            Pattern p = Pattern.compile("C[0-9]*");

    	    while (line != null) {
    	    	Matcher m = p.matcher(line);
            	while (m.find()) {
            		if(m.group().length()>5){
                		 if(conceptFreqs.containsKey(m.group()))
                		 {
                			int CFreq = conceptFreqs.get(m.group());
                			CFreq++;
                			conceptFreqs.remove(m.group());
                			conceptFreqs.put(m.group(),CFreq);
                		 }else{
                 			conceptFreqs.put(m.group(),1);

                		 }
                		}
            	}
    	    	line = br.readLine();

    	    }
    	    br.close();
    	    
    	}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	
    	
    }

    public void extractConceptsFromTextFile1(String file){
    	
    	try(BufferedReader br = new BufferedReader(
    			   new InputStreamReader(new FileInputStream(file), "UTF8"))) {
    	    String line = br.readLine();

    	    while (line != null) {
    	    	
    	    	String[] parts= line.split("\t");
  	
    	    	if(parts.length==3){
    	    		if(conceptFreqs.containsKey(parts[0])){
                			int CFreq = conceptFreqs.get(parts[0]);
                			CFreq++;
                			conceptFreqs.remove(parts[0]);
                			conceptFreqs.put(parts[0],CFreq);
                			
                		 }else{
                 			conceptFreqs.put(parts[0],1);
                			conceptLabels.put(parts[0], parts[1]);
                		 }
                		}
    	    		line = br.readLine();
    	    }
    	    
    	}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
public void extractConceptsFromTextFile2(String file){
    	
    	try(BufferedReader br = new BufferedReader(
    			   new InputStreamReader(new FileInputStream(file), "UTF8"))) {
    	    String line = br.readLine();

    	    while (line != null) {
    	    	
    	    	String[] parts= line.split("\t");
  	
    	    	if(parts.length>=2){
    	    		if(conceptFreqs.containsKey(parts[0])){
                			int CFreq = conceptFreqs.get(parts[0]);
                			CFreq++;
                			conceptFreqs.remove(parts[0]);
                			conceptFreqs.put(parts[0],CFreq);
                			
                		 }else{
                 			conceptFreqs.put(parts[0],1);
                			conceptLabels.put(parts[0], parts[1]);
                			if(parts.length>=5)
                				conceptTypes.put(parts[0], parts[4]);
                		 }
                		}
    	    		line = br.readLine();
    	    }
    	    
    	}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }



public void parseAndAnnotateString(String text,String output) throws Exception{
	
	   		 annotate(text, filterText(text),0,output);


}

    
    public void parseAndAnnotateFullText(String file, String outputfile){
    	
    	int i=0;
    	try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    StringBuilder sb = new StringBuilder();
    	    String line = br.readLine();

    	    while (line != null) {
    	     
    	    	num++;
    	    	String[] parts= line.split("\t");
        	    System.out.println(parts[1]);

    	        

        	    FilterLinks fl =new FilterLinks();
           	    org.jsoup.select.Elements paragraphs = fl.getHTMLParagraphs(parts[1]);
           	    paragraphs.addAll(fl.getHTMLH1(parts[1]));
           	    paragraphs.addAll(fl.getHTMLH2(parts[1]));
           	    paragraphs.addAll(fl.getHTMLH3(parts[1]));
           	    paragraphs.addAll(fl.getHTMLH4(parts[1]));


        	    for(Element p : paragraphs){

        	    	 String text = p.text();

        	    	 i++;
         	    	 if(text.length()>0)
         	    		 annotate(parts[0], filterText(text),i,outputfile);

        	    }
        	    i=0;
    	        line = br.readLine();
    	    	//line =null;
    	    }
    	    String everything = sb.toString();
    	    System.out.println("num:"+num);
    	}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	
  	
    	
    }
    
String filterText(String text){
    	
    	return text.replace(';', ' ').replace('/', ' ').replace('*', ' ').replace('%', ' ').replace('?', ' ');
    	
    }
    public void parseAndAnnotate(String file,String outputfile){
    	int i=0;
    	try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    StringBuilder sb = new StringBuilder();
    	    String line = br.readLine();

    	    while (line != null) {
    	     
    	    	
    	    	String[] parts= line.split("\t");
    	    	
    	    	
    	    	annotate(parts[0], parts[1],i,outputfile);
    	    	
    	    	
    	        line = br.readLine();
    	    }
    	    String everything = sb.toString();
    	}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    
    
    public void annotate (String obj, String textToAnnotate,int idx,String outputfile) throws Exception{
    	
    	String urlParameters;
        JsonNode annotations;

        /*
        // Get just annotations
        urlParameters = "text=" + textToAnnotate;
        annotations = jsonToNode(get(REST_URL + "/annotator?" + urlParameters));
        printAnnotations(annotations);
         
        // Annotations with hierarchy
        urlParameters = "max_level=3&text=" + textToAnnotate;
        annotations = jsonToNode(get(REST_URL + "/annotator?" + urlParameters));
        printAnnotations(obj,textToAnnotate, annotations ,outputfile);


	
        // Annotations using POST (necessary for long text)
        urlParameters = "text=" + textToAnnotate;
        annotations = jsonToNode(post(REST_URL + "/annotator", urlParameters));
        printAnnotations(annotations);
        
      	*/

        //conn.setRequestProperty("expand_semantic_types_hierarchy", "true");
        //conn.setRequestProperty("expand_mappings", "true");

        // Get labels, synonyms, and definitions with returned annotations
        //urlParameters = "longest_only=true&expand_semantic_types_hierarchy=true&include=prefLabel,cui,synonym,definition&text=<" +textToAnnotate.replace("+", "%20")+">";
      
        //System.out.println(REST_URL + "/annotator?"+urlParameters);
        //textToAnnotate ="Chez l�enfant, l�AC est plus souvent la cons�quence d�une d�faillance respiratoire ou circulatoire qu�un arr�t cardiaque primitif caus� par une arythmie. De ce fait, il est important de reconna�tre pr�cocement les signes de ces d�faillances afin de pr�venir la survenue de l�AC. Avant la pubert�, l�appel au service d�aide m�dicale urgente (SAMU-Centre 15) est fait apr�s cinq insufflations et une minute de RCP si le sauveteur est seul. Toutefois, quel que soit son �ge, si l�enfant pr�sente un AC sans prodrome et s�effondre devant un t�moin seul, le t�moin appelle en premier puis d�bute la RCP de base. Si deux sauveteurs sont pr�sents face � un enfant sans r�ponse, l�un appelle imm�diatement les secours, l�autre commence la RCP. S�il s�agit d�un nourrisson de moins d�un an, la RCP est poursuivie autant que possible pendant l�appel. La recherche de pouls par un professionnel de sant� s�effectue au niveau brachial ou ou f�moral avant l��ge de 1 an, au niveau pouls carotidien ou f�moral apr�s l��ge de 1 an. Chez l�enfant inconscient, une bradycardie inf�rieure � 60 battements par minute associ�e � des troubles h�modynamiques p�riph�riques impose la mise en �uvre de la RCP. Le rythme du massage cardiaque externe est de 100 minute. Les compressions thoraciques sont r�alis�es sur le tiers inf�rieur du sternum avec une d�pression du tiers de la hauteur du thorax. Le MCE est r�alis� avec deux doigts chez le nourrisson, avec le talon d�une ou deux mains en fonction de la morphologie chez l�enfant plus grand. En cas d�obstruction des voies a�riennes sup�rieures, si l�enfant ne respire pas, il faut pratiquer 5 insufflations. En cas d�insufflation inefficace (le thorax ne se soul�ve pas), il faut suspecter une inhalation de corps �tranger (CE) dans les voies a�riennes. En cas d�inhalation devant t�moin se manifestant par une toux inefficace, une suffocation (l�enfant ne peut plus parler), une d�tresse respiratoire brutale avec stridor, une cyanose, une perte de conscience, il faut pratiquer les man�uvres de d�sobstruction. La man�uvre de Heimlich ne doit pas �tre pratiqu�es chez l�enfant jeune, et est dangereuse chez l�enfant de moins de 1 an. Chez l�enfant conscient qui tousse apr�s inhalation d�un corps �tranger, il faut encourager la toux pour qu�elle reste efficace et ensuite surveiller l�enfant jusqu�� l�arriv�e des secours. La technique de ventilation est le bouche � bouche-nez ou le bouche � bouche chez l�enfant de moins de 1 an, et le le bouche � bouche chez l�enfant de plus de 1 an. Chaque insufflation dure 1 � 1,5 secondes et est effectu�e avec une pression suffisante pour soulever le thorax. En pr�sence d�un seul sauveteur, le rapport compressions thoraciques insufflations est de 30 2 chez le nourrisson, l�enfant et l�adulte. En pr�sence de deux sauveteurs, le rapport compressions thoraciques insufflations est de 15 2 pour deux sauveteurs quel que soit l��ge de l�enfant, (nouveau-n� exclu). La d�fibrillation peut �tre r�alis�e avec un DAE d�s l��ge de 1 an. Si le DAE n�est pas adapt� � la p�diatrie, l�usage d�att�nuateur d��nergie est recommand� chez l�enfant de moins de 8 ans ou de 25 kg. Les �lectrodes adultes sont utilisables chez l�enfant de plus de 10 kg. L��nergie recommand�e pour chaque choc est de 4 J kg. Les modalit�s de la RCP m�dicalis�e chez l�enfant sont globalement similaires � celles de l�adulte. D�s l�arriv�e sur place, l��quipe m�dicale doit obtenir la confirmation du diagnostic d�AC avec absence de conscience, absence de ventilation et absence de pouls per�u en brachial chez l�enfant de moins de 1 an, en carotidien chez l�enfant de plus de 1 an. La ventilation en FiO2 100  , par masque reli� � un insufflateur manuel, est instaur�e ou poursuivie, et l�intubation endotrach�ale doit �tre r�alis�e pr�cocement avec une sonde si possible � ballonnet. Le masque laryng� constitue une alternative en cas d�intubation difficile, mais sa mise en place est plus d�licate que chez l�adulte. Lorsque les voies a�riennes sont s�curis�es (intubation), il n�y a pas lieu d�interrompre le MCE durant les insufflations. L�obtention d�une voie veineuse p�riph�rique (VVP) est souvent difficile chez l�enfant. La voie intra-osseuse (VIO) est recommand�e si une VVP ne peut pas �tre imm�diatement obtenue. La voie intra-trach�ale est une voie d�urgence utilisable uniquement pour la premi�re dose d�adr�naline. La posologie de l�adr�naline intra-trach�ale est de 100 g kg dilu� dans du s�rum sal� isotonique. Le s�rum sal� isotonique est le vecteur intraveineux ou intra-osseux de l�ensemble des m�dicaments. Une expansion vol�mique ne doit �tre utilis�e que pour compenser � une hypovol�mie. L�adr�naline doit �tre administr�e chez l�enfant � la dose de 10 ?g kg en IV ou IO pour la premi�re dose et pour les doses suivantes. Bien qu�il n�existe aucune �tude dans l�AC de l�enfant en faveur de l�amiodarone, par analogie avec l�adulte, elle est le m�dicament � utiliser en premi�re intention dans la FV TV r�cidivante. Elle est utilis�e en bolus � la dose de 5 mg kg. Le magn�sium ne peut �tre recommand�, comme pour l�adulte qu�en cas d�hypomagn�s�mie av�r�e ou de torsades de pointes. Le calcium n�a pas d�indication en cas d�AC, sauf s�il existe une hypocalc�mie connue ou une intoxication aux inhibiteurs calciques. La prise en charge post-RACS n�cessite l�admission en service sp�cialis�. L�objectif de la ventilation apr�s un AC est d�obtenir une normoxie et une normocapnie. Une hypothermie th�rapeutique peut �tre effectu�e durant les 24-36 premi�res heures post RACS   � d�faut, toute hyperthermie post-RACS doit �tre activement combattue. L�objectif d�une hom�ostasie m�tabolique en particulier glucidique est un objectif majeur des premiers jours de r�animation post-RACS.";
        annotations = jsonToNode(sendPost(textToAnnotate));
        //printShortAnnotations(obj,textToAnnotate, annotations ,outputfile);
        //System.out.println(textToAnnotate.replace('�', ' ')); 
       
        extractAnnotations(obj,textToAnnotate,idx,annotations,outputfile);

    }
    	
    
 // HTTP GET request
 	private String sendGet(String text) throws Exception {

 		System.out.println(text);
 		
 		String workText = URLEncoder.encode(text, "UTF-8");
 	 		
 		String url = "http://services.bioportal.lirmm.fr/annotator/?text="+
 				workText
 				+".&longest_only=true&exclude_numbers=true&whole_word_only=true&exclude_synonyms=true&expand_mappings=false&negation=false&experiencer=false&temporality=false&score=cvalueh&score_threshold=0&confidence_threshold=0&lemmatize=false&display_links=false&display_context=false&apikey=1de0a270-29c5-4dda-b043-7c3580628cd5";
 		String line ="";
 		String result ="";
 		
 		
 		System.out.println(url);
 		
 		URL obj = new URL(url);
 		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

 		// optional default is GET
 		con.setRequestMethod("GET");

 		//int responseCode = con.getResponseCode();
 		//System.out.println("\nSending 'GET' request to URL : " + url);
 		//System.out.println("Response Code : " + responseCode);

 		
 		  BufferedReader rd = new BufferedReader(
                  new InputStreamReader(con.getInputStream(), "UTF-8"));
          while ((line =  rd.readLine()) != null) {
              result += line;
          }
          rd.close();
 		
          return result;

 	}
 	
 	
 	
 // HTTP POST request
 	public String sendPost(String text) throws Exception {

 		
 		String line ="";
 		String result ="";
 		
 		
 		String url = "http://services.bioportal.lirmm.fr/annotator/";
 		URL obj = new URL(url);
 		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

 		//add reuqest header
 		 conn.setRequestMethod("POST");
 		 conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
         conn.setRequestProperty("Accept", "application/json");
         conn.setRequestProperty("charset", "UTF-8");
     
 		
         //&ontologies=MSHFRE,SNMIFRE
         //,MDRFRE
         String urlParameters = "text="+URLEncoder.encode(text, "UTF-8")+"&ontologies=MSHFRE,SNMIFRE,MDRFRE&longest_only=true&exclude_numbers=true&whole_word_only=true&exclude_synonyms=true&expand_mappings=false&negation=false&experiencer=false&temporality=false&score_threshold=0&confidence_threshold=0&lemmatize=false&display_links=false&display_context=false&apikey=1de0a270-29c5-4dda-b043-7c3580628cd5";
 		
 		// Send post request
 		conn.setDoOutput(true);
 		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
 		wr.writeBytes(urlParameters);
 		wr.flush();
 		wr.close();

 		int responseCode = conn.getResponseCode();
 		//System.out.println("\nSending 'POST' request to URL : " + url);
 		//System.out.println("Post parameters : " + urlParameters);
 		//System.out.println("Response Code : " + responseCode);

 		 BufferedReader rd = new BufferedReader(
                 new InputStreamReader(conn.getInputStream(), "UTF-8"));
         while ((line =  rd.readLine()) != null) {
             result += line;
         }
         rd.close();
		
         return result;
 	}

   
   
    
    int number_without_annotations=0;

    private void printAnnotations(String obj, String textToAnnotate, JsonNode annotations, String file) {
    	
    	try {
    	    //create a temporary file
    	    File logFile=new File(file);

    	    BufferedWriter writer = new BufferedWriter(new FileWriter(logFile,true));

    	    writer.write (obj);
            writer.write ("\n");

            if(annotations.size()==0)
            	number_without_annotations++;

            
            
        for (JsonNode annotation : annotations) {
            // Get the details for the class that was found in the annotation and print
        	
            JsonNode classDetails = jsonToNode(get(annotation.get("annotatedClass").get("links").get("self").asText()));
            //System.out.println(classDetails);
    	    writer.write ("Class details");
            writer.write ("\n");
    	    writer.write ("\tid: " + classDetails.get("@id").asText());
            writer.write ("\n");
    	    writer.write ("\tprefLabel: " + classDetails.get("prefLabel").asText());
            writer.write ("\n");
            String queryText= classDetails.get("@id").asText().replaceAll("#", "%23");
            JsonNode queryResults= jsonToNode(get("http://data.stageportal.lirmm.fr/search?apikey=22522d5c-c4fe-45fc-afc6-d43e2e613169&q="+queryText+"&ontologies=&include_properties=false&include_views=false&includeObsolete=false&require_definition=false&exact_match=true&categories="));
            //System.out.println("http://data.stageportal.lirmm.fr/search?apikey=22522d5c-c4fe-45fc-afc6-d43e2e613169&q="+queryText+"&ontologies=&include_properties=false&include_views=false&includeObsolete=false&require_definition=false&exact_match=true&categories=".replaceAll(" ", "%20"));
            
            
            /*writer.write ("\tcui: " + queryResults.toString());
            writer.write ("\n");*/
            
            //textToAnnotate.replaceAll(classDetails.get("prefLabel").asText(),  "<h1 style=\"color:blue;\">"+classDetails.get("prefLabel").asText()+"</h1>");         
            Pattern p = Pattern.compile("C[0-9]*");
           
            
            
            JsonNode collection = jsonToNode(queryResults.get("collection").get(0).toString());
          
            if(collection.get("cui")!=null){
            	writer.write ("\tcui: ");
            	Matcher m = p.matcher(collection.get("cui").toString());
            	while (m.find()) {
            		 writer.write(m.group()+",");
            		 
            		 if(conceptFreqs.containsKey(m.group()))
            		 {
            			int CFreq = conceptFreqs.get(m.group());
            			CFreq++;
            			conceptFreqs.remove(m.group());
            			conceptFreqs.put(m.group(),CFreq);
            			

            		 }else{
             			conceptFreqs.put(m.group(),1);
             			conceptLabels.put(m.group(), classDetails.get("prefLabel").asText());

            		 }
            		 

            		 
            		 
            		 
            	}			
               	writer.write ("\n");
            }
            if(collection.get("synonym")!=null){
            	     	
            	writer.write ("\tsynonym: ");
            	
            	Matcher m = p.matcher(collection.get("synonym").toString());
            	while (m.find()){
            			
            		if(m.group().length()>5){
            		writer.write(m.group()+",");
            		 if(conceptFreqs.containsKey(m.group()))
            		 {
            			int CFreq = conceptFreqs.get(m.group());
            			CFreq++;
            			conceptFreqs.remove(m.group());
            			conceptFreqs.put(m.group(),CFreq);
            		 }else{
             			conceptFreqs.put(m.group(),1);
             			conceptLabels.put(m.group(), classDetails.get("prefLabel").asText());

            		 }
            		}
            		
            		
            		
            	}
            	
            	
            	
            	
               	writer.write ("\n");
               
            }            /*if(queryResults.get("collection").size()!=0 &&queryResults.get("collection").get(0).get("cui")!=null)
            	System.out.println("\tcui: "+  queryResults.get("collection").get(0).get("cui").get(0).asText());*/
            writer.write ("\tontology: " + classDetails.get("links").get("ontology").asText());
            writer.write ("\n");

            JsonNode hierarchy = annotation.get("hierarchy");
            // If we have hierarchy annotations, print the related class information as well
            if (hierarchy.isArray() && hierarchy.elements().hasNext()) {
                System.out.println("\tHierarchy annotations");
                for (JsonNode hierarchyAnnotation : hierarchy) {
                    classDetails = jsonToNode(get(hierarchyAnnotation.get("annotatedClass").get("links").get("self").asText()));
                    System.out.println("\t\tClass details");
                    System.out.println("\t\t\tid: " + classDetails.get("@id").asText());
                    System.out.println("\t\t\tprefLabel: " + classDetails.get("prefLabel").asText());
                    System.out.println("\tcui: " + classDetails.get("cui").asText());
                    System.out.println("\t\t\tontology: " + classDetails.get("links").get("ontology").asText());
                }
            }
        }
    	    //Close writer
    	    writer.close();
    	    
    	    
    	} catch(Exception e) {
    	    e.printStackTrace();
    	}
    	
    }
    
    
    
    
public void extractAnnotations(String url, String text,int idx, JsonNode annotations,String outputfile){
	 try {

		File logFile=new File(outputfile);

		BufferedWriter writer = new BufferedWriter(new FileWriter(logFile,true));
		
		
		if(annotations.size()==0)
		 	return;
		
	 	writer.write(new String (text.getBytes("UTF-8"))+"\n");

		
		 for (JsonNode annotation : annotations) {
				String line ="";

		    
			 	if(annotation.get("annotatedClass").get("cui")!=null){
			 		for(int i=0; i< annotation.get("annotations").size();i++){
			 			JsonNode cuis = annotation.get("annotatedClass").get("cui");
				 		for(int j=0; j< cuis.size();j++){
				 			//line+= annotation.get("annotatedClass").get("cui").get(j).asText()+"\t";
				 			if(cuis.get(j)!=null){
				 				line+= cuis.get(j).asText()+"\t";
				 				line+=annotation.get("annotations").get(i).get("text").asText().toLowerCase()+"\t";
				 				line+=annotation.get("annotations").get(i).get("from").asText()+"\t";
				 				line+=annotation.get("annotations").get(i).get("to").asText()+"\t";
				 				if(annotation.get("annotatedClass").get("semanticType")!=null)
				 					line+=annotation.get("annotatedClass").get("semanticType").get(0).asText()+"\t";
				 				line+="\n";
				 				
				 				String hashIdx = "";
				 				if(annotation.get("annotatedClass").get("semanticType")!=null)
				 					hashIdx = url+"\tP"+idx+"\t"+cuis.get(j).asText()+"\t"+annotation.get("annotatedClass").get("prefLabel").asText().toLowerCase()+"\t"+annotation.get("annotations").get(i).get("from").asText()+
				 						"\t"+annotation.get("annotations").get(i).get("to").asText()+"\t"+annotation.get("annotatedClass").get("semanticType").get(0).asText();
				 				else
				 					hashIdx = url+"\tP"+idx+"\t"+cuis.get(j).asText()+"\t"+annotation.get("annotatedClass").get("prefLabel").asText().toLowerCase()+"\t"+annotation.get("annotations").get(i).get("from").asText()+
			 						"\t"+annotation.get("annotations").get(i).get("to").asText();
				 				String hashIdx1=url+":<P"+ idx+">\n" + text;
				 				
				 				if(!this.Paragraphs.containsKey(hashIdx1))
				 					this.Paragraphs.put(hashIdx1, new  Hashtable<String,Integer>());
				 				 if(this.Paragraphs.get(hashIdx1).containsKey(hashIdx))
			            		 {
			            			int CFreq = this.Paragraphs.get(hashIdx1).get(hashIdx);
			            			CFreq++;
			            			this.Paragraphs.get(hashIdx1).remove(hashIdx);
			            			this.Paragraphs.get(hashIdx1).put(hashIdx,CFreq);
			            		 }else{
			            			 this.Paragraphs.get(hashIdx1).put(hashIdx,1);
			             			 conceptLabels.put(cuis.get(j).asText(), annotation.get("annotations").get(i).get("text").asText().toLowerCase());

			            		 }
				 				
				 				

				 				
				 				
				 			}
				 		}
			 			//line += annotation.get("annotatedClass").get("prefLabel").asText()+"\t";
			 	    	line+="\n";
			 		}
			 	    
			 	}else{
			 		/*for(int i=0; i< annotation.get("annotations").size();i++){
			 			line+="C0000000\t";
		 	    		line+=annotation.get("annotations").get(0).get("text").asText().toLowerCase()+"\t";
			 			line += annotation.get("annotatedClass").get("prefLabel").asText()+"\t";
			 	    	line+="\n";
			 		}*/

			 	}

			 	writer.write(new String (line.getBytes("UTF-8")));
		 }
 	    
		 writer.close();

	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}    

 private void printShortAnnotations(String obj, String textToAnnotate, JsonNode annotations, String file) {
    	
    	try {
    	    //create a temporary file
    	    File logFile=new File(file);

    	    BufferedWriter writer = new BufferedWriter(new FileWriter(logFile,true));

    	    writer.write (obj);
            writer.write ("\n");

            writer.write (new String(annotations.toString().getBytes("UTF-8")));
            writer.write ("\n");

    	    //Close writer
    	    writer.close();
    	    
    	    
    	} catch(Exception e) {
    	    e.printStackTrace();
    	}
    	
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

    private String get(String urlToGet) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("charset", "utf-8");

            rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String post(String urlToGet, String urlParameters) {
        URL url;
        HttpURLConnection conn;

        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("charset", "UTF-8");
        

            conn.setUseCaches(false);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            conn.disconnect();

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line =  rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    
    
    public void printConceptFrequencies(){
    	
    	int  sTypes[] = new int[300];
    	
    	  for (int i = 0; i < sTypes.length; i++)
    		  sTypes[i]=0;
    	
    	Enumeration<String> keys = conceptFreqs.keys();
        while(keys.hasMoreElements()){
          
        	String CID = keys.nextElement();
        	int Cfreq = conceptFreqs.get(CID);
        	String label= conceptLabels.get(CID);
        	String sType=conceptTypes.get(CID);
        	System.out.println(CID+"\t"+label+"\t"+Cfreq+"\t"+sType+"\t");
   			if(sType!=null){
   				int idx = Integer.parseInt(sType.replaceAll("T",""));
   				sTypes[idx]+= Cfreq;

   			}
        	
        	
        }
        for (int i = 0; i < sTypes.length; i++) {


        	if(sTypes[i]!=0){
        		if(i<10)
        			System.out.println("T00"+i+"\t"+sTypes[i]);
        		else if(i<100)
        			System.out.println("T0"+i+"\t"+sTypes[i]);
        		else
        			System.out.println("T"+i+"\t"+sTypes[i]);

        		

        	}
		
			
		}
   	
    }
    
    
    
    public void printParagpathInfo() throws Exception{
    	
    	Enumeration<String> enumParagraph = this.Paragraphs.keys();
    	
    	
    	
    	
    	while(enumParagraph.hasMoreElements()){
    		String paragraph = enumParagraph.nextElement();
    		System.out.println(paragraph);
        	Enumeration<String> enumConcepts = this.Paragraphs.get(paragraph).keys();
        	System.out.println("-------------------------------------------------------------------------------");
        	while (enumConcepts.hasMoreElements()){
        		String conceptInfo =  enumConcepts.nextElement();
        		String parts[] = conceptInfo.split("\t");
        		//System.out.println(conceptInfo);
        		
        		ConceptInfo myConcept = new ConceptInfo(parts[2]);
        		String name ="";
        		if (myConcept.getConcept()!=null)
        			name = myConcept.getConcept().getName();
        		else
        			name ="****";
        		
        		
        		if(parts.length>6){
        			System.out.println(parts[2]+"\t"+conceptLabels.get(parts[2])+"\t"+parts[3]+"\t"+name+"\t"+parts[4]+"\t"+parts[5]+"\t"+allSemanticTypes.get( parts[6].trim()));
 
            	}
        		
        		else
        			System.out.println(parts[2]+"\t"+conceptLabels.get(parts[2])+"\t"+parts[3]+"\t"+name+"\t"+parts[4]+"\t"+parts[5]);
        		
        		
        	//System.out.println("\n");

    	}
        	//System.out.println("-------------------------------------------------------------------------------");

    	
    }
    	
    	
    
    }

}