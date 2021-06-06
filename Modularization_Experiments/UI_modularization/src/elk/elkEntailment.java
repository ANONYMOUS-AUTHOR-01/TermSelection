package elk;

import BackTrack.BackTrack;
import Test.TestForgetting;
import convertion.BackConverter;
import formula.*;
import forgetting.LDiff;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;

import java.io.File;
import java.util.*;
public class elkEntailment {
    public static HashMap<OWLAxiom,Boolean>hasChecked_OnO2 = new HashMap<>();
    public  static boolean  entailed(OWLReasoner reasoner, OWLAxiom i  ){
        //return reasoner.isEntailed(i);
        if(i.toString().contains("Definer")) return false;

        boolean isentailed = false;
        if (i instanceof OWLSubObjectPropertyOfAxiom){
            OWLSubObjectPropertyOfAxiom now = (OWLSubObjectPropertyOfAxiom) i;
            OWLObjectPropertyExpression sub = now.getSubProperty();
            OWLObjectPropertyExpression sup = now.getSuperProperty();
            if(sub.equals(sup)) isentailed = true;
            else {
                NodeSet<OWLObjectPropertyExpression> superList = reasoner.getSuperObjectProperties(sub, false);

                isentailed = superList.containsEntity(sup);
            }
        }
        else if( i instanceof OWLClassAxiom)  isentailed =  reasoner.isEntailed(i);
        else{
            System.out.print("this is unknown type ");

        }

        return isentailed;


    }
    //this function uses a cache to avoid checking the axioms which had been checked before. This function only been used while forgetting.
    public  static boolean  entailed(OWLReasoner reasoner, OWLAxiom i ,Integer tag ){
        if(i.toString().contains("Definer")) return false;
        if(tag == 2 && hasChecked_OnO2.containsKey(i)){

            return hasChecked_OnO2.get(i);
        }

        boolean isentailed = false;
        if (i instanceof OWLSubObjectPropertyOfAxiom){
            OWLSubObjectPropertyOfAxiom now = (OWLSubObjectPropertyOfAxiom) i;
            OWLObjectPropertyExpression sub = now.getSubProperty();
            OWLObjectPropertyExpression sup = now.getSuperProperty();
            if(sub.equals(sup)) isentailed = true;
            else {
                NodeSet<OWLObjectPropertyExpression> superList = reasoner.getSuperObjectProperties(sub, false);

                isentailed = superList.containsEntity(sup);
            }
        }
        else if( i instanceof OWLClassAxiom)  isentailed =  reasoner.isEntailed(i);
        else{
            System.out.print("this is unknown type ");

        }


        //boolean isentailed = reasoner.isEntailed(i);
        if(tag == 2){
            hasChecked_OnO2.put(i,isentailed);
        }
        return isentailed;
    }

    public static void check(OWLOntology onto_2,List<Formula> uniform_interpolant)throws Exception{
        BackConverter bc = new BackConverter();
        OWLReasoner reasoner = new ElkReasonerFactory().createReasoner(onto_2);
        int i = 0;
        int len = uniform_interpolant.size();
        for(Formula formula : uniform_interpolant){
            OWLAxiom axiom = bc.toOWLAxiom(formula);
            if(!entailed(reasoner,axiom)){
                System.out.println("Unexpected Axiom: " + formula +" "+ TestForgetting.isExtra );
                BackTrack.getInferencePath(formula);
                throw new Exception("unexpected");

            }
            else{
                System.out.println("Yes "+i+" "+len);
            }
            i++;
        }
        reasoner.dispose();
    }
    public static void check2(OWLOntology onto_2,List<Formula> uniform_interpolant){
        BackConverter bc = new BackConverter();
        OWLReasoner reasoner = new ElkReasonerFactory().createReasoner(onto_2);
        int i = 0;
        int len = uniform_interpolant.size();
        for(Formula formula : uniform_interpolant){
            OWLAxiom axiom = bc.toOWLAxiom(formula);
            if(!entailed(reasoner,axiom,2)){
                System.out.println("Unexpected Axiom: " + formula);
                BackTrack.getInferencePath(formula);
            }
            else{
                System.out.println("Yes "+i+" "+len);
            }
            i++;
        }
        reasoner.dispose();
    }

    public static void main(String [] args) throws Exception{

        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
        System.out.println("Onto_2 Path: ");
        String filePath2 = "/Users/liuzhao/nju/NCBO/data/snomedcttest/snomed_ct_intl_20170731.owl/snomed_ct_intl_20170731.owl";
        OWLOntology onto_2 = manager2.loadOntologyFromOntologyDocument(new File(filePath2));

        OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
        System.out.println("ui Path: ");
        String filePath1 = "/Users/liuzhao/nju/NCBO/data/snomedcttest/snomed_ct_intl_20170731.owl/ui.owl";
        OWLOntology ui = manager1.loadOntologyFromOntologyDocument(new File(filePath1));

        System.out.println(onto_2.getLogicalAxioms().size()+" "+ui.getLogicalAxioms().size());
    }
}
