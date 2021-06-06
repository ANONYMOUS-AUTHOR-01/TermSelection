package forgetting;

import BackTrack.BackTrack;
import formula.Formula;


import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import roles.AtomicRole;
import concepts.AtomicConcept;
import convertion.BackConverter;
import convertion.Converter;

//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.collect.Sets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import elk.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 *
 * @author Yizheng
 */
public class LDiff {

	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public void saveUI(Set<OWLAxiom> ui, String path)throws Exception{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology now = manager.createOntology(ui);
		OutputStream ops = new FileOutputStream(new File(path));
		manager.saveOntology(now, new OWLXMLDocumentFormat(), ops);
	}

    /**
     *
     * @param onto_1  the old version of ontology
     * @param onto_2  the new version of ontology
     * @throws Exception
	 * @return four ontologies, uniform interpolation, complete witness, explicit witness, implicit witness
     */
    public List<OWLOntology> LDiff(OWLOntology onto_1,OWLOntology onto_2)throws Exception{ // f
    	List<OWLOntology> ans = new ArrayList<>();
		Set<OWLClass> c_sig_1 = onto_1.getClassesInSignature();
		Set<OWLClass> c_sig_2 = onto_2.getClassesInSignature();
		Set<OWLClass> c_sig = new LinkedHashSet<>(Sets.difference(c_sig_2, c_sig_1));
		Set<OWLObjectProperty> r_sig_1 = onto_1.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> r_sig_2 = onto_2.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> r_sig = new LinkedHashSet<>(Sets.difference(r_sig_2, r_sig_1));

		Set<OWLEntity> forgettingSignatures = new HashSet<>();
		forgettingSignatures.addAll(r_sig);
		forgettingSignatures.addAll(c_sig);
		// Extract module to speed our tool on common signature.
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(manager, onto_2, ModuleType.BOT);
		Set<OWLAxiom> moduleOnto_2OnForgettingSig = extractor.extract(Sets.difference(onto_2.getSignature(),forgettingSignatures));
		Set<OWLLogicalAxiom>moduleOnto_2_OnCommonSig_logical = new HashSet<>();

		for(OWLAxiom axiom : moduleOnto_2OnForgettingSig){
			if(axiom instanceof OWLLogicalAxiom){
				moduleOnto_2_OnCommonSig_logical.add((OWLLogicalAxiom)axiom);
			}
		}
		System.out.println("module size "+moduleOnto_2_OnCommonSig_logical.size()+"  o2 size "+ onto_2.getLogicalAxioms().size());


		Converter ct = new Converter();
		BackConverter bc = new BackConverter();
		Forgetter forgetter = new Forgetter();

		Set<AtomicRole> role_set = ct.getRolesfromObjectProperties(r_sig);
		Set<AtomicConcept> concept_set = ct.getConceptsfromClasses(c_sig);

		//List<Formula> formula_list = ct.AxiomsConverter(moduleOnto_2OnCommonSig_logical_temp);
		List<Formula> formula_list = ct.AxiomsConverter(moduleOnto_2_OnCommonSig_logical);

		System.out.println("The forgetting task is to eliminate [" + concept_set.size() + "] concept names and ["
				+ role_set.size() + "] role names from [" + formula_list.size() + "] normalized axioms");
		long startTime_1 = System.currentTimeMillis();
		List<Formula> uniform_interpolantList = forgetter.Forgetting(role_set, concept_set, formula_list, onto_2);
		long endTime_1 = System.currentTimeMillis();
		System.out.println("Forgetting Duration = " + (endTime_1 - startTime_1) + " millis");
		//elkEntailment.check(onto_2,uniform_interpolantList);
		List<Formula> onto_2_formula_list = ct.OntologyConverter(onto_2);
		Set<Formula> uniform_interpolantSet = Sets.difference(new HashSet<>(uniform_interpolantList),new HashSet<>(onto_2_formula_list));
		Set<OWLAxiom> uniform_interpolant = bc.toOWLAxioms(new ArrayList<>(uniform_interpolantSet));
		// as we compute the uniform_interpolant on module, we must add the axioms in O2 with no new signatures because they may be explicit witness.
		for(OWLLogicalAxiom axiom : onto_2.getLogicalAxioms()){
			if(Sets.intersection(axiom.getSignature(),forgettingSignatures).size() == 0 ){
				uniform_interpolant.add(axiom);
			}
		}
		OWLOntology ui = OWLManager.createOWLOntologyManager().createOntology(uniform_interpolant);
		ans.add(ui);
		OWLReasoner reasoner =  new ElkReasonerFactory().createReasoner(onto_1);
		OWLOntology witness_complete_onto = manager.createOntology();
		OWLOntology witness_explicit_onto = manager.createOntology();
		OWLOntology witness_implicit_onto = manager.createOntology();



		long startTime_2 = System.currentTimeMillis();
		System.out.println("ui size: "+uniform_interpolant.size());
		for (OWLAxiom axiom : uniform_interpolant) {
			if(!elkEntailment.entailed(reasoner,axiom)){
				//if (!reasoner.isEntailed(axiom)) {
				manager.applyChange(new AddAxiom(witness_complete_onto, axiom));
				System.out.println("witness_complete = " + axiom);
				if (onto_2.getAxioms().contains(axiom)) {
					manager.applyChange(new AddAxiom(witness_explicit_onto, axiom));
					System.out.println("witness_explicit = " + axiom);
				} else {
					manager.applyChange(new AddAxiom(witness_implicit_onto, axiom));
					System.out.println("witness_implicit = " + axiom);
				}
			}
		}
		long endTime_2 = System.currentTimeMillis();
		System.out.println("Entailment Duration = " + (endTime_2 - startTime_2) + " millis");
		ans.add(witness_complete_onto);ans.add(witness_explicit_onto);ans.add(witness_implicit_onto);
		reasoner.dispose();
		return ans;
	}
	public void compute_LDiff(OWLOntology onto_1, OWLOntology onto_2, String path)
			throws Exception {
		List<OWLOntology> ans = LDiff(onto_1,onto_2);
		OutputStream os_complete;
		OutputStream os_explicit;
		OutputStream os_implicit;
		OutputStream os_ui;
		try {
			os_ui = new FileOutputStream(new File(path + "/ui.owl"));
			manager.saveOntology(ans.get(0), new OWLXMLDocumentFormat(), os_ui);
			os_complete = new FileOutputStream(new File(path + "/witness_complete.owl"));
			manager.saveOntology(ans.get(1), new OWLXMLDocumentFormat(), os_complete);
			os_explicit = new FileOutputStream(new File(path + "/witness_explicit.owl"));
			manager.saveOntology(ans.get(2), new OWLXMLDocumentFormat(), os_explicit);
			os_implicit = new FileOutputStream(new File(path + "/witness_implicit.owl"));
			manager.saveOntology(ans.get(3), new OWLXMLDocumentFormat(), os_implicit);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public static void main(String[] args)
			throws Exception {


		OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
		System.out.println("Onto_1 Path: ");
		String filePath1 ="/Users/liuzhao/Desktop/GD-subontology.owl";
		OWLOntology onto_1 = manager1.loadOntologyFromOntologyDocument(new File(filePath1));
		System.out.println("onto_1 size = " + onto_1.getLogicalAxiomCount());
		System.out.println("c_sig_1 size = " + onto_1.getClassesInSignature().size());
		System.out.println("r_sig_1 size = " + onto_1.getObjectPropertiesInSignature().size());
		OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		System.out.println("Onto_2 Path: ");
		String filePath2 ="/Users/liuzhao/Desktop/GD-starmod.owl";
		OWLOntology onto_2 = manager2.loadOntologyFromOntologyDocument(new File(filePath2));
		System.out.println("onto_2 size = " + onto_2.getLogicalAxiomCount());
		System.out.println("c_sig_2 size = " + onto_2.getClassesInSignature().size());
		System.out.println("r_sig_2 size = " + onto_2.getObjectPropertiesInSignature().size());
		long startTime1 = System.currentTimeMillis(); LDiff diff = new LDiff();
		String saveWitnessPath = "/Users/liuzhao/Desktop";
		diff.compute_LDiff(onto_1, onto_2,  saveWitnessPath);
		long endTime1 = System.currentTimeMillis();
		System.out.println("Total Duration = " + (endTime1 - startTime1) + "millis");


	/*
		OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();

		System.out.println("Onto_1 Path: ");
		String filePath1 = "/Users/liuzhao/nju/NCBO/data/snomedcttest/snomed_ct_intl_20170131.owl/snomed_ct_intl_20170131.owl";
		OWLOntology onto_1 = manager1.loadOntologyFromOntologyDocument(new File(filePath1));

		OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		System.out.println("Onto_2 Path: ");
		String filePath2 = "/Users/liuzhao/nju/NCBO/data/snomedcttest/snomed_ct_intl_20170731.owl/snomed_ct_intl_20170731.owl";
		OWLOntology onto_2 = manager2.loadOntologyFromOntologyDocument(new File(filePath2));


		OWLOntologyManager manager3 = OWLManager.createOWLOntologyManager();
		System.out.println("ui Path: ");
		String filePath3 = "/Users/liuzhao/nju/NCBO/data/snomedcttest/snomed_ct_intl_20170731.owl/ui.owl";
		OWLOntology ui = manager3.loadOntologyFromOntologyDocument(new File(filePath3));
		System.out.println(ui.getLogicalAxioms().size());
		System.out.println(onto_1.getLogicalAxioms().size());
		System.out.println(onto_2.getLogicalAxioms().size());
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(manager, onto_2, ModuleType.STAR);
		Set<OWLClass> c_sig_1 = onto_1.getClassesInSignature();
		Set<OWLClass> c_sig_2 = onto_2.getClassesInSignature();
		Set<OWLClass> c_sig = new LinkedHashSet<>(Sets.difference(c_sig_2, c_sig_1));
		Set<OWLObjectProperty> r_sig_1 = onto_1.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> r_sig_2 = onto_2.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> r_sig = new LinkedHashSet<>(Sets.difference(r_sig_2, r_sig_1));

		Set<OWLEntity> forgettingSignatures = new HashSet<>();
		forgettingSignatures.addAll(r_sig);
		forgettingSignatures.addAll(c_sig);
		Set<OWLLogicalAxiom> uniform_interpolant = ui.getLogicalAxioms();
		for(OWLLogicalAxiom axiom : onto_2.getLogicalAxioms()){
			if(Sets.intersection(axiom.getSignature(),forgettingSignatures).size() == 0 ){
				uniform_interpolant.add(axiom);
			}
		}
		uniform_interpolant = Sets.difference(uniform_interpolant,onto_1.getLogicalAxioms());
		System.out.println(uniform_interpolant.size());
		Set<OWLAxiom> temp = new HashSet<>();
		for(OWLAxiom a: uniform_interpolant){
			temp.add(a);
		}
		System.out.println(Sets.intersection(manager1.createOntology(temp).getSignature(),forgettingSignatures).size());

	 */
	}



}
