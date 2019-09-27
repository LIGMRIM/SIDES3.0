import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;

public class RelationManager {
	
	private Hashtable<String,Integer> fileIndex = new Hashtable<String,Integer>();
	private Hashtable<String,ConceptInfo> fileConcepts = new Hashtable<String,ConceptInfo>();
	private Hashtable<String,Integer> relations = new Hashtable<String,Integer>();
	private Hashtable<String,Double> semanticTypes = new Hashtable<String,Double>();
	private Hashtable<String,String>  allSemanticTypes=  new Hashtable<String,String>();
	
	
	
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
	
	
	private void countSemanticTypes(ConceptInfo currentInfo) {
		
		double Cfreq= fileIndex.get(currentInfo.getID());
		if(currentInfo.getSemanticTypes().size()!=0)
			Cfreq= fileIndex.get(currentInfo.getID())/currentInfo.getSemanticTypes().size();
		
		
		for(int i=0; i< currentInfo.getSemanticTypes().size();i++){
			
			String sti= currentInfo.getSemanticTypes().get(i);
			if(semanticTypes.get(sti) !=null){
				double STfreq = semanticTypes.get(sti);
				STfreq=STfreq+Cfreq;
				semanticTypes.remove(sti);
				semanticTypes.put(sti, STfreq);
			}else
				semanticTypes.put(sti, Cfreq);
		}
	}
	
	public Hashtable<String,Integer> countRelationsandSemanticTypes() throws Exception{
		
		Enumeration<String> firstScan = fileIndex.keys();
		
		while(firstScan.hasMoreElements())
		{
			
			String CID =  firstScan.nextElement();
			ConceptInfo currentInfo = new ConceptInfo(CID);
			this.fileConcepts.put(CID, currentInfo);
			
			
			//count semantic types
			this.countSemanticTypes(currentInfo);

			Enumeration<String> secondScan  = fileIndex.keys();
			//count relations
			while (secondScan.hasMoreElements()){
				String SCID = secondScan.nextElement();
				String relation = whatRelation(currentInfo,SCID);
				if(relation!=null){
					
					if(relations.get(CID) !=null){
						int freq = relations.get(CID);
						freq++;
						relations.remove(CID);
						relations.put(CID, freq);
					}else
						relations.put(CID, 1);

				}
							
			}
		}
		return relations;
	} 
	
	

	public String whatRelation(ConceptInfo C1 ,String C2 ){
		
		return C1.getRelations().get(C2);
	}
	
	
	public void printRelations(){
		
		Enumeration<String> rels  = relations.keys();
		
		while (rels.hasMoreElements()){
			
			String CID = rels.nextElement();
			System.out.println(CID+"\t"+relations.get(CID));
		}


	}
	
	public void printALLInfo(){
		
		Enumeration<String> concepts  = fileIndex.keys();
		
		while (concepts.hasMoreElements()){
			
			String CID = concepts.nextElement();
			int freqC = fileIndex.get(CID);
			Integer freqR = relations.get(CID);
			if(freqR!=null)
				System.out.println(CID+"\t"+freqC+"\t"+freqR.intValue());
			else
				System.out.println(CID+"\t"+freqC);

		}
			
		
	}
	
	
public void printSemanticTypes(){
		
		Enumeration<String> sts  = semanticTypes.keys();
		
		while (sts.hasMoreElements()){
			
			String STID = sts.nextElement();
			System.out.println(allSemanticTypes.get(STID)+"\t"+semanticTypes.get(STID));
		}
}
	
 public void loadFile(String filePath){
	 try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
 	    StringBuilder sb = new StringBuilder();
 	    String line = br.readLine();

 	    while (line != null) {
 	     
 	    	
 	    	String[] parts= line.split("\t");
 	    	
 	    	fileIndex.put(parts[0], Integer.parseInt( parts[1]));

 	        line = br.readLine();
 	    }
 	    String everything = sb.toString();
 	}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	
	 
	 
 }	
	

}
