reactionsStId <- read.table("allReactions.txt",
                            header = FALSE,
                            stringsAsFactors = FALSE)$V1

ourReactions <- read.table("ourReactions.txt",
                           header = FALSE,
                           stringsAsFactors = FALSE)$V1

length(reactionsStId)
length(ourReactions)

accessions.r <- as.character(
    read.table("~/Documents/pathways/uniprot-reactome.list")$V1)

### proteins sharing a complex
query <- paste("MATCH",
               "(re:ReferenceEntity)",
               "<-[:referenceEntity]-",
               "(:EntityWithAccessionedSequence)",
               "<-[:hasComponent*..6]-",
               "(:Complex)",
               "-[:hasComponent*..6]->",
               "(:EntityWithAccessionedSequence)",
               "-[:referenceEntity]->",
               "(re2:ReferenceEntity)",
               "WHERE",
               "re.identifier IN {placeholder}",
               "AND",
               "re2.identifier IN {placeholder}",
               "RETURN DISTINCT",
               "re.identifier AS proteinA,",
               "re2.identifier AS proteinB")

inComplex <- cypherToList(graph, query,
                          placeholder = accessions.r)

length(inComplex)

proteinA <- rep(NA, length(inComplex))
proteinB <- rep(NA, length(inComplex))
i <- 0
j <- 0
while (i < length(inComplex)) {
    i <- i+1
    if (! inComplex[[i]]$proteinA == inComplex[[i]]$proteinB) {
        j <- j+1
        proteinA[j] <- inComplex[[i]]$proteinA
        proteinB[j] <- inComplex[[i]]$proteinB
    }
}

min(which(is.na(proteinA)))
min(which(is.na(proteinB)))
inCdf <- data.frame(proteinA = proteinA[1:(min(which(is.na(proteinA)))-1)],
                    proteinB = proteinB[1:(min(which(is.na(proteinA)))-1)])
head(inCdf)
nrow(inCdf)                    ## 31506 nest6: 110670
length(unique(inCdf$proteinA)) ##  3154 nest6: 3772
length(unique(inCdf$proteinB)) ##  3154 nest6: 3772
length(accessions.r)           ## 10111


## how many direct connections does each protein have?
nrC <- sapply(unique(inCdf$proteinA), function (x) {
    sum(x == inCdf$proteinA)
})
hist(nrC, breaks = 1000)

length(inComplex) - nrow(inCdf)   ## 580 proteins are connected to themselves
## Those are proteins with multiple forms, which have 1 UNIPROT ID, but multiple EWASses connected to them. For example:
## inComplex[[1]]$proteinA
## Q14674









## A inputOutput B
ioquery <- paste("MATCH",
                 "(a:ReferenceEntity{identifier:{startprotein}})",
                 "<-[:referenceEntity]-",
                 "(:EntityWithAccessionedSequence)",
                 "<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "input*]-",
                 "(r:Reaction)",
                 "-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "output*]->",
                 "(:EntityWithAccessionedSequence)",
                 "-[:referenceEntity]->",
                 "(b:ReferenceEntity)",
                 "WHERE",
                 "r.stId IN {reactions}",
                 "AND",
                 "b.identifier IN {accessions}",
                 "RETURN DISTINCT",
                 "a.identifier AS proteinA,",
                 "b.identifier AS proteinB")

library("pbapply")

iop <- pbsapply(accessions.r, function (startprotein) {
    cypherToList(graph, ioquery,
                 startprotein = startprotein,
                 reactions = ourReactions,
                 accessions = accessions.r)
})

length(iop)
length(ourReactions)
length(accessions.r)

iop[[1]]


## A inputRegulation B
irquery <- paste("MATCH",
                 "(a:ReferenceEntity{identifier:{startprotein}})",
                 "<-[:referenceEntity]-",
                 "(:EntityWithAccessionedSequence)",
                 "<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "input*]-",
                 "(r:Reaction)",
                 "-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "regulatedBy|regulator*]->",
                 "(:EntityWithAccessionedSequence)",
                 "-[:referenceEntity]->",
                 "(b:ReferenceEntity)",
                 "WHERE",
                 "r.stId IN {reactions}",
                 "AND",
                 "b.identifier IN {accessions}",
                 "RETURN DISTINCT",
                 "a.identifier AS proteinA,",
                 "b.identifier AS proteinB")

irp <- pbsapply(accessions.r, function (startprotein) {
    cypherToList(graph, irquery,
                 startprotein = startprotein,
                 reactions = ourReactions,
                 accessions = accessions.r)
})

length(irp)
length(ourReactions)
length(accessions.r)

irp[[1]]


## A inputCatalyst B
icquery <- paste("MATCH",
                 "(a:ReferenceEntity{identifier:{startprotein}})",
                 "<-[:referenceEntity]-",
                 "(:EntityWithAccessionedSequence)",
                 "<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "input*]-",
                 "(r:Reaction)",
                 "-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "catalystActivity|physicalEntity*]->",
                 "(:EntityWithAccessionedSequence)",
                 "-[:referenceEntity]->",
                 "(b:ReferenceEntity)",
                 "WHERE",
                 "r.stId IN {reactions}",
                 "AND",
                 "b.identifier IN {accessions}",
                 "RETURN DISTINCT",
                 "a.identifier AS proteinA,",
                 "b.identifier AS proteinB")

icp <- pbsapply(accessions.r, function (startprotein) {
    cypherToList(graph, icquery,
                 startprotein = startprotein,
                 reactions = ourReactions,
                 accessions = accessions.r)
})

length(icp)
length(ourReactions)
length(accessions.r)

icp[[1]]


## A regulationOutput B
roquery <- paste("MATCH",
                 "(a:ReferenceEntity{identifier:{startprotein}})",
                 "<-[:referenceEntity]-",
                 "(:EntityWithAccessionedSequence)",
                 "<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "regulatedBy|regulator*]-",
                 "(r:Reaction)",
                 "-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "output*]->",
                 "(:EntityWithAccessionedSequence)",
                 "-[:referenceEntity]->",
                 "(b:ReferenceEntity)",
                 "WHERE",
                 "r.stId IN {reactions}",
                 "AND",
                 "b.identifier IN {accessions}",
                 "RETURN DISTINCT",
                 "a.identifier AS proteinA,",
                 "b.identifier AS proteinB")

rop <- pbsapply(accessions.r, function (startprotein) {
    cypherToList(graph, roquery,
                 startprotein = startprotein,
                 reactions = ourReactions,
                 accessions = accessions.r)
})

length(rop)
length(ourReactions)
length(accessions.r)

rop[[1]]


## A catalystOutput B
coquery <- paste("MATCH",
                 "(a:ReferenceEntity{identifier:{startprotein}})",
                 "<-[:referenceEntity]-",
                 "(:EntityWithAccessionedSequence)",
                 "<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "catalystActivity|physicalEntity*]-",
                 "(r:Reaction)",
                 "-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "output*]->",
                 "(:EntityWithAccessionedSequence)",
                 "-[:referenceEntity]->",
                 "(b:ReferenceEntity)",
                 "WHERE",
                 "r.stId IN {reactions}",
                 "AND",
                 "b.identifier IN {accessions}",
                 "RETURN DISTINCT",
                 "a.identifier AS proteinA,",
                 "b.identifier AS proteinB")

cop <- pbsapply(accessions.r, function (startprotein) {
    cypherToList(graph, coquery,
                 startprotein = startprotein,
                 reactions = ourReactions,
                 accessions = accessions.r)
})

length(cop)
length(ourReactions)
length(accessions.r)

cop[[1]]


## A regulationCatalyst B
rcquery <- paste("MATCH",
                 "(a:ReferenceEntity{identifier:{startprotein}})",
                 "<-[:referenceEntity]-",
                 "(:EntityWithAccessionedSequence)",
                 "<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "regulator*]-",
                 "(r:Reaction)",
                 "-[:hasComponent|hasMember|hasCandidate|repeatedUnit|",
                 "physicalEntity*]->",
                 "(:EntityWithAccessionedSequence)",
                 "-[:referenceEntity]->",
                 "(b:ReferenceEntity)",
                 "WHERE",
                 "r.stId IN {reactions}",
                 "AND",
                 "b.identifier IN {accessions}",
                 "RETURN DISTINCT",
                 "a.identifier AS proteinA,",
                 "b.identifier AS proteinB")

rcp <- pbsapply(accessions.r, function (startprotein) {
    cypherToList(graph, rcquery,
                 startprotein = startprotein,
                 reactions = ourReactions,
                 accessions = accessions.r)
})

length(rcp)
length(ourReactions)
length(accessions.r)

rcp[[1]]



## inCdf
## iop
## irp
## icp
## rop
## cop
## rcp

par(mfrow=c(2, 3))
hist(sapply(unique(inCdf$proteinA), function (x) {
    sum(x == inCdf$proteinA)
}), breaks = 1000)
hist(sapply(iop, length), breaks = 1000)
hist(sapply(irp, length), breaks = 1000)
hist(sapply(icp, length), breaks = 1000)
hist(sapply(rop, length), breaks = 1000)
hist(sapply(cop, length), breaks = 1000)


## open file connection
fileConn <- file(paste0("ProtFran.txt"),
                 open = "w")

## proteins in same Complex
## A complex B
i <- 0
while (i < nrow(inCdf)) {
    i <- i+1
    writeLines(
        paste(inCdf[i, 'proteinA'], "complex", inCdf[i, 'proteinB']),
        fileConn)
}

## indirectly connected proteins
## A inputOutput B
i <- 0
while (i < length(iop)) {
    i <- i+1
    j <- 0
    while (j < length(iop[[i]])) {
        j <- j+1
        if (iop[[i]][[j]]$proteinA != iop[[i]][[j]]$proteinB) {
            writeLines(
                paste(iop[[i]][[j]]$proteinA,
                      "inputOutput",
                      iop[[i]][[j]]$proteinB),
                fileConn)
        }
    }
}

## A inputRegulation B
i <- 0
while (i < length(irp)) {
    i <- i+1
    j <- 0
    while (j < length(irp[[i]])) {
        j <- j+1
        if (irp[[i]][[j]]$proteinA != irp[[i]][[j]]$proteinB) {
            writeLines(
                paste(irp[[i]][[j]]$proteinA,
                      "inputRegulation",
                      irp[[i]][[j]]$proteinB),
                fileConn)
        }
    }
}

## A inputCatalyst B
i <- 0
while (i < length(icp)) {
    i <- i+1
    j <- 0
    while (j < length(icp[[i]])) {
        j <- j+1
        if (icp[[i]][[j]]$proteinA != icp[[i]][[j]]$proteinB) {
            writeLines(
                paste(icp[[i]][[j]]$proteinA,
                      "inputCatalyst",
                      icp[[i]][[j]]$proteinB),
                fileConn)
        }
    }
}

## A regulationOutput B
i <- 0
while (i < length(rop)) {
    i <- i+1
    j <- 0
    while (j < length(rop[[i]])) {
        j <- j+1
        if (rop[[i]][[j]]$proteinA != rop[[i]][[j]]$proteinB) {
            writeLines(
                paste(rop[[i]][[j]]$proteinA,
                      "regulationOutput",
                      rop[[i]][[j]]$proteinB),
                fileConn)
        }
    }
}

## A catalystOutput B
i <- 0
while (i < length(cop)) {
    i <- i+1
    j <- 0
    while (j < length(cop[[i]])) {
        j <- j+1
        if (cop[[i]][[j]]$proteinA != cop[[i]][[j]]$proteinB) {
            writeLines(
                paste(cop[[i]][[j]]$proteinA,
                      "catalystOutput",
                      cop[[i]][[j]]$proteinB),
                fileConn)
        }
    }
}

## A regulationCatalyst B
i <- 0
while (i < length(rcp)) {
    i <- i+1
    j <- 0
    while (j < length(rcp[[i]])) {
        j <- j+1
        if (rcp[[i]][[j]]$proteinA != rcp[[i]][[j]]$proteinB) {
            writeLines(
                paste(rcp[[i]][[j]]$proteinA,
                      "regulationCatalyst",
                      rcp[[i]][[j]]$proteinB),
                fileConn)
        }
    }
}

## close file connection
close(fileConn)
