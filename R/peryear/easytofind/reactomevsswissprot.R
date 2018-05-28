path <- "../../../Java/PathwayQuery/"
nprotPerYear <- rep(NA, length(1934:2017))
protPerYear <- list()
cat("   ")
for (year in 1934:2017) {
    cat(paste0("\b\b\b\b", year))
    inFile <- paste0(path, year, "centralities.csv")
    protPerYear[[year]] <- read.csv(inFile, stringsAsFactors=FALSE)$Protein
    nprotPerYear[year-1933] <- length(protPerYear[[year]])
}

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


pdf("annotationSwiss.pdf", height=4, width=4)
barplot(
    matrix(c(unname(c(0, table(evidence[evidence$Entry %in% protPerYear[[2017]],
                                      "Annotation"]))),
           unname(table(evidence$Annotation))),
           nrow=2, byrow=TRUE),
    beside=TRUE, col=c("red", "black"), axes=FALSE,
    xlab="Annotation score", ylab="# Proteins")
axis(1, at=c(2, 5, 8, 11, 14), labels=seq(1, 5))
axis(2, at=c(0, 6000, 12000))
dev.off()

pdf("annotationSwiss2.pdf", height=4, width=4)
par(mar=c(4.1, 4.1, 0.5, 0))
barplot(
    matrix(c(unname(c(0, table(evidence[evidence$Entry %in% protPerYear[[2017]],
                                      "Annotation"]))),
           unname(table(evidence$Annotation))),
           nrow=2, byrow=TRUE),
    beside=TRUE, col=c("red", "black"), axes=FALSE,
    xlab="Annotation score", ylab="# Proteins")
axis(1, at=c(2, 5, 8, 11, 14), labels=seq(1, 5))
axis(2, at=c(0, 6000, 12000))
dev.off()

pdf("annotationSwiss2legend.pdf", height=4, width=4)
par(mar=c(4.1, 4.1, 0.5, 0), lend='square')
barplot(
    matrix(c(unname(c(0, table(evidence[evidence$Entry %in% protPerYear[[2017]],
                                      "Annotation"]))),
           unname(table(evidence$Annotation))),
           nrow=2, byrow=TRUE),
    beside=TRUE, col=c("red", "black"), axes=FALSE,
    xlab="Annotation score", ylab="# Proteins")
axis(1, at=c(2, 5, 8, 11, 14), labels=seq(1, 5))
axis(2, at=c(0, 6000, 12000))
legend('topleft',
       legend=c("SwissProt", "Reactome"),
       col=c("black", "red"), lty=1, lwd=5)
dev.off()


pdf("annotationSwiss3.pdf", height=3, width=3)
par(mar=c(4.1, 4.1, 0.75, 0))
barplot(
    matrix(c(unname(c(0, table(evidence[evidence$Entry %in% protPerYear[[2017]],
                                      "Annotation"]))),
           unname(table(evidence$Annotation))),
           nrow=2, byrow=TRUE),
    beside=TRUE, col=c("red", "black"), axes=FALSE,
    xlab="Annotation score", ylab="# Proteins")
axis(1, at=c(2, 5, 8, 11, 14), labels=seq(1, 5))
axis(2, at=c(0, 6000, 12000))
dev.off()

pdf("annotationSwiss3legend.pdf", height=3, width=3)
par(mar=c(4.1, 4.1, 0.75, 0), lend='square')
barplot(
    matrix(c(unname(c(0, table(evidence[evidence$Entry %in% protPerYear[[2017]],
                                      "Annotation"]))),
           unname(table(evidence$Annotation))),
           nrow=2, byrow=TRUE),
    beside=TRUE, col=c("red", "black"), axes=FALSE,
    xlab="Annotation score", ylab="# Proteins")
axis(1, at=c(2, 5, 8, 11, 14), labels=seq(1, 5))
axis(2, at=c(0, 6000, 12000))
legend('topleft',
       legend=c("SwissProt", "Reactome"),
       col=c("black", "red"), lty=1, lwd=5)
dev.off()
