path <- "../../../Java/PathwayQuery/"

years <- 1934:2017
for (year in years) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    pdf(paste0(year, "RadInt.pdf"), width=7, height=7)
    plot(centralities$Radiality, centralities$Integration,
         main = "Radiality vs Integration",
         ylab = "Radiality", xlab = "Integration",
         ylim = c(0, 1),
         xlim = c(0, 1))
    abline(0,1,col='lightgray')
    dev.off()
}


pdf(paste0("AllYears", "RadInt.pdf"), width=7, height=7)
for (year in years) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    plot(centralities$Radiality, centralities$Integration,
         main = year,
         ylab = "Radiality", xlab = "Integration",
         ylim = c(0, 1),
         xlim = c(0, 1))
    abline(0,1,col='lightgray')
}
dev.off()

pdf(paste0("RadIntAnnot.pdf"), width=10, height=5)
par(mfrow=c(1,2))
for (year in c(1985, 2017)) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    plot(centralities$Radiality, centralities$Integration,
         main = year, type='n',
         ylab = "Radiality", xlab = "Integration",
         ylim = c(0, 1),
         xlim = c(0, 1))
    abline(0,1,col='lightgray')
    points(centralities$Radiality, centralities$Integration,
         pch = ifelse(centralities$Radiality < 0.1 &
                      centralities$Integration < 0.1,
                      5,
               ifelse(centralities$Radiality < 0.1,
                      2,
               ifelse(centralities$Integration < 0.1,
                      6,
                      1))),
         col = ifelse(centralities$Radiality < 0.1 &
                      centralities$Integration < 0.1,
                      'red',
               ifelse(centralities$Radiality < 0.1,
                      'blue',
               ifelse(centralities$Integration < 0.1,
                      'purple',
                      'darkgreen'))))
}
dev.off()


## only visible jitter in early years
pdf(paste0("AllYears", "RadIntJitter.pdf"), width=7, height=7)
for (year in years) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    plot(jitter(centralities$Radiality),
         jitter(centralities$Integration),
         main = year,
         ylab = "Radiality", xlab = "Integration",
         ylim = c(0, 1),
         xlim = c(0, 1))
    abline(0,1,col='lightgray')
}
dev.off()


centralities2017 <-
    read.csv(paste0(path, 2017, "centralities.csv"),
             stringsAsFactors=FALSE)

## isolated
length(centralities2017[centralities2017$Radiality < 0.2 &
                 centralities2017$Integration < 0.2, "Protein"])
## 399
isolated <- centralities2017[centralities2017$Radiality < 0.2 &
                             centralities2017$Integration < 0.2, "Protein"]

## start of chain proteins
length(centralities2017[centralities2017$Radiality > 0.2 &
                 centralities2017$Integration < 0.2, "Protein"])
## 766
socp <- centralities2017[centralities2017$Radiality > 0.2 &
                         centralities2017$Integration < 0.2, "Protein"]

## end of chain proteins
length(centralities2017[centralities2017$Radiality < 0.2 &
                 centralities2017$Integration > 0.2, "Protein"])
## 385
eocp <- centralities2017[centralities2017$Radiality < 0.2 &
                         centralities2017$Integration > 0.2, "Protein"]

## main cluster
length(centralities2017[centralities2017$Radiality > 0.2 &
                        centralities2017$Integration > 0.2, "Protein"])
## 5998
maincluster <- centralities2017[centralities2017$Radiality > 0.2 &
                                centralities2017$Integration > 0.2, "Protein"]



swissprot <- read.table("../../../resources/swissprot/swissHumanMapping20170823.tab",
                        header=TRUE,sep="\t",quote="",
                        comment.char="",
                        stringsAsFactors=FALSE)



write.csv(
    data.frame(
        uniprotID =
            swissprot[swissprot$Entry %in% socp, "Entry"],
        proteinName =
            swissprot[swissprot$Entry %in% socp, "Protein.names"],
        radiality =
            centralities2017[centralities2017$Protein %in% socp, "Radiality"],
        integration =
            centralities2017[centralities2017$Protein %in% socp, "Integration"]),
    file = "startofchainproteins.csv", row.names=FALSE)

write.csv(
    data.frame(
        uniprotID =
            swissprot[swissprot$Entry %in% eocp, "Entry"],
        proteinName =
            swissprot[swissprot$Entry %in% eocp, "Protein.names"],
        radiality =
            centralities2017[centralities2017$Protein %in% eocp, "Radiality"],
        integration =
            centralities2017[centralities2017$Protein %in% eocp, "Integration"]),
    file = "endofchainproteins.csv", row.names=FALSE)

write.csv(
    data.frame(
        uniprotID =
            swissprot[swissprot$Entry %in% isolated, "Entry"],
        proteinName =
            swissprot[swissprot$Entry %in% isolated, "Protein.names"],
        radiality =
            centralities2017[centralities2017$Protein %in% isolated, "Radiality"],
        integration =
            centralities2017[centralities2017$Protein %in% isolated, "Integration"]),
    file = "isolatedproteins.csv", row.names=FALSE)

write.csv(
    data.frame(
        uniprotID =
            swissprot[swissprot$Entry %in% maincluster, "Entry"],
        proteinName =
            swissprot[swissprot$Entry %in% maincluster, "Protein.names"],
        radiality =
            centralities2017[centralities2017$Protein %in% maincluster, "Radiality"],
        integration =
            centralities2017[centralities2017$Protein %in% maincluster, "Integration"]),
    file = "mainclusterproteins.csv", row.names=FALSE)
