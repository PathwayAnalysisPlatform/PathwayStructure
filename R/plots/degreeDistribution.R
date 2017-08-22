# 
# This script takes the iGraph data frames from various functional resources and plots their degree distributions.
#
startTimeAll <- proc.time()



## Libraries

library(igraph)
library(ggplot2)



## Parameters

mainCategoryNames <- c("Complexes", "Pathways", "Interactions", "Composite")
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

verticesString <- read.table("resources/iGraph/string/string_v10.5_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesString <- read.table("resources/iGraph/string/string_v10.5_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")


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


reactomeDistributionIn <- degree.distribution(reactome, mode = "in")
reactomeReactionsDistributionIn <- degree.distribution(reactomeReactions, mode = "in")
reactomeDistributionOut <- degree.distribution(reactome, mode = "out")
reactomeReactionsDistributionOut <- degree.distribution(reactomeReactions, mode = "out")

# Plot directed distributions

print(paste(Sys.time(), " Plotting reactome in out degrees", sep = ""))

degree <- 0:(length(reactomeDistributionIn)-1)
frequency <- reactomeDistributionIn
category <- rep("in", length(reactomeDistributionIn))

degree <- c(degree, 0:(length(reactomeDistributionOut)-1))
frequency <- c(frequency, reactomeDistributionOut)
category <- c(category, rep("out", length(reactomeDistributionOut)))

ddPlotData <- data.frame(degree, frequency, category, stringsAsFactors = T)
ddPlotData <- ddPlotData[ddPlotData$frequency > 0, ]
ddPlotData$degree <- log10(ddPlotData$degree)
ddPlotData$frequency <- log10(ddPlotData$frequency)

ddPlot <- ggplot()
ddPlot <- ddPlot + geom_point(data = ddPlotData, aes(x = degree, y = frequency, col = category), alpha = 0.3)
ddPlot <- ddPlot + theme_bw()
ddPlot <- ddPlot + scale_color_manual(name = element_blank(), values = c("darkblue", "darkred"))
ddPlot <- ddPlot + scale_x_continuous(name = "degree [log10]")
ddPlot <- ddPlot + scale_y_continuous(name = "p [log10]")

png("resources/iGraph/plots/distributions/reactomeIO.png", width = 800, height = 600)
plot(ddPlot)
dummy <- dev.off()


degree <- 0:(length(reactomeReactionsDistributionIn)-1)
frequency <- reactomeReactionsDistributionIn
category <- rep("in", length(reactomeReactionsDistributionIn))

degree <- c(degree, 0:(length(reactomeReactionsDistributionOut)-1))
frequency <- c(frequency, reactomeReactionsDistributionOut)
category <- c(category, rep("out", length(reactomeReactionsDistributionOut)))

ddPlotData <- data.frame(degree, frequency, category, stringsAsFactors = T)
ddPlotData <- ddPlotData[ddPlotData$degree > 0, ]
ddPlotData <- ddPlotData[ddPlotData$frequency > 0, ]
ddPlotData$degree <- log10(ddPlotData$degree)
ddPlotData$frequency <- log10(ddPlotData$frequency)

ddPlot <- ggplot()
ddPlot <- ddPlot + geom_point(data = ddPlotData, aes(x = degree, y = frequency, col = category), alpha = 0.3)
ddPlot <- ddPlot + theme_bw()
ddPlot <- ddPlot + scale_color_manual(name = element_blank(), values = c("darkblue", "darkred"))
ddPlot <- ddPlot + scale_x_continuous(name = "degree [log10]")
ddPlot <- ddPlot + scale_y_continuous(name = "p [log10]")

png("resources/iGraph/plots/distributions/reactomeReactionsIO.png", width = 800, height = 600)
plot(ddPlot)
dummy <- dev.off()

# Plot distributions

print(paste(Sys.time(), " Plotting degree distributions", sep = ""))

degree <- unname(complexesDegrees)
category <- rep(categoryNames[1], length(complexesDegrees))
mainCategory <- rep(mainCategoryNames[1], length(complexesDegrees))

degree <- c(degree, unname(reactomeDegrees))
category <- c(category, rep(categoryNames[2], length(reactomeDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(reactomeDegrees)))

degree <- c(degree, unname(reactomeReactionsDegrees))
category <- c(category, rep(categoryNames[3], length(reactomeReactionsDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(reactomeReactionsDegrees)))

degree <- c(degree, unname(keggDegrees))
category <- c(category, rep(categoryNames[4], length(keggDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(keggDegrees)))

degree <- c(degree, unname(biogridDegrees))
category <- c(category, rep(categoryNames[5], length(biogridDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(biogridDegrees)))

degree <- c(degree, unname(intactDegrees))
category <- c(category, rep(categoryNames[6], length(intactDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(intactDegrees)))

degree <- c(degree, unname(gygiDegrees))
category <- c(category, rep(categoryNames[7], length(gygiDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(gygiDegrees)))

degree <- c(degree, unname(mannDegrees))
category <- c(category, rep(categoryNames[8], length(mannDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(mannDegrees)))

degree <- c(degree, unname(stringDegrees))
category <- c(category, rep(categoryNames[9], length(stringDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[4], length(stringDegrees)))

dPlotData <- data.frame(degree, category, mainCategory, stringsAsFactors = F)
dPlotData$mainCategory <- factor(dPlotData$mainCategory, levels = mainCategoryNames)
dPlotData$category <- factor(dPlotData$category, levels = categoryNames)
dPlotData <- dPlotData[degree > 0, ]
dPlotData$degree <- log10(dPlotData$degree)

dPlot <- ggplot()
dPlot <- dPlot + geom_density(data = dPlotData, aes(x = degree, fill = category, col = category), alpha = 0.3)
dPlot <- dPlot + theme_bw()
dPlot <- dPlot + scale_color_manual(name = element_blank(), values = categoryColors)
dPlot <- dPlot + scale_fill_manual(name = element_blank(), values = categoryColors)
dPlot <- dPlot + scale_x_continuous(name = "degree [log10]", expand = c(0, 0))
dPlot <- dPlot + scale_y_continuous(expand = c(0, 0))
dPlot <- dPlot + theme(axis.title.y = element_blank(),
                       axis.text.y = element_blank(),
                       axis.ticks.y = element_blank())

dPlot <- dPlot + facet_grid(mainCategory ~ .)

png("resources/iGraph/plots/distributions/degreeDistributions_density.png", width = 800, height = 600)
plot(dPlot)
dummy <- dev.off()


degree <- 0:(length(complexesDistribution)-1)
frequency <- complexesDistribution
category <- rep(categoryNames[1], length(complexesDistribution))
mainCategory <- rep(mainCategoryNames[1], length(complexesDistribution))

degree <- c(degree, 0:(length(reactomeDistribution)-1))
frequency <- c(frequency, reactomeDistribution)
category <- c(category, rep(categoryNames[2], length(reactomeDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(reactomeDistribution)))

degree <- c(degree, 0:(length(reactomeReactionsDistribution)-1))
frequency <- c(frequency, reactomeReactionsDistribution)
category <- c(category, rep(categoryNames[3], length(reactomeReactionsDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(reactomeReactionsDistribution)))

degree <- c(degree, 0:(length(keggDistribution)-1))
frequency <- c(frequency, keggDistribution)
category <- c(category, rep(categoryNames[4], length(keggDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[2], length(keggDistribution)))

degree <- c(degree, 0:(length(biogridDistribution)-1))
frequency <- c(frequency, biogridDistribution)
category <- c(category, rep(categoryNames[5], length(biogridDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(biogridDistribution)))

degree <- c(degree, 0:(length(intactDistribution)-1))
frequency <- c(frequency, intactDistribution)
category <- c(category, rep(categoryNames[6], length(intactDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(intactDistribution)))

degree <- c(degree, 0:(length(gygiDistribution)-1))
frequency <- c(frequency, gygiDistribution)
category <- c(category, rep(categoryNames[7], length(gygiDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(gygiDistribution)))

degree <- c(degree, 0:(length(mannDistribution)-1))
frequency <- c(frequency, mannDistribution)
category <- c(category, rep(categoryNames[8], length(mannDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(mannDistribution)))

degree <- c(degree, 0:(length(stringDistribution)-1))
frequency <- c(frequency, stringDistribution)
category <- c(category, rep(categoryNames[9], length(stringDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[4], length(stringDistribution)))


dPlotData <- data.frame(degree, frequency, category, mainCategory, stringsAsFactors = F)
dPlotData$category <- factor(dPlotData$category, levels = categoryNames)
dPlotData$mainCategory <- factor(dPlotData$mainCategory, levels = mainCategoryNames)
dPlotData <- dPlotData[dPlotData$degree > 0, ]
dPlotData <- dPlotData[dPlotData$frequency > 0, ]
dPlotData$degree <- log10(dPlotData$degree)
dPlotData$frequency <- log10(dPlotData$frequency)

dPlot <- ggplot()
dPlot <- dPlot + geom_point(data = dPlotData, aes(x = degree, y = frequency, col = category), alpha = 0.3)
dPlot <- dPlot + theme_bw()
dPlot <- dPlot + scale_color_manual(name = element_blank(), values = categoryColors)
dPlot <- dPlot + scale_x_continuous(name = "degree [log10]")
dPlot <- dPlot + scale_y_continuous(name = "p [log10]")

dPlot <- dPlot + facet_grid(mainCategory ~ .)

png("resources/iGraph/plots/distributions/degreeDistributions_scatter.png", width = 800, height = 600)
plot(dPlot)
dummy <- dev.off()
