import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class SeedModuleExtractor {
    static public String onto_file_path = "data/SnomedCT/ontology_202007.owl";
    static public String refset_seed_dir = "data/SnomedCT/nhs_seed_random_iri/";
    static public String module_file_dir = "data/SnomedCT/nhs_output_random_module_owl/";
    static public String module_iri_dir = "data/SnomedCT/nhs_output_random_module_iri/";

    static public int max_seed_num = 5;

    public static OWLOntology createModule(OWLOntology originalOnto, Set<OWLEntity> eta, String filename) throws Exception{
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(manager, originalOnto, ModuleType.STAR);

        Set<OWLAxiom> moduleAxioms = extractor.extract(eta);
        OWLOntology ont = manager.createOntology(moduleAxioms);

        manager.saveOntology(ont, new FileOutputStream(new File(filename)));
        return ont;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Start");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology onto = manager.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(onto_file_path)));
        System.out.println(("Loading ontology done!"));

        File dir = new File(refset_seed_dir);
        int refset_nums = dir.listFiles().length;
        int counter = 0;
        for (File item : dir.listFiles()) {

//            if (item.getName().compareTo("der2_Refset_pregnancyRelatedAnemiaActiveSnapshot_1000189_20200731.txt") != 0)
//
//                continue;

            BufferedReader seed_file = new BufferedReader(new FileReader(refset_seed_dir + item.getName()));
            String str;
            Set<OWLEntity> entitiesInSignature = new HashSet<>();

            while ((str = seed_file.readLine()) != null) {
                // Add current seed to seed set
                IRI iri = IRI.create(str);
                entitiesInSignature.addAll(onto.getEntitiesInSignature(iri));

                if ((entitiesInSignature.size() == 1) || (entitiesInSignature.size() == 5)) {
                    // Extract module and save ontology
                    OWLOntology ex_onto = createModule(
                            onto,
                            entitiesInSignature,
                            module_file_dir + item.getName().replace(".txt", "#" + entitiesInSignature.size() + ".owl")
                    );

                    // Save iri as txt
                    BufferedWriter out = new BufferedWriter(new FileWriter(module_iri_dir + item.getName().replace(".txt", "#" + entitiesInSignature.size() + ".txt")));
                    for (OWLEntity entity : ex_onto.getSignature()) {
                        if (entity.getEntityType() == EntityType.CLASS) {
                            out.write(entity.getIRI().toString() + "\n");
                            out.flush();
                        }
                    }
                    out.close();
                }

                if (entitiesInSignature.size() >= max_seed_num) {
                    break;
                }
            }

            System.out.println(++counter + " out of " + refset_nums + " finished.");
        }
    }
}
