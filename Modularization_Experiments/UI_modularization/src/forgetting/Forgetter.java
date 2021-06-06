package forgetting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.*;

import Test.TestForgetting;
import Test.writeFile;
import checkTautology.TautologyChecker;
import checkexistence.EChecker;
import concepts.TopConcept;
import javafx.util.*;
import com.google.common.collect.Sets;
import connectives.And;
import connectives.Exists;
import connectives.Inclusion;
import convertion.BackConverter;
import convertion.Converter;
import elk.*;
import org.apache.tools.ant.DirectoryScanner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;

import checkfrequency.FChecker;
import concepts.AtomicConcept;
import extraction.SubsetExtractor;
import formula.Formula;
import inference.DefinerIntroducer;
import inference.Inferencer;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import roles.AtomicRole;
import Test.writeFile.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import javax.swing.event.ListDataEvent;


public class Forgetter {
    public static  int isExtra = 0;
    public List<AtomicConcept> ordering(Set<AtomicConcept> c_sig,List<Formula> c_sig_list_normalised){
    	List<Formula> now_c_sig_list_normalised = new ArrayList<>(c_sig_list_normalised);
		List<AtomicConcept> now = new ArrayList<>(c_sig);
		FChecker fc = new FChecker();
		Queue<Pair<Integer,AtomicConcept>> Q = new PriorityQueue<>(new queueComparator());
		List<AtomicConcept> ans = new ArrayList<>();
		SubsetExtractor se = new SubsetExtractor();
		int t = 0;
		for(AtomicConcept concept : now){
			int num = 0;
			List<Formula>pivot_list_normalised = se.getConceptSubset(concept, now_c_sig_list_normalised);
			num=fc.positive(concept,pivot_list_normalised);
			num*=fc.negative(concept,pivot_list_normalised);
			Pair<Integer,AtomicConcept> temp= new Pair<>(num,concept);
			Q.add(temp);
			System.out.println(now.size()+" "+t);
			t++;

		}
		while(!Q.isEmpty()){
			Pair<Integer,AtomicConcept> temp=Q.poll();
			System.out.println(temp.getKey());
			ans.add(temp.getValue());
		}
		return ans;

	}
	public List<AtomicRole> ordering2(Set<AtomicRole> c_sig,List<Formula> r_sig_list_normalised){
		List<Formula> now_r_sig_list_normalised = new ArrayList<>(r_sig_list_normalised);
		List<AtomicRole> now = new ArrayList<>(c_sig);
		FChecker fc = new FChecker();
		Queue<Pair<Integer,AtomicRole>> Q = new PriorityQueue<>(new queueComparator2());
		List<AtomicRole> ans = new ArrayList<>();
		SubsetExtractor se = new SubsetExtractor();
		int t = 0;
		for(AtomicRole role : now){
			int num = 0;
			List<Formula>pivot_list_normalised = se.getRoleSubset(role, now_r_sig_list_normalised);
			num=fc.positive(role,pivot_list_normalised);
			num*=fc.negative(role,pivot_list_normalised);
			Pair<Integer,AtomicRole> temp= new Pair<>(num,role);
			Q.add(temp);
			System.out.println(now.size()+" "+t);
			t++;

		}
		while(!Q.isEmpty()){
			Pair<Integer,AtomicRole> temp=Q.poll();
			System.out.println(temp.getKey());
			ans.add(temp.getValue());
		}
		return ans;
	}
	public Set<OWLAxiom> ForgettingAPI(Set<OWLObjectProperty> roles, Set<OWLClass> concepts, OWLOntology onto) throws Exception{
    	Converter ct = new Converter();
    	Set<AtomicRole> r_sig = ct.getRolesfromObjectProperties(roles);
    	Set<AtomicConcept> c_sig = ct.getConceptsfromClasses(concepts);
    	List<Formula> formula_list_normalised = ct.OntologyConverter(onto);
    	List<Formula> res = Forgetting(r_sig,c_sig,formula_list_normalised,onto);
    	BackConverter bc = new BackConverter();
    	Set<OWLAxiom> axioms = bc.toOWLAxioms(res);
    	return axioms;
	}
	public List<Formula> Forgetting(Set<AtomicRole> r_sig, Set<AtomicConcept> c_sig,
			List<Formula> formula_list_normalised, OWLOntology onto2) throws Exception {

        TestForgetting.isExtra = 0;
        Forgetter.isExtra = 0;
		System.out.println("The Forgetting Starts:");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        //OWLOntology onto = manager.createOntology(onto2.getAxioms());// it is a copy of onto2
		OWLOntology onto  = manager.copyOntology(onto2, OntologyCopy.DEEP);
		DefinerIntroducer di = new DefinerIntroducer();
		//Simplifier pp = new Simplifier();
		SubsetExtractor se = new SubsetExtractor();
		BackConverter bc = new BackConverter();
		Inferencer inf = new Inferencer();
		FChecker fc = new FChecker();
		di.definer_left_map = new HashMap<>();
		di.definer_left_map = new HashMap<>();
		di.owldefiner_set = new LinkedHashSet<>();
		di.definer_set = new LinkedHashSet<>();
		di.reverse_definer_left_map = new HashMap<>();
		di.reverse_definer_right_map = new HashMap<>();
		if (!r_sig.isEmpty()) {
			List<Formula> r_sig_list_normalised = se.getRoleSubset(r_sig, formula_list_normalised);
			List<Formula> pivot_list_normalised = null;
			//List<AtomicRole> r_sig_ordering = ordering2(r_sig,r_sig_list_normalised);
/*
			int nn = 0;
			for(AtomicRole role:r_sig){
				System.out.println(role);
				nn++;
				int positive = 0;
				int negative = 0;
				int equiv = 0;
				for(Formula formula : r_sig_list_normalised){

					if(formula.toString().contains(role.toString())){
						System.out.println(role+" ///  "+formula);
						if(formula instanceof Inclusion){
							if(formula.getSubFormulas().get(0).toString().contains(role.toString())){
								negative++;
							}
							else{
								positive++;
							}
						}


					}
				}
				System.out.println(nn+" "+positive+" "+negative+" "+equiv+"-------------");
				System.out.println(444);
			}

 */


			int i = 1;
			for (AtomicRole role : r_sig) {

				System.out.println("Forgetting Role [" + i + "] = " + role);
				i++;
				//if(!role.toString().contains("http://snomed.info/id/732945000")) continue;
				System.out.println(r_sig_list_normalised.size());
				pivot_list_normalised = se.getRoleSubset(role, r_sig_list_normalised);
				if (pivot_list_normalised.isEmpty()) {

				} else {

                    pivot_list_normalised = di.introduceDefiners(role, pivot_list_normalised);///
                    pivot_list_normalised = inf.combination_R(role, pivot_list_normalised, onto);

					r_sig_list_normalised.addAll(pivot_list_normalised);
				}
			}

			formula_list_normalised.addAll(r_sig_list_normalised);
		}

		if (!c_sig.isEmpty()) {
			List<Formula> c_sig_list_normalised = se.getConceptSubset(c_sig, formula_list_normalised);
			List<Formula> pivot_list_normalised = null;
			int j = 1;
			List<AtomicConcept> c_sig_ordering = ordering(c_sig,c_sig_list_normalised);
			int bep2 =0,ben2 = 0;

//			writeFile.writeFile("/Users/liuzhao/Desktop/logf.txt","before forgetting " +" "+bep2+" "+ben2+"\n");
			for (AtomicConcept concept : c_sig_ordering) {
			//for (AtomicConcept concept : c_sig) {
				System.out.println("Forgetting Concept [" + j + "] = " + concept);
				j++;
				pivot_list_normalised = se.getConceptSubset(concept, c_sig_list_normalised);

				if (pivot_list_normalised.isEmpty()) {
					
				} else if (fc.positive(concept, pivot_list_normalised) == 0 ||
						fc.negative(concept, pivot_list_normalised) == 0) {
					c_sig_list_normalised.addAll(inf.Purify(concept, pivot_list_normalised));

				} else {
                    pivot_list_normalised = di.introduceDefiners(concept, pivot_list_normalised);
					pivot_list_normalised = inf.combination_A(concept, pivot_list_normalised, onto);
					c_sig_list_normalised.addAll(pivot_list_normalised);
				}
				int be = c_sig_list_normalised.size();
				c_sig_list_normalised = new ArrayList<>(new HashSet<>(c_sig_list_normalised));
				int en = c_sig_list_normalised.size();
				if(en != be){
					System.out.println(be+" "+en+" ==============");
				}

//				writeFile.writeFile("/Users/liuzhao/Desktop/logf.txt","after forgetting " +concept+" "+p2+" "+n2+"\n");


			}

			formula_list_normalised.addAll(c_sig_list_normalised);

		}





		/*
		if (!DefinerIntroducer.definer_set.isEmpty()) {
			List<Formula> d_sig_list_normalised = new ArrayList<>();
			List<Formula> forgetting_Definer_output = new ArrayList<>();
			List<Formula> pivot_list_normalised = null;
			Set<AtomicConcept> definer_set = null;
			////this is the case of the cyclic cases, that's why the ACK_A is not re-used. 
			//In case the results of contains this case. report!
			int k = 1;
			do {
				if (DefinerIntroducer.definer_set.isEmpty()) {
					System.out.println("Forgetting Successful!");
					System.out.println("===================================================");
					formula_list_normalised.addAll(forgetting_Definer_output);

					return formula_list_normalised;
				}
				
				definer_set = new LinkedHashSet<>(DefinerIntroducer.definer_set);
				d_sig_list_normalised = se.getConceptSubset(DefinerIntroducer.definer_set, formula_list_normalised);

				for (AtomicConcept concept : definer_set) {
					System.out.println("Forgetting Definer [" + k + "] = " + concept +" definer_set size :"+DefinerIntroducer.definer_set.size());
					k++;
					pivot_list_normalised = se.getConceptSubset(concept, d_sig_list_normalised);
					if (pivot_list_normalised.isEmpty()) {
						DefinerIntroducer.definer_set.remove(concept);

					} else if (fc.positive(concept, pivot_list_normalised) == 0) {
						System.out.println("purify 1");
						List<Formula> temp = inf.Purify(concept, pivot_list_normalised);
						forgetting_Definer_output.addAll(temp);
						for(Formula i : temp){
							System.out.println(i);
						}
						System.out.println("-----------");
						DefinerIntroducer.definer_set.remove(concept);

					} else if (fc.negative(concept, pivot_list_normalised) == 0) {
						System.out.println("purify 2");
						List<Formula> temp = inf.Purify(concept, pivot_list_normalised);
						forgetting_Definer_output.addAll(temp);
						for(Formula i : temp){
							System.out.println(i);
						}
						System.out.println("-----------");
						DefinerIntroducer.definer_set.remove(concept);

					} else {
						pivot_list_normalised = di.introduceDefiners(concept, pivot_list_normalised);
						pivot_list_normalised = inf.combination_A(concept, pivot_list_normalised, onto);
						forgetting_Definer_output.addAll(pivot_list_normalised);
					}
				}

			} while (true);
			*/

		if (!di.definer_set.isEmpty()) {
			List<Formula> d_sig_list_normalised = se.getConceptSubset(di.definer_set, formula_list_normalised);
			List<Formula> pivot_list_normalised = null;
			Set<AtomicConcept> definer_set = null;
			List<Formula> forgetting_Definer_output = new ArrayList<>();

			////this is the case of the cyclic cases, that's why the ACK_A is not re-used.
			//In case the results of contains this case. report!
			int k = 1;
			int num = 0;

			do {
				if (di.definer_set.isEmpty()) {
					System.out.println("Forgetting Successful!");
					System.out.println("===================================================");
					formula_list_normalised.addAll(d_sig_list_normalised);

					return formula_list_normalised;
				}

				definer_set = new LinkedHashSet<>(di.definer_set);

				//List<AtomicConcept>  definer_set_inverse = new ArrayList<>(definer_set.size());
				//List<AtomicConcept> definer_set_ordering = ordering(definer_set,d_sig_list_normalised);
				//for (AtomicConcept concept : definer_set_ordering) {
				for (AtomicConcept concept : definer_set) {
				num++;
					System.out.println("Forgetting Definer [" + k + "] = " + concept +" definer_set size :"+di.definer_set.size());
					k++;
					pivot_list_normalised = se.getConceptSubset(concept, d_sig_list_normalised);
					if (pivot_list_normalised.isEmpty()) {
						di.definer_set.remove(concept);

					} else if (fc.positive(concept, pivot_list_normalised) == 0) {

						List<Formula> ans = inf.Purify(concept, pivot_list_normalised);

						d_sig_list_normalised.addAll(ans);
						di.definer_set.remove(concept);

					} else if (fc.negative(concept, pivot_list_normalised) == 0) {

						List<Formula> ans = inf.Purify(concept, pivot_list_normalised);

						d_sig_list_normalised.addAll(ans);
						di.definer_set.remove(concept);

					} else {
						pivot_list_normalised = di.introduceDefiners(concept, pivot_list_normalised);
						pivot_list_normalised = inf.combination_A(concept, pivot_list_normalised, onto);
						d_sig_list_normalised.addAll(pivot_list_normalised);
						di.definer_set.remove(concept);

					}
				}
				if(num > 12000){
					TestForgetting.isExtra = 1;
                    Forgetter.isExtra = 1;
                    System.out.println("There is extra expressivity !");
					break;
				}
			} while (true);


		}
		else{
			System.out.println("DefinersSet is empty!! ");
			System.out.println("Forgetting Successful!");
			System.out.println("===================================================");


		}
		

		return formula_list_normalised;
	}
	public static void main(String [] args) throws  Exception {
		System.out.println(System.getProperty("user.dir"));
		String[] ontology_names = {"ncit","foodon","helis"};
		String[] ontology_file_names = {"ncit_20.12d.owl","foodon-merged.owl","helis_v1.00.origin.owl"};
		Integer ontology_index = 1;
		String input_o = "..\\test_ontologies\\"+ontology_file_names[ontology_index];

		String per = "0.1";
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[]{"**_"+per+".txt"});
		scanner.setBasedir("..\\test_terms\\"+ontology_names[ontology_index]+"\\");
		scanner.setCaseSensitive(false);
		scanner.scan();
		String[] files = scanner.getIncludedFiles();

		OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager1.getOWLDataFactory();

		OWLOntology input_onto = manager1.loadOntologyFromOntologyDocument(new File(input_o));
		int k=0;

		for(String concept_file:files){
			System.out.println(concept_file);



			System.out.println(concept_file.substring(0,concept_file.length()-4));
			Scanner scan_sig = new Scanner(new File("..\\test_terms\\"+ontology_names[ontology_index]+"\\"+concept_file));
			Set<String> sig_iri = new HashSet<>();

			Set<OWLEntity> c_hold = new HashSet<>();
			Set<OWLClass> classes = input_onto.getClassesInSignature();
			Set<AtomicConcept> c_forget = new LinkedHashSet<>();
			//		Set<OWLEntity> onto_sig = input_onto.getSignature();
			//		System.out.println(onto_sig.toArray()[0]);

			while(scan_sig.hasNextLine()){
				String n = scan_sig.nextLine();
				sig_iri.add(n);
				c_hold.add(factory.getOWLEntity(EntityType.CLASS, IRI.create(n)));
			}


			for(OWLClass c:classes){
				String id = c.toStringID();
				//			System.out.println("id: "+id);
				if(sig_iri.contains(id) == false){
					c_forget.add(new AtomicConcept(id));
				}
			}

			System.out.println("ontology class size: " + classes.size());
			System.out.println("forgetting class size: " + c_forget.size());
			System.out.println("c_hold class size: "+c_hold.size());

			Converter ct = new Converter();
			SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(manager1, input_onto, ModuleType.BOT);
			Set<OWLAxiom> moduleOnto_2OnForgettingSig = extractor.extract(c_hold);
			Set<OWLLogicalAxiom> moduleOnto_2OnCommonSig_logical = new HashSet<>();

			System.out.println("module finished");
			for (OWLAxiom axiom : moduleOnto_2OnForgettingSig) {
				if (axiom instanceof OWLLogicalAxiom) {
					moduleOnto_2OnCommonSig_logical.add((OWLLogicalAxiom) axiom);
				}

			}
			System.out.println("moduleOnto_2OnCommonSig size: "+moduleOnto_2OnCommonSig_logical.size());
			List<Formula> formulaList = ct.AxiomsConverter(moduleOnto_2OnCommonSig_logical);

			List<Formula> formula_list_normalised = ct.OntologyConverter(input_onto);

			Forgetter fg = new Forgetter();
			BackConverter bc = new BackConverter();
			OWLOntology onto = bc.toOWLOntology(new ArrayList<>(formula_list_normalised));
			System.out.println("module: "+formulaList.size()+formulaList);
			System.out.println("Start forgetting");
			long startTime = System.nanoTime();
			List<Formula> ui = fg.Forgetting(new LinkedHashSet<>(),c_forget,formulaList,input_onto);
			long endTime = System.nanoTime();

			OWLOntology result_onto = bc.toOWLOntology(new ArrayList<>(ui));

			Set<OWLClass> result_classes = result_onto.getClassesInSignature();
			System.out.println("End forgetting");
			System.out.println("Using "+(endTime-startTime)/1000000+" seconds");
//			File outfile = new File("..\\test_terms\\"+ontology_names[ontology_index]+"-ui\\"+concept_file);
			OutputStream os = new FileOutputStream("..\\test_terms\\"+ontology_names[ontology_index]+"-ui\\"+concept_file.substring(0,concept_file.length()-4)+".owl");
			manager1.saveOntology(result_onto, new OWLXMLDocumentFormat(), os);
//			FileWriter fw = new FileWriter(outfile);
//			for (OWLClass c : result_classes) {
//				fw.write(c.toStringID() +"\n");
//				fw.flush();
//			}
//			fw.close();

	}


}
class queueComparator implements  Comparator<Pair<Integer,AtomicConcept>>{
	public int compare(Pair<Integer,AtomicConcept> e1, Pair<Integer,AtomicConcept> e2) {
		return e1.getKey() - e2.getKey();//升序
		//return e2.getKey() - e1.getKey();//降序

	}
}
class queueComparator2 implements  Comparator<Pair<Integer,AtomicRole>>{
	public int compare(Pair<Integer,AtomicRole> e1, Pair<Integer,AtomicRole> e2) {
		return e1.getKey() - e2.getKey();//升序
		//return e2.getKey() - e1.getKey();//降序

	}
}}