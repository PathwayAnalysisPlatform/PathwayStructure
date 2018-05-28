library("Matrix")
library("RNeo4j")
graph <- startGraph("http://localhost:7474/db/data/")

allpathways <- unlist(cypherToList(
    graph,
    paste0("MATCH ",
           "(p:Pathway{speciesName:'Homo sapiens'})",
           "RETURN p.stId AS parent")))

## nr subpathways (children) per pathway
nrSubPathTot <- rep(NA, length(allpathways))
i <- 0
while (i < length(allpathways)) {
    i <- i+1
    nrSubPathTot[i] <- cypher(
        graph,
        paste0("MATCH ",
               "(p:Pathway{stId:{placeholder}})",
               "-[h:hasEvent*]->",
               "(p2:Pathway{speciesName:'Homo sapiens'})",
               "RETURN COUNT(DISTINCT p2.stId) AS child"),
        placeholder = allpathways[i])
}
nrSubPathTot <- unlist(nrSubPathTot)

sum(nrSubPathTot == 0) ## 1397
sum(nrSubPathTot > 0) ## 654
sum(nrSubPathTot > 100) ## 9
sum(nrSubPathTot > 200) ## 3

cbind(allpathways[nrSubPathTot > 100],
      nrSubPathTot[nrSubPathTot > 100])
cbind(
    cypher(graph,
           paste0("MATCH (p:Pathway)",
                  " WHERE p.stId IN {placeholder}",
                  " RETURN p.displayName"),
           placeholder = allpathways[nrSubPathTot > 100]),
    cypher(graph,
           paste0("MATCH (p:Pathway)",
                  " WHERE p.stId IN {placeholder}",
                  " RETURN p.stId"),
           placeholder = allpathways[nrSubPathTot > 100]))

##                           p.displayName        p.stId nrSub
## 1                   Signal Transduction  R-HSA-162582   327
## 2                         Immune System  R-HSA-168256   176
## 3                Metabolism of proteins  R-HSA-392499   104
## 4                            Metabolism R-HSA-1430728   308
## 5                  Innate Immune System  R-HSA-168249   117
## 6                            Cell Cycle R-HSA-1640170   130
## 7 Metabolism of lipids and lipoproteins  R-HSA-556833   107
## 8                       Gene Expression   R-HSA-74160   151
## 9                               Disease R-HSA-1643685   443

library("ggplot2")

ggplot(data.frame(counts=nrSubPathTot[nrSubPathTot != 0]),
       aes(counts)) +
    ## geom_bar(fill=rgb(137, 208, 245, maxColorValue=250),
    ##          col=rgb(137/2, 208/2, 245/2, maxColorValue=250)) +
    geom_bar(fill=rgb(137, 208, 245, maxColorValue=250),
             col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_y_continuous(name = "# Pathways") +
scale_x_continuous(name = "# Sub-pathways") +
annotate("text", label="Immune System ",
         x=443, y=15, hjust="right") +
    annotate("segment",
             x=443, y=15, xend=443, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label=" Disease",
         x=327, y=25, hjust="left") +
    annotate("segment",
             x=327, y=25, xend=327, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label=" Cell Cycle",
         x=308, y=35, hjust="left") +
    annotate("segment",
             x=308, y=35, xend=308, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label=" Metabolism",
         x=176, y=45, hjust="left") +
    annotate("segment",
             x=176, y=45, xend=176, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label=" Metabolism of proteins",
         x=151, y=55, hjust="left") +
    annotate("segment",
             x=151, y=55, xend=151, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label=" Signal Transduction",
         x=130, y=65, hjust="left") +
    annotate("segment",
             x=130, y=65, xend=130, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label=" Innate Immune System",
         x=117, y=75, hjust="left") +
    annotate("segment",
             x=117, y=75, xend=117, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label=" Metabolism of Lipids and Lipoproteins",
         x=107, y=85, hjust="left") +
    annotate("segment",
             x=107, y=85, xend=107, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label=" Gene Expression",
         x=104, y=95, hjust="left") +
    annotate("segment",
             x=104, y=95, xend=104, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm")))

ggsave("FigureS2.pdf", width=174, height=100, units="mm")
