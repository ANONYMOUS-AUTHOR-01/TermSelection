package modularization;

import org.apache.tools.ant.DirectoryScanner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.*;
import java.util.*;

public class moduleTest {

    public static void main(String[] args) throws Exception {
        String[] ontology_names = {"ncit","foodon","helis"};
        String[] ontology_file_names = {"ncit_20.12d.owl","foodon-merged.owl","helis_v1.00.origin.owl"};
        Integer ontology_index = 2;
//        String[] pers = {"0.1","0.3","0.5","0.7"};
        String type = "star";
        String input_o = "..\\test_ontologies\\"+ontology_file_names[ontology_index];

        OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager1.getOWLDataFactory();
        OWLOntology input_onto = manager1.loadOntologyFromOntologyDocument(new File(input_o));
//        for(String per:pers){
            DirectoryScanner scanner = new DirectoryScanner();
//            scanner.setIncludes(new String[]{"**_"+per+".txt"});
            scanner.setIncludes(new String[]{"**.txt"});
//            scanner.setBasedir("..\\test_terms\\"+ontology_names[ontology_index]);
            scanner.setBasedir("..\\helis\\seed_random_iri\\");
            scanner.setCaseSensitive(false);
            scanner.scan();
            String[] files = scanner.getIncludedFiles();


            for(String concept_file:files){
                System.out.println(concept_file);
                System.out.println(concept_file.substring(0,concept_file.length()-4));
//                Scanner scan_sig = new Scanner(new File("..\\test_terms\\"+ontology_names[ontology_index]+"\\"+concept_file));
                Scanner scan_sig = new Scanner(new File("..\\helis\\seed_random_iri\\"+concept_file));
                Set<OWLEntity> sig = new HashSet<>();
                while(scan_sig.hasNextLine()){
                    String n = scan_sig.nextLine();
                    sig.add(factory.getOWLEntity(EntityType.CLASS, IRI.create(n)));
                }
                SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(manager1, input_onto, ModuleType.TOP);
                Set<OWLAxiom> moduleAxioms = extractor.extract(sig);
                OWLOntology module_onto  = manager1.createOntology(moduleAxioms);
                Set<OWLClass> moduleClasses = module_onto.getClassesInSignature();
                Set<String> moduleClasses_str = new HashSet<>();
                for(OWLClass c:moduleClasses){
                    moduleClasses_str.add(c.getIRI().toString());
                }
                BufferedWriter out = new BufferedWriter(new FileWriter("..\\test_terms\\"+ontology_names[ontology_index]+"-seed-locality-"+type+"\\concept\\"+concept_file.substring(0,concept_file.length()-4)+".txt"));
                Iterator it = moduleClasses_str.iterator();
                while(it.hasNext()){
                    out.write((String)it.next());
                    out.newLine();
                }
                out.close();
                OutputStream os = new FileOutputStream("..\\test_terms\\"+ontology_names[ontology_index]+"-seed-locality-"+type+"\\ontology\\"+concept_file.substring(0,concept_file.length()-4)+".owl");
                manager1.saveOntology(module_onto, new OWLXMLOntologyFormat(), os);
            }
//        }






    }
}
