import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//of course these are customizable
@JsonIgnoreProperties({"classType","ui","suppressible","dateAdded",
	"majorRevisionDate","status","semanticTypes","atomCount","attributeCount",
	"cvMemberCount","atoms","definitions","relations","defaultPreferredAtom",
	"relationCount","name"})

public class ConceptLite {
	
	
	private String ui;
	private String name;
	private String semanticTypes;
	private int atomCount;
	private String atoms;
	private String relations;
	private String definitions;
	private String defaultPreferredAtom;
	
	
	
	public ConceptLite (String json){
		
		try {
			JSONObject obj = new JSONObject(json);
			this.ui= obj.getString("ui");
			this.name= obj.getString("name");
			this.atoms= obj.getString("atoms");
			this.relations= obj.getString("relations");
			this.definitions= obj.getString("definitions");
			this.defaultPreferredAtom= obj.getString("defaultPreferredAtom");
			this.atomCount= Integer.parseInt(obj.getString("atomCount"));
			this.semanticTypes = obj.getString("semanticTypes");

			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public String getUi() {
		
		return this.ui;
	}
	
	public String getName() {
		
		return this.name;
	}
	
    public String getSemanticTypes() {
		
		return this.semanticTypes; 
	}
    
	public String getAtoms() {
		
		return this.atoms;
	}

	public int getAtomCount() {
		
		return this.atomCount;
	}
	
	public String getDefinitions() {
		
		return this.definitions;
	}
	
	public String getRelations() {
		
		return this.relations;
	}
	
	public String getDefaultPreferredAtom() {
		
		return this.defaultPreferredAtom;
	}
	
	private void setAtoms(String atoms) {
		
		this.atoms = atoms;
	}
	
	private void setUi(String ui) {
		
		this.ui = ui;
	}
	
	private void setName(String name){
		
		this.name=name;
	}
	
    public void setSemanticTypes(String stys) {
		
		this.semanticTypes = stys;
	}
	
	private void setDefinitions (String definitions) {
		
		this.definitions = definitions;
	}
	
	private void setRelations (String relations) {
		
		this.relations = relations;
	}
	
	private void setDefaultPreferredAtom(String defaultPreferredAtom) {
		
		this.defaultPreferredAtom = defaultPreferredAtom;
		
	}
}