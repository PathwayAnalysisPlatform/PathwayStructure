#Cypher queries to extract data from Reactome Neo4j database

## Get the reactions for a given Pathway
~~~~
MATCH (p:Pathway{stId:"R-HSA-195253"})-[:hasEvent*]->(rle:ReactionLikeEvent)
RETURN p.stId AS Pathway, rle.stId AS Reaction, rle.displayName AS ReactionName
~~~~

## Get preceding reactions
INPUT: stId of a reaction
OUTPUT: List of ReactionLikeEvents 
~~~~
MATCH (r:ReactionLikeEvent{stId:'R-HSA-5246693'})-[:precedingEvent]->(pR:ReactionLikeEvent)
RETURN pR.stId, pR.displayName
~~~~

## Get preceding reactions of a list of reactions
INPUT: List of stId of reactions
OUTPUT: List of ReactionLikeEvents 
~~~~
MATCH (r:ReactionLikeEvent)-[:precedingEvent]->(pR:ReactionLikeEvent)
WHERE r.stId in ["R-HSA-5246693", "R-HSA-195251", "R-HSA-5229343", "R-HSA-195304", "R-HSA-195318", "R-HSA-195287"]
RETURN pR.stId as PrecedingReaction, pR.displayName as PRName, r.stId as SuccessorReaction, r.displayName as SRName
~~~~

## Get preceding reactions of all reactions in a Pathway
INPUT: Pathway stId
OUTPUT: list of reaction pairs as: Predecessor, Successor
~~~~
MATCH (p:Pathway{stId:"R-HSA-195253"})-[:hasEvent*]->(rle:ReactionLikeEvent)
WITH rle
MATCH (rle)-[:precedingEvent]->(pR:ReactionLikeEvent)
RETURN pR.stId as PrecedingReaction, pR.displayName as PRName, rle.stId as SuccessorReaction, rle.displayName as SRName
~~~~

## Get participants of some reactions
INPUT: List of reaction stIds
OUTPUT: List of physicalEntities with their role in the reaction
~~~~
MATCH (rle:ReactionLikeEvent)-[role:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]->(pe:PhysicalEntity)
WHERE rle.stId in ["R-HSA-5246693", "R-HSA-195251", "R-HSA-5229343", "R-HSA-195304", "R-HSA-195318", "R-HSA-195287"]
RETURN DISTINCT rle.stId AS Reaction, pe.stId as Participant, extract(x IN role | type(x)) as Role, pe.displayName AS DisplayName
~~~~


