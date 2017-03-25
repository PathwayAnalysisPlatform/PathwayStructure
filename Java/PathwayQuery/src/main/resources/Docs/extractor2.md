# Graph extractor v2.0

## Objective: 

Extract a graph composed by vertices and edges that represents the hierarchical structure of pathways, reactions, complexes, sets and proteins defined in Reactome.

## Graph definition

Vertices:
  * Proteins
  * Complexes/Sets
  * Reactions
  * Pathways

Edges:

* Vertical:
    - [ ] complex has a protein (Cp).
    - [ ] set has a protein (Sp).
    - [ ] complex has a complex (CC).
    - [ ] set has a complex (SC).
    - [ ] complex has a set (CS).
    - [ ] set has a set (SS).
    - [ ] reaction has a protein (Rp).
    - [ ] reaction has a complex (RC).
    - [ ] reaction has a set (RS).
    - [ ] pathway has a reaction (PR).
    - [ ] pathway has a pathway (PP).
* Horizontal:
    - [ ] Interaction between proteins: input-output, catalyst-input, catalyst-output, regulator-input, regulator-output
    - [ ] Interaction between Complexes or Sets:  input-output, catalyst-input, catalyst-output, regulator-input, regulator-output
    - [ ] Binary interactions between proteins obtained from Uniprot, which in turn gets them from IntAct.
    - [ ] Reactions chained: Two reactions _R1_ and _R2_ are chained if the output of _R1_ is the input of _R2_. Where the input and output can be a protein, complex or set, but not a small molecule such as water, ATP or others.

## Input

* **Case 1:** Generate a graph in respect to a set of proteins.


    The input is a file with a list of Uniprot Identifiers of the desired proteins.

* **Case 2:** Generate graph for the whole proteome considered in Reactome.

    No input is necessary.

## Output 

A file with the graph in one of the desired file formats: sif or json.

## Extra information

#### Number of vertices

Entities(vertices) for _Homo sapiens_ in v59 Reactome: 44,827
* Proteins: 20,102
~~~~
MATCH (re:ReferenceEntity) WHERE re.databaseName = 'UniProt' RETURN count(re)
~~~~
* Complexes: 10,112
~~~~
MATCH (c:Complex) WHERE c.speciesName = 'Homo sapiens' RETURN count(c)
~~~~
* Sets: 3,876 (DefinedSet: 2,959, OpenSet: 2, CandidateSet: 915)
~~~~
MATCH (es:EntitySet) WHERE es.speciesName = 'Homo sapiens' RETURN count(es)
~~~~
* Reactions: 8,686
~~~~
MATCH (r:Reaction) WHERE r.speciesName = 'Homo sapiens' RETURN count(r)
~~~~
* Pathways: 2,051
~~~~
MATCH (p:Pathway) WHERE p.speciesName = 'Homo sapiens' RETURN count(p)
~~~~

#### Number of edges

Relationships (edges) for _Homo sapiens_ in v59 Reactome: 70,991

* Cp: 12,301
~~~~
MATCH (c:Complex)-[:hasComponent]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)
WHERE c.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'
RETURN DISTINCT c.stId, re.identifier
~~~~
* Sp: 13,811
~~~~
MATCH (es:EntitySet)-[:hasMember|hasCandidate]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)
WHERE es.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'
RETURN DISTINCT es.stId, re.identifier
~~~~
* CC: 5,966 
~~~~
MATCH (c1:Complex)-[:hasComponent]->(c2:Complex)
WHERE c1.speciesName = 'Homo sapiens' AND c2.speciesName = 'Homo sapiens'
RETURN DISTINCT c1.stId, c2.stId
~~~~
* SC: 2,715
~~~~
MATCH (es:EntitySet)-[:hasMember|hasCandidate]->(c:Complex)
WHERE c.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'
RETURN DISTINCT c.stId, es.stId
~~~~
* CS: 3,378
~~~~
MATCH (c:Complex)-[:hasComponent]->(es:EntitySet)
WHERE c.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'
RETURN DISTINCT c.stId, es.stId
~~~~
* SS: 614
~~~~
MATCH (es1:EntitySet)-[:hasMember|hasCandidate]->(es2:EntitySet)
WHERE es1.speciesName = 'Homo sapiens' AND es2.speciesName = 'Homo sapiens'
RETURN DISTINCT es1.stId, es2.stId
~~~~
* Rp: 5,642 
~~~~
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)
WHERE r.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'
RETURN DISTINCT r.stId AS Reaction, re.identifier as Participant
~~~~
* RC: 12,016
~~~~
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(c:Complex)
WHERE r.speciesName = 'Homo sapiens' AND c.speciesName = 'Homo sapiens'
RETURN DISTINCT r.stId AS Reaction, c.stId as Participant
~~~~
* RS: 3,366 
~~~~
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(es:EntitySet)
WHERE r.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'
RETURN DISTINCT r.stId AS Reaction, es.stId as Participant
~~~~
* PR: 9,073 
~~~~
MATCH (p:Pathway)-[:hasEvent]->(r:Reaction)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'
RETURN DISTINCT p.stId AS Pathway, r.stId AS Reaction
~~~~
* PP: 2,109 
~~~~
MATCH (p1:Pathway)-[:hasEvent]->(p2:Pathway)
WHERE p1.speciesName = 'Homo sapiens' AND p2.speciesName = 'Homo sapiens'
RETURN DISTINCT p1.stId, p2.stId
~~~~

#### stId

#### Out of the scope

* In the Reactome data model there is a class called: ReactionLikeEvent. It has 3 subtypes: Reaction, BlackBoxEvent, Polymersation and Depolymerisation. 
Only the Reaction objects will be considered, since those are the only typical reactions with balanced input and output.