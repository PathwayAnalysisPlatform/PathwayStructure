library("Matrix")
library("RNeo4j")
graph <- startGraph("http://localhost:7474/db/data/")

library("ggplot2")
library("gridExtra")

allpathways <- unlist(cypherToList(
    graph,
    paste0("MATCH ",
           "(p:Pathway{speciesName:'Homo sapiens'})",
           "RETURN p.stId AS parent")))

## nr proteins in each pathway
nrProt <- rep(NA, length(allpathways))
## nr proteins in each pathway, incl subpathways
nrProtTot <- rep(NA, length(allpathways))
i <- 0
while (i < length(allpathways)) {
    i <- i+1
    nrProt[i] <- cypher(
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
            "<-[:hasEvent]-",
            "(p:Pathway{stId:{placeholder}})",
            " RETURN COUNT(DISTINCT re) as nrProt"),
        placeholder = allpathways[i])
    nrProtTot[i] <- cypher(
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
            "(p:Pathway{stId:{placeholder}})",
            " RETURN COUNT(DISTINCT re) as nrProt"),
        placeholder = allpathways[i])
}

nrProt <- unlist(nrProt)
sum(nrProt == 0) ## 674
sum(nrProt > 0) ## 1377


fig3a <- ggplot(data.frame(counts=nrProt[nrProt > 0]),
       aes(counts)) +
        geom_bar(fill=rgb(137, 208, 245, maxColorValue=250),
             col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_y_continuous(name = "# Pathways") +
scale_x_continuous(name = "# Proteins in the pathway") +
annotate("text", label="Immunoregulatory interactions \n between a Lymphoid \n and a non-Lymphoid cell ",
         x=480, y=20, hjust="right") +
    annotate("segment",
             x=480, y=20, xend=480, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
    annotate("text", label="A", x=0, y=80,
             fontface="bold", hjust="right", vjust = "bottom")


## number of proteins in a pathway, also indirectly
## so counting those in subpathways
nrProtTot <- unlist(nrProtTot)
summary(nrProtTot)
sum(nrProtTot == 0) ## 316
sum(nrProtTot == 1) ## 43
sum(nrProtTot > 0) ## 1735
max(nrProtTot) ## 2444
allpathways[which(nrProtTot == max(nrProtTot))]

fig3b <- ggplot(data.frame(counts=nrProtTot[nrProtTot > 0]),
                aes(counts)) +
    geom_bar(fill=rgb(137, 208, 245, maxColorValue=250),
             col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_y_continuous(name = "# Pathways") +
scale_x_continuous(name = "# Proteins in the pathway (+ sub-pathways)") +
annotate("text", label="Signal Transduction ",
         x=2444, y=20, hjust="right") +
    annotate("segment",
             x=2444, y=20, xend=2444, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
    annotate("text", label="B", x=0, y=80,
             fontface="bold", hjust="right", vjust = "bottom")


## library("gridExtra")
## grid.arrange(plotA, plotB, ncol=2)

## ggsave("FigureS3.pdf", grid.arrange(plotA, plotB, ncol=2),
##        width=10, height=5, units="in")

## ggsave("FigureS3.pdf", grid.arrange(plotA, plotB, ncol=2),
##        width=174, height=87, units="mm")

nrAllPathways <-
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
            "(p:Pathway{speciesName:'Homo sapiens'})",
            " RETURN re.identifier AS uniprot,",
            " COUNT(DISTINCT p) as nrpathways"))


pathperprotID <- unlist(lapply(nrAllPathways, function (x) x$uniprot))
pathperprot <- unlist(lapply(nrAllPathways, function (x) x$nrpathways))

pathperprot[pathperprot > 250]
pathperprotID[pathperprot > 250]

fig3c <- ggplot(data.frame(counts=pathperprot),
                aes(counts)) +
    geom_bar(fill=rgb(137, 208, 245, maxColorValue=250),
             col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_y_continuous(name = "# Proteins") +
scale_x_continuous(name = "# Pathways") +
annotate("text", label="Ubiquitin-40S ribosomal protein S27a ",
         x=286, y=900, hjust="right", vjust="bottom") +
    annotate("segment",
             x=286, y=900, xend=286, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label="Ubiquitin-40S ribosomal protein L40 ",
         x=282, y=750, hjust="right", vjust = "bottom") +
    annotate("segment",
             x=282, y=750, xend=282, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label="Polyubiquitin-B \nPolyubiquitin-C ",
         x=265, y=450, hjust="right") +
    annotate("segment",
             x=265, y=450, xend=265, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
    annotate("text", label="C", x=0, y=2250,
             fontface="bold", hjust="right", vjust = "bottom")



nrAllPathwaysDirect <-
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
            "<-[:hasEvent]-",
            "(p:Pathway{speciesName:'Homo sapiens'})",
            " RETURN re.identifier AS uniprot,",
            " COUNT(DISTINCT p) as nrpathways"))


pathperprotIDDirect <- unlist(lapply(nrAllPathwaysDirect, function (x) x$uniprot))
pathperprotDirect <- unlist(lapply(nrAllPathwaysDirect, function (x) x$nrpathways))

pathperprotDirect[pathperprotDirect > 100]
pathperprotIDDirect[pathperprotDirect > 100]

fig3d <- ggplot(data.frame(counts=pathperprotDirect),
                aes(counts)) +
    geom_bar(fill=rgb(137, 208, 245, maxColorValue=250),
             col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_y_continuous(name = "# Proteins") +
scale_x_continuous(name = "# Pathways") +
annotate("text", label="Ubiquitin-40S ribosomal protein S27a ",
         x=119, y=1250, hjust="right", vjust = "bottom") +
    annotate("segment",
             x=119, y=1250, xend=119, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label="Ubiquitin-40S ribosomal protein L40 ",
         x=116, y=1000, hjust="right", vjust = "bottom") +
    annotate("segment",
             x=116, y=1000, xend=116, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label="Polyubiquitin-B \nPolyubiquitin-C ",
         x=107, y=450, hjust="right") +
    annotate("segment",
             x=107, y=450, xend=107, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
    annotate("text", label="D", x=0, y=5000,
             fontface="bold", hjust="right", vjust = "bottom")


grid.arrange(fig3a, fig3b, fig4a, fig4b, ncol=2)

ggsave("FigureS3.pdf",
       grid.arrange(fig3a, fig3b, fig3c, fig3d, ncol=2),
       width=261, height=200, units="mm")
