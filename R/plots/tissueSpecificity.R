# 
# This script takes tissue specific information and plots the effect on reactome.
#
startTimeAll <- proc.time()



## Libraries

library(igraph)
library(dplyr)
library(ggplot2)


## Parameters



## Main script


# Load protein abundances

print(paste(Sys.time(), " Loading protein abundances", sep = ""))

rnaAbundance <- read.table("resources/tissues/RNA.gz", header = T, stringsAsFactors = F, sep = "\t", quote = "", comment.char = "")
proteinAbundance <- read.table("resources/tissues/Protein_MS.gz", header = T, stringsAsFactors = F, sep = "\t", quote = "", comment.char = "")
proteinAbundance <- proteinAbundance[proteinAbundance$intensity_average > 0, ]

rnaTissues <- as.factor(rnaAbundance$tissue)
proteinTissues <- as.factor(proteinAbundance$tissue)

rnaTissuesTable <- table(rnaTissues)
proteinTissuesTable <- table(proteinTissues)

rnaTissuesDataFrame <- data.frame(rnaTissuesTable)
proteinTissuesDataFrame <- data.frame(proteinTissuesTable)

write.table(rnaTissuesDataFrame, "resources/tissues/rnaTissues.txt", sep = "\t", col.names = T, row.names = F, quote = F)
write.table(proteinTissuesDataFrame, "resources/tissues/proteinTissues.txt", sep = "\t", col.names = T, row.names = F, quote = F)

tissuesMatched <- read.table("resources/tissues/matched.txt", header = T, sep = "\t", quote = "", stringsAsFactors = F)

rnaAbundanceMatched <- rnaAbundance[rnaAbundance$tissue %in% tissuesMatched$rnaTissues, ]
proteinAbundanceMatched <- proteinAbundance[proteinAbundance$tissue %in% tissuesMatched$proteinTissues, ]

rnaAbundanceMatched <- merge(rnaAbundanceMatched, tissuesMatched, by.x = "tissue", by.y = "rnaTissues", all.x = T, all.y = F)


# Plot rna and protein

print(paste(Sys.time(), " Plotting RNA and protein relative abundances", sep = ""))

rnaMerge <- rnaAbundanceMatched[, c("proteinTissues", "accession", "name", "geneNames", "tpm")]
names(rnaMerge) <- c("tissue", "accession", "name", "geneNames", "tpm")
rnaMerge$key <- paste(rnaMerge$tissue, rnaMerge$accession)

proteinMerge <- proteinAbundanceMatched[, c("category", "tissue", "accession", "name", "intensity_average")]
proteinMerge$key <- paste(proteinMerge$tissue, proteinMerge$accession)

abundanceMerge <- merge(rnaMerge, proteinMerge, by = "key", all = T)

abundanceMerge$tissue <- ifelse(is.na(abundanceMerge$tissue.x), abundanceMerge$tissue.y, abundanceMerge$tissue.x)
abundanceMerge$tissueFactor <- as.factor(abundanceMerge$tissue)
abundanceMerge$tissueindex <- as.numeric(abundanceMerge$tissueFactor)
abundanceMerge$accession <- ifelse(is.na(abundanceMerge$accession.x), abundanceMerge$accession.y, abundanceMerge$accession.x)
abundanceMerge$name <- ifelse(is.na(abundanceMerge$name.x), abundanceMerge$name.y, abundanceMerge$name.x)

abundanceMerge2 <- abundanceMerge[, c("tissue", "tissueFactor", "tissueindex", "accession", "name", "tpm", "intensity_average")]

abundanceCommon <- abundanceMerge2[!is.na(abundanceMerge2$tpm) & !is.na(abundanceMerge2$intensity_average), ]
abundanceRnaOnly <- abundanceMerge2[!is.na(abundanceMerge2$tpm) & is.na(abundanceMerge2$intensity_average), ]
abundanceProteinOnly <- abundanceMerge2[is.na(abundanceMerge2$tpm) & !is.na(abundanceMerge2$intensity_average), ]

abundanceCommon$rna <- log10(abundanceCommon$tpm)
abundanceCommon$protein <- abundanceCommon$intensity_average

abundanceRnaOnly$rna <- log10(abundanceRnaOnly$tpm)

abundanceProteinOnly$protein <- abundanceProteinOnly$intensity_average

minProtein <- min(abundanceCommon$protein, abundanceProteinOnly$protein)
maxProtein <- max(abundanceCommon$protein, abundanceProteinOnly$protein)
minRna <- min(abundanceCommon$rna, abundanceRnaOnly$rna)
maxRna <- max(abundanceCommon$rna, abundanceRnaOnly$rna)

tissuesLevels <- levels(as.factor(abundanceMerge2$tissue))
tissuesColors <- colorRampPalette(colors = c("red", "blue"))(length(tissuesLevels))

abundancePlot <- ggplot()
abundancePlot <- abundancePlot + theme_bw(base_size = 22)
abundancePlot <- abundancePlot + geom_point(data = abundanceCommon, aes(x = rna, y = protein, col = tissue), alpha = 0.3)
abundancePlot <- abundancePlot + scale_color_manual(values = tissuesColors, name = "")
abundancePlot <- abundancePlot + scale_x_continuous(limits = c(minRna, maxRna))
abundancePlot <- abundancePlot + scale_y_continuous(limits = c(minProtein, maxProtein))
abundancePlot <- abundancePlot + theme(axis.text = element_blank(),
                                       axis.ticks = element_blank(),
                                       legend.position = 'none')

png(paste("resources/iGraph/plots/tissue/rna-protein.png", sep = ""), width = 800, height = 600)
plot(abundancePlot)
dev.off()

rnaOnlyPlot <- ggplot()
rnaOnlyPlot <- rnaOnlyPlot + theme_bw(base_size = 22)
rnaOnlyPlot <- rnaOnlyPlot + geom_jitter(data = abundanceRnaOnly, aes(x = tissue, y = rna, col = tissue), alpha = 0.3, height = 0, width = 0.3)
rnaOnlyPlot <- rnaOnlyPlot + scale_color_manual(values = tissuesColors, name = "")
rnaOnlyPlot <- rnaOnlyPlot + scale_y_continuous(limits = c(minRna, maxRna))
rnaOnlyPlot <- rnaOnlyPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5),
                                   axis.text.y = element_blank(),
                                   axis.ticks = element_blank(),
                                   axis.title = element_blank(),
                                   legend.position = 'none')

png(paste("resources/iGraph/plots/tissue/rna-only.png", sep = ""), width = 800, height = 600)
plot(rnaOnlyPlot)
dummy <- dev.off()

proteinOnlyPlot <- ggplot()
proteinOnlyPlot <- proteinOnlyPlot + theme_bw(base_size = 22)
proteinOnlyPlot <- proteinOnlyPlot + geom_jitter(data = abundanceProteinOnly, aes(x = tissue, y = protein, col = tissue), alpha = 0.3, height = 0, width = 0.3)
proteinOnlyPlot <- proteinOnlyPlot + scale_color_manual(values = tissuesColors, name = "")
proteinOnlyPlot <- proteinOnlyPlot + scale_y_continuous(limits = c(minProtein, maxProtein))
proteinOnlyPlot <- proteinOnlyPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5),
                                           axis.text.y = element_blank(),
                                           axis.ticks = element_blank(),
                                           axis.title = element_blank(),
                                           legend.position = 'none')

png(paste("resources/iGraph/plots/tissue/protein-only.png", sep = ""), width = 800, height = 600)
plot(proteinOnlyPlot)
dummy <- dev.off()


# Load Reactome graph

print(paste(Sys.time(), " Loading Reactome graph", sep = ""))

verticesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

reactome <- graph_from_data_frame(d=edgesReactome, vertices=verticesReactome, directed=F)
reactome <- simplify(reactome, remove.multiple = T, remove.loops = T) 

# Get communities

print(paste(Sys.time(), " Getting communities", sep = ""))

verticesReactome$comm <- membership(cluster_fast_greedy(reactome))
verticesReactome <- verticesReactome[order(verticesReactome$comm, verticesReactome$id), ]


# Plot default matrix

print(paste(Sys.time(), " Plotting adjacency matrix", sep = ""))

heatMapDataFrame <- edgesReactome %>% mutate(
  to = factor(to, levels = verticesReactome$id),
  from = factor(from, levels = verticesReactome$id))
heatMapDataFrame$typeFactor <- as.factor(heatMapDataFrame$type)

heatMapPlot <- ggplot()
heatMapPlot <- heatMapPlot + theme_bw(base_size = 22)
heatMapPlot <- heatMapPlot + geom_tile(data = heatMapDataFrame, aes(x = from, y = to, fill = typeFactor))
heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
heatMapPlot <- heatMapPlot + scale_fill_manual(values = c("darkgreen", "black", "blue", "darkred"))
heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                   axis.text = element_blank(),
                                   axis.ticks = element_blank(),
                                   panel.grid = element_blank(),
                                   legend.position = 'none')

png("resources/iGraph/plots/tissue/adjacencyMatrix.png", width = 3200, height = 2400)
plot(heatMapPlot)
dummy <- dev.off()


# Plot tissue matrix

for (i in 1:nrow(tissuesMatched)) {
  
  rnaTissue <- tissuesMatched$rnaTissues[i]
  proteintissue <- tissuesMatched$proteinTissues[i]
  
  print(paste(Sys.time(), " Plotting matrices for ", proteintissue, " (", i, " of ", nrow(tissuesMatched), ")", sep = ""))
  
  # Get tissue specific proteins
  
  rnaAccessions <- rnaAbundanceMatched$accession[rnaAbundanceMatched$tissue == rnaTissue]
  proteinAccessions <- proteinAbundanceMatched$accession[proteinAbundanceMatched$tissue == proteintissue]
  
  if (length(rnaAccessions) > 2) {
    
    # RNA
    
    edgesTemp <- edgesReactome[edgesReactome$from %in% rnaAccessions & edgesReactome$to %in% rnaAccessions, ]
    verticesTemp <- verticesReactome[verticesReactome$id %in% edgesTemp$from | verticesReactome$id %in% edgesTemp$to, ]
    
    reactomeTemp <- graph_from_data_frame(d=edgesTemp, vertices=verticesTemp, directed=F)
    reactomeTemp <- simplify(reactomeTemp, remove.multiple = T, remove.loops = T) 
    
    # Get tissue communities
    
    verticesTemp$comm <- membership(cluster_fast_greedy(reactomeTemp))
    verticesTemp <- verticesTemp[order(verticesTemp$comm, verticesTemp$id), ]
    
    
    # Plot tissue matrix
    
    tempDataFrame <- edgesTemp %>% mutate(
      to = factor(to, levels = verticesTemp$id),
      from = factor(from, levels = verticesTemp$id))
    tempDataFrame$typeFactor <- as.factor(tempDataFrame$type)
    
    heatMapPlot <- ggplot()
    heatMapPlot <- heatMapPlot + theme_bw(base_size = 88)
    heatMapPlot <- heatMapPlot + geom_tile(data = tempDataFrame, aes(x = from, y = to, fill = typeFactor))
    heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
    heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
    heatMapPlot <- heatMapPlot + scale_fill_manual(values = c("darkgreen", "black", "blue", "darkred"))
    heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                       axis.text = element_blank(),
                                       axis.ticks = element_blank(),
                                       panel.grid = element_blank(),
                                       legend.position = 'none',
                                       plot.title = element_text(hjust = 1))
    heatMapPlot <- heatMapPlot + ggtitle(paste(proteintissue, " - RNA", sep = ""))
    
    png(paste("resources/iGraph/plots/tissue/matrices/adjacencyMatrix_", proteintissue, "_RNA.png", sep = ""), width = 3200, height = 2400)
    plot(heatMapPlot)
    dummy <- dev.off()
    
  }
  
  if (length(proteinAccessions) > 2) {
    
    # Protein
    
    edgesTemp <- edgesReactome[edgesReactome$from %in% proteinAccessions & edgesReactome$to %in% proteinAccessions, ]
    verticesTemp <- verticesReactome[verticesReactome$id %in% edgesTemp$from | verticesReactome$id %in% edgesTemp$to, ]
    
    reactomeTemp <- graph_from_data_frame(d=edgesTemp, vertices=verticesTemp, directed=F)
    reactomeTemp <- simplify(reactomeTemp, remove.multiple = T, remove.loops = T) 
    
    # Get tissue communities
    
    verticesTemp$comm <- membership(cluster_fast_greedy(reactomeTemp))
    verticesTemp <- verticesTemp[order(verticesTemp$comm, verticesTemp$id), ]
    
    
    # Plot tissue matrix
    
    tempDataFrame <- edgesTemp %>% mutate(
      to = factor(to, levels = verticesTemp$id),
      from = factor(from, levels = verticesTemp$id))
    tempDataFrame$typeFactor <- as.factor(tempDataFrame$type)
    
    heatMapPlot <- ggplot()
    heatMapPlot <- heatMapPlot + theme_bw(base_size = 88)
    heatMapPlot <- heatMapPlot + geom_tile(data = tempDataFrame, aes(x = from, y = to, fill = typeFactor))
    heatMapPlot <- heatMapPlot + geom_tile(data = tempDataFrame, aes(x = from, y = to, fill = typeFactor))
    heatMapPlot <- heatMapPlot + scale_x_discrete(name = element_blank(), expand = c(0, 0))
    heatMapPlot <- heatMapPlot + scale_y_discrete(name = element_blank(), position = "right", expand = c(0, 0))
    heatMapPlot <- heatMapPlot + scale_fill_manual(values = c("darkgreen", "black", "blue", "darkred"))
    heatMapPlot <- heatMapPlot + theme(axis.title = element_blank(),
                                       axis.text = element_blank(),
                                       axis.ticks = element_blank(),
                                       panel.grid = element_blank(),
                                       legend.position = 'none',
                                       plot.title = element_text(hjust = 1))
    heatMapPlot <- heatMapPlot + ggtitle(paste(proteintissue, " - Protein", sep = ""))
    
    png(paste("resources/iGraph/plots/tissue/matrices/adjacencyMatrix_", proteintissue, "_Protein.png", sep = ""), width = 3200, height = 2400)
    plot(heatMapPlot)
    dummy <- dev.off()
    
  }
}





