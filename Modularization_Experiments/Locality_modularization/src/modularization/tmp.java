package modularization;

import org.apache.tools.ant.DirectoryScanner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class tmp {
    public static void main(String[] args) throws Exception {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{"**.owl"});
        scanner.setBasedir("../test_terms/helis-locality-star/");
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] ui_files = scanner.getIncludedFiles();
        String type = "-star";
        FileWriter wrt = new FileWriter(new File("../test_terms/loc_star_size.txt"));
        wrt.write("filename\tsize\t");
        for(String file:ui_files){
            String input_loc = "../test_terms/helis-locality-star/"+file;
            OWLOntologyManager manager_loc = OWLManager.createOWLOntologyManager();
            OWLOntology loc_onto = manager_loc.loadOntologyFromOntologyDocument(new File(input_loc));
            Set<OWLClass> cl = loc_onto.getClassesInSignature();
            Set<OWLObjectProperty> op = loc_onto.getObjectPropertiesInSignature();
            int sz = cl.size() + op.size();
            System.out.println(file + " " + sz);

            wrt.write(file + "\t" + sz+"\n");
            wrt.flush();
        }
        wrt.close();
//        for(String file:ui_files){
//            String input_seed_loc = "../test_terms/helis-seed-locality-star/ontology/"+file;
//            String input_loc = "../test_terms/helis-locality"+type+"/"+file.substring(0,file.length()-4)+"_0.1.owl";
//            System.out.println(file);
//            wrt.write(file+"\n");
//
//            OWLOntologyManager manager_seed_loc = OWLManager.createOWLOntologyManager();
//            OWLOntologyManager manager_loc = OWLManager.createOWLOntologyManager();
////            OWLDataFactory factory_seed_loc = manager_seed_loc.getOWLDataFactory();
//            OWLOntology seed_loc_onto = manager_seed_loc.loadOntologyFromOntologyDocument(new File(input_seed_loc));
//            OWLOntology loc_onto = manager_loc.loadOntologyFromOntologyDocument(new File(input_loc));
//            Set<OWLLogicalAxiom> seed_loc_axioms = seed_loc_onto.getLogicalAxioms();
//            Set<OWLLogicalAxiom> loc_axioms = loc_onto.getLogicalAxioms();
//            Set<String> seed_loc_axioms_str = new HashSet<>();
//            Set<String> loc_axioms_str = new HashSet<>();
//
//            System.out.println("---------------------loc - seed_loc axiom difference ------------------");
//            int diff = 0;
//            for(OWLLogicalAxiom a: seed_loc_axioms){
//                seed_loc_axioms_str.add(a.toString());
//            }
//            for(OWLLogicalAxiom a:loc_axioms){
//                loc_axioms_str.add(a.toString());
//            }
//            for(String a:loc_axioms_str){
//                if(seed_loc_axioms_str.contains(a)==false){
//                    System.out.println(a);
//                    diff += 1;
////                    wrt.write(a+"\n");
//                }
//            }
//            wrt.write("loc - seed_loc: "+diff+"\n");
//            diff = 0;
//            System.out.println("--------------------- seed-loc - loc axiom difference ------------------");
//            for(String a:seed_loc_axioms_str){
//                if(loc_axioms_str.contains(a)==false){
//                    System.out.println(a);
//                    diff += 1;
////                    wrt.write(a+"\n");
//                }
//            }
//            wrt.write("seed_loc - loc: "+diff+"\n");
//            wrt.flush();
//
//        }
//        wrt.close();


    }
}
