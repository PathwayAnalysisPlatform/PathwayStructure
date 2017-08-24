# 
# This script takes the interaction iGraph data frames and plots their degree distributions.
#
startTimeAll <- proc.time()



## Libraries

library(igraph)
library(ggplot2)



## Parameters

mainCategoryNames <- c("Complex", "Pathway", "Interaction", "Composite")
categoryNames <- c("Complexes", "Reactome", "Reactions", "Kegg", "Biogrid", "Intact", "Huttlin", "Hein", "String")
categoryColors <- c("#e31a1c", "#1f78b4", "#a6cee3", "#33a02c", "#ff7f00", "#6a3d9a", "#fdbf6f", "#cab2d6", "#999999")


## Main script


# Load data

print(paste(Sys.time(), " Loading data", sep = ""))

verticesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesGygi <- read.table("resources/iGraph/biogrid/28514442_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesGygi <- read.table("resources/iGraph/biogrid/28514442_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesMann <- read.table("resources/iGraph/intact/26496610_mann_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesMann <- read.table("resources/iGraph/intact/26496610_mann_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")


# Get graphs and degrees

print(paste(Sys.time(), " Processing graphs", sep = ""))

biogrid <- graph_from_data_frame(d = edgesBiogrid, vertices = verticesBiogrid, directed = F)
intact <- graph_from_data_frame(d = edgesIntact, vertices = verticesIntact, directed = F)
gygi <- graph_from_data_frame(d = edgesGygi, vertices = verticesGygi, directed = F)
mann <- graph_from_data_frame(d = edgesMann, vertices = verticesMann, directed = F)

biogridDegrees <- degree(biogrid)
intactDegrees <- degree(intact)
gygiDegrees <- degree(gygi)
mannDegrees <- degree(mann)

biogridDistribution <- degree.distribution(biogrid)
intactDistribution <- degree.distribution(intact)
gygiDistribution <- degree.distribution(gygi)
mannDistribution <- degree.distribution(mann)

biogridDistributionScaled <- biogridDistribution * nrow(verticesBiogrid)
intactDistributionScaled <- intactDistribution * nrow(verticesIntact)
gygiDistributionScaled <- gygiDistribution * nrow(verticesGygi)
mannDistributionScaled <- mannDistribution * nrow(verticesMann)


# Get size information

print(paste(Sys.time(), " Plotting vertices and edges", sep = ""))

sizeDataFrame <- data.frame(categoryNames, categoryColors, stringsAsFactors = F)
sizeDataFrame$category <- factor(sizeDataFrame$categoryNames, levels = categoryNames)

# number of vertices

sizeDataFrame$nVertices <- c(
  nrow(verticesComplexes),
  nrow(verticesReactome),
  nrow(verticesReactions),
  nrow(verticesKegg),
  nrow(verticesBiogrid),
  nrow(verticesIntact),
  nrow(verticesGygi),
  nrow(verticesMann),
  nrow(verticesString)
)

# number of edges

sizeDataFrame$nEdges <- c(
  nrow(edgesComplexes),
  nrow(edgesReactome),
  nrow(edgesReactions),
  nrow(edgesKegg),
  nrow(edgesBiogrid),
  nrow(edgesIntact),
  nrow(edgesGygi),
  nrow(edgesMann),
  nrow(edgesString)
)

trimmedDataFrame <- sizeDataFrame[sizeDataFrame$categoryNames %in% c("Biogrid", "Intact", "Huttlin", "Hein"), ]


# Plotting size information

print(paste(Sys.time(), " Exporting plots", sep = ""))

evPlot <- ggplot()
evPlot <- evPlot + geom_point(data = trimmedDataFrame, aes(x = nVertices, y = nEdges, col = category), size = 8)
evPlot <- evPlot + geom_vline(aes(xintercept = 20201), col = "green", linetype = "dashed")
evPlot <- evPlot + theme_bw(base_size = 33)
evPlot <- evPlot + scale_color_manual(name = element_blank(), values = trimmedDataFrame$categoryColors)
evPlot <- evPlot + scale_x_log10(name = "# Proteins")
evPlot <- evPlot + scale_y_log10(name = "# Links")
evPlot <- evPlot + theme(legend.position = 'none')
evPlot <- evPlot + geom_text_repel(data = trimmedDataFrame, aes(x = nVertices, y = nEdges, label = categoryNames), point.padding = unit(1.6, 'lines'), size = 8)


png("resources/iGraph/plots/interactions/edges-vertices.png", width = 800, height = 600)
plot(evPlot)
dummy <- dev.off()


# Plotting degree information


degree <- 0:(length(biogridDistributionScaled)-1)
frequency <- biogridDistributionScaled
category <- rep(categoryNames[5], length(biogridDistributionScaled))
colors <- categoryColors[5]

degree <- c(degree, 0:(length(intactDistributionScaled)-1))
frequency <- c(frequency, intactDistributionScaled)
category <- c(category, rep(categoryNames[6], length(intactDistributionScaled)))
colors <- c(colors, categoryColors[6])

degree <- c(degree, 0:(length(gygiDistributionScaled)-1))
frequency <- c(frequency, gygiDistributionScaled)
category <- c(category, rep(categoryNames[7], length(gygiDistributionScaled)))
colors <- c(colors, categoryColors[7])

degree <- c(degree, 0:(length(mannDistributionScaled)-1))
frequency <- c(frequency, mannDistributionScaled)
category <- c(category, rep(categoryNames[8], length(mannDistributionScaled)))
colors <- c(colors, categoryColors[8])


dPlotData <- data.frame(degree, frequency, category, stringsAsFactors = F)
dPlotData$category <- factor(dPlotData$category, levels = categoryNames)
dPlotData <- dPlotData[dPlotData$degree > 0, ]
dPlotData <- dPlotData[dPlotData$frequency > 0, ]
dPlotData$degree <- log10(dPlotData$degree)
dPlotData$frequency <- log10(dPlotData$frequency)

dPlot <- ggplot()
dPlot <- dPlot + geom_point(data = dPlotData, aes(x = degree, y = frequency, col = category), alpha = 0.3, size = 3)
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_color_manual(name = element_blank(), values = colors)
dPlot <- dPlot + scale_x_continuous(name = "degree [log10]")
dPlot <- dPlot + scale_y_continuous(name = "# Proteins [log10]")
dPlot <- dPlot + theme(legend.position = 'right')

png("resources/iGraph/plots/interactions/degreeDistributions_scatter.png", width = 600, height = 800)
plot(dPlot)
dummy <- dev.off()

