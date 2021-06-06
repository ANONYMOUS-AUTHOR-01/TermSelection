# Reproducibility Resources for "Metadata-based Term Selection for Modularization and Uniform Interpolation of OWL Ontologies"

## Term Selection Experiments

### To show or compute detailed results

> Code is tested on **Ubuntu 18.04.3 LTS** and **Python 3.6.9**
>
> To install the dependencies, we recommend building a virtual environment with `venv`.
>
> Usage:
>
> ```bash
> git clone https://github.com/ANONYMOUS-AUTHOR-01/TermSelection.git
> cd TermSelection
> cat data/SnomedCT/concept_embedding.z* > data/SnomedCT/concept_embedding_cat.zip
> unzip data/SnomedCT/concept_embedding_cat.zip data/SnomedCT/
> 
> python3 -m venv ts_general_env
> source ts_general_env/bin/activate
> pip3 install -r general_requirements.txt
> jupyter notebook
> ```
>
> Connect to the `jupyter notebook` service by keying the prompted URL in your browser, then open  `show_result.ipynb` or `compute_result.ipynb`to run the code blocks.

Detailed experiments result are placed in `result/`, and `show_result.ipynb` is used to show the metrics (mean, standard deviation, etc.) of these experiments.

The NHS/NRC refsets are placed in `data/SnomedCT/{nhs_nrc}_refset_iri/`

The seed signatures are placed in `data/SnomedCT/{nhs,nrc}_seed_{random,select}_iri/`

To **reproduce results of NN-RANK** and other baseline models, use `compute_result.ipynb` following the instructions above.

### To reproduce concept embedding & fine-tuning

> Code is tested on **Ubuntu 18.04.3 LTS** and **Python 3.6.9**
>
> To install the dependencies, we recommend building a virtual environment with `venv`.
>
> Usage:
>
> ```bash
> git clone https://github.com/ANONYMOUS-AUTHOR-01/TermSelection.git
> cd TermSelection
> unzip data/SnomedCT/ontology.zip -d data/SnomedCT/
> 
> python3 -m venv ts_embed_env
> source ts_embed_env/bin/activate
> pip3 install -r embed_requirements.txt
> 
> python3 Generate_embedding.py`
> 
> cat checkpoints/model.z* > checkpoints/model_cat.zip
> unzip checkpoints/model_cat.zip checkpoints/
> python3 NNRANK_finetuning.py
> ```

The pre-computed concept embedding is stored in`data/SnomedCT/concept_embedding.csv`. 

To re-generate concept embedding,  use `python3 Generate_embedding.py`.

To re-generate fine-tuned concept embedding, unzip the language model in `checkpoints/`, and run `NNRANK_finetuning.py`.

### To reproduce predictions of MetaSVDD:

> Code is tested on **Ubuntu 18.04.3 LTS** and **Python 3.7.10**
>
> To install the dependencies, we recommend building a virtual environment with `venv`.
>
> Usage:
>
> ```bash
> git clone https://github.com/ANONYMOUS-AUTHOR-01/TermSelection.git
> cd TermSelection
> 
> python3 -m venv ts_mta_env
> source ts_mta_env/bin/activate
> pip3 install -r metasvdd_requirements.txt
> pip3 install --upgrade scikit-learn
> 
> notebook
> ```
> Connect to the `jupyter notebook` service by keying the prompted URL in your browser, then open  `data_preprocessing.ipynb` and `MetaSVDD.ipynb` to run the code blocks.

To re-generate predictions of MetaSVDD, run `data_preprocessing.ipynb` first to generate necessary auxiliary files (e.g., refset concept embedding), and run `MetaSVDD.ipynb` to make predictions.

### To reproduce predictions of Signature-extension

> Code is tested on **Windows 10 20H2** , **Python 3.6.9** and **JDK-14**
>
> Usage:
>
> ```bash
>git clone https://github.com/ANONYMOUS-AUTHOR-01/TermSelection.git
> cd TermSelection
> unzip data/SnomedCT/ontology.zip -d data/SnomedCT/
> 
> python3 -m venv ts_general_env
> source ts_general_env/bin/activate
> pip3 install -r general_requirements.txt
> 
> # change the java_path specified in sigExtension-ghadah.py
> python3 sigExtension-ghadah.py
> ```

To reproduce `Signature-Extension` results, change the `java_path` specified in `sigExtension-ghadah.py` to a java environment with `jdk 14` or `jdk 12` , and run `sigExtension-ghadah.py`.

The computation results of `Signature-Extension` for evaluation is placed in `data/SnomedCT/{nhs,nrc}_seed_{random,select}_extended_*/`

### To reproduce predictions of star-modularization

> Code is tested using `JetBrains IDEA` and `JDK-8`
>
> Usage:
>
> Open the directory `data/Star-Modularization/` with `IDEA` , open `File -> Project Structure`, change Project SDK to `1.8`. Then go to the `Libraries` section in the same window, include `TermSelection/Modularization_Experiments/UI_modularization/elk-owlapi4-tryout-0.0.1-SNAPSHOT.jar` as a java library, click `Apply`.

 Run `src/main/java/SeedModuleExtractor.java`  to reproduce predictions of star-modularization

The computation results of `Star-Modularization` for evaluation is placed in `data/SnomedCT/{nhs,nrc}_output_{random,select}_module_iri/`

## Modularization Experiments

The seed signatures are placed in `data/HeLis/seed_random_iri/`, using which `NN-RANK` computes the concept ranking. The ranking lists are placed in `data/HeLis/seed_random_iri/`

The result of using `Signature-Extension` to extend the seed signature is placed in `data/HeLis/seed_random_iri_extended_{depth}`.

> `jdk 8` is required to reproduce the following results

`Locality-based modularization` can be generated by running `Modularization_Experiments\Locality_modularization\src\modularization\moduleTest.java` 

`Uniform interpolant (UI)` can be generated by running `Modularization_Experiments\UI_modularization\src\forgetting\Forgetting.java`

The compactness test of `locality-based modularization` and `UI` is in `Modularization_Experiments\Locality_modularization\src\modularization\moduleEvaluation.java`

Evaluation results and test signatures are in `Modularization_Experiments\test_terms`
