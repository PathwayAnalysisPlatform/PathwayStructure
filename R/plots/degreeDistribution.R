# 
# This script takes the iGraph data frames from various functional resources and plots their degree distributions.
#
startTimeAll <- proc.time()



## Libraries

library(igraph)
library(ggplot2)



## Parameters

mainCategoryNames <- c("Complexes", "Pathways", "Interactions")
categoryNames <- c("Complexes", "Reactome", "Reactions", "Intact", "Mann")
categoryColors <- c("#4daf4a", "#377eb8", "#984ea3", "#e41a1c", "#ff7f00")


## Main script


# Load data

print(paste(Sys.time(), " Loading data", sep = ""))

verticesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_vertices", header = T, sep = " ", stringsAsFactors = F)
edgesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F)

verticesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_vertices", header = T, sep = " ", stringsAsFactors = F)
edgesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F)

edgesReactions <- edgesReactome[edgesReactome$type == "Reaction", c("from", "to")]
verticesReactions <- verticesReactome[verticesReactome$id %in% edgesReactions$from | verticesReactome$id %in% edgesReactions$to, ]

verticesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_vertices", header = T, sep = " ", stringsAsFactors = F)
edgesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F)

verticesMann <- read.table("resources/iGraph/intact/26496610_mann_vertices", header = T, sep = " ", stringsAsFactors = F)
edgesMann <- read.table("resources/iGraph/intact/26496610_mann_edges", header = T, sep = " ", stringsAsFactors = F)


# Get graphs and degrees

print(paste(Sys.time(), " Processing graphs", sep = ""))

complexes <- graph_from_data_frame(d = edgesComplexes, vertices = verticesComplexes, directed = F)
reactome <- graph_from_data_frame(d=edgesReactome, vertices=verticesReactome, directed=T)
reactomeReactions <- graph_from_data_frame(d=edgesReactions, vertices=verticesReactions, directed=T)
intact <- graph_from_data_frame(d = edgesIntact, vertices = verticesIntact, directed = F)
mann <- graph_from_data_frame(d = edgesMann, vertices = verticesMann, directed = F)

complexesDegrees <- degree(complexes)
reactomeDegreesIn <- degree(reactome, mode = "in")
reactomeDegreesOut <- degree(reactome, mode = "out")
reactomeDegrees <- degree(reactome, mode = "all")
reactomeReactionsDegreesIn <- degree(reactomeReactions, mode = "in")
reactomeReactionsDegreesOut <- degree(reactomeReactions, mode = "out")
reactomeReactionsDegrees <- degree(reactomeReactions, mode = "all")
intactDegrees <- degree(intact)
mannDegrees <- degree(mann)

complexesDistribution <- degree.distribution(complexes)
reactomeDistributionIn <- degree.distribution(reactome, mode = "in")
reactomeDistributionOut <- degree.distribution(reactome, mode = "out")
reactomeDistribution <- degree.distribution(reactome, mode = "all")
reactomeReactionsDistributionIn <- degree.distribution(reactomeReactions, mode = "in")
reactomeReactionsDistributionOut <- degree.distribution(reactomeReactions, mode = "out")
reactomeReactionsDistribution <- degree.distribution(reactomeReactions, mode = "all")
intactDistribution <- degree.distribution(intact)
mannDistribution <- degree.distribution(mann)


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

degree <- c(degree, unname(intactDegrees))
category <- c(category, rep(categoryNames[4], length(intactDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(intactDegrees)))

degree <- c(degree, unname(mannDegrees))
category <- c(category, rep(categoryNames[5], length(mannDegrees)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(mannDegrees)))

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

degree <- c(degree, 0:(length(intactDistribution)-1))
frequency <- c(frequency, intactDistribution)
category <- c(category, rep(categoryNames[4], length(intactDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(intactDistribution)))

degree <- c(degree, 0:(length(mannDistribution)-1))
frequency <- c(frequency, mannDistribution)
category <- c(category, rep(categoryNames[5], length(mannDistribution)))
mainCategory <- c(mainCategory, rep(mainCategoryNames[3], length(mannDistribution)))


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
