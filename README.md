# Reproducibility Resources for "Metadata-based Term Selection for Modularization and Uniform Interpolation of OWL Ontologies"

## Term Selection Experiments

### For detailed results

**Detailed experiments result are placed in `result/`, and `show_result.ipynb` is used to show the metrics (mean, standard error, etc.) of these experiments.**

> General requirements: Python 3.7 with the following packages: numpy, scikit-learn, pandas. All third-party modules should be up-to-date.

The NHS/NRC refsets are placed in `data/SnomedCT/{nhs_nrc}_refset_iri/`

The seed signatures are placed in `data/SnomedCT/{nhs,nrc}_seed_{random,select}_iri/`

### For reproduce NN-RANK and NN-RANK  + fine-tuning results

> To reproduce all the results, file paths specified in each scripts should be set accordingly based on the current (refset, seed_signature) pair.

To reproduce `NN-RANK` results, unzip the *pre-computed concept embedding* in `data/SnomedCT/concept_embedding.zip`, and run the `NN_rank random` and `NN_rank select` sections in `compute_result.ipynb`

> The pre-computed concept embedding is generated using the original version of OWL2Vec\*. For re-generating concept embedding,  use `python OWL2Vec_Plus.py`.  The corpus needed by OWL2Vec\* can be generated according to [this paper](https://arxiv.org/pdf/2009.14654.pdf).

To reproduce `NN-RANK + fine-tuning` results, unzip the language model in `checkpoints/`, and run `NNRANK_finetuning.py`.

> Requirements for reproducing `NN-RANK + fine-tuning` results are specified in `ft_requirements.txt`

### For reproduce results of other models

To reproduce `Star-Modularization` results, run `src/main/java/SeedModuleExtractor.java` with `jdk 8`.  The requirements are specified in `pom.xml`.

> The computation results of `Star-Modularization` for evaluation is placed in `data/SnomedCT/{nhs,nrc}_output_{random,select}_module_iri/`

To reproduce `Signature-Extension` results, change the `java_path` specified in `sigExtension-ghadah.py` to a java environment with `jdk 11` or `jdk 12` , and run `sigExtension-ghadah.py`.

> The computation results of `Signature-Extension` for evaluation is placed in `data/SnomedCT/{nhs,nrc}_seed_{random,select}_extended_*/`

To reproduce `Meta-SVDD` results, use `data_preprocessing.ipynb` to export refset embeddings, and run `MetaSVDD.ipynb`.

## Modularization Experiments

The seed signatures are placed in `data/HeLis/seed_random_iri/`, using which `NN-RANK` computes the concept ranking. The ranking lists are placed in `data/HeLis/seed_random_iri/`

The result of using `Signature-Extension` to extend the seed signature is placed in `data/HeLis/seed_random_iri_extended_{depth}`.

> `jdk 8` is required to reproduce the following results

`Locality-based modularization` can be generated by running `Modularization_Experiments\Locality_modularization\src\modularization\moduleTest.java` 

`Uniform interpolant (UI)` can be generated by running `Modularization_Experiments\UI_modularization\src\forgetting\Forgetting.java`

The compactness test of `locality-based modularization` and `UI` is in `Modularization_Experiments\Locality_modularization\src\modularization\moduleEvaluation.java`

Evaluation results and test signatures are in `Modularization_Experiments\test_terms`
