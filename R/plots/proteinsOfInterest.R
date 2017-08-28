# 
# This script exports proteins of interest from the network of Reactome.
#
startTimeAll <- proc.time()



## Libraries

library(igraph)
library(ggplot2)


## Main script


# Load data

print(paste(Sys.time(), " Loading data", sep = ""))

verticesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

reactome <- graph_from_data_frame(d=edgesReactome, vertices=verticesReactome, directed=T)

reactomeDegrees <- degree(reactome, mode = "all")
reactomeDistribution <- degree.distribution(reactome, mode = "all")
reactomeDistributionScaled <- reactomeDistribution * nrow(verticesReactome)
degree <- 0:(length(reactomeDistributionScaled)-1)

# Extraction of proteins of interest

print(paste(Sys.time(), " Extracting proteins of interest", sep = ""))

degreeDataFrame <- cbind(verticesReactome, reactomeDegrees)
degreeDataFrame <- degreeDataFrame[degreeDataFrame$reactomeDegrees > 0, ]

lowDegree <- degreeDataFrame[degreeDataFrame$reactomeDegrees <= 5, ]
write.table(lowDegree, "resources/iGraph/plots/poi/lowDegree.txt", col.names = T, row.names = F, quote = F, sep = "\t")

distributionDataFrame <- data.frame(degree, reactomeDistributionScaled)
distributionDataFrame <- distributionDataFrame[distributionDataFrame$degree > 0 & distributionDataFrame$reactomeDistributionScaled > 0, ]

distributionDataFrame$degreeLog <- log10(distributionDataFrame$degree)
distributionDataFrame$reactomeDistributionScaledLog <- log10(distributionDataFrame$reactomeDistributionScaled)
distributionDataFrame$high <- distributionDataFrame$reactomeDistributionScaledLog > 3.8 - 0.8 * distributionDataFrame$degreeLog

specificDegrees <- distributionDataFrame$degree[distributionDataFrame$high]
specificProteins <- degreeDataFrame[degreeDataFrame$reactomeDegrees %in% specificDegrees, ]
write.table(specificProteins, "resources/iGraph/plots/poi/specific.txt", col.names = T, row.names = F, quote = F, sep = "\t")

highDegree <- degreeDataFrame[degreeDataFrame$reactomeDegrees > 1000, ]
write.table(highDegree, "resources/iGraph/plots/poi/highDegree.txt", col.names = T, row.names = F, quote = F, sep = "\t")



# Plot degree distribution

dPlotData <- data.frame(degree, reactomeDistributionScaled, stringsAsFactors = F)
dPlotData <- dPlotData[dPlotData$degree > 0 & dPlotData$reactomeDistributionScaled > 0, ]

print(paste(Sys.time(), " Plotting degree distribution", sep = ""))

dPlot <- ggplot()
dPlot <- dPlot + geom_point(data = dPlotData, aes(x = degree, y = reactomeDistributionScaled), col = "black", alpha = 0.3, size = 3)
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_x_log10(name = "degree", limits = c(1, 5000))
dPlot <- dPlot + scale_y_log10(name = "# Proteins", limits = c(1, 1000))
dPlot <- dPlot + theme(legend.position = 'none',
                       plot.title = element_text(hjust = 1))


png(paste("resources/iGraph/plots/poi/degree.png", sep = ""), width = 800, height = 600)
plot(dPlot)
dummy <- dev.off()

dPlotData$category <- ""
dPlotData$category[dPlotData$degree <= 5] <- "low"

dPlot <- ggplot()
dPlot <- dPlot + geom_point(data = dPlotData, aes(x = degree, y = reactomeDistributionScaled, col = category), alpha = 0.3, size = 3)
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_x_log10(name = "degree", limits = c(1, 5000))
dPlot <- dPlot + scale_y_log10(name = "# Proteins", limits = c(1, 1000))
dPlot <- dPlot + scale_color_manual(values = c("black", "red"))
dPlot <- dPlot + theme(legend.position = 'none',
                       plot.title = element_text(hjust = 1))


png(paste("resources/iGraph/plots/poi/degree1.png", sep = ""), width = 800, height = 600)
plot(dPlot)
dummy <- dev.off()

dPlotData$category[dPlotData$degree > 1000] <- "high"

dPlot <- ggplot()
dPlot <- dPlot + geom_point(data = dPlotData, aes(x = degree, y = reactomeDistributionScaled, col = category), alpha = 0.3, size = 3)
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_x_log10(name = "degree", limits = c(1, 5000))
dPlot <- dPlot + scale_y_log10(name = "# Proteins", limits = c(1, 1000))
dPlot <- dPlot + scale_color_manual(values = c("black", "green", "red"))
dPlot <- dPlot + theme(legend.position = 'none',
                       plot.title = element_text(hjust = 1))


png(paste("resources/iGraph/plots/poi/degree2.png", sep = ""), width = 800, height = 600)
plot(dPlot)
dummy <- dev.off()

dPlotData$category[dPlotData$degree %in% specificDegrees] <- "special"

dPlot <- ggplot()
dPlot <- dPlot + geom_point(data = dPlotData, aes(x = degree, y = reactomeDistributionScaled, col = category), alpha = 0.3, size = 3)
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_x_log10(name = "degree", limits = c(1, 5000))
dPlot <- dPlot + scale_y_log10(name = "# Proteins", limits = c(1, 1000))
dPlot <- dPlot + scale_color_manual(values = c("black", "green", "red", "blue"))
dPlot <- dPlot + theme(legend.position = 'none',
                       plot.title = element_text(hjust = 1))


png(paste("resources/iGraph/plots/poi/degree3.png", sep = ""), width = 800, height = 600)
plot(dPlot)
dummy <- dev.off()



dPlot <- ggplot()
dPlot <- dPlot + geom_point(aes(x = log10(dPlotData$degree), y = log10(dPlotData$reactomeDistributionScaled), col = dPlotData$category1), alpha = 0.3, size = 3)
dPlot <- dPlot + geom_abline(aes(slope = -0.8, intercept = 3.8))
dPlot <- dPlot + theme_bw(base_size = 22)
dPlot <- dPlot + scale_x_continuous(name = "degree")
dPlot <- dPlot + scale_y_continuous(name = "# Proteins")
dPlot <- dPlot + scale_color_manual(values = c("black", "red"))
dPlot <- dPlot + theme(legend.position = 'none',
                       plot.title = element_text(hjust = 1))


