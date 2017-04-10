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
    - [X] complex has a protein (Cp).
    - [X] set has a protein (Sp).
    - [X] complex has a complex (CC).
    - [X] set has a complex (SC).
    - [X] complex has a set (CS).
    - [X] set has a set (SS).
    - [X] reaction has a protein (Rp).
    - [X] reaction has a complex (RC).
    - [X] reaction has a set (RS).
    - [X] pathway has a reaction (PR).
    - [X] pathway has a pathway (PP).
* Horizontal:
    - [X] Interaction between proteins: input-output, catalyst-input, catalyst-output, regulator-input, regulator-output
    - [ ] Binary interactions between proteins obtained from Uniprot, which in turn gets them from IntAct.
    - [X] Reactions chained (RR): Two reactions _R1_ and _R2_ are chained if the output of _R1_ is the input of _R2_. Where the input and output can be a protein, complex or set, but not a small molecule such as water, ATP or others.

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
RETURN DISTINCT c.stId as source, re.identifier as destiny
~~~~
* Sp: 13,811
~~~~
MATCH (es:EntitySet)-[:hasMember|hasCandidate]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)
WHERE es.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'
RETURN DISTINCT es.stId as source, re.identifier as destiny
~~~~
* CC: 5,966 
~~~~
MATCH (c1:Complex)-[:hasComponent]->(c2:Complex)
WHERE c1.speciesName = 'Homo sapiens' AND c2.speciesName = 'Homo sapiens'
RETURN DISTINCT c1.stId as source, c2.stId as destiny
~~~~
* SC: 2,715
~~~~
MATCH (es:EntitySet)-[:hasMember|hasCandidate]->(c:Complex)
WHERE c.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'
RETURN DISTINCT c.stId as source, es.stId as destiny
~~~~
* CS: 3,378
~~~~
MATCH (c:Complex)-[:hasComponent]->(es:EntitySet)
WHERE c.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'
RETURN DISTINCT c.stId as source, es.stId as destiny
~~~~
* SS: 614
~~~~
MATCH (es1:EntitySet)-[:hasMember|hasCandidate]->(es2:EntitySet)
WHERE es1.speciesName = 'Homo sapiens' AND es2.speciesName = 'Homo sapiens'
RETURN DISTINCT es1.stId as source, es2.stId as destiny
~~~~
* Rp: 5,642 
~~~~
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)
WHERE r.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'
RETURN DISTINCT r.stId AS source, re.identifier as destiny
~~~~
* RC: 12,016
~~~~
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(c:Complex)
WHERE r.speciesName = 'Homo sapiens' AND c.speciesName = 'Homo sapiens'
RETURN DISTINCT r.stId AS source, c.stId as destiny
~~~~
* RS: 3,366 
~~~~
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(es:EntitySet)
WHERE r.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'
RETURN DISTINCT r.stId AS source, es.stId as destiny
~~~~
* PR: 9,073 
~~~~
MATCH (p:Pathway)-[:hasEvent]->(r:Reaction)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'
RETURN DISTINCT p.stId AS source, r.stId AS destiny
~~~~
* PP: 2,109 
~~~~
MATCH (p1:Pathway)-[:hasEvent]->(p2:Pathway)
WHERE p1.speciesName = 'Homo sapiens' AND p2.speciesName = 'Homo sapiens'
RETURN DISTINCT p1.stId as source, p2.stId as destiny
~~~~
* RR:
~~~~
MATCH (r1:Reaction)-[role1:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]->(ewas:EntityWithAccessionedSequence)<-[role2:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]-(r2:Reaction)
WHERE NOT (r1.stId = r2.stId)
WITH r1, head(extract(x IN role1 | type(x))) as role1, ewas, last(extract(x IN role2 | type(x))) as role2, r2
MATCH (ewas)-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WHERE role1 = 'output' AND role2 = 'input'
RETURN DISTINCT r1.stId as Reaction1, role1, re.identifier as protein,  role2, r2.stId as Reaction2 ORDER BY Reaction1
~~~~

#### stId

#### Out of the scope

* In the Reactome data model there is a class called: ReactionLikeEvent. It has 3 subtypes: Reaction, BlackBoxEvent, Polymersation and Depolymerisation. 
Only the Reaction objects will be considered, since those are the only typical reactions with balanced input and output.