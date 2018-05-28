library("Matrix")
library("RNeo4j")
graph <- startGraph("http://localhost:7474/db/data/")

library("ggplot2")

## how many top level pathways does a protein belong to?
nrTopPathways <-
    cypherToList(
        graph,
        paste0(
            "MATCH ",
            "(re:ReferenceEntity{databaseName:'UniProt'})",
            "<-[:referenceEntity]-",
            "(ewas:EntityWithAccessionedSequence{speciesName:'Homo sapiens'})",
            "<-[:input|output|",
            "catalystActivity|physicalEntity|",
            "regulatedBy|regulator|",
            "hasMember|hasCandidate|hasComponent*]-",
            "(:Reaction{speciesName:'Homo sapiens'})",
            "<-[:hasEvent*]-",
            "(p:TopLevelPathway{speciesName:'Homo sapiens'})",
            " RETURN re.identifier AS uniprot,",
            " COUNT(DISTINCT p) as nrpathways"))

tlpathperprot <- unlist(lapply(nrTopPathways, function (x) x$nrpathways))
tlpathperprotID <- unlist(lapply(nrTopPathways, function (x) x$uniprot))
max(tlpathperprot)
min(tlpathperprot)

df <- data.frame(nrPathways = tlpathperprot,
                  protein = tlpathperprotID)

fig4b <- ggplot(df, aes(nrPathways)) +
    geom_bar(fill=rgb(137, 208, 245, maxColorValue=250),
             col=rgb(137/2, 208/2, 245/2, maxColorValue=250)) +
    coord_flip() +
    scale_y_log10(name = "# Proteins") +
scale_x_continuous(name = "# Pathways") +
    annotate("text", label=paste("Ubiquitin-40S ribosomal protein S27a",
                                 "Ubiquitin-40S ribosomal protein L40",
                                 sep='\n'),
             x=14, y=1000, hjust="left", vjust = "bottom", angle=270) +
    annotate("segment",
             x=14, y=1000, xend=14, yend=3, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
    annotate("text", label=paste("Polyubiquitin-B",
                                 "Polyubiquitin-C",
                                 sep='\n'),
             x=13, y=100, hjust="left", vjust = "bottom", angle=270) +
    annotate("segment",
             x=13, y=100, xend=13, yend=3, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
        theme(axis.title.y = element_text(angle = 270))

ggsave("Figure4b.pdf", fig4b, height=18, width=7.5, units="cm")

for (i in which(tlpathperprot == max(tlpathperprot))) {
    print(sprintf("%-15s",
                  nrTopPathways[[i]]$uniprot))
}
## P62979
## P62987

## 13 or 14 TLP
for (i in which(tlpathperprot >= tail(sort(tlpathperprot), n=4)[1])) {
    print(sprintf("%-15s",
                  nrTopPathways[[i]]$uniprot))
}
## P0CG48
## P0CG47
