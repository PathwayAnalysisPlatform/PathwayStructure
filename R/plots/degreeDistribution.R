# 
# This script takes the iGraph data frames from various functional resources and plots their degree distributions.
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

verticesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

edgesReactions <- edgesReactome[edgesReactome$type == "Reaction", c("from", "to")]
verticesReactions <- verticesReactome[verticesReactome$id %in% edgesReactions$from | verticesReactome$id %in% edgesReactions$to, ]

verticesKegg <- read.table("resources/iGraph/kegg/kegg_21.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesKegg <- read.table("resources/iGraph/kegg/kegg_21.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesGygi <- read.table("resources/iGraph/biogrid/28514442_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesGygi <- read.table("resources/iGraph/biogrid/28514442_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesMann <- read.table("resources/iGraph/intact/26496610_mann_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesMann <- read.table("resources/iGraph/intact/26496610_mann_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesString <- read.table("resources/iGraph/string/string_v10.5_medium_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesString <- read.table("resources/iGraph/string/string_v10.5_medium_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")


# Get graphs and degrees

print(paste(Sys.time(), " Processing graphs", sep = ""))

complexes <- graph_from_data_frame(d = edgesComplexes, vertices = verticesComplexes, directed = F)
reactome <- graph_from_data_frame(d=edgesReactome, vertices=verticesReactome, directed=T)
reactomeReactions <- graph_from_data_frame(d=edgesReactions, vertices=verticesReactions, directed=T)
kegg <- graph_from_data_frame(d = edgesKegg, vertices = verticesKegg, directed = F)
biogrid <- graph_from_data_frame(d = edgesBiogrid, vertices = verticesBiogrid, directed = F)
intact <- graph_from_data_frame(d = edgesIntact, vertices = verticesIntact, directed = F)
gygi <- graph_from_data_frame(d = edgesGygi, vertices = verticesGygi, directed = F)
mann <- graph_from_data_frame(d = edgesMann, vertices = verticesMann, directed = F)
string <- graph_from_data_frame(d = edgesString, vertices = verticesString, directed = F)


complexesDegrees <- degree(complexes)
reactomeDegrees <- degree(reactome, mode = "all")
reactomeReactionsDegrees <- degree(reactomeReactions, mode = "all")
keggDegrees <- degree(kegg)
biogridDegrees <- degree(biogrid)
intactDegrees <- degree(intact)
gygiDegrees <- degree(gygi)
mannDegrees <- degree(mann)
stringDegrees <- degree(string)

reactomeDegreesIn <- degree(reactome, mode = "in")
reactomeDegreesOut <- degree(reactome, mode = "out")
reactomeReactionsDegreesIn <- degree(reactomeReactions, mode = "in")
reactomeReactionsDegreesOut <- degree(reactomeReactions, mode = "out")

complexesDistribution <- degree.distribution(complexes)
reactomeDistribution <- degree.distribution(reactome, mode = "all")
reactomeReactionsDistribution <- degree.distribution(reactomeReactions, mode = "all")
keggDistribution <- degree.distribution(kegg)
biogridDistribution <- degree.distribution(biogrid)
intactDistribution <- degree.distribution(intact)
gygiDistribution <- degree.distribution(gygi)
mannDistribution <- degree.distribution(mann)
stringDistribution <- degree.distribution(string)

complexesDistributionScaled <- complexesDistribution * nrow(verticesComplexes)
reactomeDistributionScaled <- reactomeDistribution * nrow(verticesReactome)
reactomeReactionDistributionScaled <- reactomeReactionDistribution * nrow(verticesReactomeReaction)
keggDistributionScaled <- keggDistribution * nrow(verticesKegg)
biogridDistributionScaled <- biogridDistribution * nrow(verticesBiogrid)
intactDistributionScaled <- intactDistribution * nrow(verticesIntact)
gygiDistributionScaled <- gygiDistribution * nrow(verticesGygi)
mannDistributionScaled <- mannDistribution * nrow(verticesMann)
stringDistributionScaled <- stringDistribution * nrow(verticesString)


reactomeDistributionIn <- degree.distribution(reactome, mode = "in")
reactomeReactionsDistributionIn <- degree.distribution(reactomeReactions, mode = "in")
reactomeDistributionOut <- degree.distribution(reactome, mode = "out")
reactomeReactionsDistributionOut <- degree.distribution(reactomeReactions, mode = "out")

reactomeDistributionInScaled <- reactomeDistributionIn * nrow(verticesReactome)
reactomeDistributionOutScaled <- reactomeDistributionOut * nrow(verticesReactome)
reactomeReactionsDistributionInScaled <- reactomeReactionsDistributionIn * nrow(verticesReactions)
reactomeReactionsDistributionOutScaled <- reactomeReactionsDistributionOut * nrow(verticesReactions)


# Plot directed distributions

print(paste(Sys.time(), " Plotting reactome in out degrees", sep = ""))

degree <- 0:(length(reactomeDistributionInScaled)-1)
frequency <- reactomeDistributionInScaled
category <- rep("in", length(reactomeDistributionInScaled))

degree <- c(degree, 0:(length(reactomeDistributionOutScaled)-1))
frequency <- c(frequency, reactomeDistributionOutScaled)
category <- c(category, rep("out", length(reactomeDistributionOutScaled)))

ddPlotData <- data.frame(degree, frequency, category, stringsAsFactors = T)
ddPlotData <- ddPlotData[ddPlotData$degree > 0 & ddPlotData$frequency > 0, ]
ddPlotData$degree <- log10(ddPlotData$degree)
ddPlotData$frequency <- log10(ddPlotData$frequency)

ddPlot <- ggplot()
ddPlot <- ddPlot + geom_point(data = ddPlotData, aes(x = degree, y = frequency, col = category), alpha = 0.3, size = 3)
ddPlot <- ddPlot + theme_bw(base_size = 22)
ddPlot <- ddPlot + scale_color_manual(name = element_blank(), values = c("darkblue", "darkred"))
ddPlot <- ddPlot + scale_x_continuous(name = "degree [log10]")
ddPlot <- ddPlot + scale_y_continuous(name = "# Proteins [log10]")
ddPlot <- ddPlot + theme(legend.position = 'none')

png("resources/iGraph/plots/distributions/reactomeIO.png", width = 600, height = 800)
plot(ddPlot)
dummy <- dev.off()


degree <- 0:(length(reactomeReactionsDistributionInScaled)-1)
frequency <- reactomeReactionsDistributionInScaled
category <- rep("in", length(reactomeReactionsDistributionInScaled))

degree <- c(degree, 0:(length(reactomeReactionsDistributionOutScaled)-1))
frequency <- c(frequency, reactomeReactionsDistributionOutScaled)
category <- c(category, rep("out", length(reactomeReactionsDistributionOutScaled)))

ddPlotData <- data.frame(degree, frequency, category, stringsAsFactors = T)
ddPlotData <- ddPlotData[ddPlotData$degree > 0 & ddPlotData$frequency > 0, ]
ddPlotData$degree <- log10(ddPlotData$degree)
ddPlotData$frequency <- log10(ddPlotData$frequency)

ddPlot <- ggplot()
ddPlot <- ddPlot + geom_point(data = ddPlotData, aes(x = degree, y = frequency, col = category), alpha = 0.3, size = 3)
ddPlot <- ddPlot + theme_bw(base_size = 22)
ddPlot <- ddPlot + scale_color_manual(name = element_blank(), values = c("darkblue", "darkred"))
ddPlot <- ddPlot + scale_x_continuous(name = "degree [log10]")
ddPlot <- ddPlot + scale_y_continuous(name = "# Proteins [log10]")
ddPlot <- ddPlot + theme(legend.position = 'none')

png("resources/iGraph/plots/distributions/reactomeReactionsIO.png", width = 600, height = 800)
plot(ddPlot)
dummy <- dev.off()


# Plot distributions

print(paste(Sys.time(), " Plotting degree distributions", sep = ""))

degree <- unname(complexesDegrees)
category <- rep(categoryNames[1], length(complexesDegrees))
mainCategory <- rep(mainCategoryNames[1], length(complexesDegrees))
colors <- categoryColors[1]

degree <- c(degree, unname(reactomeDegrees))
category <- c(category, rep(categoryNames[2], length(reactomeDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(reactomeDegrees)))
colors <- c(colors, categoryColors[2])

degree <- c(degree, unname(keggDegrees))
category <- c(category, rep(categoryNames[4], length(keggDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(keggDegrees)))
colors <- c(colors, categoryColors[4])

degree <- c(degree, unname(biogridDegrees))
category <- c(category, rep(categoryNames[5], length(biogridDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(biogridDegrees)))
colors <- c(colors, categoryColors[5])

degree <- c(degree, unname(intactDegrees))
category <- c(category, rep(categoryNames[6], length(intactDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(intactDegrees)))
colors <- c(colors, categoryColors[6])

degree <- c(degree, unname(stringDegrees))
category <- c(category, rep(categoryNames[9], length(stringDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[4], length(stringDegrees)))
colors <- c(colors, categoryColors[9])

dPlotData <- data.frame(degree, category, mainCategory, stringsAsFactors = F)
dPlotData$mainCategory <- factor(dPlotData$mainCategory, levels = mainCategoryNames)
dPlotData$category <- factor(dPlotData$category, levels = categoryNames)
dPlotData <- dPlotData[degree > 0, ]
dPlotData$degree <- log10(dPlotData$degree)

dPlot <- ggplot()
dPlot <- dPlot + geom_density(data = dPlotData, aes(x = degree, fill = category, col = category), alpha = 0.3)
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_color_manual(name = element_blank(), values = colors)
dPlot <- dPlot + scale_fill_manual(name = element_blank(), values = colors)
dPlot <- dPlot + scale_x_continuous(name = "degree [log10]", expand = c(0, 0))
dPlot <- dPlot + scale_y_continuous(expand = c(0, 0))
dPlot <- dPlot + guides(col = guide_legend(nrow = 1))
dPlot <- dPlot + theme(axis.title.y = element_blank(),
                       axis.text.y = element_blank(),
                       axis.ticks.y = element_blank(),
                       legend.position = 'top')

dPlot <- dPlot + facet_grid(mainCategory ~ .)

png("resources/iGraph/plots/distributions/degreeDistributions_density_legend.png", width = 800, height = 600)
plot(dPlot)
dummy <- dev.off()


degree <- 0:(length(complexesDistributionScaled)-1)
frequency <- complexesDistributionScaled
category <- rep(categoryNames[1], length(complexesDistributionScaled))
mainCategory <- rep(mainCategoryNames[1], length(complexesDistributionScaled))
colors <- categoryColors[1]

degree <- c(degree, 0:(length(reactomeDistributionScaled)-1))
frequency <- c(frequency, reactomeDistributionScaled)
category <- c(category, rep(categoryNames[2], length(reactomeDistributionScaled)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(reactomeDistributionScaled)))
colors <- c(colors, categoryColors[2])

degree <- c(degree, 0:(length(keggDistributionScaled)-1))
frequency <- c(frequency, keggDistributionScaled)
category <- c(category, rep(categoryNames[4], length(keggDistributionScaled)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(keggDistributionScaled)))
colors <- c(colors, categoryColors[4])

degree <- c(degree, 0:(length(biogridDistributionScaled)-1))
frequency <- c(frequency, biogridDistributionScaled)
category <- c(category, rep(categoryNames[5], length(biogridDistributionScaled)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(biogridDistributionScaled)))
colors <- c(colors, categoryColors[5])

degree <- c(degree, 0:(length(intactDistributionScaled)-1))
frequency <- c(frequency, intactDistributionScaled)
category <- c(category, rep(categoryNames[6], length(intactDistributionScaled)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(intactDistributionScaled)))
colors <- c(colors, categoryColors[6])

degree <- c(degree, 0:(length(stringDistributionScaled)-1))
frequency <- c(frequency, stringDistributionScaled)
category <- c(category, rep(categoryNames[9], length(stringDistributionScaled)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[4], length(stringDistributionScaled)))
colors <- c(colors, categoryColors[9])


dPlotData <- data.frame(degree, frequency, category, mainCategory, stringsAsFactors = F)
dPlotData$category <- factor(dPlotData$category, levels = categoryNames)
dPlotData$mainCategory <- factor(dPlotData$mainCategory, levels = mainCategoryNames)
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
dPlot <- dPlot + theme(legend.position = 'none')

dPlot <- dPlot + facet_grid(mainCategory ~ .)

png("resources/iGraph/plots/distributions/degreeDistributions_scatter.png", width = 600, height = 800)
plot(dPlot)
dummy <- dev.off()



