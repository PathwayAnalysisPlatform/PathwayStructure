## make connection to graph db
library("Matrix")
library("RNeo4j")
graph <- startGraph("http://localhost:7474/db/data")

accessions <- as.character(
    read.table("uniprot-all.list")$V1)
head(accessions)

##  ><>< nr.found
## how many times is each accession number for uniprot in reactome
nr.found <- unlist(lapply(accessions, function (acc) {
    unname(
        cypher(
            graph,
            paste("MATCH (re:ReferenceEntity{identifier:{a}})",
                  "RETURN COUNT(re)"),
            a = acc))
}))

summary(nr.found)

i <- 11
while (i > 0) {
    i <- i - 1
    writeLines(paste(i, sum(nr.found == i)))
}
## 10 1
## 9 0
## 8 1
## 7 0
## 6 2
## 5 4
## 4 7
## 3 43
## 2 91
## 1 9962
## 0 10018

## drop the uniprot IDs that are not in reactome
accessions.r <- accessions[nr.found > 0]
length(accessions)                      # 20129
length(accessions.r)                    # 10111

head(accessions.r)
write.table(accessions.r,
            file = "~/Documents/pathways/uniprot-reactome.list",
            row.names = FALSE,
            col.names = FALSE)
accessions.r <- as.character(
    read.table("~/Documents/pathways/uniprot-reactome.list")$V1)



##  ><>< nr.ents
## how many EntityWithAccessionedSequence things are there
## for each uniprot ID
nr.ents <- unlist(lapply(accessions.r, function (acc) {
    unname(
        cypher(
            graph,
            paste("MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(:ReferenceEntity{identifier:{a}})",
                  "RETURN COUNT(ewas)"),
            a = acc))
}))

summary(nr.ents)

i <- 12
while (i > 0) {
    i <- i - 1
    if (i > 10)
        writeLines(paste(i, sum(nr.ents >= i)))
    else
        writeLines(paste(i, sum(nr.ents == i)))
}

## 11 178
## 10 23
## 9 79
## 8 54
## 7 140
## 6 124
## 5 181
## 4 330
## 3 638
## 2 1436
## 1 6920
## 0 8
