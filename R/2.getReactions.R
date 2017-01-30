### Discussion w/ Ragnhild: (Harald agreed)
## 1) Get all the reactions with a catalyst
## 2) For the remaining ones, get all the binding reactions
##    => all reactions with > 1 input
## 3) Then see how many reactions are left and what kind of stuff they are.
##    If they are just moving stuff from one place to another without any other stuff thing whatever going on, we are not interested in it..? But perhaps other stuff things are going on?


### Step 1: Get all the reactions with a catalyst
## restrict reactions and ewas to Homo sapiens
## CatalystActivity does not have a speciesName
query <- paste("MATCH",
               "(r:Reaction)",
               "-[:input|output|requiredInputComponent]->",
               "(ewas:EntityWithAccessionedSequence)",
               "WHERE",
               "(r)-[:catalystActivity]->(:CatalystActivity)",
               "AND",
               "r.speciesName = 'Homo sapiens'",
               "AND",
               "ewas.speciesName = 'Homo sapiens'",
               "RETURN",
               "DISTINCT r.stId")

catStId <- unlist(cypherToList(graph, query))
head(catStId)

## try same thing 2 ways:
## get all reactions without catalyst by
## 1) previous query => "WHERE NOT",
## 2) previous query =>
## "(r)-[:catalystActivity]->(:CatalystActivity)"
## becomes NOT r.stId IN catDisNam

## get all reactions without catalyst by
## 1) previous query => "WHERE NOT",
query <- paste("MATCH",
               "(r:Reaction)",
               "-[:input|output|requiredInputComponent]->",
               "(ewas:EntityWithAccessionedSequence)",
               "WHERE NOT",
               "(r)-[:catalystActivity]->(:CatalystActivity)",
               "AND",
               "r.speciesName = 'Homo sapiens'",
               "AND",
               "ewas.speciesName = 'Homo sapiens'",
               "RETURN",
               "DISTINCT r.stId")

noCatStId <- unlist(cypherToList(graph, query))
head(noCatStId)

length(catStId)   ##  828
length(noCatStId) ## 2377

## get all reactions without catalyst by
## 2) previous query =>
## "(r)-[:catalystActivity]->(:CatalystActivity)"
## becomes NOT r.stId IN catDisNam
query <- paste("MATCH",
               "(r:Reaction)",
               "-[:input|output|requiredInputComponent]->",
               "(ewas:EntityWithAccessionedSequence)",
               "WHERE",
               "NOT r.stId IN {placeholder}",
               "AND",
               "r.speciesName = 'Homo sapiens'",
               "AND",
               "ewas.speciesName = 'Homo sapiens'",
               "RETURN",
               "DISTINCT r.stId")

noCatStId2 <- unlist(cypherToList(graph, query,
                                  placeholder = catStId))
head(noCatStId2)

length(catStId)    ##  828
length(noCatStId)  ## 2377
length(noCatStId2) ## 2377


## Just to check, the total should be:
querywRIC <- paste("MATCH",
               "(r:Reaction)",
               "-[:input|output|requiredInputComponent]->",
               "(ewas:EntityWithAccessionedSequence)",
               "WHERE",
               "r.speciesName = 'Homo sapiens'",
               "AND",
               "ewas.speciesName = 'Homo sapiens'",
               "RETURN",
               "DISTINCT r.stId")

querywoRIC <- paste("MATCH",
               "(r:Reaction)",
               "-[:input|output]->",
               "(ewas:EntityWithAccessionedSequence)",
               "WHERE",
               "r.speciesName = 'Homo sapiens'",
               "AND",
               "ewas.speciesName = 'Homo sapiens'",
               "RETURN",
               "DISTINCT r.stId")

allStIdwRIC <- unlist(cypherToList(graph, querywRIC))
allStIdwoRIC <- unlist(cypherToList(graph, querywoRIC))
strange <- allStIdwRIC[!allStIdwRIC %in% allStIdwoRIC]
length(strange) ## 6
strange



## Step 2 For the remaining reactions
## (the ones without a catalyst activity),
## get all the binding reactions
##    => all reactions with > 1 input

length(noCatStId) ## 2377
head(noCatStId)

query <- paste("MATCH",
               "()",
               "<-[:input]-",
               "(r:Reaction)",
               "-[:input]->",
               "()",
               "WHERE",
               "r.stId IN {placeholder}",
               "RETURN",
               "DISTINCT r.stId")

reacMultIn <- unlist(cypherToList(graph, query,
                                  placeholder = noCatStId))

length(reacMultIn) ## 2144 (from 2377)
head(reacMultIn)

query <- paste("MATCH",
               "(r:Reaction)",
               "WHERE",
               "r.stId IN {placeholder}",
               "RETURN",
               "DISTINCT r.displayName")
reacMultInN <- unlist(cypherToList(graph, query,
                                  placeholder = reacMultIn))

length(reacMultInN) ## 'only 2138'

sum(unlist(lapply(reacMultInN, function (x) {
    any(grepl(":", x,
              fixed=TRUE))
})))
## 375 reactions with multiple inputs have a colon in the name



## Step 3: see how many reactions are left and what kind of stuff they are.
## If we go with option a) in step 2, we still have 233 reactions left...
step3 <- noCatStId[!noCatStId %in% reacMultIn]
length(step3)

query <- paste("MATCH",
               "(r:Reaction)",
               "WHERE",
               "r.stId IN {placeholder}",
               "RETURN",
               "DISTINCT r.displayName")
step3Names <- unlist(cypherToList(graph, query,
                                  placeholder = step3))

step3Names




### Combine the reactions
all(length(unique(c(catStId, noCatStId)) == length(c(catStId, noCatStId))))

## all reactions (combining those with catalyst and those without catalyst)
reactionsStId <- c(catStId, noCatStId)

## the reactions that we will use: reactions with catalyst, and binding reactions
ourReactions <- c(catStId, reacMultIn)

write.table(reactionsStId, file = "allReactions.txt", quote = FALSE,
            row.names = FALSE, col.names = FALSE)
write.table(ourReactions, file = "ourReactions.txt", quote = FALSE,
            row.names = FALSE, col.names = FALSE)


