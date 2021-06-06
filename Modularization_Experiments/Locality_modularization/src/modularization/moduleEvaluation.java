package modularization;

import org.apache.tools.ant.DirectoryScanner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Number;
import java.lang.Object;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class moduleEvaluation {
    private OWLOntology moduleOWL;
    private OWLOntology ontologyOWL;
    private OWLEntity key;

    private int mClass;
    private int mOP;
    private int mDP;
    private int mInd;
    private int mSize;
    private int mAx;
    private double mIntramoddist;
    private double mCohesion;
    private double mAttRich;
    private double mInhRich;
    private  double mAtomicSize;

    private int oClass;
    private int oOP;
    private int oDP;
    private int oInd;
    private int oSize;
    private  int oAx;
    private double oAttRich;
    private double oInhRich;
    private double oAtomicSize;

    private double oIntramoddist;

    private double rIntramoddist;


    private boolean mLoaded = true;
    private boolean oLoaded = true;


    private double tempCoh = 0;
    private double tempCoh2 = 0;

    public void metrics_o(){
        oClass = ontologyOWL.getClassesInSignature(true).size();
        oDP = ontologyOWL.getDataPropertiesInSignature(true).size();
        oOP = ontologyOWL.getObjectPropertiesInSignature(true).size();
        oInd = ontologyOWL.getIndividualsInSignature(true).size();
        oSize = oClass + oDP + oOP;
        oAx = ontologyOWL.getAxiomCount();

        Set <OWLOntology> ontos = ontologyOWL.getDirectImports();

        for(OWLOntology o: ontos){
            oAx += o.getAxiomCount();
        }


        oAttRich = getAttributeRichness(ontologyOWL);//(double) oDP / oClass;
        oInhRich = getInheritanceRichness(ontologyOWL);

        oAtomicSize = getAtomicSize(ontologyOWL);

        oIntramoddist = intraModuleDistance(ontologyOWL);
    }
    public void metrics() {
        float sumMS = 0;
        mClass = moduleOWL.getClassesInSignature(true).size();
        mDP = moduleOWL.getDataPropertiesInSignature(true).size();
        mOP = moduleOWL.getObjectPropertiesInSignature(true).size();
        mInd = moduleOWL.getIndividualsInSignature(true).size();
        mSize = mClass + mDP + mOP;
        mAx = moduleOWL.getAxiomCount();



        Set <OWLOntology> ontos = moduleOWL.getDirectImports();

        for(OWLOntology m: ontos){
            mAx += m.getAxiomCount();
        }

        mAtomicSize = getAtomicSize(moduleOWL);

        mAttRich = getAttributeRichness(moduleOWL); //(double) mDP / mClass;
        mInhRich = getInheritanceRichness(moduleOWL);

        int newSize = moduleOWL.getClassesInSignature(true).size()+moduleOWL.getObjectPropertiesInSignature(true).size()+
                moduleOWL.getDataPropertiesInSignature(true).size();

        mIntramoddist = intraModuleDistance(moduleOWL);
        mCohesion = getCohesion(moduleOWL, newSize);


        if (oIntramoddist == 0 || mIntramoddist == 0){
            rIntramoddist = -1;
        }

        else{
            rIntramoddist = (double) oIntramoddist / (double) mIntramoddist;
        }



    }


    public double getAtomicSize(OWLOntology onto) {
        double logicalAx = onto.getLogicalAxiomCount();
        double noClass = onto.getClassesInSignature(true).size();
        double noOP = onto.getObjectPropertiesInSignature(true).size();
        double noDP = onto.getDataPropertiesInSignature(true).size();
        double noInd = onto.getIndividualsInSignature(true).size();
        double noEnt = noClass + noOP + noDP + noInd;

        Set<OWLClass> classes = onto.getClassesInSignature(true);
        Set<OWLObjectProperty> op = onto.getObjectPropertiesInSignature(true);
        Set<OWLDataProperty> dp = onto.getDataPropertiesInSignature(true);
        Set<OWLNamedIndividual> indi = onto.getIndividualsInSignature(true);

        double sum = 0;
        for (OWLClass c : classes) {

            // System.out.println(" class "+c);
            Set<OWLAxiom> ax = c.getReferencingAxioms(onto, true);
            sum += ax.size();
            for (OWLAxiom a : ax) {
                //System.out.println(a);
            }

            // System.out.println();
        }

        for (OWLObjectProperty o : op) {
            Set<OWLAxiom> ax = o.getReferencingAxioms(onto, true);
            sum += ax.size();
        }

        for (OWLDataProperty d : dp) {
            Set<OWLAxiom> ax = d.getReferencingAxioms(onto, true);
            sum += ax.size();
        }

        for (OWLNamedIndividual i : indi) {
            Set<OWLAxiom> ax = i.getReferencingAxioms(onto, true);
            sum += ax.size();
        }

        double atomicSize = sum / noEnt;
        System.out.println("The atomic size is " + atomicSize);
        return atomicSize;
    }


    public boolean getIndependence(double encap, double coup) {
        boolean ind;
        if ((encap == 1) && (coup == 0)) {
            ind = true;
        } else {
            ind = false;
        }
        System.out.println("Independent? " + ind);
        return ind;
    }


    public double getEncap(OWLOntology mod, Set<OWLOntology> modSet) {

        Set<OWLAxiom> set1 = mod.getAxioms();
        double encap = 0;
        double t = 0;
        for (OWLOntology m : modSet) {
            double some = 0;
            double count = 0;

            Set<OWLAxiom> temp = m.getAxioms();

            //int encap =0;
            for (OWLAxiom ax : set1) {
                // System.out.println("ax2 "+ax);
                if (temp.contains(ax)) {
                    count++;
                    //System.out.println(count);
                    //  System.out.println(mod+" contains axiom "+ax);
                }

                //  System.out.println("e1 "+encap);
            }
            // some+=count;
            t += count / set1.size();
            //      System.out.println("t "+t+" "+count+" "+set1.size());
        }
        encap = 1 - (t / (modSet.size() + 1));
        //System.out.println("e2 "+encap);
        System.out.println("The encapsulation is " + encap);
        return encap;
    }

    private boolean getCompleteness(OWLOntology ont, OWLOntology mod) {
        boolean comp = true;
        Set<OWLEntity> entOnt = ont.getSignature();
        Set<OWLEntity> entMod = mod.getSignature();

        for (OWLEntity a : entOnt) {
            for (OWLEntity b : entMod) {
                if (a.equals(b)) {
                    if (a.isOWLClass()) {
                        // if(a.equals(b)){
                        Set<OWLClassAxiom> s1 = ont.getAxioms(a.asOWLClass());
                        Set<OWLClassAxiom> s2 = mod.getAxioms(b.asOWLClass());
                        if (!(s1.equals(s2))) {
                            comp = false;
                            key = a;
                            break;
                        }
                        // }
                    }

                    if (a.isOWLObjectProperty()) {
                        // if(a.equals(b)){
                        Set<OWLObjectPropertyAxiom> s1 = ont.getAxioms(a.asOWLObjectProperty());
                        Set<OWLObjectPropertyAxiom> s2 = mod.getAxioms(b.asOWLObjectProperty());
                        if (!(s1.equals(s2))) {
                            comp = false;
                            key = a;
                            break;
                        }
                    } //}
                    else if (a.isOWLDataProperty()) {
                        //if(a.equals(b)){
                        Set<OWLDataPropertyAxiom> s1 = ont.getAxioms(a.asOWLDataProperty());
                        Set<OWLDataPropertyAxiom> s2 = mod.getAxioms(b.asOWLDataProperty());
                        if (!(s1.equals(s2))) {
                            comp = false;
                            key = a;
                            break;
                        }
                        //}
                    } else if (a.isOWLNamedIndividual()) {
                        // if(a.equals(b)){
                        Set<OWLIndividualAxiom> s1 = ont.getAxioms(a.asOWLNamedIndividual());
                        Set<OWLIndividualAxiom> s2 = mod.getAxioms(b.asOWLNamedIndividual());
                        if (!(s1.equals(s2))) {
                            comp = false;
                            key = a;
                            break;
                            //   }
                        }
                    } else if (a.isOWLAnnotationProperty()) {
                        //if(a.equals(b)){
                        Set<OWLAnnotationAxiom> s1 = ont.getAxioms(a.asOWLAnnotationProperty());
                        Set<OWLAnnotationAxiom> s2 = mod.getAxioms(b.asOWLAnnotationProperty());
                        if (!(s1.equals(s2))) {
                            comp = false;
                            key = a;
                            break;
                            //   }
                        }
                    }
                }
            }
        }

        System.out.println("Complete ? " + comp);
        return comp;
    }

    OWLAxiom cAx;

    private boolean getCorrectness(OWLOntology ont, OWLOntology mod) {
        Set<OWLAxiom> mAxioms = mod.getAxioms();
        //  Set <OWLAxiom> oAxioms = ont.getAxioms();
        boolean corr = true;
        for (OWLAxiom m : mAxioms) {
            if (!(ont.containsAxiom(m))) {
                corr = false;
                //   System.out.println(ont+" does not contain axiom "+m);
                cAx = m;
                break;
            }

        }
        System.out.println("Correct ? " + corr);
        return corr;
    }

    public double getAppropriateness(int numAxioms) {
        double val = 0;
        if (numAxioms > 0 && numAxioms < 500) {
            double cos = Math.cos(numAxioms * (Math.PI / 250));

            //  Fraction f1 =new Fraction(1,2);
            //  double ii = (.5) * (cos);
            val = (.5) - ((.5) * cos);
        } else {
            val = -1;
        }
        System.out.println("Appropriateness " + val);
        return val;
    }

    public double getRelationshipRichness(OWLOntology onto) {

        return 0;
    }

    public double getInheritanceRichness(OWLOntology onto) {
        Set<OWLClass> classes = onto.getClassesInSignature(true);

        double agg = 0;
        int parentclasses=0;
        for (OWLClass c : classes) {
            int countsub = 0;
            countsub = c.getSubClasses(onto).size();


            if (countsub > 0) {
                // agg += (1 / countsub);
                agg += countsub;
                parentclasses++;
            }
        }
        // System.out.println("IR " + agg + " " + classes.size());
        //double ir = (double) (agg / classes.size());
        double ir = (double) (agg / parentclasses);
        System.out.println("The inheritance richness is " + ir);
        return ir;
    }

    public double getAttributeRichness(OWLOntology onto){
        Set<OWLClass> classes = onto.getClassesInSignature(true);

        int attributes= 0;
        double ar = 0;
        for (OWLClass c : classes) {
            // System.out.println("classes "+c);
            Set <OWLAxiom> axSet = c.getReferencingAxioms(onto, true);

            //System.out.println("class "+c);
            // System.out.println("set "+moduleOWL.getAxioms(c));
            //  System.out.println();

            for(OWLAxiom a: axSet){
                // System.out.println("ax "+a);
                if( (!(a.getAxiomType()== AxiomType.OBJECT_PROPERTY_DOMAIN)) && (!(a.getAxiomType()== AxiomType.OBJECT_PROPERTY_RANGE))
                        &&   (!(a.getAxiomType()== AxiomType.DATA_PROPERTY_DOMAIN)) && (!(a.getAxiomType()== AxiomType.DATA_PROPERTY_RANGE))
                ){
                    Set <OWLObjectProperty> op = a.getObjectPropertiesInSignature();
                    Set <OWLDataProperty> dp = a.getDataPropertiesInSignature();
                    attributes+=op.size();
                    attributes+=dp.size();
                    // System.out.println("op "+op);
                    //   System.out.println("dp "+dp);
                }
            }
            //System.out.println(axSet.toString());
            // Set <OWLObjectProperty> op = c.getObjectPropertiesInSignature();
            // Set <OWLDataProperty> dp = c.getDataPropertiesInSignature();

            // System.out.println("OP in sig "+op.toString());
            // System.out.println("DP in sig "+dp.toString());
        }
        ar = (double) attributes/ (double)classes.size();
        // System.out.println("att "+attributes);
        //System.out.println("class "+classes.size());
        System.out.println("The attribute richness is "+ar+"); // att size" +attributes+"cl size"+classes.size());
        return ar;


    }

    // int jumps = 0;
    public double getCohesion(OWLOntology onto, int size) {
        // double coh = 0;
        /*Set<OWLClass> classes = onto.getClassesInSignature(true);
        OWLClass[] classArray = new OWLClass[500000];
        int k = 0;

        for (OWLClass a : classes) {
            classArray[k] = a;
            k++;
        }
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {

                //   System.out.println(i+" - "+j);
                // System.out.println("farness "+bfsFarness(classArray[i],classArray[j], onto)+"-- coh "+(1/(bfsFarness(classArray[i],classArray[j], onto))));
                if (!(bfsFarness(classArray[i], classArray[j], onto) == 0)) {
                    coh += (1 / (bfsFarness(classArray[i], classArray[j], onto)));
                }
            }
        }


        Set <OWLObjectProperty> ops = onto.getObjectPropertiesInSignature(true);
        OWLObjectProperty[] opArray = new OWLObjectProperty[500000];
        k = 0;



            for (OWLObjectProperty a : ops) {
            opArray[k] = a;
            k++;
        }
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {

                //   System.out.println(i+" - "+j);
                // System.out.println("farness "+bfsFarness(classArray[i],classArray[j], onto)+"-- coh "+(1/(bfsFarness(classArray[i],classArray[j], onto))));
                if (!(bfsFarnessOP(opArray[i], opArray[j], onto) == 0)) {
                    coh += (1 / (bfsFarnessOP(opArray[i], opArray[j], onto)));
                }
            }
        }

         Set <OWLDataProperty> dps = onto.getDataPropertiesInSignature(true);
         OWLDataProperty[] dpArray = new OWLDataProperty[500000];
         k = 0;

             for (OWLDataProperty a : dps) {
            dpArray[k] = a;
            k++;
        }
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {

                //   System.out.println(i+" - "+j);
                // System.out.println("farness "+bfsFarness(classArray[i],classArray[j], onto)+"-- coh "+(1/(bfsFarness(classArray[i],classArray[j], onto))));
                if (!(bfsFarnessDP(dpArray[i], dpArray[j], onto) == 0)) {
                    coh += (1 / (bfsFarnessDP(dpArray[i], dpArray[j], onto)));
                }
            }
        }
        // System.out.println("numer " + coh);
        // System.out.println("denoom " + (size) * (size - 1));
        coh = coh / ((size) * (size - 1));
*/
        double coh  = tempCoh / ((size) * (size-1));
        System.out.println("The cohesion is " + coh);
        return coh;
    }

    public double getCohesionOnt(OWLOntology onto, int size) {
        double coh  = tempCoh2 / ((size) * (size-1));
        System.out.println("The cohesion is " + coh);
        return coh;
    }
    public double intraModuleDistance(OWLOntology onto) {
        tempCoh = 0;
        tempCoh2 = 0;
        double tempVar = 0;
//        System.out.println("here");
        double agg = 0;
        Set<OWLClass> classes = onto.getClassesInSignature(true);

        //   Set <OWLObjectProperty> oprops = onto.getObjectPropertiesInSignature();
        // Set <OWLDataProperty> dprops = onto.getDataPropertiesInSignature();
        OWLClass[] classArray = new OWLClass[500000];
        int k = 0;

        ArrayList temp1 = new ArrayList();
        ArrayList temp2 = new ArrayList();
        temp1.add(k);
        temp2.add(k);

        int yy = 0;
        for (OWLClass a : classes) {
            classArray[k] = a;
            k++;
        }

//        System.out.println("here");
        // System.out.println("check");
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {

                //   if( (temp1.contains(classArray[i]) && temp2.contains(classArray[j])) && (temp1.indexOf(classArray[i]) == temp2.indexOf(classArray[j]))  ){
                //  }
                //   else{
                //  System.out.println(i+" - "+j);
                //     System.out.println("index "+temp1.indexOf(classArray[i])+"  dd"+temp2.indexOf(classArray[j]));
                // agg += bfsFarness(classArray[i], classArray[j], onto) / 2;
                tempVar = bfsFarness(classArray[i], classArray[j], onto);
                agg+=tempVar;
                // System.out.println("distance between "+classArray[i]+" and "+classArray[j]);
                if (tempVar == 0){
                    tempCoh+=0;
                }

                else{
                    tempCoh+=1/ (tempVar);
                }
                //agg += bfsFarness(classArray[i], classArray[j], onto);
                // System.out.println(" distance between "+classArray[i]+" "+classArray[j]+" is "+agg);
                temp1.add(classArray[i]);
                temp2.add(classArray[j]);
                //System.out.println(yy+" - "+classArray[i]+"  "+classArray[j]);
                yy++;
                //System.out.println(i+" "+j);
                //  }

            }
        }

        System.out.println("Done class distances");

        Set<OWLObjectProperty> op = onto.getObjectPropertiesInSignature(true);
        OWLObjectProperty[] opArray = new OWLObjectProperty[500000];
        k = 0;

        ArrayList tempOp1 = new ArrayList();
        ArrayList tempOp2 = new ArrayList();
        tempOp1.add(k);
        tempOp2.add(k);

        yy = 0;
        for (OWLObjectProperty a : op) {
            opArray[k] = a;
            k++;
        }
        // System.out.println("check");
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {

                //   if( (temp1.contains(classArray[i]) && temp2.contains(classArray[j])) && (temp1.indexOf(classArray[i]) == temp2.indexOf(classArray[j]))  ){
                //  }
                //   else{
                //  System.out.println(i+" - "+j);
                //     System.out.println("index "+temp1.indexOf(classArray[i])+"  dd"+temp2.indexOf(classArray[j]));
                // agg += bfsFarness(classArray[i], classArray[j], onto) / 2;
                // System.out.println("farness "+classArray[i]+" "+classArray[j]);
                // agg += bfsFarnessOP(opArray[i], opArray[j], onto);


                tempVar = bfsFarnessOP(opArray[i],opArray[j], onto);
                agg+=tempVar;
                //   System.out.println("distance between "+opArray[i]+" and "+opArray[j]);
                if (tempVar == 0){
                    tempCoh+=0;
                }

                else{
                    tempCoh+=1/ (tempVar);
                }

                tempOp1.add(opArray[i]);
                tempOp2.add(opArray[j]);
                //System.out.println(yy+" - "+classArray[i]+"  "+classArray[j]);
                yy++;
                //System.out.println(i+" "+j);
                //  }

            }
        }
        System.out.println("Done OP distances");
        Set<OWLDataProperty> dp2 = onto.getDataPropertiesInSignature(true);
        OWLDataProperty[] dpArray = new OWLDataProperty[500000];
        k = 0;

        ArrayList tempDp1 = new ArrayList();
        ArrayList tempDp2 = new ArrayList();
        tempDp1.add(k);
        tempDp2.add(k);

        yy = 0;
        for (OWLDataProperty a : dp2) {
            dpArray[k] = a;
            k++;
        }
        // System.out.println("check");
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {

                //   if( (temp1.contains(classArray[i]) && temp2.contains(classArray[j])) && (temp1.indexOf(classArray[i]) == temp2.indexOf(classArray[j]))  ){
                //  }
                //   else{
                //  System.out.println(i+" - "+j);
                //     System.out.println("index "+temp1.indexOf(classArray[i])+"  dd"+temp2.indexOf(classArray[j]));
                // agg += bfsFarness(classArray[i], classArray[j], onto) / 2;
                // System.out.println("farness "+classArray[i]+" "+classArray[j]);
                //  agg += bfsFarnessDP(dpArray[i], dpArray[j], onto);


                tempVar = bfsFarnessDP(dpArray[i], dpArray[j], onto);
                agg+=tempVar;
                //   System.out.println("distance between "+dpArray[i]+" and "+dpArray[j]);
                if (tempVar == 0){
                    tempCoh+=0;
                }

                else{
                    tempCoh+=1/ (tempVar);
                }
                tempDp1.add(dpArray[i]);
                tempDp2.add(dpArray[j]);
                //System.out.println(yy+" - "+classArray[i]+"  "+classArray[j]);
                yy++;
                //System.out.println(i+" "+j);
                //  }

            }
        }

        System.out.println("Done DP distances");
        System.out.println("The intramodule distance is " + agg);
        return agg;
    }

    public double intraModuleDistance(OWLOntology onto, Set<OWLClass> classes, Set<OWLObjectProperty> op, Set<OWLDataProperty> dp) {
        //tempCoh = 0;
        tempCoh2 = 0;
        double agg = 0;
        double tempVar = 0;
        Set<OWLClass> classes2 = onto.getClassesInSignature(true);
        Set<OWLClass> newSet = new HashSet();

        //  System.out.println("classes "+classes.toString());
// System.out.println("classes2 "+classes2.toString());
        String tempstring= "";
        String tempstring2 = "";
        for (OWLClass c2 : classes2) {
            if(c2.toString().contains("#")){
                tempstring2 = c2.toString().substring(c2.toString().indexOf("#"));
                // System.out.println(tempstring2+" ts2");
            }

            for (OWLClass c1 : classes) {
                if(c1.toString().contains("#")){
                    tempstring = c1.toString().substring(c1.toString().indexOf("#"));
                    //  System.out.println(tempstring+" ts");
                }

                if((tempstring.equals(tempstring2))&& (!(tempstring.isEmpty())) ){
                    newSet.add(c2);
                }

                else{
                    newSet.add(c1);
                }
            }
        }

        OWLClass[] classArray = new OWLClass[500000];
        int k = 0;

        ArrayList temp1 = new ArrayList();
        ArrayList temp2 = new ArrayList();
        temp1.add(k);
        temp2.add(k);

        int yy = 0;
        for (OWLClass a : newSet) {
            classArray[k] = a;
            //System.out.println(classArray[k]);
            k++;
        }
        // System.out.println("check");
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {

                //   if( (temp1.contains(classArray[i]) && temp2.contains(classArray[j])) && (temp1.indexOf(classArray[i]) == temp2.indexOf(classArray[j]))  ){
                //  }
                //   else{
                //  System.out.println(i+" - "+j);
                //     System.out.println("index "+temp1.indexOf(classArray[i])+"  dd"+temp2.indexOf(classArray[j]));
                // agg += bfsFarness(classArray[i], classArray[j], onto) / 2;
                // System.out.println("farness "+classArray[i]+" "+classArray[j]);
                //agg += bfsFarness(classArray[i], classArray[j], onto);
//java heapspace

                tempVar = bfsFarness(classArray[i], classArray[j], onto);
                //System.out.println("check "+classArray[i]+" "+classArray[j]);
                agg+=tempVar;
                //System.out.println("distance between "+classArray[i]+" and "+classArray[j]);
                if (tempVar == 0){
                    tempCoh2+=0;
                }

                else{
                    tempCoh2+=1/ (tempVar);
                }

                temp1.add(classArray[i]);
                temp2.add(classArray[j]);
                //System.out.println(yy+" - "+classArray[i]+"  "+classArray[j]);
                yy++;
                //System.out.println(i+" "+j);
                //  }

            }
        }
        System.out.println("Done class distances");
        Set<OWLObjectProperty> op2 = onto.getObjectPropertiesInSignature(true);
        Set<OWLObjectProperty> newSetOp = new HashSet();

        String tempstringOp= "";
        String tempstringOp2 = "";
        for (OWLObjectProperty o2 : op2) {
            if(o2.toString().contains("#")){
                tempstringOp2 = o2.toString().substring(o2.toString().indexOf("#"));
            }

            for (OWLObjectProperty o1 : op) {
                if(o1.toString().contains("#")){
                    tempstringOp = o1.toString().substring(o1.toString().indexOf("#"));
                }

                if((tempstringOp.equals(tempstringOp2))&& (!(tempstringOp.isEmpty())) ){
                    newSetOp.add(o2);
                }
            }
        }

        OWLObjectProperty[] opArray = new OWLObjectProperty[500000];
        k = 0;

        ArrayList tempOp1 = new ArrayList();
        ArrayList tempOp2 = new ArrayList();
        tempOp1.add(k);
        tempOp2.add(k);

        yy = 0;
        for (OWLObjectProperty a : op2) {
            opArray[k] = a;
            k++;
        }
        // System.out.println("check");
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {

                //   if( (temp1.contains(classArray[i]) && temp2.contains(classArray[j])) && (temp1.indexOf(classArray[i]) == temp2.indexOf(classArray[j]))  ){
                //  }
                //   else{
                //  System.out.println(i+" - "+j);
                //     System.out.println("index "+temp1.indexOf(classArray[i])+"  dd"+temp2.indexOf(classArray[j]));
                // agg += bfsFarness(classArray[i], classArray[j], onto) / 2;
                // System.out.println("farness "+classArray[i]+" "+classArray[j]);
                //agg += bfsFarnessOP(opArray[i], opArray[j], onto);


                tempVar = bfsFarnessOP(opArray[i], opArray[j], onto);
                agg+=tempVar;
                // System.out.println("distance between "+opArray[i]+" and "+opArray[j]);
                if (tempVar == 0){
                    tempCoh2+=0;
                }

                else{
                    tempCoh2+=1/ (tempVar);
                }


                tempOp1.add(opArray[i]);
                tempOp2.add(opArray[j]);
                //System.out.println(yy+" - "+classArray[i]+"  "+classArray[j]);
                yy++;
                //System.out.println(i+" "+j);
                //  }

            }
        }

        System.out.println("Done OP distances");
        Set<OWLDataProperty> dp2 = onto.getDataPropertiesInSignature(true);
        Set<OWLDataProperty> newSetDp = new HashSet();

        String tempstringDp= "";
        String tempstringDp2 = "";
        for (OWLDataProperty d2 : dp2) {
            if(d2.toString().contains("#")){
                tempstringDp2 = d2.toString().substring(d2.toString().indexOf("#"));
            }

            for (OWLDataProperty d1 : dp) {
                if(d1.toString().contains("#")){
                    tempstringDp = d1.toString().substring(d1.toString().indexOf("#"));
                }

                if((tempstringDp.equals(tempstringDp2))&& (!(tempstringDp.isEmpty())) ){
                    newSetDp.add(d2);
                }
            }
        }

        OWLDataProperty[] dpArray = new OWLDataProperty[500000];
        k = 0;

        ArrayList tempDp1 = new ArrayList();
        ArrayList tempDp2 = new ArrayList();
        tempDp1.add(k);
        tempDp2.add(k);

        yy = 0;
        for (OWLDataProperty a : dp2) {
            dpArray[k] = a;
            k++;
        }
        // System.out.println("check");
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {

                //   if( (temp1.contains(classArray[i]) && temp2.contains(classArray[j])) && (temp1.indexOf(classArray[i]) == temp2.indexOf(classArray[j]))  ){
                //  }
                //   else{
                //  System.out.println(i+" - "+j);
                //     System.out.println("index "+temp1.indexOf(classArray[i])+"  dd"+temp2.indexOf(classArray[j]));
                // agg += bfsFarness(classArray[i], classArray[j], onto) / 2;
                // System.out.println("farness "+classArray[i]+" "+classArray[j]);
                //  agg += bfsFarnessDP(dpArray[i], dpArray[j], onto);
                tempVar = bfsFarnessDP(dpArray[i], dpArray[j], onto);
                agg+=tempVar;
                // System.out.println("distance between "+dpArray[i]+" and "+dpArray[j]);
                if (tempVar == 0){
                    tempCoh2+=0;
                }

                else{
                    tempCoh2+=1/ (tempVar);
                }

                tempDp1.add(dpArray[i]);
                tempDp2.add(dpArray[j]);
                //System.out.println(yy+" - "+classArray[i]+"  "+classArray[j]);
                yy++;
                //System.out.println(i+" "+j);
                //  }

            }
        }
        System.out.println("Done DP distances");
        System.out.println("The intramodule distance for the original ontology is " + agg);
        return agg;
    }

    public double bfsFarnessOP(OWLObjectProperty a, OWLObjectProperty b, OWLOntology onto) {

        Set<OWLObjectProperty> visited = new HashSet();
        Set<OWLObjectProperty> current = new HashSet();
        Set<OWLObjectProperty> temp = new HashSet();
        current.add(a);
        double jumps = 0;
        // System.out.println();
        boolean f = false;
        //System.out.println("Hops : "+jumps);
        //         System.out.println("visited "+visited.toString());
        //      System.out.println("current "+current.toString());
        //  System.out.println("temp "+temp.toString());

        while ((!current.contains(b))) {
            f = false;

            // System.out.println(jumps);
            //for(OWLClass cclass: current){
            visited.addAll(current);
            temp.clear();
            temp.addAll(current);
            current.clear();
            for (OWLObjectProperty op : temp) {
                Set<OWLObjectPropertyExpression> sub = op.getSubProperties(onto);
                // System.out.println("cclass "+cclass);
                for (OWLObjectPropertyExpression s1 : sub) {
                    if(!(s1.isAnonymous())){
                        if ((!(visited.contains(s1.asOWLObjectProperty())))) {
                            //   System.out.println("s1 "+s1+s1.asOWLClass());
                            current.add(s1.asOWLObjectProperty());
                        }
                    }
                }

                Set<OWLObjectPropertyExpression> sup = op.getSuperProperties(onto);
                for (OWLObjectPropertyExpression s2 : sup) {
                    if(!(s2.isAnonymous())){

                        if ((!(visited.contains(s2.asOWLObjectProperty())))) {
                            //System.out.println("s2 "+s2+ s2.asOWLClass());
                            current.add(s2.asOWLObjectProperty());
                        }
                    }
                }

            }
            //
            // System.out.println();
             /*
             for(OWLClass v: visited){
             if(current.contains(v)){
             f=true;
             }
             }
             if(f==false){*/
            jumps++;
            //  System.out.println("Hops : "+jumps);
            // System.out.println("visited "+visited.toString());
            //System.out.println("current "+current.toString());
            //  System.out.println("temp "+temp.toString());

            //  System.out.println("OP jumps "+jumps);
            if (jumps > 0 && current.isEmpty()) {

                jumps = 0;
                break;
            }

            //  }
        }
        //  System.out.println();

        // System.out.println("newcurrent "+current.toString());
        // System.out.println("Distance between "+a+"  and "+b+" is: "+jumps+" .");
        // System.out.println("Farness between "+a+"  and "+b+" is: "+jumps/2+" .");
        return jumps;

    }

    public double bfsFarnessDP(OWLDataProperty a, OWLDataProperty b, OWLOntology onto) {

        Set<OWLDataProperty> visited = new HashSet();
        Set<OWLDataProperty> current = new HashSet();
        Set<OWLDataProperty> temp = new HashSet();
        current.add(a);
        double jumps = 0;
        // System.out.println();
        boolean f = false;
        //System.out.println("Hops : "+jumps);
        //         System.out.println("visited "+visited.toString());
        //      System.out.println("current "+current.toString());
        //  System.out.println("temp "+temp.toString());

        while ((!current.contains(b))) {
            f = false;

            // System.out.println(jumps);
            //for(OWLClass cclass: current){
            visited.addAll(current);
            temp.clear();
            temp.addAll(current);
            current.clear();
            for (OWLDataProperty op : temp) {
                Set<OWLDataPropertyExpression> sub = op.getSubProperties(onto);
                // System.out.println("cclass "+cclass);
                for (OWLDataPropertyExpression s1 : sub) {
                    if ((!(visited.contains(s1.asOWLDataProperty())))) {
                        //   System.out.println("s1 "+s1+s1.asOWLClass());
                        current.add(s1.asOWLDataProperty());
                    }

                }

                Set<OWLDataPropertyExpression> sup = op.getSuperProperties(onto);
                for (OWLDataPropertyExpression s2 : sup) {
                    if ((!(visited.contains(s2.asOWLDataProperty())))) {
                        //System.out.println("s2 "+s2+ s2.asOWLClass());
                        current.add(s2.asOWLDataProperty());
                    }
                }

            }
            //
            // System.out.println();
             /*
             for(OWLClass v: visited){
             if(current.contains(v)){
             f=true;
             }
             }
             if(f==false){*/
            jumps++;
            //  System.out.println("Hops : "+jumps);
            // System.out.println("visited "+visited.toString());
            //System.out.println("current "+current.toString());
            //  System.out.println("temp "+temp.toString());
//System.out.println("DP jumps "+jumps);
            if (jumps > 0 && current.isEmpty()) {

                jumps = 0;
                break;
            }

            //  }
        }
        //  System.out.println();

        // System.out.println("newcurrent "+current.toString());
        // System.out.println("Distance between "+a+"  and "+b+" is: "+jumps+" .");
        // System.out.println("Farness between "+a+"  and "+b+" is: "+jumps/2+" .");
        return jumps;

    }



    public double bfsFarness(OWLClass a, OWLClass b, OWLOntology onto) {

        //System.out.println("class a "+a);
        // System.out.println("class b "+b);
        // System.out.println("in here ");
        Set<OWLClass> visited = new HashSet();
        Set<OWLClass> current = new HashSet();
        Set<OWLClass> temp = new HashSet();

        current.add(a);
        double jumps = 0;
        //  System.out.println("ab  " + a + " " + b);
        boolean f = false;
        //System.out.println("Hops : "+jumps);
        //         System.out.println("visited "+visited.toString());
        //      System.out.println("current "+current.toString());
        //  System.out.println("temp "+temp.toString());

        while ((!current.contains(b))) {
            f = false;

            // System.out.println(jumps);
            //for(OWLClass cclass: current){
            visited.addAll(current);
            temp.clear();
            temp.addAll(current);
            current.clear();
            int ind = 1;
            for (OWLClass cclass : temp) {
                //
                // System.out.println(ind+") cclass "+cclass);
                ind++;
                Set<OWLClassExpression> sub = cclass.getSubClasses(onto);

                //   System.out.println("cclass "+cclass+" sub "+sub.toString());
                for (OWLClassExpression s1 : sub) {
                    //    System.out.println("sub s1 "+s1);
                    if (s1.getClassExpressionType() == ClassExpressionType.OWL_CLASS && (!(visited.contains(s1.asOWLClass())))) {
                        //       System.out.println("s1 "+s1+s1.asOWLClass());
                        current.add(s1.asOWLClass());

                    }

                }

                Set<OWLClassExpression> sup = cclass.getSuperClasses(onto);
                for (OWLClassExpression s2 : sup) {
                    // System.out.println("sup s2 "+s2);
                    if ((s2.getClassExpressionType() == ClassExpressionType.OWL_CLASS) && (!(visited.contains(s2.asOWLClass())))) {
                        //System.out.println("s2 "+s2+ s2.asOWLClass());
                        current.add(s2.asOWLClass());

                    }
                }

                Set<OWLClassExpression> ex = cclass.getEquivalentClasses(onto);
                // OWLEntity foo;
                // Set <OWLEntityEcpress> exdd = cclass.g
                //   if(ex.size() == 1){
                for (OWLClassExpression s3 : sup) {
                    if ((s3.getClassExpressionType() == ClassExpressionType.OWL_CLASS) && (!(visited.contains(s3.asOWLClass())))) {
                        //System.out.println("s2 "+s2+ s2.asOWLClass());
                        current.add(s3.asOWLClass());
                        // System.out.println("eq "+cclass+ " and "+s3+" and "+s3.asOWLClass());

                    }
                    // }
                }

            }
            //
            // System.out.println();
             /*
             for(OWLClass v: visited){
             if(current.contains(v)){
             f=true;
             }
             }
             if(f==false){*/
            jumps++;
              /*System.out.println("Hops : "+jumps);
             System.out.println("visited "+visited.toString());
            System.out.println("current "+current.toString());
              System.out.println("temp "+temp.toString());
            System.out.println("CLASS jumps "+jumps);*/
            if (jumps > 0 && current.isEmpty()) {

                jumps = 0;
                break;
            }

            //  }
        }
        //  System.out.println();

        // System.out.println("newcurrent "+current.toString());
        //  System.out.println("Distance between "+a+"  and "+b+" is: "+jumps+" .");
        // System.out.println("Farness between "+a+"  and "+b+" is: "+jumps/2+" .");
        return jumps;
    }

    public double getRedundancy(OWLOntology[] modSet) {

        double axiomTotal = 0;
        Set<OWLAxiom> nodup = new HashSet();

        int count = 0;
        for (OWLOntology modSet1 : modSet) {
            if (!(modSet1 == null)) {
                //System.out.println("COUNT " + count);
                axiomTotal += modSet1.getAxiomCount();
                Set<OWLOntology> ontos = modSet1.getDirectImports();
                nodup.addAll(modSet1.getAxioms());
                for (OWLOntology o:ontos){
                    axiomTotal += o.getAxiomCount();
                    nodup.addAll(o.getAxioms());
                }



                // System.out.println(axiomTotal);
                // System.out.println(nodup.size());
                count++;
            }

        }

        double axiomDist = nodup.size();

        double red = (axiomTotal - axiomDist) / axiomTotal;
        NumberFormat formatter = new DecimalFormat("0.00000000000");
        String string = formatter.format(red);
        System.out.println("The redundancy of the set: " + red);
        System.out.println("The redundancy of the set: " + string);
        return red;
    }

    public static void main(String[] args) throws Exception {

        String[] ontology_names = {"ncit","foodon","helis"};
        String[] ontology_file_names = {"ncit_20.12d.owl","foodon-merged.owl","helis_v1.00.origin.owl"};
        String[] module_type = {"-ui","-locality"};
        Integer module_index = 1;
        Integer ontology_index = 2;
        String[] pers = {"0.1"};

        String type = "-star";

        String input_o = "../test_ontologies/"+ontology_file_names[ontology_index];

        moduleEvaluation evaluation = new moduleEvaluation();

        FileWriter wrt = new FileWriter(new File("../test_terms/loc_star_size.txt"));
        wrt.write("filename\tsize\t");
        for(String per:pers){
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology whole_onto = manager.loadOntologyFromOntologyDocument(new File(input_o));
            evaluation.ontologyOWL = whole_onto;
            evaluation.metrics_o();
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setIncludes(new String[]{"**_"+per+".owl"});
//            scanner.setIncludes(new String[]{"**.owl"});
            scanner.setBasedir("../test_terms/"+ ontology_names[ontology_index]+module_type[module_index]+type);
//            scanner.setBasedir("../test_terms/helis-seed-locality"+type+"/ontology");
            scanner.setCaseSensitive(false);
            scanner.scan();
            String[] files = scanner.getIncludedFiles();


            FileWriter wrt = new FileWriter(new File("../test_terms/"+ontology_names[ontology_index]+module_type[module_index]+type+"_"+per+"_evaluation.txt"));
//            FileWriter wrt = new FileWriter(new File("../test_terms/"+ontology_names[ontology_index]+"-seed"+module_type[module_index]+type+"_"+per+"_evaluation.txt"));
            wrt.write("filename\tmSiza\tmAttRich\tmInhRich\tmIntramoddist\toIntramoddist\trIntramoddist\tmCohesion\n");
            for(String file_name:files){
                System.out.println(file_name);
                OWLOntology local_module = manager.loadOntologyFromOntologyDocument(new File("../test_terms/"+ ontology_names[ontology_index]+module_type[module_index]+type+"/"+file_name));
//                OWLOntology local_module = manager.loadOntologyFromOntologyDocument(new File("../test_terms/helis-seed-locality"+type+"/ontology/"+file_name));
                evaluation.moduleOWL = local_module;
                evaluation.metrics();

                System.out.println("module size: "+evaluation.mSize);
                System.out.println("module attribute richness: "+evaluation.mAttRich);
                System.out.println("module inheritance richness: "+evaluation.mInhRich);
                System.out.println("module intra distance: "+evaluation.mIntramoddist);
                System.out.println("ontology intra distance: "+evaluation.oIntramoddist);
                System.out.println("relative intra-module distance: "+evaluation.rIntramoddist);
                System.out.println("module cohesion: "+evaluation.mCohesion);
                wrt.write(file_name+"\t"+evaluation.mSize+"\t"+evaluation.mAttRich+"\t"+evaluation.mInhRich+"\t"+evaluation.mIntramoddist+"\t"+evaluation.oIntramoddist+"\t"+evaluation.rIntramoddist+"\t"+evaluation.mCohesion+"\n");
                wrt.flush();
            }
            wrt.close();
        }




    }
}
