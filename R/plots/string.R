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

verticesStringAll <- read.table("resources/iGraph/string/string_v10.5_all_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesStringAll <- read.table("resources/iGraph/string/string_v10.5_all_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesStringLow <- read.table("resources/iGraph/string/string_v10.5_low_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesStringLow <- read.table("resources/iGraph/string/string_v10.5_low_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesStringMedium <- read.table("resources/iGraph/string/string_v10.5_medium_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesStringMedium <- read.table("resources/iGraph/string/string_v10.5_medium_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesStringHigh <- read.table("resources/iGraph/string/string_v10.5_high_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesStringHigh <- read.table("resources/iGraph/string/string_v10.5_high_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesStringHighest <- read.table("resources/iGraph/string/string_v10.5_highest_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesStringHighest <- read.table("resources/iGraph/string/string_v10.5_highest_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

edgesExperimental <- edgesStringAll[edgesStringAll$level == "experimental", ]
verticesExperimental <- verticesStringAll[verticesStringAll$id %in% edgesExperimental$from | verticesStringAll$id %in% edgesExperimental$to, ]

edgesDatabase <- edgesStringAll[edgesStringAll$level == "experimental" | edgesStringAll$level == "database", ]
verticesDatabase <- verticesStringAll[verticesStringAll$id %in% edgesDatabase$from | verticesStringAll$id %in% edgesDatabase$to, ]

edgesCoexpression <- edgesStringAll[edgesStringAll$level == "experimental" | edgesStringAll$level == "database" | edgesStringAll$level == "coexpression", ]
verticesCoexpression <- verticesStringAll[verticesStringAll$id %in% edgesCoexpression$from | verticesStringAll$id %in% edgesCoexpression$to, ]


# Make graphs

print(paste(Sys.time(), " Make graphs", sep = ""))

stringAll <- graph_from_data_frame(d = edgesStringAll, vertices = verticesStringAll, directed = F)

stringLow <- graph_from_data_frame(d = edgesStringLow, vertices = verticesStringLow, directed = F)
stringMedium <- graph_from_data_frame(d = edgesStringMedium, vertices = verticesStringMedium, directed = F)
stringHigh <- graph_from_data_frame(d = edgesStringHigh, vertices = verticesStringHigh, directed = F)
stringHighest <- graph_from_data_frame(d = edgesStringHighest, vertices = verticesStringHighest, directed = F)

stringExperimental <- graph_from_data_frame(d = edgesExperimental, vertices = verticesExperimental, directed = F)
stringDatabase <- graph_from_data_frame(d = edgesDatabase, vertices = verticesDatabase, directed = F)
stringCoexpression <- graph_from_data_frame(d = edgesCoexpression, vertices = verticesCoexpression, directed = F)


# Get degree distribution

print(paste(Sys.time(), " Get degree distribution", sep = ""))

distributionAll <- degree.distribution(stringAll) * nrow(verticesStringAll)

distributionLow <- degree.distribution(stringLow) * nrow(verticesStringLow) 
distributionMedium <- degree.distribution(stringMedium) * nrow(verticesStringMedium)
distributionHigh <- degree.distribution(stringHigh) * nrow(verticesStringHigh)
distributionHighest <- degree.distribution(stringHighest) * nrow(verticesStringHighest)

distributionExperimental <- degree.distribution(stringExperimental) * nrow(verticesExperimental)
distributionDatabase <- degree.distribution(stringDatabase) * nrow(verticesDatabase)
distributionCoexpression <- degree.distribution(stringCoexpression) * nrow(verticesCoexpression)


# plot link level share

edgesStringAll$evidence <- factor(edgesStringAll$level, levels = c("experimental", "database", "coexpression", "textMining", "other"))
colors <- c("green", "darkgreen", "darkorange", "red", "black")
labels <- c("Experimental", "Database", "Coexpression", "Text Mining", "Other")

linkLevelPlot <- ggplot()
linkLevelPlot <- linkLevelPlot + geom_bar(data = edgesStringAll, aes(x = "Evidence", fill = evidence))
linkLevelPlot <- linkLevelPlot + theme_bw(base_size = 22)
linkLevelPlot <- linkLevelPlot + scale_fill_manual(values = colors, labels = labels, name = "")
linkLevelPlot <- linkLevelPlot + scale_x_discrete(expand = c(0, 0))
linkLevelPlot <- linkLevelPlot + scale_y_continuous(expand = c(0, 0))
linkLevelPlot <- linkLevelPlot + theme(axis.title = element_blank(),
                                       panel.border = element_blank(),
                                       axis.title.x = element_blank(),
                                       axis.ticks.x = element_blank())


png("resources/iGraph/plots/string/evidence.png", width = 300, height = 400)
plot(linkLevelPlot)
dummy <- dev.off()


# plot degree distributions


degree <- 0:(length(distributionAll)-1)
frequency <- distributionAll
category <- rep("All", length(distributionAll))
colors <- "red"

degree <- c(degree, 0:(length(distributionCoexpression)-1))
frequency <- c(frequency, distributionCoexpression)
category <- c(category, rep("Coexpression", length(distributionCoexpression)))
colors <- c(colors, "darkorange")

degree <- c(degree, 0:(length(distributionDatabase)-1))
frequency <- c(frequency, distributionDatabase)
category <- c(category, rep("Database", length(distributionDatabase)))
colors <- c(colors, "darkgreen")

degree <- c(degree, 0:(length(distributionExperimental)-1))
frequency <- c(frequency, distributionExperimental)
category <- c(category, rep("Experimental", length(distributionExperimental)))
colors <- c(colors, "green")


dPlotData <- data.frame(degree, frequency, category, stringsAsFactors = F)
dPlotData$category <- factor(dPlotData$category, levels = c("All", "Coexpression", "Database", "Experimental"))
dPlotData <- dPlotData[dPlotData$degree > 0, ]
dPlotData <- dPlotData[dPlotData$frequency > 0, ]

dPlot <- ggplot()
dPlot <- dPlot + geom_point(data = dPlotData, aes(x = degree, y = frequency, col = category), alpha = 0.3, size = 3)
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_color_manual(name = element_blank(), values = colors)
dPlot <- dPlot + scale_x_log10(name = "degree")
dPlot <- dPlot + scale_y_log10(name = "# Proteins")
dPlot <- dPlot + theme(legend.position = 'top')

png("resources/iGraph/plots/string/degreeDistributions_level_scatter.png", width = 600, height = 800)
plot(dPlot)
dummy <- dev.off()


# plot score distribution

degree <- 0:(length(distributionAll)-1)
frequency <- distributionAll
category <- rep("All", length(distributionAll))
colors <- "black"

degree <- c(degree, 0:(length(distributionLow)-1))
frequency <- c(frequency, distributionLow)
category <- c(category, rep("Low", length(distributionLow)))
colors <- c(colors, "red")

degree <- c(degree, 0:(length(distributionMedium)-1))
frequency <- c(frequency, distributionMedium)
category <- c(category, rep("Medium", length(distributionMedium)))
colors <- c(colors, "darkorange")

degree <- c(degree, 0:(length(distributionHigh)-1))
frequency <- c(frequency, distributionHigh)
category <- c(category, rep("High", length(distributionHigh)))
colors <- c(colors, "darkgreen")

degree <- c(degree, 0:(length(distributionHighest)-1))
frequency <- c(frequency, distributionHighest)
category <- c(category, rep("Highest", length(distributionHighest)))
colors <- c(colors, "green")


dPlotData <- data.frame(degree, frequency, category, stringsAsFactors = F)
dPlotData$category <- factor(dPlotData$category, levels = c("All", "Low", "Medium", "High", "Highest"))
dPlotData <- dPlotData[dPlotData$degree > 0, ]
dPlotData <- dPlotData[dPlotData$frequency > 0, ]

dPlot <- ggplot()
dPlot <- dPlot + geom_point(data = dPlotData, aes(x = degree, y = frequency, col = category), alpha = 0.3, size = 3)
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_color_manual(name = element_blank(), values = colors)
dPlot <- dPlot + scale_x_log10(name = "degree")
dPlot <- dPlot + scale_y_log10(name = "# Proteins")
dPlot <- dPlot + theme(legend.position = 'top')

png("resources/iGraph/plots/string/degreeDistributions_score_scatter.png", width = 600, height = 800)
plot(dPlot)
dummy <- dev.off()







