path <- "../../../Java/PathwayQuery/"
dPerYear <- rep(NA, length(1934:2017))
nverticesPerYear <- rep(NA, length(1934:2017))
for (year in 1934:2017) {
    print(year)
    inFile <- paste0(path, year, "lengthShortestPaths.csv")
    dPerYear[year-1933] <- max(as.matrix(read.csv(inFile, row.names=1)))
    nverticesPerYear[year-1933] <-
        sum(as.matrix(read.csv(inFile, row.names=1)) == 1)
}

nprotPerYear <- rep(NA, length(1934:2017))
protPerYear <- list()
cat("   ")
for (year in 1934:2017) {
    cat(paste0("\b\b\b\b", year))
    inFile <- paste0(path, year, "centralities.csv")
    protPerYear[[year]] <- read.csv(inFile, stringsAsFactors=FALSE)$Protein
    nprotPerYear[year-1933] <- length(protPerYear[[year]])
}

protPerYear[[1934]]

## get the list of all proteins and their scores
evidence <-
    read.table("uniprot-all.20171018.tab",
               header = TRUE, na.strings="",
               sep="\t", quote="", comment.char="",
               stringsAsFactors=FALSE)[, c("Entry",
                                           "Annotation",
                                           "Protein.existence")]
evidence$Annotation <- as.integer(substr(evidence$Annotation, 1, 1))
evidence$Protein.existence <-
    sapply(evidence$Protein.existence, function (x) {
        if (x == "Evidence at protein level") {
            return (5)
        } else if (x == "Evidence at transcript level") {
            return (4)
        } else if (x == "Inferred from homology") {
            return (3)
        } else if (x == "Predicted") {
            return (2)
        } else {
            return (1)
        }
    })

annotPY <- data.frame(year = numeric(), A1 = numeric(),
                      A2 = numeric(), A3 = numeric(),
                      A4 = numeric(), A5 = numeric())
evidPY <- data.frame(year = numeric(), E1 = numeric(),
                     E2 = numeric(), E3 = numeric(),
                     E4 = numeric(), E5 = numeric())
for (year in 1934:2017) {
    annotPY[nrow(annotPY)+1, ] <-
        c(year, sapply(seq(1,5), function (x) {
            sum(evidence$Annotation[
                             evidence$Entry %in%
                             protPerYear[[year]]] == x) }) )
    evidPY[nrow(evidPY)+1, ] <-
        c(year, sapply(seq(1,5), function (x) {
            sum(evidence$Protein.existence[
                             evidence$Entry %in%
                             protPerYear[[year]]] == x) }) )
}
annotPY$nProt <- rowSums(annotPY) - annotPY$year
evidPY$nProt <- rowSums(evidPY) - evidPY$year

head(annotPY)
head(evidPY)

pdf("AnnotEvidPerYear.pdf", width=14, height=7)
par(mfrow=c(1,2))
rain5 <- rainbow(5)
plot(NULL, xlim=c(1930, 2020), ylim=c(0,1),
     xlab="Year", ylab="Annotation score distribution",
     main = "Distribution of annotation scores per year")
with(annotPY, lines(year, A1/nProt, type='l', col=rain5[1]))
with(annotPY, lines(year, A2/nProt, type='l', col=rain5[2]))
with(annotPY, lines(year, A3/nProt, type='l', col=rain5[3]))
with(annotPY, lines(year, A4/nProt, type='l', col=rain5[4]))
with(annotPY, lines(year, A5/nProt, type='l', col=rain5[5]))
legend('bottomleft', legend=c("Best", "", "", "", "Worst"),
       lty=1, col=rev(rain5))

rain5 <- rainbow(5)
plot(NULL, xlim=c(1930, 2020), ylim=c(0,1),
     xlab="Year", ylab="Evidence level distribution",
     main = "Distribution of evidence levels per year")
with(evidPY, lines(year, E1/nProt, type='l', col=rain5[1]))
with(evidPY, lines(year, E2/nProt, type='l', col=rain5[2]))
with(evidPY, lines(year, E3/nProt, type='l', col=rain5[3]))
with(evidPY, lines(year, E4/nProt, type='l', col=rain5[4]))
with(evidPY, lines(year, E5/nProt, type='l', col=rain5[5]))
legend('bottomleft', legend=c("Best", "", "", "", "Worst"),
       lty=1, col=rev(rain5))
dev.off()

pdf('AnnotationPerYear.pdf')
rain5 <- rainbow(5)
plot(NULL, xlim=c(1930, 2020), ylim=c(0,1),
     xlab="Year", ylab="Annotation score distribution",
     main = "Distribution of annotation scores per year")
with(annotPY, lines(year, A1/nProt, type='l', col=rain5[1]))
with(annotPY, lines(year, A2/nProt, type='l', col=rain5[2]))
with(annotPY, lines(year, A3/nProt, type='l', col=rain5[3]))
with(annotPY, lines(year, A4/nProt, type='l', col=rain5[4]))
with(annotPY, lines(year, A5/nProt, type='l', col=rain5[5]))
legend('bottomleft', legend=c("Best", "", "", "", "Worst"),
       lty=1, col=rev(rain5))
dev.off()
pdf('EvidencePerYear.pdf')
rain5 <- rainbow(5)
plot(NULL, xlim=c(1930, 2020), ylim=c(0,1),
     xlab="Year", ylab="Evidence level distribution",
     main = "Distribution of evidence levels per year")
with(evidPY, lines(year, E1/nProt, type='l', col=rain5[1]))
with(evidPY, lines(year, E2/nProt, type='l', col=rain5[2]))
with(evidPY, lines(year, E3/nProt, type='l', col=rain5[3]))
with(evidPY, lines(year, E4/nProt, type='l', col=rain5[4]))
with(evidPY, lines(year, E5/nProt, type='l', col=rain5[5]))
legend('bottomleft', legend=c("Best", "", "", "", "Worst"),
       lty=1, col=rev(rain5))
dev.off()



pdf('annotationLarge.pdf', height=4, width=4)
rain5 <- rainbow(5)
plot(NULL, xlim=c(1930, 2020), ylim=c(0,1),
     ## ylab="Annotation score distribution",
     ylab="% Proteins w/ annotation",
     xlab="Year")
with(annotPY, lines(year, A1/nProt, type='l', col=rain5[1]))
with(annotPY, lines(year, A2/nProt, type='l', col=rain5[2]))
with(annotPY, lines(year, A3/nProt, type='l', col=rain5[3]))
with(annotPY, lines(year, A4/nProt, type='l', col=rain5[4]))
with(annotPY, lines(year, A5/nProt, type='l', col=rain5[5]))
text(2010, .9, "Best", col=rain5[5])
text(2010, .05, "Worst", col=rain5[1])
## legend('bottomleft', legend=c("Best", "", "", "", "Worst"),
##        lty=1, col=rev(rain5))
dev.off()


pdf('annotationLarge2.pdf', height=4, width=4)
par(mar=c(4.1, 4.1, 0.5, 0.5), lend='square')
rain5 <- rainbow(5)
plot(NULL, xlim=c(1930, 2020), ylim=c(0,1),
     ## ylab="Annotation score distribution",
     axes=FALSE,
     ylab="% Proteins w/ annotation",
     xlab="Year")
with(annotPY, lines(year, A1/nProt, type='l', col=rain5[1]))
with(annotPY, lines(year, A2/nProt, type='l', col=rain5[2]))
with(annotPY, lines(year, A3/nProt, type='l', col=rain5[3]))
with(annotPY, lines(year, A4/nProt, type='l', col=rain5[4]))
with(annotPY, lines(year, A5/nProt, type='l', col=rain5[5]))
text(2010, .9, "Best", col=rain5[5])
text(2010, .05, "Worst", col=rain5[1])
axis(1, at = c(1930, 1960, 1980, 2000, 2020))
axis(2, at = seq(0,1, 0.2))
## legend('bottomleft', legend=c("Best", "", "", "", "Worst"),
##        lty=1, col=rev(rain5))
dev.off()

pdf('annotationLarge3.pdf', height=3, width=3)
par(mar=c(4.1, 4.1, 0.5, 0.75), lend='square')
rain5 <- rainbow(5)
plot(NULL, xlim=c(1930, 2020), ylim=c(0,1),
     ## ylab="Annotation score distribution",
     axes=FALSE,
     ylab="% Proteins w/ annotation",
     xlab="Year")
with(annotPY, lines(year, A1/nProt, type='l', col=rain5[1]))
with(annotPY, lines(year, A2/nProt, type='l', col=rain5[2]))
with(annotPY, lines(year, A3/nProt, type='l', col=rain5[3]))
with(annotPY, lines(year, A4/nProt, type='l', col=rain5[4]))
with(annotPY, lines(year, A5/nProt, type='l', col=rain5[5]))
text(2010, .9, "Best", col=rain5[5])
text(2010, .05, "Worst", col=rain5[1])
## axis(1, at = c(1930, 1960, 1980, 2000, 2020))
axis(1, at = c(1930, 1960, 1990, 2020))
axis(2, at = seq(0,1, 0.2))
## legend('bottomleft', legend=c("Best", "", "", "", "Worst"),
##        lty=1, col=rev(rain5))
dev.off()






pathPathway <- "../../../Java/PathwayQuery/perPathway170808/"

allFiles <- list.files(path=pathPathway,
                       pattern="centralities.csv",
                       full.names=TRUE,
                       recursive=FALSE)
length(allFiles)
protPerPath <- list()
for (thisFile in allFiles) {
    pathName <- sub("centralities.csv", "",
                    sub(pathPathway, "",
                        thisFile))
    if (nchar(pathName) == 0) {
        pathName <- "All"
    }
    print(pathName)
    protPerPath[[pathName]] <-
        read.csv(thisFile, stringsAsFactors=FALSE)$Protein
}

aepathyear <- data.frame(year = numeric(), pathway = as.character(),
                         A1 = numeric(), A2 = numeric(),
                         A3 = numeric(), A4 = numeric(),
                         A5 = numeric(), E1 = numeric(),
                         E2 = numeric(), E3 = numeric(),
                         E4 = numeric(), E5 = numeric(),
                         stringsAsFactors=FALSE)
for (pathway in names(protPerPath)) {
    for (year in 1934:2017) {
        aepathyear[nrow(aepathyear)+1, ] <-
            c(year, pathway,
              sapply(seq(1,5), function (x) {
                  sum(evidence$Annotation[
                                   evidence$Entry %in%
                                   protPerYear[[year]] &
                               evidence$Entry %in%
                                   protPerPath[[pathway]]] == x) }),
              sapply(seq(1,5), function (x) {
                  sum(evidence$Protein.existence[
                                   evidence$Entry %in%
                                   protPerYear[[year]] &
                                   evidence$Entry %in%
                                   protPerPath[[pathway]]] == x) }) )
    }
}
aepathyear[, -2] <- sapply(aepathyear[, -2], as.integer)
aepathyear$A <- rowSums(aepathyear[, c("A1", "A2", "A3", "A4", "A5")])
aepathyear$E <- rowSums(aepathyear[, c("E1", "E2", "E3", "E4", "E5")])
head(aepathyear)
nrow(aepathyear)

length(allFiles)
par(mfrow=c(5, 5))
for (pathway in unique(aepathyear$pathway)) {
    barplot(as.matrix(aepathyear[aepathyear$year == 2017 &
                                 aepathyear$pathway == pathway,
                                 c("A1", "A2", "A3", "A4", "A5")]),
            main=pathway)
}

pdf('AnnotationPerYearPerPathway.pdf', height=14, width=14)
par(mfrow=c(5,5))
for (pathway in unique(aepathyear$pathway)) {
    plot(NULL,
         xlim=c(1930, 2020),
         ylim=c(0,aepathyear[aepathyear$pathway == pathway &
                           aepathyear$year == 2017, "A"]),
         xlab="Year", ylab="#",
         main=pathway)
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A1, type='l', col=rain5[1]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A2, type='l', col=rain5[2]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A3, type='l', col=rain5[3]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A4, type='l', col=rain5[4]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A5, type='l', col=rain5[5]))
}
dev.off()

pdf('EvidencePerYearPerPathway.pdf', height=14, width=14)
par(mfrow=c(5,5))
for (pathway in unique(aepathyear$pathway)) {
    plot(NULL,
         xlim=c(1930, 2020),
         ylim=c(0,aepathyear[aepathyear$pathway == pathway &
                           aepathyear$year == 2017, "E"]),
         xlab="Year", ylab="#",
         main=pathway)
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E1, type='l', col=rain5[1]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E2, type='l', col=rain5[2]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E3, type='l', col=rain5[3]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E4, type='l', col=rain5[4]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E5, type='l', col=rain5[5]))
}
dev.off()




pdf('AnnotationPerYearPerPathwayFraction.pdf', height=14, width=14)
par(mfrow=c(5,5))
for (pathway in unique(aepathyear$pathway)) {
    plot(NULL,
         xlim=c(1930, 2020),
         ylim=c(0,1),
         xlab="Year", ylab="#",
         main=pathway)
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A1/A, type='l', col=rain5[1]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A2/A, type='l', col=rain5[2]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A3/A, type='l', col=rain5[3]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A4/A, type='l', col=rain5[4]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, A5/A, type='l', col=rain5[5]))
}
dev.off()

pdf('EvidencePerYearPerPathway.pdf', height=14, width=14)
par(mfrow=c(5,5))
for (pathway in unique(aepathyear$pathway)) {
    plot(NULL,
         xlim=c(1930, 2020),
         ylim=c(0,1),
         xlab="Year", ylab="#",
         main=pathway)
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E1/E, type='l', col=rain5[1]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E2/E, type='l', col=rain5[2]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E3/E, type='l', col=rain5[3]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E4/E, type='l', col=rain5[4]))
    with(aepathyear[aepathyear$pathway == pathway, ],
         lines(year, E5/E, type='l', col=rain5[5]))
}
dev.off()

