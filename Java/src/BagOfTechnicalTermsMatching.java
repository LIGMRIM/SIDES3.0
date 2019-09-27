import java.util.Enumeration;
import java.util.Hashtable;

public class BagOfTechnicalTermsMatching {

public static void main(String[] args) throws Exception{
	
		
    	/*Annotation questions code*/
		/*Hashtable<String, Question> questions = Question.loadQuestions("./Isolated_questions_data_Virtuoso5.csv");
		System.out.println(questions.size());
		Enumeration<String> qIDs = questions.keys();
		while(qIDs.hasMoreElements()){
			String qID = qIDs.nextElement();
			Question q = questions.get(qID);
			//String[] parts = qID.split("[#]");
			File ques = new File("./QuestionTextAndCorrectPropsAlleq (Smd MSH MDR)/"+qID);
			if(!ques.exists()){
				q.fillAllAnnotations();
				q.fuseAnnoations();
				q.printFullAnnotations("./QuestionTextAndCorrectPropsAlleq (Smd MSH MDR)/");
			}
		}*/
		
		 /*finished Annotation*/
		

		/* Matching code */
		Hashtable<String, Hashtable<String,Double>> questions = Question.loadAnnotatedQuestions(".\\Datasets\\Dataset3\\QuestionsAsBagsOfTechnicalTerms\\");
		Enumeration<String> qKeys= questions.keys();
		while(qKeys.hasMoreElements()){
			String qID= qKeys.nextElement();
			Hashtable<String, Hashtable<String,Double>> learningObjectives = Question.loadAnnotatedLearningObjective(".\\Datasets\\Dataset3\\LearningObjectivesAsBagsOfTechnicalTerms (236)\\");
			//Question.filterQrel("C:\\Users\\mohan\\Desktop\\Post-doc\\Work\\Wiki-Sides\\Links\\Linking Questions\\QREL\\qrel_QMA.txt",learningObjectives);
			Question.MatchBM25(qID, questions.get(qID), learningObjectives);
			//System.out.println(qID+"\t"+Question.getQuestionLength(questions,qID));
		}		
		
	}
	

}

