centralities <- read.csv("../../Java/PathwayQuery/perPathway170808/centralities.csv", stringsAsFactors=FALSE)
colnames(centralities)
nrow(centralities)

topNbetweenness <- function (n) {
    topBet <- centralities[head(order(centralities$Betweenness,
                                      decreasing=TRUE), n=n),
                           c("Protein", "Betweenness")]
    topBet
}

topNbetweenness(20)$Protein
topNbetweenness(20)

swisstonames <- read.table("../../resources/swissprot/swissHumanMapping20170823.tab",
                           stringsAsFactors=FALSE, header=TRUE, sep="\t",
                           quote="", comment="")
colnames(swisstonames)

swisstonames[swisstonames$Entry %in% topNbetweenness(20)$Protein,
             "Protein.names"]

## size of largest component in 2017 is 7196
plot(sort(centralities$Betweenness)/(7195*7194))
plot(sort(centralities$Betweenness)/(7195*7194), log='y')
sum(centralities$Betweenness/(7195*7194) > 0.01)
swisstonames[which(centralities$Betweenness/(7195*7194) > 0.01),
             "Protein.names"]
which(centralities$Betweenness/(7195*7194) > 0.05)
swisstonames[which(centralities$Betweenness/(7195*7194) > 0.05),
             "Protein.names"]



## PER PATHWAY
## "" is complete thing
path <- "../../Java/PathwayQuery/perPathway170808/"

allFiles <- list.files(path=path,
                       pattern="centralities.csv",
                       full.names=TRUE,
                       recursive=FALSE)
length(allFiles)

for (thisFile in allFiles) {
    pathName <- sub("centralities.csv", "",
                    sub(path, "",
                        thisFile))
    print(pathName)
    currentFile <- read.csv(thisFile, stringsAsFactors=FALSE)
    topBet <- currentFile[head(order(currentFile$Betweenness,
                                     decreasing=TRUE), n=20),
                          c("Protein", "Betweenness")]
    print(topBet)
}


## PER YEAR
path <- "../../Java/PathwayQuery/"

allFiles <- list.files(path=path,
                       pattern="centralities.csv",
                       full.names=TRUE,
                       recursive=FALSE)
length(allFiles)
allFiles <-
    allFiles[-which(startsWith(allFiles, paste0(path, "Out")))]
allFiles <-
    allFiles[-which(startsWith(allFiles, paste0(path, "In")))]
allFiles <-
    allFiles[-which(startsWith(allFiles, paste0(path, "cen")))]
length(allFiles)

nrowCurr <- 0
for (thisFile in allFiles) {
    pathName <- sub("centralities.csv", "",
                    sub(path, "",
                        thisFile))
    currentFile <- read.csv(thisFile, stringsAsFactors=FALSE)
    if (nrowCurr < nrow(currentFile)) {
        nrowCurr <- nrow(currentFile)
        print(pathName)
        topBet <- currentFile[head(order(currentFile$Betweenness,
                                         decreasing=TRUE), n=20),
                              c("Protein", "Betweenness")]
        print(topBet)
    }
}
