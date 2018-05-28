library('ggplot2')
library("gridExtra")

library("Matrix")
library("RNeo4j")
graph <- startGraph("http://localhost:7474/db/data/")


GOdf <- read.delim("../swissprotHuman20170713.tab",
                   stringsAsFactor=FALSE)

GOlist <- list()
## split into separate terms for each protein
for (i in seq_len(nrow(GOdf))) {
    GOlist[[GOdf$Entry[i]]] <-
        unlist(
            strsplit(
                GOdf$Gene.ontology..GO.[i],
                "; "))
}

length(unlist(GOlist)) ## 244504 annotations
length(unique(unlist(GOlist))) ## 17179 terms

## count the number of occurences of each GO annotation term
ontologiesCount <- rep(0, length(unique(unlist(GOlist))))
names(ontologiesCount) <- unique(unlist(GOlist))
for (ontology in unlist(GOlist)) {
    ontologiesCount[ontology] <- ontologiesCount[ontology]+1
}

swissInReactome <-
    unique(unlist(
        cypherToList(
            graph,
            paste0(
                "MATCH ",
                "(re:ReferenceEntity{databaseName:'UniProt'})",
                " WHERE re.identifier IN {swissprot}",
                " RETURN re.identifier AS uniprot"),
            swissprot = GOdf$Entry)))

## count the number of occurences of each GO annotation term
## for the subset of proteins that are in Reactome
ontologiesCountReact <- rep(0, length(unique(unlist(GOlist))))
names(ontologiesCountReact) <- unique(unlist(GOlist))
for (ontology in unlist(GOlist[swissInReactome])) {
    ontologiesCountReact[ontology] <-
        ontologiesCountReact[ontology]+1
}

relative <- rep(0, length(ontologiesCount))
names(relative) <- names(ontologiesCount)
for (name in names(ontologiesCountReact))
    relative[name] <-
        ontologiesCountReact[name] / ontologiesCount[name]



GObiolist <- list()
## split into separate terms for each protein
for (i in seq_len(nrow(GOdf))) {
    GObiolist[[GOdf$Entry[i]]] <-
        unlist(
            strsplit(
                GOdf$Gene.ontology..biological.process.[i],
                "; "))
}
GObio <- unique(unlist(GObiolist))

GOmollist <- list()
## split into separate terms for each protein
for (i in seq_len(nrow(GOdf))) {
    GOmollist[[GOdf$Entry[i]]] <-
        unlist(
            strsplit(
                GOdf$Gene.ontology..molecular.function.[i],
                "; "))
}
GOmol <- unique(unlist(GOmollist))

group <- rep(as.character(NA), length(ontologiesCount))
for (i in seq_along(ontologiesCount)) {
    if (names(ontologiesCount)[i] %in% GObio)
        group[i] <- 'bio'
    else if (names(ontologiesCount)[i] %in% GOmol)
        group[i] <- 'mol'
    else
        group[i] <- 'cel'
}




swiss <- read.csv(file='../uniprotAnalysisSwiss2.csv',
                  header=TRUE,
                  colClasses=c(rep("character", 3),
                               "numeric",
                               rep("character", 2),
                               "POSIXct",
                               "character"),
                  stringsAsFactors=FALSE)

swiss[,"Mass"] <- as.numeric(gsub(",", "", swiss[,"Mass"]))
swiss[,"Annotation"] <- as.numeric(substr(swiss[,"Annotation"], 1, 1))
swiss[, "Evidence"] <- 0
swiss[
    swiss[, "Protein.existence"] ==
    "Evidence at protein level",
    "Evidence"] <- 5
swiss[
    swiss[, "Protein.existence"] ==
    "Evidence at transcript level",
    "Evidence"] <- 4
swiss[
    swiss[, "Protein.existence"] ==
    "Inferred from homology",
    "Evidence"] <- 3
swiss[
    swiss[, "Protein.existence"] ==
    "Predicted",
    "Evidence"] <- 2
swiss[
    swiss[, "Protein.existence"] ==
    "Uncertain",
    "Evidence"] <- 1




swissInReactome <-
    unlist(
        cypherToList(
            graph,
            paste0(
                "MATCH ",
                "(re:ReferenceEntity{databaseName:'UniProt'})",
                " WHERE re.identifier IN {swissprot}",
                " RETURN re.identifier AS uniprot"),
            swissprot = swiss$Entry))

dim(swiss[swiss$Entry %in% swissInReactome, ])
reactome <- swiss$Entry %in% swissInReactome
swiss[, "reactome"] <- reactome
swiss[, "MassR"] <- swiss[, "Mass"]
swiss[reactome, "MassR"] <- NA

onlyReactome <- swiss[reactome, ]


countScores <- function (x) {
    sapply(seq(1,5), function (y) sum(x == y))
}




fig6an <- ggplot(data.frame(occ=relative), aes(occ)) +
    geom_histogram(binwidth = 0.01,
                   fill=rgb(137, 208, 245, maxColorValue=250),
                   col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_x_continuous(name = "Occurrence in Reactome / SwissProt") +
    scale_y_continuous(name = "Frequency",
                       breaks = c(0, 2000, 4000, 6000, 8000),
                       labels = c(0, "2k", "4k", "6k", "8k"))

fig6bn <- ggplot(data.frame(go = ontologiesCount,
                          react = ontologiesCountReact,
                          group = group) ) +
    geom_point(aes(log(go), log(react), col=group)) +
    labs(col = "Ontology") +
    scale_colour_manual(values=c("bio" = "blue",
                                 "mol" = "darkgreen",
                                 "cel" = "red"),
                        labels = c('bio' = 'Biological Process',
                                   'mol' = 'Molecular Function',
                                   'cel' = 'Cellular Component')) +
    scale_x_continuous(name = "SwissProt", lim = c(-0.25, 8.5)) +
    scale_y_continuous(name = "Reactome", lim = c(-0.25, 8.5)) +
    theme(legend.position=c(0.25,.75),
          legend.background=element_rect(fill=rgb(1,1,1,0)))

fig6cn <- ggplot(data.frame(annot = rep(seq(1, 5), 2),
              db = rep(c("s", "r"), each=5),
              freq = c(countScores(swiss$Annot),
                       countScores(onlyReactome$Annot))),
       aes(annot, y = freq, fill = db)) +
    geom_col(position = "dodge") +
    labs(fill = "Source") +
    scale_fill_manual(values=c("s" = rgb(137, 208, 245,
                                         maxColorValue=250),
                               "r" = "black"),
                      labels = c('s' = 'SwissProt',
                                 'r' = 'Reactome'),
                      drop=FALSE) +
    theme(legend.position=c(0.17,.75),
          legend.background=element_rect(fill=rgb(1,1,1,0))) +
    xlab("Annotation Score") +
    scale_y_continuous(name = "# Proteins",
                       breaks = c(0, 4000, 8000, 12000),
                       labels = c(0, "4k", "8k", "12k"))


fig6dn <- ggplot(data.frame(annot = rep(seq(1, 5), 2),
              db = rep(c("s", "r"), each=5),
              freq = c(countScores(swiss$Evid),
                       countScores(onlyReactome$Evid))),
       aes(annot, y = freq, fill = db)) +
    geom_col(position = "dodge") +
    labs(fill = "Source") +
    scale_fill_manual(values=c("s" = rgb(137, 208, 245,
                                         maxColorValue=250),
                               "r" = "black"),
                      labels = c('s' = 'SwissProt',
                                 'r' = 'Reactome'),
                      drop=FALSE) +
    theme(legend.position=c(0.17,.75),
          legend.background=element_rect(fill=rgb(1,1,1,0))) +
    xlab("Evidence Score") +
    scale_y_continuous(name = "# Proteins",
                       breaks = c(0, 5000, 10000, 15000),
                       labels = c(0, "5k", "10k", "15k"))

grid.arrange(fig6an, fig6bn, fig6cn, fig6dn, ncol=2)


ggsave("FigureS6a.pdf", fig6an,
       width=85, height=85, units="mm")
ggsave("FigureS6b.pdf", fig6bn,
       width=85, height=85, units="mm")
ggsave("FigureS6c.pdf", fig6cn,
       width=85, height=85, units="mm")
ggsave("FigureS6d.pdf", fig6dn,
       width=85, height=85, units="mm")
